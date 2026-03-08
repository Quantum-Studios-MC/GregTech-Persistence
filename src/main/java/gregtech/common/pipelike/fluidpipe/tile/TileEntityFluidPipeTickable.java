package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverableView;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.GTFluid;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidDataProperty;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.EntityDamageUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.covers.CoverPump;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidPressureData;
import gregtech.common.pipelike.fluidpipe.net.PipeTankList;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable, IDataInfoProvider {

    public static final int MAX_INTEGRITY = 1000;
    private static final int INTEGRITY_CRITICAL = 500;
    private static final int INTEGRITY_SEVERE = 250;
    private static final int SURFACE_TENSION_TINY_THRESHOLD = 30;
    private static final int SURFACE_TENSION_SMALL_THRESHOLD = 20;
    private static final int DIRTY_THRESHOLD = 50;
    private static final double CONDUCTIVITY_TRACE_THRESHOLD = 0.01;
    private static final double CONDUCTIVITY_LOW_THRESHOLD = 1.0;
    private static final double CONDUCTIVITY_MEDIUM_THRESHOLD = 100.0;
    private static final double CONDUCTIVITY_HIGH_THRESHOLD = 10000.0;

    public byte lastReceivedFrom = 0, oldLastReceivedFrom = 0;
    private PipeTankList pipeTankList;
    private final EnumMap<EnumFacing, PipeTankList> tankLists = new EnumMap<>(EnumFacing.class);
    private FluidTank[] fluidTanks;
    private long timer = 0L;
    private final int offset = GTValues.RNG.nextInt(20);

    private int integrity = MAX_INTEGRITY;
    private int lastSavedIntegrity = MAX_INTEGRITY;
    private boolean isClogged = false;
    private Fluid cachedDegradationFluid = null;
    private int cachedDegradationRate = 0;
    private double cachedViscosityFactor = 1.0;
    private double cachedSurfaceTensionLeakRate = 0.0;

    private boolean cachedHasSludge = false;
    private boolean cachedHasAcid = false;
    private boolean cachedHasCorrosive = false;
    private boolean cachedHasToxic = false;
    private boolean cachedHasRadioactive = false;
    private boolean cachedHasFlammable = false;
    private boolean cachedHasOutOfRangePH = false;
    private double cachedFluidPH = 7.0;
    private boolean cachedIsBurning = false;
    private boolean cachedIsLeaking = false;
    private boolean cachedIsShattering = false;

    private final FluidPressureData pressureData = new FluidPressureData();
    private int cachedFluidViscosity = 0;

    public long getOffsetTimer() {
        return timer + offset;
    }

    public int getIntegrity() {
        return integrity;
    }

    public boolean isClogged() {
        return isClogged;
    }

    public FluidPressureData getPressureData() {
        return pressureData;
    }

    /**
     * Returns a comparator signal (0-15) proportional to current pressure vs burst pressure.
     */
    public int getComparatorPressureSignal() {
        if (!ConfigHolder.machines.pressure.enablePressureSystem) return 0;
        int burstPressure = getNodeData().getBurstPressure();
        if (burstPressure <= 0) return 0;
        int signal = (int) ((long) pressureData.getPressure() * 15 / burstPressure);
        return Math.max(0, Math.min(15, signal));
    }

    public boolean isElectricallyEnergized() {
        if (world == null || pos == null) return false;
        if (world.isBlockPowered(pos)) return true;

        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos neighborPos = pos.offset(facing);
            Block neighborBlock = world.getBlockState(neighborPos).getBlock();
            if (world.isBlockPowered(neighborPos) || neighborBlock == Blocks.REDSTONE_WIRE ||
                    neighborBlock == Blocks.REDSTONE_TORCH || neighborBlock == Blocks.LIT_REDSTONE_LAMP) {
                return true;
            }
        }
        return false;
    }

    public int getConductivityTier() {
        double conductivity = getMaxFluidConductivity();
        if (conductivity <= CONDUCTIVITY_TRACE_THRESHOLD) return 0;
        if (conductivity > CONDUCTIVITY_MEDIUM_THRESHOLD) return 3;
        if (conductivity > CONDUCTIVITY_LOW_THRESHOLD) return 2;
        return 1;
    }

    public double getMaxFluidConductivity() {
        double maxConductivity = 0.0;
        for (FluidTank tank : getFluidTanks()) {
            FluidStack stack = tank.getFluid();
            if (stack == null || stack.amount <= 0) continue;
            maxConductivity = Math.max(maxConductivity, getFluidConductivity(stack));
        }
        return maxConductivity;
    }

    private static double getFluidConductivity(@NotNull FluidStack stack) {
        Material material = FluidUnifier.getMaterialFromFluid(stack.getFluid());
        if (material == null && stack.getFluid() instanceof GTFluid.GTMaterialFluid) {
            material = ((GTFluid.GTMaterialFluid) stack.getFluid()).getMaterial();
        }
        if (material == null || !material.hasProperty(PropertyKey.FLUID_DATA)) return 0.0;
        return material.getProperty(PropertyKey.FLUID_DATA).getElectricalConductivity();
    }

    private static int getBaseConductivityDamage(double conductivity) {
        if (conductivity > CONDUCTIVITY_HIGH_THRESHOLD) {
            return 6;
        } else if (conductivity > CONDUCTIVITY_MEDIUM_THRESHOLD) {
            return 4;
        } else if (conductivity > CONDUCTIVITY_LOW_THRESHOLD) {
            return 2;
        } else {
            return 1;
        }
    }

    private static String getConductivityTierKey(int tier) {
        switch (tier) {
            case 3:
                return "gregtech.fluid_pipe.conductivity_tier.high";
            case 2:
                return "gregtech.fluid_pipe.conductivity_tier.medium";
            case 1:
                return "gregtech.fluid_pipe.conductivity_tier.low";
            default:
                return "gregtech.fluid_pipe.conductivity_tier.none";
        }
    }

    public void repairIntegrity(int amount) {
        integrity = Math.min(MAX_INTEGRITY, integrity + amount);
        isClogged = false;
        cachedDegradationFluid = null;
        lastSavedIntegrity = integrity;
        markDirty();
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            PipeTankList tankList = getTankList(facing);
            if (tankList == null)
                return null;
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tankList);
        }
        return super.getCapabilityInternal(capability, facing);
    }

    @Override
    public void update() {
        timer++;
        getCoverableImplementation().update();
        if (!world.isRemote && getOffsetTimer() % FREQUENCY == 0) {
            lastReceivedFrom &= 63;
            if (lastReceivedFrom == 63) {
                lastReceivedFrom = 0;
            }

            // Update pressure based on fluid fill level
            if (ConfigHolder.machines.pressure.enablePressureSystem) {
                updatePressureFromContents();
                // Check for burst condition
                if (pressureData.tickBurst(pressureData.getPressure(), getNodeData().getBurstPressure())) {
                    handlePipeBurst();
                    return;
                }
            }

            boolean shouldDistribute = (oldLastReceivedFrom == lastReceivedFrom);
            int tanks = getNodeData().getTanks();
            for (int i = 0, j = GTValues.RNG.nextInt(tanks); i < tanks; i++) {
                int index = (i + j) % tanks;
                FluidTank tank = getFluidTanks()[index];
                FluidStack fluid = tank.getFluid();
                if (fluid == null)
                    continue;
                if (fluid.amount <= 0) {
                    tank.setFluid(null);
                    continue;
                }

                if (cachedDegradationRate > 0) {
                    applyDegradationEffects(fluid);
                }

                if (shouldDistribute) {
                    distributeFluid(index, tank, fluid);
                    lastReceivedFrom = 0;
                }
            }
            oldLastReceivedFrom = lastReceivedFrom;

            if (Math.abs(integrity - lastSavedIntegrity) >= DIRTY_THRESHOLD) {
                lastSavedIntegrity = integrity;
                markDirty();
            }
        }

        if (!world.isRemote && isClogged && getOffsetTimer() % 10 == 0) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.BLOCK_DUST, 3);
            if (getOffsetTimer() % 40 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_SLIME_BREAK, SoundCategory.BLOCKS, 0.2F, 0.6F);
            }
        }

        // Pressure stress visual/audio feedback
        if (!world.isRemote && ConfigHolder.machines.pressure.enablePressureSystem) {
            int pressure = pressureData.getPressure();
            int burstRating = getNodeData().getBurstPressure();
            if (burstRating > 0) {
                if (pressure > burstRating && getOffsetTimer() % 5 == 0) {
                    // Critical: rapid steam jets + loud hissing
                    TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CLOUD, 5);
                    world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.6F, 1.8F);
                } else if (pressure > burstRating * 3 / 4 && getOffsetTimer() % 20 == 0) {
                    // Danger: occasional steam puffs + hiss
                    TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CLOUD, 2);
                    world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.3F, 1.5F);
                } else if (pressure > burstRating / 2 && getOffsetTimer() % 40 == 0) {
                    // Warning: rare small puff
                    TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.SMOKE_NORMAL, 1);
                }
            }
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    /**
     * Updates the node pressure based on fluid contents and propagated network pressure.
     * <p>
     * Passive pressure (from fill level alone) is capped at gravityPenaltyPerBlock (100 mBar),
     * which is too low to push fluid upward. A Pipe Pump injects real pressure that propagates
     * through the connected network:
     * <ul>
     *   <li>Horizontal neighbors: pressure propagates freely</li>
     *   <li>Neighbor below (pushing up to us): costs gravityPenaltyPerBlock</li>
     *   <li>Neighbor above (flowing down to us): free (gravity assists)</li>
     * </ul>
     */
    private void updatePressureFromContents() {
        int totalFluid = 0;
        int totalCapacity = 0;
        for (FluidTank tank : getFluidTanks()) {
            FluidStack fs = tank.getFluid();
            totalFluid += (fs != null) ? fs.amount : 0;
            totalCapacity += tank.getCapacity();
        }
        if (totalCapacity <= 0) {
            pressureData.setPressure(0);
            pressureData.setSiphonPressure(0);
            return;
        }

        // Passive pressure from fill level (max gravityPenaltyPerBlock when full)
        int gravityPenalty = ConfigHolder.machines.pressure.gravityPenaltyPerBlock;
        int passivePressure = (int) ((long) totalFluid * gravityPenalty / totalCapacity);

        // Calculate siphon pressure if enabled
        int siphonPressure = 0;
        if (ConfigHolder.machines.pressure.enableSiphon && totalFluid > 0) {
            siphonPressure = calculateSiphonPressure(passivePressure);
        }
        pressureData.setSiphonPressure(siphonPressure);

        // Propagate pressure from connected pipe neighbors
        int networkPressure = passivePressure + siphonPressure;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (!isConnected(facing)) continue;
            TileEntity neighbor = getNeighbor(facing);
            if (!(neighbor instanceof TileEntityFluidPipeTickable neighborPipe)) continue;

            int neighborPressure = neighborPipe.pressureData.getPressure();
            // Cost depends on direction: upward from below costs gravity, everything else is free
            int cost = (facing.getYOffset() < 0) ? gravityPenalty : 0;
            int offered = neighborPressure - cost;
            if (offered > networkPressure) {
                networkPressure = offered;
            }
        }

        // Apply: rise instantly to network pressure, decay slowly when source is removed
        int current = pressureData.getPressure();
        if (current < networkPressure) {
            pressureData.setPressure(networkPressure);
        } else if (current > networkPressure) {
            int decay = Math.max(1, (current - networkPressure) / 20);
            pressureData.setPressure(Math.max(networkPressure, current - decay));
        }
    }

    /**
     * Calculate siphon pressure from a lower neighbor with fluid.
     * Siphon can lift fluid up to maxSiphonHeight blocks without a pump,
     * but with reduced effectiveness compared to active pumps.
     * 
     * Conditions for siphon:
     * - Must have a neighbor below (down facing) that is also a pipe
     * - That neighbor must have fluid
     * - The height difference must be within maxSiphonHeight
     * - Siphon pressure = passive pressure of lower pipe * siphonEfficiency
     */
    private int calculateSiphonPressure(int ownPassivePressure) {
        TileEntity below = getNeighbor(EnumFacing.DOWN);
        if (!(below instanceof TileEntityFluidPipeTickable lowerPipe)) {
            return 0;
        }

        // Check if lower pipe has fluid to siphon from
        boolean lowerHasFluid = false;
        for (FluidTank tank : lowerPipe.getFluidTanks()) {
            FluidStack fs = tank.getFluid();
            if (fs != null && fs.amount > 0) {
                lowerHasFluid = true;
                break;
            }
        }
        if (!lowerHasFluid) {
            return 0;
        }

        // Check siphon height limit - walk upward from current position
        int height = 1;
        BlockPos checkPos = pos.up();
        while (height < ConfigHolder.machines.pressure.maxSiphonHeight && checkPos.getY() < 256) {
            TileEntity checkTE = world.getTileEntity(checkPos);
            if (!(checkTE instanceof TileEntityFluidPipeTickable)) {
                break;  // No pipe above, siphon height limited
            }
            height++;
            checkPos = checkPos.up();
        }

        // Siphon works: lower pipe's passive pressure can help push fluid up
        int lowerPassivePressure = lowerPipe.pressureData.getPressure();
        if (lowerPassivePressure <= 0) {
            return 0;
        }

        // Apply siphon efficiency and height penalty
        int maxSiphonHeight = ConfigHolder.machines.pressure.maxSiphonHeight;
        int heightCost = Math.min(100, height * 10);  // Small height-based cost
        double efficiency = ConfigHolder.machines.pressure.siphonEfficiency;
        int siphonPressure = (int) (lowerPassivePressure * efficiency) - heightCost;
        
        return Math.max(0, siphonPressure / 2);  // Divide by 2 to ensure siphon is secondary to pumps
    }

    /**
     * Handle pipe rupture - spill fluid, ignite volatile, destroy block.
     */
    private void handlePipeBurst() {
        FluidStack spillFluid = null;
        for (FluidTank tank : getFluidTanks()) {
            FluidStack fs = tank.getFluid();
            if (fs != null && fs.amount > 0) {
                spillFluid = fs.copy();
                tank.setFluid(null);
            }
        }

        // Spawn burst particles and sound
        TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CLOUD, 10);
        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0F, 1.5F);

        // If the fluid is flammable, ignite surroundings
        if (spillFluid != null && spillFluid.getFluid() instanceof AttributedFluid attributedFluid) {
            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (attribute.equals(FluidAttributes.FLAMMABLE)) {
                    TileEntityFluidPipe.setNeighboursToFire(world, pos);
                    this.doExplosion(1.5f + GTValues.RNG.nextFloat());
                    return;
                }
            }
        }

        // Non-flammable burst just destroys the pipe
        world.setBlockToAir(pos);
    }

    private double computeViscosityFactor(@NotNull Fluid fluid) {
        Material material = FluidUnifier.getMaterialFromFluid(fluid);
        if (material == null && fluid instanceof GTFluid.GTMaterialFluid matFluid) {
            material = matFluid.getMaterial();
        }
        if (material != null && material.hasProperty(PropertyKey.FLUID_DATA)) {
            double viscosity = material.getProperty(PropertyKey.FLUID_DATA).getViscosity();
            if (viscosity > 1.0) {
                return 1.0 / (1.0 + Math.log10(viscosity));
            }
        }
        return 1.0;
    }

    private double computeSurfaceTensionLeakRate(@NotNull Fluid fluid) {
        Material material = FluidUnifier.getMaterialFromFluid(fluid);
        if (material == null && fluid instanceof GTFluid.GTMaterialFluid matFluid) {
            material = matFluid.getMaterial();
        }
        if (material != null && material.hasProperty(PropertyKey.FLUID_DATA)) {
            double surfaceTension = material.getProperty(PropertyKey.FLUID_DATA).getSurfaceTension();
            FluidPipeType pipeType = getPipeType();
            if (pipeType == FluidPipeType.TINY && surfaceTension < SURFACE_TENSION_TINY_THRESHOLD) {
                return 0.03 + (SURFACE_TENSION_TINY_THRESHOLD - surfaceTension) * 0.002;
            }
            if (pipeType == FluidPipeType.SMALL && surfaceTension < SURFACE_TENSION_SMALL_THRESHOLD) {
                return 0.02 + (SURFACE_TENSION_SMALL_THRESHOLD - surfaceTension) * 0.001;
            }
        }
        return 0.0;
    }

    private void distributeFluid(int channel, FluidTank tank, FluidStack fluid) {
        if (isClogged) return;

        List<FluidTransaction> tanks = new ArrayList<>();
        int amount = fluid.amount;

        FluidStack maxFluid = fluid.copy();
        double availableCapacity = 0;

        for (byte i = 0, j = (byte) GTValues.RNG.nextInt(6); i < 6; i++) {
            byte side = (byte) ((i + j) % 6);
            EnumFacing facing = EnumFacing.VALUES[side];

            if (!isConnected(facing) || (lastReceivedFrom & (1 << side)) != 0) {
                continue;
            }

            TileEntity neighbor = getNeighbor(facing);
            if (neighbor == null) continue;
            IFluidHandler fluidHandler = neighbor.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    facing.getOpposite());
            if (fluidHandler == null) continue;

            IFluidHandler pipeTank = tank;
            Cover cover = getCoverableImplementation().getCoverAtSide(facing);

            if (cover != null) {
                pipeTank = cover.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, pipeTank);
                if (pipeTank == null || checkForPumpCover(cover)) continue;
            } else {
                CoverableView coverable = neighbor.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
                        facing.getOpposite());
                if (coverable != null) {
                    cover = coverable.getCoverAtSide(facing.getOpposite());
                    if (checkForPumpCover(cover)) continue;
                }
            }

            FluidStack drainable = pipeTank.drain(maxFluid, false);
            if (drainable == null || drainable.amount <= 0) {
                continue;
            }

            int filled = Math.min(fluidHandler.fill(maxFluid, false), drainable.amount);

            if (filled > 0) {
                tanks.add(new FluidTransaction(fluidHandler, pipeTank, filled, facing));
                availableCapacity += filled;
            }
            maxFluid.amount = amount;
        }

        if (availableCapacity <= 0)
            return;

        if (fluid.getFluid() != cachedDegradationFluid) {
            updateCachedFluidData(fluid.getFluid());
        }

        if (cachedSurfaceTensionLeakRate > 0 && getOffsetTimer() % 20 == 0) {
            int leaked = (int) (fluid.amount * cachedSurfaceTensionLeakRate);
            if (leaked > 0) {
                fluid.amount = Math.max(0, fluid.amount - leaked);
                TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.DOWN, EnumParticleTypes.DRIP_WATER, 2);
            }
        }

        // When pressure system is enabled, block upward flow unless pump pressure overcomes gravity.
        // Horizontal and downward flow always uses the normal fill-level distribution.
        if (ConfigHolder.machines.pressure.enablePressureSystem) {
            int gravityThreshold = ConfigHolder.machines.pressure.gravityPenaltyPerBlock;
            tanks.removeIf(transaction -> {
                if (transaction.facing != EnumFacing.UP) return false; // only gate upward
                // Need pressure strictly above one gravity block to push fluid up
                return pressureData.getPressure() <= gravityThreshold;
            });
            // Recalculate available capacity after filtering
            availableCapacity = 0;
            for (FluidTransaction t : tanks) {
                availableCapacity += t.amount;
            }
            if (availableCapacity <= 0) return;
        }

        // Standard fill-level distribution (used in both legacy and pressure modes)
        double baseMaxAmount = Math.min(getCapacityPerTank() / 2, fluid.amount);
        double viscosityAdjusted = baseMaxAmount * cachedViscosityFactor;
        double integrityThroughputMultiplier = getIntegrityThroughputMultiplier();
        double maxAmount = viscosityAdjusted * integrityThroughputMultiplier;

        for (FluidTransaction transaction : tanks) {
            if (availableCapacity > maxAmount) {
                transaction.amount = (int) Math.floor(transaction.amount * maxAmount / availableCapacity);
            }
            if (transaction.amount == 0) {
                if (tank.getFluidAmount() <= 0) break;
                transaction.amount = 1;
            } else if (transaction.amount < 0) {
                continue;
            }

            FluidStack toInsert = fluid.copy();
            toInsert.amount = transaction.amount;

            int inserted = transaction.target.fill(toInsert, true);
            if (inserted > 0) {
                transaction.pipeTank.drain(inserted, true);
            }
        }
    }

    private double getIntegrityThroughputMultiplier() {
        if (integrity >= INTEGRITY_CRITICAL) return 1.0;
        if (integrity >= INTEGRITY_SEVERE) return 0.5;
        if (integrity > 0) return 0.1;
        return 0.0;
    }



    private int getFluidViscosityForPressure(@NotNull Fluid fluid) {
        Material material = FluidUnifier.getMaterialFromFluid(fluid);
        if (material == null && fluid instanceof GTFluid.GTMaterialFluid matFluid) {
            material = matFluid.getMaterial();
        }
        if (material != null && material.hasProperty(PropertyKey.FLUID_DATA)) {
            return (int) material.getProperty(PropertyKey.FLUID_DATA).getViscosity();
        }
        return fluid.getViscosity();
    }

    private void updateCachedFluidData(@NotNull Fluid fluid) {
        cachedDegradationFluid = fluid;
        cachedDegradationRate = computeDegradationRate(fluid);
        cachedViscosityFactor = computeViscosityFactor(fluid);
        cachedSurfaceTensionLeakRate = computeSurfaceTensionLeakRate(fluid);

        FluidPipeProperties prop = getNodeData();

        cachedHasSludge = false;
        cachedHasAcid = false;
        cachedHasCorrosive = false;
        cachedHasToxic = false;
        cachedHasRadioactive = false;
        cachedHasFlammable = false;
        cachedHasOutOfRangePH = false;
        cachedFluidPH = 7.0;

        cachedIsBurning = prop.getMaxFluidTemperature() < fluid.getTemperature();
        cachedIsLeaking = !prop.isGasProof() && fluid.isGaseous();
        cachedIsShattering = !prop.isCryoProof() && fluid.getTemperature() < FluidConstants.CRYOGENIC_FLUID_THRESHOLD;

        FluidDataProperty fluidData = null;
        Material fluidMaterial = FluidUnifier.getMaterialFromFluid(fluid);
        if (fluidMaterial == null && fluid instanceof GTFluid.GTMaterialFluid matFluid) {
            fluidMaterial = matFluid.getMaterial();
        }
        if (fluidMaterial != null && fluidMaterial.hasProperty(PropertyKey.FLUID_DATA)) {
            fluidData = fluidMaterial.getProperty(PropertyKey.FLUID_DATA);
            cachedFluidPH = fluidData.getPH();
            cachedHasOutOfRangePH = !prop.canContainPH(cachedFluidPH);
        }

        if (fluid instanceof AttributedFluid attributedFluid) {
            FluidState state = attributedFluid.getState();
            if (cachedIsBurning && state == FluidState.PLASMA && prop.canContain(FluidState.PLASMA)) {
                cachedIsBurning = false;
            }
            if (!prop.canContain(state)) {
                if (state == FluidState.GAS) cachedIsLeaking = true;
                if (state == FluidState.PLASMA) cachedIsBurning = true;
            }

            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (!prop.canContain(attribute)) {
                    if (attribute.equals(FluidAttributes.SLUDGE)) {
                        if (ConfigHolder.machines.enableSludgeMechanics) cachedHasSludge = true;
                    } else if (attribute.equals(FluidAttributes.ACID)) cachedHasAcid = true;
                    else if (attribute.equals(FluidAttributes.CORROSIVE)) cachedHasCorrosive = true;
                    else if (attribute.equals(FluidAttributes.TOXIC)) cachedHasToxic = true;
                    else if (attribute.equals(FluidAttributes.RADIOACTIVE)) cachedHasRadioactive = true;
                    else if (attribute.equals(FluidAttributes.FLAMMABLE)) cachedHasFlammable = true;
                }
            }
        }

        if (cachedHasOutOfRangePH && fluidData != null) {
            if (cachedFluidPH < prop.getMinPH() && !prop.isAcidProof()) {
                cachedHasAcid = true;
            }
            if (cachedFluidPH > prop.getMaxPH() || (cachedFluidPH < prop.getMinPH() && !prop.isCorrosiveProof())) {
                cachedHasCorrosive = true;
            }
        }
    }

    private int computeDegradationRate(@NotNull Fluid fluid) {
        FluidPipeProperties prop = getNodeData();
        int rate = 0;

        boolean temperatureDamage = false;
        if (prop.getMaxFluidTemperature() < fluid.getTemperature()) {
            int tempDiff = fluid.getTemperature() - prop.getMaxFluidTemperature();
            rate += Math.max(1, tempDiff / 50);
            temperatureDamage = true;
        }

        if (!prop.isCryoProof() && fluid.getTemperature() < FluidConstants.CRYOGENIC_FLUID_THRESHOLD) {
            int tempDiff = FluidConstants.CRYOGENIC_FLUID_THRESHOLD - fluid.getTemperature();
            rate += Math.max(1, tempDiff / 25);
        }

        if (!prop.isGasProof() && fluid.isGaseous()) {
            rate += 2;
        }

        if (fluid instanceof AttributedFluid attributedFluid) {
            FluidState state = attributedFluid.getState();

            if (!prop.canContain(state)) {
                if (state == FluidState.GAS) rate += 2;
                if (state == FluidState.PLASMA) rate += 10;
            }

            if (temperatureDamage && state == FluidState.PLASMA && prop.canContain(FluidState.PLASMA)) {
                rate -= Math.max(1, (fluid.getTemperature() - prop.getMaxFluidTemperature()) / 50);
            }

            Material fluidMaterial = FluidUnifier.getMaterialFromFluid(fluid);
            if (fluidMaterial == null && fluid instanceof GTFluid.GTMaterialFluid matFluid) {
                fluidMaterial = matFluid.getMaterial();
            }
            FluidDataProperty fluidData = null;
            if (fluidMaterial != null && fluidMaterial.hasProperty(PropertyKey.FLUID_DATA)) {
                fluidData = fluidMaterial.getProperty(PropertyKey.FLUID_DATA);
            }

            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (!prop.canContain(attribute)) {
                    rate += getAttributeDegradationRate(attribute, fluidData, fluid);
                }
            }

            if (fluidData != null) {
                if (!prop.canContainPH(fluidData.getPH())) {
                    double lowDelta = Math.max(0.0, prop.getMinPH() - fluidData.getPH());
                    double highDelta = Math.max(0.0, fluidData.getPH() - prop.getMaxPH());
                    rate += Math.max(1, (int) Math.ceil((lowDelta + highDelta) * 1.8));
                }

                double heatCapacity = fluidData.getSpecificHeatCapacity();
                if (heatCapacity > 3.0 && temperatureDamage) {
                    double reduction = (heatCapacity - 3.0) * 0.3;
                    rate = Math.max(1, (int) (rate - reduction));
                }
            }
        }

        return rate;
    }

    private int getAttributeDegradationRate(@NotNull FluidAttribute attribute,
                                            @Nullable FluidDataProperty fluidData,
                                            @NotNull Fluid fluid) {
        if (attribute.equals(FluidAttributes.ACID)) {
            if (fluidData != null) {
                double pH = fluidData.getPH();
                return Math.max(1, (int) (7.0 - pH));
            }
            return 3;
        }
        if (attribute.equals(FluidAttributes.CORROSIVE)) {
            if (fluidData != null) {
                double pH = fluidData.getPH();
                if (pH < 7.0) return Math.max(2, (int) (8.0 - pH));
                if (pH > 7.0) return Math.max(2, (int) (pH - 6.0));
            }
            return 4;
        }
        if (attribute.equals(FluidAttributes.RADIOACTIVE)) {
            return 2;
        }
        if (attribute.equals(FluidAttributes.TOXIC)) {
            return 1;
        }
        if (attribute.equals(FluidAttributes.FLAMMABLE)) {
            if (fluid.getTemperature() > 500) return 5;
            return 0;
        }
        if (attribute.equals(FluidAttributes.SLUDGE)) {
            return ConfigHolder.machines.enableSludgeMechanics ? 2 : 0;
        }
        return 3;
    }

    public void checkAndDestroy(@NotNull FluidStack stack) {
        Fluid fluid = stack.getFluid();

        if (fluid != cachedDegradationFluid) {
            updateCachedFluidData(fluid);
        }

        if (cachedDegradationRate <= 0) return;

        integrity -= cachedDegradationRate;
        if (integrity < 0) integrity = 0;
    }

    private void applyDegradationEffects(@NotNull FluidStack stack) {
        Fluid fluid = stack.getFluid();

        if (cachedHasSludge && integrity <= 0 && !isClogged) {
            isClogged = true;
            world.playSound(null, pos, SoundEvents.BLOCK_SLIME_PLACE, SoundCategory.BLOCKS, 1.0F, 0.5F);
            markDirty();
            return;
        }

        if (integrity < INTEGRITY_CRITICAL && getOffsetTimer() % 20 == 0) {
            world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.3F, 1.0F);
        }

        if (cachedHasSludge && getOffsetTimer() % 20 == 0) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.BLOCK_DUST, 2);
        }

        if (cachedIsLeaking && integrity < INTEGRITY_CRITICAL) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.SMOKE_NORMAL, 3);
            stack.amount = Math.max(0, stack.amount * 9 / 10);

            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entity : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entity, fluid.getTemperature(), 2.0F, 10);
                }
            }

            if (integrity <= 0 && GTValues.RNG.nextInt(cachedIsBurning ? 3 : 7) == 0) {
                this.doExplosion(1.0f + GTValues.RNG.nextFloat());
                return;
            }
        }

        if ((cachedHasAcid || cachedHasCorrosive) && integrity < INTEGRITY_CRITICAL) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CRIT_MAGIC, 2);
            int voidPercent = cachedHasCorrosive ? 30 : 20;
            if (cachedHasOutOfRangePH) {
                voidPercent += 10;
            }
            stack.amount = Math.max(0, stack.amount * (100 - voidPercent) / 100);

            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(1));
                for (EntityLivingBase entity : entities) {
                    EntityDamageUtil.applyChemicalDamage(entity, cachedHasCorrosive ? 3 : 2);
                }
            }

            if (integrity <= 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
                return;
            }
        }

        if (cachedHasToxic && integrity < INTEGRITY_CRITICAL) {
            if (getOffsetTimer() % 20 == 0) {
                TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.SPELL_WITCH, 3);
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entity : entities) {
                    entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
                    entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
                }
            }
            stack.amount = Math.max(0, stack.amount * 95 / 100);

            if (integrity <= 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
                return;
            }
        }

        if (cachedHasRadioactive && getOffsetTimer() % 20 == 0) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.SPELL_INSTANT, 2);
            int radius = integrity < INTEGRITY_SEVERE ? 3 : 2;
            List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                    new AxisAlignedBB(getPipePos()).grow(radius));
            for (EntityLivingBase entity : entities) {
                entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 100, integrity < INTEGRITY_SEVERE ? 1 : 0));
                entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0));
            }
            stack.amount = Math.max(0, stack.amount * 95 / 100);

            if (integrity <= 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
                return;
            }
        }

        if (cachedHasFlammable && fluid.getTemperature() > 500 && integrity < INTEGRITY_CRITICAL) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.FLAME, 2);
            stack.amount = Math.max(0, stack.amount * 3 / 4);

            if (integrity <= 0) {
                stack.amount = 0;
                TileEntityFluidPipe.setNeighboursToFire(world, pos);
                this.doExplosion(2.0f + GTValues.RNG.nextFloat() * 2.0f);
                return;
            }

            if (integrity < INTEGRITY_SEVERE && GTValues.RNG.nextInt(4) == 0) {
                TileEntityFluidPipe.setNeighboursToFire(world, pos);
            }
        }

        if (cachedIsBurning && integrity < INTEGRITY_CRITICAL) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.FLAME, 3);
            stack.amount = Math.max(0, stack.amount / 4);

            if (GTValues.RNG.nextInt(4) == 0) {
                TileEntityFluidPipe.setNeighboursToFire(world, pos);
            }

            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entity : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entity, fluid.getTemperature(), 2.0F, 10);
                }
            }

            if (integrity <= 0) {
                stack.amount = 0;
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                return;
            }
        }

        if (cachedIsShattering && integrity < INTEGRITY_CRITICAL) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CLOUD, 3);
            stack.amount = Math.max(0, stack.amount / 4);

            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entity : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entity, fluid.getTemperature(), 2.0F, 10);
                }
            }

            if (integrity <= 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
                return;
            }
        }
    }

    public void applyElectricalConductivityDamage(@NotNull EntityLivingBase entity, @NotNull FluidStack stack) {
        double conductivity = getFluidConductivity(stack);
        if (conductivity <= CONDUCTIVITY_TRACE_THRESHOLD) return;

        boolean energized = isElectricallyEnergized();
        if (!energized && GTValues.RNG.nextInt(4) != 0) {
            return;
        }

        int damage = getBaseConductivityDamage(conductivity);
        if (!energized) {
            damage = Math.max(1, damage - 2);
        }

        entity.attackEntityFrom(DamageSources.getElectricDamage(), damage);
    }

    private boolean checkForPumpCover(@Nullable Cover cover) {
        if (cover instanceof CoverPump coverPump) {
            int pipeThroughput = getNodeData().getThroughput() * 20;
            if (coverPump.getTransferRate() > pipeThroughput) {
                coverPump.setTransferRate(pipeThroughput);
            }
            return true;
        }
        return false;
    }

    private IFluidHandler getFluidHandlerAt(EnumFacing facing, EnumFacing oppositeSide) {
        TileEntity tile = world.getTileEntity(pos.offset(facing));
        if (tile == null) {
            return null;
        }
        return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, oppositeSide);
    }

    public void receivedFrom(EnumFacing facing) {
        if (facing != null) {
            lastReceivedFrom |= (1 << facing.getIndex());
        }
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0 || channel >= getFluidTanks().length) return null;
        return getFluidTanks()[channel].getFluid();
    }

    private void createTanksList() {
        fluidTanks = new FluidTank[getNodeData().getTanks()];
        for (int i = 0; i < getNodeData().getTanks(); i++) {
            fluidTanks[i] = new FluidTank(getCapacityPerTank());
        }
        pipeTankList = new PipeTankList(this, null, fluidTanks);
        for (EnumFacing facing : EnumFacing.VALUES) {
            tankLists.put(facing, new PipeTankList(this, facing, fluidTanks));
        }
    }

    public PipeTankList getTankList() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return pipeTankList;
    }

    public PipeTankList getTankList(EnumFacing facing) {
        if (tankLists.isEmpty() || fluidTanks == null) {
            createTanksList();
        }
        return tankLists.getOrDefault(facing, pipeTankList);
    }

    public FluidTank[] getFluidTanks() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return fluidTanks;
    }

    public FluidStack[] getContainedFluids() {
        FluidStack[] fluids = new FluidStack[getFluidTanks().length];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = fluidTanks[i].getFluid();
        }
        return fluids;
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getFluidTanks().length; i++) {
            FluidStack stack1 = getContainedFluid(i);
            NBTTagCompound fluidTag = new NBTTagCompound();
            if (stack1 == null || stack1.amount <= 0)
                fluidTag.setBoolean("isNull", true);
            else
                stack1.writeToNBT(fluidTag);
            list.appendTag(fluidTag);
        }
        nbt.setTag("Fluids", list);
        nbt.setInteger("Integrity", integrity);
        nbt.setBoolean("Clogged", isClogged);
        nbt.setTag("PressureData", pressureData.serializeNBT());
        return nbt;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        createTanksList();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            if (!tag.getBoolean("isNull")) {
                fluidTanks[i].setFluid(FluidStack.loadFluidStackFromNBT(tag));
            }
        }
        if (nbt.hasKey("Integrity")) {
            integrity = nbt.getInteger("Integrity");
        }
        if (nbt.hasKey("Clogged")) {
            isClogged = nbt.getBoolean("Clogged");
        }
        if (nbt.hasKey("PressureData")) {
            pressureData.deserializeNBT(nbt.getCompoundTag("PressureData"));
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();

        list.add(new TextComponentTranslation("gregtech.fluid_pipe.integrity",
                new TextComponentTranslation(String.valueOf(integrity * 100 / MAX_INTEGRITY))
                        .setStyle(new Style().setColor(
                                integrity > INTEGRITY_CRITICAL ? TextFormatting.GREEN :
                                        integrity > INTEGRITY_SEVERE ? TextFormatting.YELLOW :
                                                integrity > 0 ? TextFormatting.RED : TextFormatting.DARK_RED))));

        if (isClogged) {
            list.add(new TextComponentTranslation("gregtech.fluid_pipe.clogged")
                    .setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
        }

        if (ConfigHolder.machines.pressure.enablePressureSystem) {
            int pressure = pressureData.getPressure();
            int burstRating = getNodeData().getBurstPressure();
            int percent = burstRating > 0 ? (int) ((long) pressure * 100 / burstRating) : 0;
            // Named risk level
            String riskKey;
            TextFormatting pressureColor;
            if (pressure > burstRating) {
                pressureColor = TextFormatting.DARK_RED;
                riskKey = "gregtech.fluid_pipe.risk.critical";
            } else if (pressure > burstRating * 3 / 4) {
                pressureColor = TextFormatting.RED;
                riskKey = "gregtech.fluid_pipe.risk.danger";
            } else if (pressure > burstRating / 2) {
                pressureColor = TextFormatting.YELLOW;
                riskKey = "gregtech.fluid_pipe.risk.warning";
            } else if (pressure > burstRating / 4) {
                pressureColor = TextFormatting.GREEN;
                riskKey = "gregtech.fluid_pipe.risk.nominal";
            } else {
                pressureColor = TextFormatting.GRAY;
                riskKey = "gregtech.fluid_pipe.risk.idle";
            }
            list.add(new TextComponentTranslation("gregtech.fluid_pipe.pressure_detail",
                    new TextComponentTranslation(String.valueOf(pressure))
                            .setStyle(new Style().setColor(pressureColor)),
                    new TextComponentTranslation(String.valueOf(burstRating))
                            .setStyle(new Style().setColor(TextFormatting.AQUA)),
                    new TextComponentTranslation(String.valueOf(percent))
                            .setStyle(new Style().setColor(pressureColor)),
                    new TextComponentTranslation(riskKey)
                            .setStyle(new Style().setColor(pressureColor))));
            if (pressureData.getBurstCountdown() >= 0) {
                int ticksLeft = pressureData.getBurstCountdown();
                double secondsLeft = ticksLeft / 20.0;
                list.add(new TextComponentTranslation("gregtech.fluid_pipe.burst_warning_detail",
                        new TextComponentTranslation(String.format("%.1fs", secondsLeft))
                                .setStyle(new Style().setColor(TextFormatting.RED).setBold(true))));
            }
        }

        int conductivityTier = getConductivityTier();
        if (conductivityTier > 0) {
            TextFormatting conductivityColor = conductivityTier > 2 ? TextFormatting.RED :
                conductivityTier > 1 ? TextFormatting.YELLOW : TextFormatting.GOLD;
            list.add(new TextComponentTranslation("gregtech.fluid_pipe.conductivity_status",
                new TextComponentTranslation(getConductivityTierKey(conductivityTier))
                    .setStyle(new Style().setColor(conductivityColor)),
                new TextComponentTranslation(isElectricallyEnergized() ?
                    "gregtech.fluid_pipe.conductivity_energized" :
                    "gregtech.fluid_pipe.conductivity_passive")
                        .setStyle(new Style().setColor(TextFormatting.GRAY))));
        }

        FluidStack[] fluids = this.getContainedFluids();
        if (fluids != null) {
            boolean allTanksEmpty = true;
            for (int i = 0; i < fluids.length; i++) {
                if (fluids[i] != null) {
                    if (fluids[i].getFluid() == null)
                        continue;

                    allTanksEmpty = false;
                    list.add(new TextComponentTranslation("behavior.tricorder.tank", i,
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(fluids[i].amount))
                                    .setStyle(new Style().setColor(TextFormatting.GREEN)),
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.getCapacityPerTank()))
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                            new TextComponentTranslation(fluids[i].getFluid().getLocalizedName(fluids[i]))
                                    .setStyle(new Style().setColor(TextFormatting.GOLD))));
                }
            }

            if (allTanksEmpty)
                list.add(new TextComponentTranslation("behavior.tricorder.tanks_empty"));
        }
        return list;
    }

    private static class FluidTransaction {

        public final IFluidHandler target;
        public final IFluidHandler pipeTank;
        public final EnumFacing facing;
        public int amount;

        private FluidTransaction(IFluidHandler target, IFluidHandler pipeTank, int amount, EnumFacing facing) {
            this.target = target;
            this.pipeTank = pipeTank;
            this.amount = amount;
            this.facing = facing;
        }
    }
}

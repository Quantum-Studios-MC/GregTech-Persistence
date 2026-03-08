package gregtech.common.pipelike.fluidpipe;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.util.EntityDamageUtil;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import org.jetbrains.annotations.NotNull;

public class BlockFluidPipe extends BlockMaterialPipe<FluidPipeType, FluidPipeProperties, WorldFluidPipeNet> {

    public BlockFluidPipe(FluidPipeType pipeType, MaterialRegistry registry) {
        super(pipeType, registry);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    @Override
    public Class<FluidPipeType> getPipeTypeClass() {
        return FluidPipeType.class;
    }

    @Override
    public WorldFluidPipeNet getWorldPipeNet(World world) {
        return WorldFluidPipeNet.getWorldPipeNet(world);
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        // TODO insert to neighbours
    }

    @Override
    public boolean canPipesConnect(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile, EnumFacing side,
                                   IPipeTile<FluidPipeType, FluidPipeProperties> sideTile) {
        return selfTile instanceof TileEntityFluidPipe && sideTile instanceof TileEntityFluidPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile, EnumFacing side,
                                         TileEntity tile) {
        return tile != null &&
                tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockFluidPipe;
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
        super.onEntityCollision(worldIn, pos, state, entityIn);
        if (worldIn.isRemote) return;
        IPipeTile<FluidPipeType, FluidPipeProperties> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile == null) return;
        TileEntityFluidPipe pipe = (TileEntityFluidPipe) pipeTile;
        if (pipe instanceof TileEntityFluidPipeTickable && pipe.getFrameMaterial() == null &&
                ((TileEntityFluidPipeTickable) pipe).getOffsetTimer() % 10 == 0) {
            if (entityIn instanceof EntityLivingBase) {
                TileEntityFluidPipeTickable tickable = (TileEntityFluidPipeTickable) pipe;
                if (tickable.getFluidTanks().length > 1) {
                    int maxTemperature = Integer.MIN_VALUE;
                    int minTemperature = Integer.MAX_VALUE;
                    for (FluidTank tank : tickable.getFluidTanks()) {
                        if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                            maxTemperature = Math.max(maxTemperature,
                                    tank.getFluid().getFluid().getTemperature(tank.getFluid()));
                            minTemperature = Math.min(minTemperature,
                                    tank.getFluid().getFluid().getTemperature(tank.getFluid()));
                            tickable.applyElectricalConductivityDamage((EntityLivingBase) entityIn,
                                    tank.getFluid());
                        }
                    }
                    if (maxTemperature != Integer.MIN_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage((EntityLivingBase) entityIn, maxTemperature, 1.0F, 5);
                    }
                    if (minTemperature != Integer.MAX_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage((EntityLivingBase) entityIn, minTemperature, 1.0F, 5);
                    }
                } else {
                    FluidTank tank = tickable.getFluidTanks()[0];
                    if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                        EntityDamageUtil.applyTemperatureDamage((EntityLivingBase) entityIn,
                                tank.getFluid().getFluid().getTemperature(), 1.0F, 5);
                        tickable.applyElectricalConductivityDamage((EntityLivingBase) entityIn,
                                tank.getFluid());
                    }
                }
            }
        }
    }

    @Override
    public TileEntityPipeBase<FluidPipeType, FluidPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return new TileEntityFluidPipeTickable();
    }

    @Override
    public boolean onPipeActivated(World world, IBlockState state, BlockPos pos, EntityPlayer entityPlayer,
                                   EnumHand hand, EnumFacing side, CuboidRayTraceResult hit,
                                   IPipeTile<FluidPipeType, FluidPipeProperties> pipeTile) {
        ItemStack heldItem = entityPlayer.getHeldItem(hand);
        if (heldItem.getItem().getToolClasses(heldItem).contains(ToolClasses.PLUNGER)) {
            if (pipeTile instanceof TileEntityFluidPipeTickable tickable && tickable.isClogged()) {
                if (!world.isRemote) {
                    tickable.repairIntegrity(0);
                    world.playSound(null, pos, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    ToolHelper.damageItem(heldItem, entityPlayer);
                    ToolHelper.playToolSound(heldItem, entityPlayer);
                }
                return true;
            }
        }
        return super.onPipeActivated(world, state, pos, entityPlayer, hand, side, hit, pipeTile);
    }
}

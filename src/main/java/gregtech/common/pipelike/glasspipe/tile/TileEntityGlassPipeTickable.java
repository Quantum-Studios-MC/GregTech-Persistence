package gregtech.common.pipelike.glasspipe.tile;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverableView;
import gregtech.common.covers.CoverPump;
import gregtech.common.pipelike.glasspipe.GlassPipeProperties;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class TileEntityGlassPipeTickable extends TileEntityGlassPipe implements ITickable {

    private static final int FREQUENCY = 20;

    public byte lastReceivedFrom = 0, oldLastReceivedFrom = 0;
    private long timer = 0L;
    private final int offset = GTValues.RNG.nextInt(20);

    public long getOffsetTimer() {
        return timer + offset;
    }

    @Override
    public boolean supportsTicking() {
        return true;
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

            boolean shouldDistribute = (oldLastReceivedFrom == lastReceivedFrom);
            FluidTank tank = getFluidTank();
            FluidStack fluid = tank.getFluid();

            if (fluid != null && fluid.amount > 0 && shouldDistribute) {
                distributeFluid(tank, fluid);
                lastReceivedFrom = 0;
            }
            oldLastReceivedFrom = lastReceivedFrom;
            syncFluidToClient();
        }
    }

    private void distributeFluid(FluidTank tank, FluidStack fluid) {
        List<FluidTransaction> targets = new ArrayList<>();
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

            // Respect covers (pumps etc.)
            Cover cover = getCoverableImplementation().getCoverAtSide(facing);
            IFluidHandler sourceTank = tank;
            if (cover != null) {
                sourceTank = cover.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, sourceTank);
                if (sourceTank == null || checkForPumpCover(cover)) continue;
            } else {
                CoverableView coverable = neighbor.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
                        facing.getOpposite());
                if (coverable != null) {
                    cover = coverable.getCoverAtSide(facing.getOpposite());
                    if (checkForPumpCover(cover)) continue;
                }
            }

            FluidStack drainable = sourceTank.drain(maxFluid, false);
            if (drainable == null || drainable.amount <= 0) continue;

            int filled = Math.min(fluidHandler.fill(maxFluid, false), drainable.amount);
            if (filled > 0) {
                targets.add(new FluidTransaction(fluidHandler, sourceTank, filled));
                availableCapacity += filled;
            }
            maxFluid.amount = amount;
        }

        if (availableCapacity <= 0) return;

        GlassPipeProperties props = getNodeData();
        double maxAmount = props != null ? Math.min(props.getThroughput(), fluid.amount) : fluid.amount;

        for (FluidTransaction transaction : targets) {
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
                transaction.source.drain(inserted, true);
            }
        }
    }

    private static boolean checkForPumpCover(Cover cover) {
        return cover instanceof CoverPump;
    }

    private static class FluidTransaction {

        final IFluidHandler target;
        final IFluidHandler source;
        int amount;

        FluidTransaction(IFluidHandler target, IFluidHandler source, int amount) {
            this.target = target;
            this.source = source;
            this.amount = amount;
        }
    }
}

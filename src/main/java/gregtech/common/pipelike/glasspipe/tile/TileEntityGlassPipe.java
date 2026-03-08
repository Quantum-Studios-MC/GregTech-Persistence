package gregtech.common.pipelike.glasspipe.tile;

import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.client.renderer.pipe.PipeRenderProperties;
import gregtech.common.pipelike.glasspipe.GlassPipeProperties;
import gregtech.common.pipelike.glasspipe.GlassPipeType;
import gregtech.common.pipelike.glasspipe.net.GlassPipeNet;
import gregtech.common.pipelike.glasspipe.net.WorldGlassPipeNet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

import static gregtech.api.capability.GregtechDataCodes.GLASS_PIPE_FLUID_UPDATE;

public class TileEntityGlassPipe extends TileEntityPipeBase<GlassPipeType, GlassPipeProperties> {

    private WeakReference<GlassPipeNet> currentPipeNet = new WeakReference<>(null);
    private FluidTank fluidTank;
    private FluidStack lastFluidForRender;

    @Override
    public Class<GlassPipeType> getPipeTypeClass() {
        return GlassPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    public int getCapacity() {
        GlassPipeProperties props = getNodeData();
        return props != null ? props.getThroughput() * 20 : 400;
    }

    public FluidTank getFluidTank() {
        if (fluidTank == null) {
            fluidTank = new FluidTank(getCapacity());
        }
        return fluidTank;
    }

    @Nullable
    public FluidStack getContainedFluid() {
        return getFluidTank().getFluid();
    }

    public GlassPipeNet getGlassPipeNet() {
        if (world == null || world.isRemote) return null;
        GlassPipeNet net = this.currentPipeNet.get();
        if (net != null && net.isValid() && net.containsNode(getPipePos()))
            return net;
        WorldGlassPipeNet worldNet = (WorldGlassPipeNet) getPipeBlock().getWorldPipeNet(getPipeWorld());
        net = worldNet.getNetFromPos(getPipePos());
        if (net != null) {
            this.currentPipeNet = new WeakReference<>(net);
        }
        return net;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new GlassPipeFluidHandler(this, facing));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        FluidTank tank = getFluidTank();
        if (tank.getFluid() != null) {
            compound.setTag("GlassFluid", tank.getFluid().writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("GlassFluid")) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("GlassFluid"));
            getFluidTank().fill(fluid, true);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IExtendedBlockState getRenderInformation(IExtendedBlockState state) {
        state = super.getRenderInformation(state);
        int fluidColor = 0;
        FluidStack fluid = getContainedFluid();
        if (fluid != null && fluid.amount > 0) {
            fluidColor = fluid.getFluid().getColor(fluid);
        }
        return state.withProperty(PipeRenderProperties.FLUID_COLOR_PROPERTY, fluidColor);
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        FluidStack fluid = getContainedFluid();
        buf.writeBoolean(fluid != null);
        if (fluid != null) {
            buf.writeCompoundTag(fluid.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            try {
                NBTTagCompound tag = buf.readCompoundTag();
                if (tag != null) {
                    FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
                    if (fluid != null) {
                        getFluidTank().fill(fluid, true);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public void syncFluidToClient() {
        if (world == null || world.isRemote) return;
        FluidStack current = getContainedFluid();
        boolean changed;
        if (current == null && lastFluidForRender == null) {
            changed = false;
        } else if (current == null || lastFluidForRender == null) {
            changed = true;
        } else {
            changed = !current.isFluidEqual(lastFluidForRender) || current.amount != lastFluidForRender.amount;
        }
        if (changed) {
            lastFluidForRender = current != null ? current.copy() : null;
            writeCustomData(GLASS_PIPE_FLUID_UPDATE, buf -> {
                buf.writeBoolean(current != null);
                if (current != null) {
                    buf.writeCompoundTag(current.writeToNBT(new NBTTagCompound()));
                }
            });
        }
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == GLASS_PIPE_FLUID_UPDATE) {
            if (buf.readBoolean()) {
                try {
                    NBTTagCompound tag = buf.readCompoundTag();
                    if (tag != null) {
                        FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
                        if (fluid != null) {
                            getFluidTank().setFluid(fluid);
                        } else {
                            getFluidTank().setFluid(null);
                        }
                    }
                } catch (Exception ignored) {}
            } else {
                getFluidTank().setFluid(null);
            }
            scheduleChunkForRenderUpdate();
        }
    }

    private static class GlassPipeFluidHandler implements IFluidHandler {

        private final TileEntityGlassPipe pipe;
        private final EnumFacing facing;

        GlassPipeFluidHandler(TileEntityGlassPipe pipe, EnumFacing facing) {
            this.pipe = pipe;
            this.facing = facing;
        }

        @Override
        public IFluidTankProperties @NotNull [] getTankProperties() {
            return pipe.getFluidTank().getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            GlassPipeProperties props = pipe.getNodeData();
            if (props == null) return 0;
            if (!props.test(resource)) return 0;
            int filled = pipe.getFluidTank().fill(resource, doFill);
            if (filled > 0 && doFill && facing != null && pipe instanceof TileEntityGlassPipeTickable tickable) {
                tickable.lastReceivedFrom |= (1 << facing.getIndex());
            }
            return filled;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) return null;
            return pipe.getFluidTank().drain(resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return pipe.getFluidTank().drain(maxDrain, doDrain);
        }
    }
}

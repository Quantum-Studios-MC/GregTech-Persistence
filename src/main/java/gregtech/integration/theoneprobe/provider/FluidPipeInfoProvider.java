package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import org.jetbrains.annotations.NotNull;

public class FluidPipeInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":fluid_pipe_provider";
    }

    @Override
    public void addProbeInfo(@NotNull ProbeMode mode, @NotNull IProbeInfo probeInfo, @NotNull EntityPlayer player,
                             @NotNull World world, @NotNull IBlockState blockState, @NotNull IProbeHitData data) {
        TileEntity tileEntity = world.getTileEntity(data.getPos());
        if (!(tileEntity instanceof TileEntityFluidPipeTickable pipe)) return;

        int integrity = pipe.getIntegrity();
        int maxIntegrity = TileEntityFluidPipeTickable.MAX_INTEGRITY;
        int percent = integrity * 100 / maxIntegrity;

        TextFormatting color = percent > 50 ? TextFormatting.GREEN :
                percent > 25 ? TextFormatting.YELLOW :
                        percent > 0 ? TextFormatting.RED : TextFormatting.DARK_RED;

        probeInfo.text(IProbeInfo.STARTLOC + "gregtech.top.fluid_pipe.integrity" + IProbeInfo.ENDLOC + " " +
                color + percent + "%");

        if (pipe.isClogged()) {
            probeInfo.text(TextFormatting.DARK_GRAY + "" + TextFormatting.BOLD +
                    IProbeInfo.STARTLOC + "gregtech.top.fluid_pipe.clogged" + IProbeInfo.ENDLOC);
        }

        int conductivityTier = pipe.getConductivityTier();
        if (conductivityTier > 0) {
            String conductivityKey = conductivityTier > 2 ? "gregtech.fluid_pipe.conductivity_tier.high" :
                conductivityTier > 1 ? "gregtech.fluid_pipe.conductivity_tier.medium" :
                    "gregtech.fluid_pipe.conductivity_tier.low";
            probeInfo.text(IProbeInfo.STARTLOC + "gregtech.top.fluid_pipe.conductivity" + IProbeInfo.ENDLOC + " " +
                IProbeInfo.STARTLOC + conductivityKey + IProbeInfo.ENDLOC);
            probeInfo.text(IProbeInfo.STARTLOC + "gregtech.top.fluid_pipe.electrical_state" + IProbeInfo.ENDLOC + " " +
                IProbeInfo.STARTLOC +
                (pipe.isElectricallyEnergized() ? "gregtech.fluid_pipe.conductivity_energized" :
                    "gregtech.fluid_pipe.conductivity_passive") +
                IProbeInfo.ENDLOC);
        }

        if (mode == ProbeMode.EXTENDED) {
            FluidPipeType pipeType = pipe.getPipeType();
            if (pipeType == FluidPipeType.TINY) {
                probeInfo.text(TextFormatting.GRAY +
                        IProbeInfo.STARTLOC + "gregtech.top.fluid_pipe.tiny_restriction" + IProbeInfo.ENDLOC);
            }
        }
    }
}

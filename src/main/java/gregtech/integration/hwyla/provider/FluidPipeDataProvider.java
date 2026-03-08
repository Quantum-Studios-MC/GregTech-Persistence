package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidPipeDataProvider implements IWailaDataProvider {

    public static final FluidPipeDataProvider INSTANCE = new FluidPipeDataProvider();

    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, BlockFluidPipe.class);
        registrar.registerNBTProvider(this, BlockFluidPipe.class);
        registrar.addConfig(GTValues.MOD_NAME, "gregtech.fluid_pipe");
    }

    @Override
    public @NotNull NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
                                              BlockPos pos) {
        if (te instanceof TileEntityFluidPipeTickable pipe) {
            NBTTagCompound subTag = new NBTTagCompound();
            subTag.setInteger("Integrity", pipe.getIntegrity());
            subTag.setBoolean("Clogged", pipe.isClogged());
            subTag.setInteger("ConductivityTier", pipe.getConductivityTier());
            subTag.setBoolean("Energized", pipe.isElectricallyEnergized());
            tag.setTag("gregtech.FluidPipe", subTag);
        }
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.fluid_pipe")) return tooltip;

        if (accessor.getNBTData().hasKey("gregtech.FluidPipe")) {
            NBTTagCompound pipeTag = accessor.getNBTData().getCompoundTag("gregtech.FluidPipe");
            int integrity = pipeTag.getInteger("Integrity");
            boolean clogged = pipeTag.getBoolean("Clogged");
            int conductivityTier = pipeTag.getInteger("ConductivityTier");
            boolean energized = pipeTag.getBoolean("Energized");
            int percent = integrity * 100 / TileEntityFluidPipeTickable.MAX_INTEGRITY;

            TextFormatting color = percent > 50 ? TextFormatting.GREEN :
                    percent > 25 ? TextFormatting.YELLOW :
                            percent > 0 ? TextFormatting.RED : TextFormatting.DARK_RED;

            tooltip.add(I18n.format("gregtech.waila.fluid_pipe.integrity") + " " + color + percent + "%");

            if (clogged) {
                tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.BOLD +
                        I18n.format("gregtech.waila.fluid_pipe.clogged"));
            }

            if (conductivityTier > 0) {
                String conductivityKey = conductivityTier > 2 ? "gregtech.fluid_pipe.conductivity_tier.high" :
                        conductivityTier > 1 ? "gregtech.fluid_pipe.conductivity_tier.medium" :
                                "gregtech.fluid_pipe.conductivity_tier.low";
                tooltip.add(I18n.format("gregtech.waila.fluid_pipe.conductivity") + " " +
                        I18n.format(conductivityKey));
                tooltip.add(I18n.format("gregtech.waila.fluid_pipe.electrical_state") + " " +
                        I18n.format(energized ? "gregtech.fluid_pipe.conductivity_energized" :
                                "gregtech.fluid_pipe.conductivity_passive"));
            }
        }
        return tooltip;
    }
}

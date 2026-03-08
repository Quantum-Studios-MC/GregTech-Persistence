package gregtech.common.pipelike.fluidpipe;

import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.ConfigHolder;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBlockFluidPipe extends ItemBlockMaterialPipe<FluidPipeType, FluidPipeProperties> {

    public ItemBlockFluidPipe(BlockFluidPipe block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        FluidPipeProperties pipeProperties = blockPipe.createItemProperties(stack);
        FluidPipeType pipeType = ((BlockFluidPipe) blockPipe).getPipeType();

        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", pipeProperties.getThroughput()));
        tooltip.add(I18n.format("gregtech.fluid_pipe.capacity", pipeProperties.getThroughput() * 20));
        tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", pipeProperties.getMaxFluidTemperature()));
        if (pipeProperties.getTanks() > 1)
            tooltip.add(I18n.format("gregtech.fluid_pipe.channels", pipeProperties.getTanks()));

        if (pipeProperties.isGasProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
        if (pipeProperties.isPlasmaProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
        if (pipeProperties.isCryoProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
        if (pipeProperties.isAcidProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));

        // Show pH range restrictions
        double minPH = pipeProperties.getMinPH();
        double maxPH = pipeProperties.getMaxPH();
        String tierKey = pipeProperties.getPHResistanceTierKey();
        String tierName = I18n.format(tierKey);
        if (minPH <= 0.0 && maxPH >= 14.0) {
            tooltip.add(I18n.format("gregtech.fluid_pipe.ph_unrestricted", tierName));
        } else {
            tooltip.add(I18n.format("gregtech.fluid_pipe.ph_range",
                    String.format("%.1f", minPH), String.format("%.1f", maxPH), tierName));
        }

        // Show pressure system info
        if (ConfigHolder.machines.pressure.enablePressureSystem) {
            int burstPressure = pipeProperties.getBurstPressure();
            int friction = pipeProperties.getFriction();
            String ratingKey = burstPressure >= 16000 ? "gregtech.fluid_pipe.pressure_rating.extreme" :
                    burstPressure >= 8000 ? "gregtech.fluid_pipe.pressure_rating.high" :
                    burstPressure >= 4000 ? "gregtech.fluid_pipe.pressure_rating.medium" :
                    "gregtech.fluid_pipe.pressure_rating.low";
            tooltip.add(I18n.format("gregtech.fluid_pipe.burst_rating", burstPressure, I18n.format(ratingKey)));
            tooltip.add(I18n.format("gregtech.fluid_pipe.friction_info", friction));
        }

        BlockMaterialPipe<?, ?, ?> blockMaterialPipe = (BlockMaterialPipe<?, ?, ?>) blockPipe;

        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: " + blockMaterialPipe.getPrefix().name +
                    blockMaterialPipe.getItemMaterial(stack).toCamelCaseString());
        }
    }
}

package gregtech.common.pipelike.glasspipe;

import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBlockGlassPipe extends ItemBlockPipe<GlassPipeType, GlassPipeProperties> {

    public ItemBlockGlassPipe(BlockGlassPipe block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        GlassPipeProperties props = blockPipe.createItemProperties(stack);
        if (props == null) return;

        GlassPipeType pipeType = ((BlockGlassPipe) blockPipe).getPipeType();

        tooltip.add(I18n.format("gregtech.glass_pipe.transparent"));
        tooltip.add(I18n.format("gregtech.glass_pipe.tier", pipeType.getTierName()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", props.getThroughput()));
        tooltip.add(I18n.format("gregtech.fluid_pipe.capacity", props.getThroughput() * 20));
        tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", props.getMaxFluidTemperature()));

        // Glass pipes are chemically inert - no pH or attribute constraints
        tooltip.add(I18n.format("gregtech.glass_pipe.chemically_inert"));

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.tool_action.wrench.connect_and_block"));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
        }
    }
}

package gregtech.common.pipelike.itempipe;

import gregtech.api.GTValues;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.utils.TooltipHelper;
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
import java.util.StringJoiner;

public class ItemBlockItemPipe extends ItemBlockMaterialPipe<ItemPipeType, ItemPipeProperties> {

    public ItemBlockItemPipe(BlockItemPipe block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemPipeProperties pipeProperties = blockPipe.createItemProperties(stack);
        ItemPipeType pipeType = ((BlockItemPipe) blockPipe).getPipeType();

        if (pipeProperties.getTransferRate() % 1 != 0) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate",
                    (int) ((pipeProperties.getTransferRate() * 64) + 0.5)));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks",
                    (int) pipeProperties.getTransferRate()));
        }
        tooltip.add(I18n.format("gregtech.item_pipe.priority", pipeProperties.getPriority()));

        // Show item size thresholds for specific OrePrefix forms
        long maxSize = pipeType.getMaxItemSize();
        if (maxSize < 0) {
            tooltip.add(I18n.format("gregtech.item_pipe.size.unlimited"));
        } else {
            tooltip.add(I18n.format("gregtech.item_pipe.size.max", maxSize / GTValues.M));
            appendFittingPrefixes(tooltip, maxSize, true);
            appendFittingPrefixes(tooltip, maxSize, false);
            tooltip.add(I18n.format("gregtech.item_pipe.size.general_note"));
        }

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.tool_action.wrench.connect"));
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
        } else {
            tooltip.add(I18n.format("gregtech.tool_action.show_tooltips"));
        }

        if (ConfigHolder.misc.debug) {
            BlockMaterialPipe<?, ?, ?> blockMaterialPipe = (BlockMaterialPipe<?, ?, ?>) blockPipe;
            tooltip.add("MetaItem Id: " + blockMaterialPipe.getPrefix().name +
                    blockMaterialPipe.getItemMaterial(stack).toCamelCaseString());
        }
    }

    /**
     * The specific OrePrefix forms that item pipes check size restrictions against.
     * Only these forms are subject to pipe size limits - general items and blocks pass freely.
     */
    public static final Object[][] SIZE_CHECKED_PREFIXES = {
            { "Nuggets", OrePrefix.nugget },
            { "Dusts", OrePrefix.dust },
            { "Ingots", OrePrefix.ingot },
            { "Gems", OrePrefix.gem },
            { "Plates", OrePrefix.plate },
            { "Gears", OrePrefix.gear },
            { "Rotors", OrePrefix.rotor },
            { "Blocks", OrePrefix.block },
            { "Dust Blocks", OrePrefix.blockDust },
            { "Dense Plates", OrePrefix.plateDense },
    };

    @SideOnly(Side.CLIENT)
    private static void appendFittingPrefixes(@NotNull List<String> tooltip, long maxSize, boolean fits) {
        StringJoiner joiner = new StringJoiner(", ");
        for (Object[] entry : SIZE_CHECKED_PREFIXES) {
            long amount = ((OrePrefix) entry[1]).getMaterialAmount(null);
            boolean canFit = amount > 0 && amount <= maxSize;
            if (canFit == fits) {
                joiner.add((String) entry[0]);
            }
        }
        String list = joiner.toString();
        if (!list.isEmpty()) {
            if (fits) {
                tooltip.add(I18n.format("gregtech.item_pipe.size.can_fit", list));
            } else {
                tooltip.add(I18n.format("gregtech.item_pipe.size.cannot_fit", list));
            }
        }
    }
}

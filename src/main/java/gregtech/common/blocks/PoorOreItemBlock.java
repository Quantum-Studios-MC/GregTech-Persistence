package gregtech.common.blocks;

import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.LocalizationUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class PoorOreItemBlock extends OreItemBlock {

    private final BlockPoorOre poorOreBlock;

    public PoorOreItemBlock(BlockPoorOre poorOreBlock) {
        super(poorOreBlock);
        this.poorOreBlock = poorOreBlock;
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack stack) {
        IBlockState blockState = poorOreBlock.getStateFromMeta(getMetadata(stack.getItemDamage()));
        StoneType stoneType = blockState.getValue(poorOreBlock.STONE_TYPE);
        String matLocalized = poorOreBlock.material.getLocalizedName();
        // Try stone-specific key first: item.material.oreprefix.orePoor.<stoneTypeName>
        String stoneSpecificKey = "item.material.oreprefix.orePoor." + stoneType.name;
        if (LocalizationUtils.hasKey(stoneSpecificKey)) {
            return LocalizationUtils.format(stoneSpecificKey, matLocalized);
        }
        return OrePrefix.orePoor.getLocalNameForItem(poorOreBlock.material);
    }
}

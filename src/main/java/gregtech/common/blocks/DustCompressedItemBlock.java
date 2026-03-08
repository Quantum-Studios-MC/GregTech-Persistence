package gregtech.common.blocks;

import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class DustCompressedItemBlock extends ItemBlock {

    private final BlockDustCompressed block;

    public DustCompressedItemBlock(BlockDustCompressed block) {
        super(block);
        this.block = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack stack) {
        return OrePrefix.blockDust.getLocalNameForItem(block.getGtMaterial(stack));
    }
}

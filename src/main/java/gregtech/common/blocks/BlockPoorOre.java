package gregtech.common.blocks;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.client.model.PoorOreBakedModel;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.stream.Collectors;

public class BlockPoorOre extends BlockOre {

    private static final Random DROP_RANDOM = new Random();

    public BlockPoorOre(Material material, StoneType[] allowedValues) {
        super(material, allowedValues);
        setTranslationKey("poor_ore_block");
        setHardness(2.0f);
        setResistance(3.0f);
    }

    @Override
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world,
                         @NotNull BlockPos pos, @NotNull net.minecraft.block.state.IBlockState state, int fortune) {
        // GTNH-style poor ore drops: crushed ore, impure dust, or gem variants
        ItemStack mainDrop;
        if (material.hasProperty(PropertyKey.GEM)) {
            mainDrop = getGemMaterialDrop();
        } else {
            mainDrop = getNonGemMaterialDrop();
        }
        if (!mainDrop.isEmpty()) {
            drops.add(mainDrop);
        }

        // Fortune bonus: chance of extra crushed ore
        if (fortune > 0 && DROP_RANDOM.nextInt(fortune + 2) - 1 > 0) {
            ItemStack bonus = OreDictUnifier.get(OrePrefix.crushed, material);
            if (!bonus.isEmpty()) {
                drops.add(bonus);
            }
        }

        // ~33% chance of stone dust as bonus drop
        if (DROP_RANDOM.nextInt(3) == 0) {
            StoneType stoneType = state.getValue(STONE_TYPE);
            if (stoneType.stoneMaterial.hasProperty(PropertyKey.DUST)) {
                ItemStack stoneDust = OreDictUnifier.get(OrePrefix.dust, stoneType.stoneMaterial);
                if (!stoneDust.isEmpty()) {
                    drops.add(stoneDust);
                }
            }
        }
    }

    /**
     * GTNH-style weighted random drops for gem materials.
     * Weights: gemExquisite(1), gemFlawless(2), gem(12), gemFlawed(5), crushed(10), gemChipped(5), dustImpure(10)
     */
    private ItemStack getGemMaterialDrop() {
        int roll = DROP_RANDOM.nextInt(45);
        if (roll < 1) {
            return getWithFallback(OrePrefix.gemExquisite, OrePrefix.gem);
        } else if (roll < 3) {
            return getWithFallback(OrePrefix.gemFlawless, OrePrefix.gem);
        } else if (roll < 15) {
            return getWithFallback(OrePrefix.gem, OrePrefix.crushed);
        } else if (roll < 20) {
            return getWithFallback(OrePrefix.gemFlawed, OrePrefix.crushed);
        } else if (roll < 30) {
            return OreDictUnifier.get(OrePrefix.crushed, material);
        } else if (roll < 35) {
            return getWithFallback(OrePrefix.gemChipped, OrePrefix.dustImpure);
        } else {
            return OreDictUnifier.get(OrePrefix.dustImpure, material);
        }
    }

    /**
     * For non-gem materials: 50% crushed ore, 50% impure dust.
     */
    private ItemStack getNonGemMaterialDrop() {
        if (DROP_RANDOM.nextBoolean()) {
            return OreDictUnifier.get(OrePrefix.crushed, material);
        } else {
            return OreDictUnifier.get(OrePrefix.dustImpure, material);
        }
    }

    private ItemStack getWithFallback(OrePrefix primary, OrePrefix fallback) {
        ItemStack stack = OreDictUnifier.get(primary, material);
        if (stack.isEmpty()) {
            stack = OreDictUnifier.get(fallback, material);
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, b -> b.getBlockState().getValidStates().stream()
                .collect(Collectors.toMap(
                        s -> s,
                        s -> PoorOreBakedModel.registerPoorOreEntry(s.getValue(STONE_TYPE), this.material))));
        for (net.minecraft.block.state.IBlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state),
                    PoorOreBakedModel.registerPoorOreEntry(state.getValue(STONE_TYPE), this.material));
        }
    }
}

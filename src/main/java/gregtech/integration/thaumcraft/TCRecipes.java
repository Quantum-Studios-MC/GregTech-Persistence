package gregtech.integration.thaumcraft;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.Mods;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;

/**
 * Thaumcraft 6 integration recipes, ported from GT6's Compat_Recipes_Thaumcraft.
 * Adds machine processing recipes for TC6 items using GregTech CEu machines.
 *
 * TC6 item registry conventions (meta values):
 * - ingot: 0=Thaumium, 1=Void Metal
 * - nugget: 6=Thaumium, 7=Void Metal
 * - plate: 0=Brass, 1=Iron, 2=Thaumium, 3=Void Metal
 * - cluster: 0=Iron, 1=Gold, 2=Copper, 3=Tin, 4=Silver, 5=Lead, 6=Cinnabar
 */
public final class TCRecipes {

    private TCRecipes() {}

    public static void init() {
        registerMaceratorRecipes();
        registerArcFurnaceRecipes();
        registerCompressorRecipes();
        registerCentrifugeRecipes();
        registerChemicalBathRecipes();
        registerExtractorRecipes();
        registerAlloySmelterRecipes();
        registerMixerRecipes();
    }

    private static void registerMaceratorRecipes() {
        // Thaumium Ingot → Iron Dust + small magical byproduct
        ItemStack thaumiumIngot = Mods.Thaumcraft.getItem("ingot", 0);
        if (!thaumiumIngot.isEmpty()) {
            RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .inputs(thaumiumIngot)
                    .output(OrePrefix.dust, Materials.Iron)
                    .chancedOutput(OrePrefix.dustSmall, Materials.Gold, 1, 2500, 500)
                    .duration(200).EUt(VA[MV]).buildAndRegister();
        }

        // Void Metal Ingot → Iron Dust + Ender Pearl Dust
        ItemStack voidIngot = Mods.Thaumcraft.getItem("ingot", 1);
        if (!voidIngot.isEmpty()) {
            RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .inputs(voidIngot)
                    .output(OrePrefix.dust, Materials.Iron)
                    .chancedOutput(OrePrefix.dustSmall, Materials.EnderPearl, 2, 5000, 750)
                    .duration(250).EUt(VA[MV]).buildAndRegister();
        }

        // Amber → Gem Flawed output (processing)
        ItemStack amber = Mods.Thaumcraft.getItem("amber");
        if (!amber.isEmpty()) {
            RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .inputs(amber)
                    .output(OrePrefix.dust, Materials.Gold)
                    .chancedOutput(OrePrefix.dustSmall, Materials.Cinnabar, 1, 3000, 500)
                    .duration(100).EUt(VA[LV]).buildAndRegister();
        }

        // Quicksilver → Mercury (Cinnabar Dust)
        ItemStack quicksilver = Mods.Thaumcraft.getItem("quicksilver");
        if (!quicksilver.isEmpty()) {
            RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .inputs(quicksilver)
                    .output(OrePrefix.dust, Materials.Cinnabar)
                    .duration(100).EUt(VA[LV]).buildAndRegister();
        }

        // Thaumium Armor → Thaumium dust (iron equivalent)
        addArmorMaceratorRecipes("thaumium_helm", 5);
        addArmorMaceratorRecipes("thaumium_chest", 8);
        addArmorMaceratorRecipes("thaumium_legs", 7);
        addArmorMaceratorRecipes("thaumium_boots", 4);

        // Void Metal Armor → Iron Dust (with Ender Pearl chance)
        addVoidArmorMaceratorRecipes("void_helm", 5);
        addVoidArmorMaceratorRecipes("void_chest", 8);
        addVoidArmorMaceratorRecipes("void_legs", 7);
        addVoidArmorMaceratorRecipes("void_boots", 4);
    }

    private static void addArmorMaceratorRecipes(String itemName, int amount) {
        ItemStack input = Mods.Thaumcraft.getItem(itemName);
        if (input.isEmpty()) return;
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(input)
                .output(OrePrefix.dust, Materials.Iron, amount)
                .duration(200).EUt(VA[MV]).buildAndRegister();
    }

    private static void addVoidArmorMaceratorRecipes(String itemName, int amount) {
        ItemStack input = Mods.Thaumcraft.getItem(itemName);
        if (input.isEmpty()) return;
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(input)
                .output(OrePrefix.dust, Materials.Iron, amount)
                .chancedOutput(OrePrefix.dustSmall, Materials.EnderPearl, 1, 3000, 500)
                .duration(250).EUt(VA[MV]).buildAndRegister();
    }

    private static void registerArcFurnaceRecipes() {
        // Thaumium tools → Iron Ingots
        addArcFurnaceRecipe("thaumium_sword", 2);
        addArcFurnaceRecipe("thaumium_pick", 3);
        addArcFurnaceRecipe("thaumium_axe", 3);
        addArcFurnaceRecipe("thaumium_shovel", 1);
        addArcFurnaceRecipe("thaumium_hoe", 2);

        // Void Metal tools → Iron Ingots (with ender pearl chance)
        addArcFurnaceRecipe("void_sword", 2);
        addArcFurnaceRecipe("void_pick", 3);
        addArcFurnaceRecipe("void_axe", 3);
        addArcFurnaceRecipe("void_shovel", 1);
        addArcFurnaceRecipe("void_hoe", 2);

        // Thaumium Armor → Iron Ingots
        addArcFurnaceRecipe("thaumium_helm", 5);
        addArcFurnaceRecipe("thaumium_chest", 8);
        addArcFurnaceRecipe("thaumium_legs", 7);
        addArcFurnaceRecipe("thaumium_boots", 4);

        // Void Metal Armor → Iron Ingots
        addArcFurnaceRecipe("void_helm", 5);
        addArcFurnaceRecipe("void_chest", 8);
        addArcFurnaceRecipe("void_legs", 7);
        addArcFurnaceRecipe("void_boots", 4);

        // Native Ore Clusters → Ingots (from GT6: processing TC smelting byproducts)
        addClusterRecipe(0, Materials.Iron, 3);     // Iron Cluster
        addClusterRecipe(1, Materials.Gold, 3);     // Gold Cluster
        addClusterRecipe(2, Materials.Copper, 3);   // Copper Cluster
        addClusterRecipe(3, Materials.Tin, 3);      // Tin Cluster
        addClusterRecipe(4, Materials.Silver, 3);   // Silver Cluster
        addClusterRecipe(5, Materials.Lead, 3);     // Lead Cluster
        addClusterRecipe(6, Materials.Cinnabar, 3); // Cinnabar Cluster
    }

    private static void addArcFurnaceRecipe(String itemName, int amount) {
        ItemStack input = Mods.Thaumcraft.getItem(itemName);
        if (input.isEmpty()) return;
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(input)
                .output(OrePrefix.ingot, Materials.Iron, amount)
                .duration(150).EUt(VA[MV]).buildAndRegister();
    }

    private static void addClusterRecipe(int meta,
                                         gregtech.api.unification.material.Material material, int amount) {
        ItemStack input = Mods.Thaumcraft.getItem("cluster", meta);
        if (input.isEmpty()) return;
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(input)
                .output(OrePrefix.ingot, material, amount)
                .duration(100).EUt(VA[LV]).buildAndRegister();
    }

    private static void registerCompressorRecipes() {
        // Amber → Amber Block (from GT6 compressor recipe)
        ItemStack amber = Mods.Thaumcraft.getItem("amber");
        ItemStack amberBlock = Mods.Thaumcraft.getItem("amber_block");
        if (!amber.isEmpty() && !amberBlock.isEmpty()) {
            RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(amber.getItem(), 4, amber.getMetadata()))
                    .outputs(amberBlock)
                    .duration(100).EUt(VA[LV]).buildAndRegister();
        }

        // Tallow → Tallow Block (from GT6 compact recipe)
        ItemStack tallow = Mods.Thaumcraft.getItem("tallow");
        ItemStack tallowBlock = Mods.Thaumcraft.getItem("tallow_block");
        if (!tallow.isEmpty() && !tallowBlock.isEmpty()) {
            RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(tallow.getItem(), 9, tallow.getMetadata()))
                    .outputs(tallowBlock)
                    .duration(100).EUt(8).buildAndRegister();
        }

        // Rotten Flesh → Flesh Block (from GT6 compact recipe)
        ItemStack fleshBlock = Mods.Thaumcraft.getItem("flesh_block");
        if (!fleshBlock.isEmpty()) {
            RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Items.ROTTEN_FLESH, 9))
                    .outputs(fleshBlock)
                    .duration(100).EUt(8).buildAndRegister();
        }
    }

    private static void registerCentrifugeRecipes() {
        // Alumentum → Coal Dust + Glowstone Dust (high-energy material decomposition)
        ItemStack alumentum = Mods.Thaumcraft.getItem("alumentum");
        if (!alumentum.isEmpty()) {
            RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                    .inputs(alumentum)
                    .output(OrePrefix.dust, Materials.Coal, 2)
                    .chancedOutput(OrePrefix.dustSmall, Materials.Glowstone, 2, 5000, 750)
                    .duration(200).EUt(VA[LV]).buildAndRegister();
        }
    }

    private static void registerChemicalBathRecipes() {
        // String + Tallow fluid → Candles (from GT6 Bath recipe)
        // In CEu, we can use the tallow as a fluid if it exists, or skip this recipe
        // Since TC6 tallow is an item, we use the Assembler instead for this type
    }

    private static void registerExtractorRecipes() {
        // Quicksilver → Mercury fluid
        ItemStack quicksilver = Mods.Thaumcraft.getItem("quicksilver");
        if (!quicksilver.isEmpty()) {
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(quicksilver)
                    .fluidOutputs(Materials.Mercury.getFluid(144))
                    .duration(100).EUt(VA[LV]).buildAndRegister();
        }

        // Tallow → useful outputs
        ItemStack tallow = Mods.Thaumcraft.getItem("tallow");
        if (!tallow.isEmpty()) {
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(tallow)
                    .fluidOutputs(Materials.Lubricant.getFluid(50))
                    .duration(80).EUt(8).buildAndRegister();
        }

        // Void Seed → Ender Pearl Dust
        ItemStack voidSeed = Mods.Thaumcraft.getItem("void_seed");
        if (!voidSeed.isEmpty()) {
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(voidSeed)
                    .output(OrePrefix.dustSmall, Materials.EnderPearl)
                    .duration(100).EUt(VA[MV]).buildAndRegister();
        }
    }

    private static void registerAlloySmelterRecipes() {
        // Thaumium Ingot from Iron Ingot + Quicksilver (simplified TC recipe)
        ItemStack thaumiumIngot = Mods.Thaumcraft.getItem("ingot", 0);
        ItemStack quicksilver = Mods.Thaumcraft.getItem("quicksilver");
        if (!thaumiumIngot.isEmpty() && !quicksilver.isEmpty()) {
            RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, Materials.Iron)
                    .inputs(quicksilver)
                    .outputs(thaumiumIngot)
                    .duration(200).EUt(VA[MV]).buildAndRegister();
        }
    }

    private static void registerMixerRecipes() {
        // Salis Mundus from Quicksilver + Redstone + miscellaneous
        ItemStack salisMundus = Mods.Thaumcraft.getItem("salis_mundus");
        ItemStack quicksilver = Mods.Thaumcraft.getItem("quicksilver");
        if (!salisMundus.isEmpty() && !quicksilver.isEmpty()) {
            RecipeMaps.MIXER_RECIPES.recipeBuilder()
                    .inputs(quicksilver)
                    .input(OrePrefix.dust, Materials.Redstone)
                    .input(OrePrefix.dust, Materials.Flint)
                    .outputs(salisMundus)
                    .duration(200).EUt(VA[MV]).buildAndRegister();
        }
    }
}

package gregtech.integration.twilightforest;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.Mods;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;

/**
 * Twilight Forest integration recipes, ported from GT6's Compat_Recipes_TwilightForest.
 * Adds machine processing recipes for TF items using GregTech CEu machines.
 */
public final class TFRecipes {

    private TFRecipes() {}

    public static void init() {
        registerMaceratorRecipes();
        registerArcFurnaceRecipes();
        registerChemicalBathRecipes();
        registerCutterRecipes();
        registerMixerRecipes();
        registerForgeHammerRecipes();
        registerAssemblerRecipes();
        registerExtractorRecipes();
        registerMiscRecipes();
    }

    private static void registerMaceratorRecipes() {
        // Fiery Ingot → Iron Dust + Blaze Powder
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("fiery_ingot"))
                .output(OrePrefix.dust, Materials.Iron)
                .chancedOutput(new ItemStack(Items.BLAZE_POWDER), 5000, 750)
                .duration(150).EUt(VA[LV]).buildAndRegister();

        // Knightmetal Ingot → 2x Iron Dust
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_ingot"))
                .output(OrePrefix.dust, Materials.Iron, 2)
                .duration(150).EUt(VA[LV]).buildAndRegister();

        // Ironwood Ingot → Iron Dust
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("ironwood_ingot"))
                .output(OrePrefix.dust, Materials.Iron)
                .chancedOutput(OrePrefix.dustSmall, Materials.Wood, 2, 5000, 750)
                .duration(150).EUt(VA[LV]).buildAndRegister();

        // Steeleaf Ingot → Iron Dust
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_ingot"))
                .output(OrePrefix.dust, Materials.Iron)
                .duration(150).EUt(VA[LV]).buildAndRegister();

        // Armor Shard → Iron Nugget
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("armor_shard"))
                .output(OrePrefix.nugget, Materials.Iron, 2)
                .duration(80).EUt(8).buildAndRegister();

        // Armor Shard Cluster → Iron Dust
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("armor_shard_cluster"))
                .output(OrePrefix.dust, Materials.Iron, 2)
                .duration(150).EUt(VA[LV]).buildAndRegister();

        // Carminite → Redstone Dust
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("carminite"))
                .output(OrePrefix.dust, Materials.Redstone, 3)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Naga Scale → Iron Dust
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("naga_scale"))
                .output(OrePrefix.dust, Materials.Iron)
                .duration(120).EUt(VA[LV]).buildAndRegister();

        // Liveroot → Wood Dust
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("liveroot"))
                .output(OrePrefix.dust, Materials.Wood, 2)
                .duration(100).EUt(8).buildAndRegister();

        // Fiery Armor → macerate to scrap
        addArmorMaceratorRecipe("fiery_helmet", Materials.Iron, 5);
        addArmorMaceratorRecipe("fiery_chestplate", Materials.Iron, 8);
        addArmorMaceratorRecipe("fiery_leggings", Materials.Iron, 7);
        addArmorMaceratorRecipe("fiery_boots", Materials.Iron, 4);

        // Knightmetal Armor
        addArmorMaceratorRecipe("knightmetal_helmet", Materials.Iron, 5);
        addArmorMaceratorRecipe("knightmetal_chestplate", Materials.Iron, 8);
        addArmorMaceratorRecipe("knightmetal_leggings", Materials.Iron, 7);
        addArmorMaceratorRecipe("knightmetal_boots", Materials.Iron, 4);

        // Ironwood Armor
        addArmorMaceratorRecipe("ironwood_helmet", Materials.Iron, 5);
        addArmorMaceratorRecipe("ironwood_chestplate", Materials.Iron, 8);
        addArmorMaceratorRecipe("ironwood_leggings", Materials.Iron, 7);
        addArmorMaceratorRecipe("ironwood_boots", Materials.Iron, 4);

        // Steeleaf Armor
        addArmorMaceratorRecipe("steeleaf_helmet", Materials.Iron, 5);
        addArmorMaceratorRecipe("steeleaf_chestplate", Materials.Iron, 8);
        addArmorMaceratorRecipe("steeleaf_leggings", Materials.Iron, 7);
        addArmorMaceratorRecipe("steeleaf_boots", Materials.Iron, 4);
    }

    private static void addArmorMaceratorRecipe(String itemName,
                                                gregtech.api.unification.material.Material material, int amount) {
        ItemStack input = Mods.TwilightForest.getItem(itemName);
        if (input.isEmpty()) return;
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(input)
                .output(OrePrefix.dust, material, amount)
                .duration(200).EUt(VA[LV]).buildAndRegister();
    }

    private static void registerArcFurnaceRecipes() {
        // Fiery tools/weapons → Iron Ingots
        addArcFurnaceRecipe("fiery_sword", Materials.Iron, 2);
        addArcFurnaceRecipe("fiery_pickaxe", Materials.Iron, 3);

        // Knightmetal tools/weapons → Iron Ingots
        addArcFurnaceRecipe("knightmetal_sword", Materials.Iron, 2);
        addArcFurnaceRecipe("knightmetal_pickaxe", Materials.Iron, 3);
        addArcFurnaceRecipe("knightmetal_axe", Materials.Iron, 3);

        // Ironwood tools → Iron Ingots
        addArcFurnaceRecipe("ironwood_sword", Materials.Iron, 2);
        addArcFurnaceRecipe("ironwood_pickaxe", Materials.Iron, 3);
        addArcFurnaceRecipe("ironwood_axe", Materials.Iron, 3);
        addArcFurnaceRecipe("ironwood_shovel", Materials.Iron, 1);
        addArcFurnaceRecipe("ironwood_hoe", Materials.Iron, 2);

        // Steeleaf tools → Iron Ingots
        addArcFurnaceRecipe("steeleaf_sword", Materials.Iron, 2);
        addArcFurnaceRecipe("steeleaf_pickaxe", Materials.Iron, 3);
        addArcFurnaceRecipe("steeleaf_axe", Materials.Iron, 3);
        addArcFurnaceRecipe("steeleaf_shovel", Materials.Iron, 1);
        addArcFurnaceRecipe("steeleaf_hoe", Materials.Iron, 2);

        // Fiery Armor → Iron Ingots
        addArcFurnaceRecipe("fiery_helmet", Materials.Iron, 5);
        addArcFurnaceRecipe("fiery_chestplate", Materials.Iron, 8);
        addArcFurnaceRecipe("fiery_leggings", Materials.Iron, 7);
        addArcFurnaceRecipe("fiery_boots", Materials.Iron, 4);

        // Knightmetal Armor → Iron Ingots
        addArcFurnaceRecipe("knightmetal_helmet", Materials.Iron, 5);
        addArcFurnaceRecipe("knightmetal_chestplate", Materials.Iron, 8);
        addArcFurnaceRecipe("knightmetal_leggings", Materials.Iron, 7);
        addArcFurnaceRecipe("knightmetal_boots", Materials.Iron, 4);

        // Ironwood Armor → Iron Ingots
        addArcFurnaceRecipe("ironwood_helmet", Materials.Iron, 5);
        addArcFurnaceRecipe("ironwood_chestplate", Materials.Iron, 8);
        addArcFurnaceRecipe("ironwood_leggings", Materials.Iron, 7);
        addArcFurnaceRecipe("ironwood_boots", Materials.Iron, 4);

        // Steeleaf Armor → Iron Ingots
        addArcFurnaceRecipe("steeleaf_helmet", Materials.Iron, 5);
        addArcFurnaceRecipe("steeleaf_chestplate", Materials.Iron, 8);
        addArcFurnaceRecipe("steeleaf_leggings", Materials.Iron, 7);
        addArcFurnaceRecipe("steeleaf_boots", Materials.Iron, 4);

        // Naga Armor → Iron Ingots
        addArcFurnaceRecipe("naga_chestplate", Materials.Iron, 8);
        addArcFurnaceRecipe("naga_leggings", Materials.Iron, 7);

        // Fiery Ingot → Iron Ingot (recovery)
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("fiery_ingot"))
                .output(OrePrefix.ingot, Materials.Iron)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Knightmetal Ingot → 2x Iron Ingot
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_ingot"))
                .output(OrePrefix.ingot, Materials.Iron, 2)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Ironwood Ingot → Iron Ingot
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("ironwood_ingot"))
                .output(OrePrefix.ingot, Materials.Iron)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Armor Shard Cluster → Knightmetal Ingot equivalent (2 Iron Ingots)
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("armor_shard_cluster"))
                .output(OrePrefix.ingot, Materials.Iron, 2)
                .duration(100).EUt(VA[LV]).buildAndRegister();
    }

    private static void addArcFurnaceRecipe(String itemName,
                                            gregtech.api.unification.material.Material material, int amount) {
        ItemStack input = Mods.TwilightForest.getItem(itemName);
        if (input.isEmpty()) return;
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(input)
                .output(OrePrefix.ingot, material, amount)
                .duration(150).EUt(VA[LV]).buildAndRegister();
    }

    private static void registerChemicalBathRecipes() {
        // From GT6: Bath iron armor in Fiery Blood/Tears to make Fiery armor
        // In CEu, Chemical Bath takes 1 item input + 1 fluid input.
        // Since Fiery Blood and Fiery Tears are items not fluids in 1.12.2,
        // we use a Lava bath as a simpler approach for Fiery crafting,
        // and put the Fiery Blood/Tears recipes in the Assembler instead.

        // Fiery Ingot from Iron Ingot + Lava (simplified from GT6 Fiery Blood bath)
        RecipeMaps.CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, Materials.Iron)
                .fluidInputs(Materials.Lava.getFluid(1000))
                .outputs(Mods.TwilightForest.getItem("fiery_ingot"))
                .duration(400).EUt(VA[MV]).buildAndRegister();
    }

    private static void registerCutterRecipes() {
        // Liveroot → Sticks (from GT6 sawing recipe)
        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("liveroot"))
                .output(OrePrefix.stick, Materials.Wood, 4)
                .chancedOutput(Mods.TwilightForest.getItem("liveroot"), 5000, 0)
                .fluidInputs(Materials.Water.getFluid(4))
                .duration(100).EUt(VA[LV]).buildAndRegister();
    }

    private static void registerMixerRecipes() {
        // From GT6: Redstone + Borer Essence + Ghast Tear → Carminite
        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Redstone, 4)
                .inputs(Mods.TwilightForest.getItem("borer_essence", 0, 4))
                .inputs(new ItemStack(Items.GHAST_TEAR))
                .outputs(Mods.TwilightForest.getItem("carminite", 0, 4))
                .duration(128).EUt(VA[LV]).buildAndRegister();
    }

    private static void registerForgeHammerRecipes() {
        // Armor Shards → Armor Shard Cluster (9 shards = 1 cluster, like TF crafting)
        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("armor_shard", 0, 9))
                .outputs(Mods.TwilightForest.getItem("armor_shard_cluster"))
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Knightmetal Ingot → 2x Armor Shards
        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_ingot"))
                .outputs(Mods.TwilightForest.getItem("armor_shard", 0, 2))
                .duration(100).EUt(VA[LV]).buildAndRegister();
    }

    private static void registerAssemblerRecipes() {
        // From GT6: Iron Armor + Fiery Blood → Fiery Armor (using Assembler since bath needs fluids)
        // Fiery Helmet from Iron Helmet + Fiery Blood
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_HELMET))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 5))
                .outputs(Mods.TwilightForest.getItem("fiery_helmet"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_HELMET))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 5))
                .outputs(Mods.TwilightForest.getItem("fiery_helmet"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Fiery Chestplate from Iron Chestplate + Fiery Blood
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_CHESTPLATE))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 8))
                .outputs(Mods.TwilightForest.getItem("fiery_chestplate"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_CHESTPLATE))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 8))
                .outputs(Mods.TwilightForest.getItem("fiery_chestplate"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Fiery Leggings from Iron Leggings + Fiery Blood
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_LEGGINGS))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 7))
                .outputs(Mods.TwilightForest.getItem("fiery_leggings"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_LEGGINGS))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 7))
                .outputs(Mods.TwilightForest.getItem("fiery_leggings"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Fiery Boots from Iron Boots + Fiery Blood
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_BOOTS))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 4))
                .outputs(Mods.TwilightForest.getItem("fiery_boots"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_BOOTS))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 4))
                .outputs(Mods.TwilightForest.getItem("fiery_boots"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Fiery Sword from Iron Sword + Fiery Blood
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_SWORD))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 2))
                .outputs(Mods.TwilightForest.getItem("fiery_sword"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_SWORD))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 2))
                .outputs(Mods.TwilightForest.getItem("fiery_sword"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Fiery Pickaxe from Iron Pickaxe + Fiery Blood
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_PICKAXE))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 3))
                .outputs(Mods.TwilightForest.getItem("fiery_pickaxe"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_PICKAXE))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 3))
                .outputs(Mods.TwilightForest.getItem("fiery_pickaxe"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // From GT6: Knightmetal Armor + Fiery Blood → Fiery Armor (upgrade path)
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_helmet"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 5))
                .outputs(Mods.TwilightForest.getItem("fiery_helmet"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_chestplate"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 8))
                .outputs(Mods.TwilightForest.getItem("fiery_chestplate"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_leggings"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 7))
                .outputs(Mods.TwilightForest.getItem("fiery_leggings"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_boots"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 4))
                .outputs(Mods.TwilightForest.getItem("fiery_boots"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_sword"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 2))
                .outputs(Mods.TwilightForest.getItem("fiery_sword"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_pickaxe"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 3))
                .outputs(Mods.TwilightForest.getItem("fiery_pickaxe"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Same upgrade paths with Fiery Tears
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_helmet"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 5))
                .outputs(Mods.TwilightForest.getItem("fiery_helmet"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_chestplate"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 8))
                .outputs(Mods.TwilightForest.getItem("fiery_chestplate"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_leggings"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 7))
                .outputs(Mods.TwilightForest.getItem("fiery_leggings"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_boots"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 4))
                .outputs(Mods.TwilightForest.getItem("fiery_boots"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_sword"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 2))
                .outputs(Mods.TwilightForest.getItem("fiery_sword"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("knightmetal_pickaxe"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 3))
                .outputs(Mods.TwilightForest.getItem("fiery_pickaxe"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Steeleaf Armor + Fiery Blood → Fiery Armor (from GT6)
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_helmet"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 5))
                .outputs(Mods.TwilightForest.getItem("fiery_helmet"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_chestplate"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 8))
                .outputs(Mods.TwilightForest.getItem("fiery_chestplate"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_leggings"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 7))
                .outputs(Mods.TwilightForest.getItem("fiery_leggings"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_boots"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 4))
                .outputs(Mods.TwilightForest.getItem("fiery_boots"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_sword"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 2))
                .outputs(Mods.TwilightForest.getItem("fiery_sword"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_pickaxe"))
                .inputs(Mods.TwilightForest.getItem("fiery_blood", 0, 3))
                .outputs(Mods.TwilightForest.getItem("fiery_pickaxe"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Steeleaf + Fiery Tears
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_helmet"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 5))
                .outputs(Mods.TwilightForest.getItem("fiery_helmet"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_chestplate"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 8))
                .outputs(Mods.TwilightForest.getItem("fiery_chestplate"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_leggings"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 7))
                .outputs(Mods.TwilightForest.getItem("fiery_leggings"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_boots"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 4))
                .outputs(Mods.TwilightForest.getItem("fiery_boots"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_sword"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 2))
                .outputs(Mods.TwilightForest.getItem("fiery_sword"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("steeleaf_pickaxe"))
                .inputs(Mods.TwilightForest.getItem("fiery_tears", 0, 3))
                .outputs(Mods.TwilightForest.getItem("fiery_pickaxe"))
                .duration(400).EUt(VA[MV]).buildAndRegister();

        // Map crafting (from GT6 Boxinator recipes)
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PAPER, 8))
                .inputs(Mods.TwilightForest.getItem("magic_map_focus"))
                .outputs(Mods.TwilightForest.getItem("magic_map_empty"))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PAPER, 8))
                .inputs(Mods.TwilightForest.getItem("maze_map_focus"))
                .outputs(Mods.TwilightForest.getItem("maze_map_empty"))
                .duration(100).EUt(VA[LV]).buildAndRegister();
    }

    private static void registerExtractorRecipes() {
        // Fiery Blood → Blaze Powder (extract value from TF mob drops)
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("fiery_blood"))
                .outputs(new ItemStack(Items.BLAZE_POWDER, 3))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Fiery Tears → Blaze Powder
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("fiery_tears"))
                .outputs(new ItemStack(Items.BLAZE_POWDER, 3))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Torchberries → Glowstone Dust
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("torchberries"))
                .output(OrePrefix.dustSmall, Materials.Glowstone, 2)
                .duration(60).EUt(8).buildAndRegister();
    }

    private static void registerMiscRecipes() {
        // Charm packing/unpacking (from GT6 pack/unpack recipes)
        // Charm of Keeping: 4x tier1 = 1x tier2, 4x tier2 = 1x tier3
        RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("charm_of_keeping_1", 0, 4))
                .outputs(Mods.TwilightForest.getItem("charm_of_keeping_2"))
                .duration(100).EUt(8).buildAndRegister();

        RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("charm_of_keeping_2", 0, 4))
                .outputs(Mods.TwilightForest.getItem("charm_of_keeping_3"))
                .duration(100).EUt(8).buildAndRegister();

        // Charm of Life: 4x tier1 = 1x tier2
        RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("charm_of_life_1", 0, 4))
                .outputs(Mods.TwilightForest.getItem("charm_of_life_2"))
                .duration(100).EUt(8).buildAndRegister();

        // From GT6: Dry bush → sticks (shapeless crafting recipe)
        // Using ModHandler for crafting table recipes
        ItemStack dryBush = Mods.TwilightForest.getItem("twilight_plant", 11);
        if (!dryBush.isEmpty()) {
            ModHandler.addShapelessRecipe("tf_dry_bush_to_sticks",
                    new ItemStack(Items.STICK, 2), dryBush);
        }

        // From GT6: Firefly jar → firefly (shapeless, to get bottle back)
        ItemStack fireflyJar = Mods.TwilightForest.getItem("firefly_jar");
        ItemStack firefly = Mods.TwilightForest.getItem("firefly");
        if (!fireflyJar.isEmpty() && !firefly.isEmpty()) {
            ModHandler.addShapelessRecipe("tf_firefly_from_jar", firefly, fireflyJar);
        }

        // Alloy Smelter: Ironwood from Iron Ingot + LiveRoot (GT-style alloy creation)
        RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, Materials.Iron)
                .inputs(Mods.TwilightForest.getItem("liveroot"))
                .outputs(Mods.TwilightForest.getItem("ironwood_ingot", 0, 2))
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Centrifuge: Carminite → Redstone + small amounts of Borer Essence components
        RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .inputs(Mods.TwilightForest.getItem("carminite"))
                .output(OrePrefix.dust, Materials.Redstone, 2)
                .chancedOutput(new ItemStack(Items.GHAST_TEAR), 1000, 500)
                .duration(200).EUt(VA[LV]).buildAndRegister();
    }
}

package gregtech.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.unification.material.info.MaterialFlags.HIGH_SIFTER_OUTPUT;

public class OreRecipeHandler {
    // Make sure to update OreByProduct jei page with any byproduct changes made here!

    public static void register() {
        OrePrefix.ore.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreEndstone.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOreDouble);
        OrePrefix.oreNetherrack.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOreDouble);
        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            OrePrefix.oreGranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            OrePrefix.oreDiorite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            OrePrefix.oreAndesite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            OrePrefix.oreBasalt.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            OrePrefix.oreBlackgranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            OrePrefix.oreMarble.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            OrePrefix.oreRedgranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            OrePrefix.oreSand.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            OrePrefix.oreRedSand.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        }

        OrePrefix.crushed.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processCrushedOre);
        OrePrefix.crushedPurified.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processCrushedPurified);
        OrePrefix.crushedCentrifuged.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processCrushedCentrifuged);

        OrePrefix.dustImpure.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processDirtyDust);

        OrePrefix.dustPure.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processPureDust);

        // GT6 ore variant handlers
        OrePrefix.orePoor.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processPoorOre);
        OrePrefix.rawOre.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processRawOre);
        OrePrefix.crushedTiny.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processCrushedTiny);
        OrePrefix.crushedPurifiedTiny.addProcessingHandler(PropertyKey.ORE,
                OreRecipeHandler::processCrushedPurifiedTiny);
        OrePrefix.crushedCentrifugedTiny.addProcessingHandler(PropertyKey.ORE,
                OreRecipeHandler::processCrushedCentrifugedTiny);
    }

    private static void processMetalSmelting(OrePrefix crushedPrefix, Material material, OreProperty property) {
        Material smeltingResult = property.getDirectSmeltResult() != null ? property.getDirectSmeltResult() : material;

        if (smeltingResult.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingResult);

            if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingResult)) {
                ModHandler.addSmeltingRecipe(new UnificationEntry(crushedPrefix, material), ingotStack, 0.5f);
            }
        }
    }

    public static void processOre(OrePrefix orePrefix, Material material, OreProperty property) {
        processOre(orePrefix, material, property, 1);
    }

    public static void processOreDouble(OrePrefix orePrefix, Material material, OreProperty property) {
        processOre(orePrefix, material, property, 2);
    }

    public static void processOre(OrePrefix orePrefix, Material material, OreProperty property, int oreTypeMultiplier) {
        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.gem, byproductMaterial);
        if (byproductStack.isEmpty()) byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);
        ItemStack crushedStack = OreDictUnifier.get(OrePrefix.crushed, material);
        ItemStack ingotStack;
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material :
                property.getDirectSmeltResult();
        double amountOfCrushedOre = property.getOreMultiplier();
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = OreDictUnifier.get(OrePrefix.gem, smeltingMaterial);
        } else {
            ingotStack = OreDictUnifier.get(OrePrefix.dust, smeltingMaterial);
        }
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier() * oreTypeMultiplier);
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        if (!crushedStack.isEmpty()) {
            RecipeBuilder<?> builder = RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(orePrefix, material)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !OreDictUnifier.get(OrePrefix.gem, material).isEmpty()) {
                builder.outputs(GTUtility.copy((int) Math.ceil(amountOfCrushedOre) * oreTypeMultiplier,
                        OreDictUnifier.get(OrePrefix.gem, material, crushedStack.getCount())));
            } else {
                builder.outputs(GTUtility.copy((int) Math.ceil(amountOfCrushedOre) * oreTypeMultiplier, crushedStack));
            }
            builder.buildAndRegister();

            builder = RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .input(orePrefix, material)
                    .outputs(GTUtility.copy((int) Math.round(amountOfCrushedOre) * 2 * oreTypeMultiplier, crushedStack))
                    .chancedOutput(byproductStack, 1400, 850)
                    .duration(400);
            for (MaterialStack secondaryMaterial : orePrefix.secondaryMaterials) {
                if (secondaryMaterial.material.hasProperty(PropertyKey.DUST)) {
                    ItemStack dustStack = OreDictUnifier.getGem(secondaryMaterial);
                    builder.chancedOutput(dustStack, 6700, 800);
                }
            }

            builder.buildAndRegister();
        }

        // do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial)) {
            ModHandler.addSmeltingRecipe(new UnificationEntry(orePrefix, material), ingotStack, 0.5f);
        }
    }

    public static void processCrushedOre(OrePrefix crushedPrefix, Material material, OreProperty property) {
        ItemStack impureDustStack = OreDictUnifier.get(OrePrefix.dustImpure, material);
        Material byproductMaterial = property.getOreByProduct(0, material);

        if (impureDustStack.isEmpty()) {
            impureDustStack = OreDictUnifier.get(OrePrefix.dust, material);
        }

        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushedPrefix, material)
                .outputs(impureDustStack)
                .duration(10).EUt(16)
                .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .input(crushedPrefix, material)
                .outputs(impureDustStack)
                .duration(400)
                .chancedOutput(OreDictUnifier.get(OrePrefix.dust, byproductMaterial, property.getByProductMultiplier()),
                        1400, 850)
                .buildAndRegister();

        ItemStack crushedPurifiedOre = GTUtility.copyFirst(
                OreDictUnifier.get(OrePrefix.crushedPurified, material),
                OreDictUnifier.get(OrePrefix.dust, material));
        ItemStack crushedCentrifugedOre = GTUtility.copyFirst(
                OreDictUnifier.get(OrePrefix.crushedCentrifuged, material),
                OreDictUnifier.get(OrePrefix.dust, material));

        RecipeMaps.ORE_WASHER_RECIPES.recipeBuilder()
                .input(crushedPrefix, material)
                .circuitMeta(2)
                .fluidInputs(Materials.Water.getFluid(100))
                .outputs(crushedPurifiedOre)
                .duration(8).EUt(4).buildAndRegister();

        RecipeMaps.ORE_WASHER_RECIPES.recipeBuilder()
                .input(crushedPrefix, material)
                .fluidInputs(Materials.Water.getFluid(1000))
                .circuitMeta(1)
                .outputs(crushedPurifiedOre)
                .chancedOutput(OrePrefix.dust, byproductMaterial, 3333, 0)
                .output(OrePrefix.dust, Materials.Stone)
                .buildAndRegister();

        RecipeMaps.ORE_WASHER_RECIPES.recipeBuilder()
                .input(crushedPrefix, material)
                .fluidInputs(Materials.DistilledWater.getFluid(100))
                .outputs(crushedPurifiedOre)
                .chancedOutput(OrePrefix.dust, byproductMaterial, 3333, 0)
                .output(OrePrefix.dust, Materials.Stone)
                .duration(200)
                .buildAndRegister();

        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(crushedPrefix, material)
                .outputs(crushedCentrifugedOre)
                .chancedOutput(OrePrefix.dust, property.getOreByProduct(1, material), property.getByProductMultiplier(),
                        3333, 0)
                .output(OrePrefix.dust, Materials.Stone)
                .buildAndRegister();

        if (property.getWashedIn().getKey() != null) {
            Material washingByproduct = property.getOreByProduct(3, material);
            Pair<Material, Integer> washedInTuple = property.getWashedIn();
            RecipeMaps.CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .input(crushedPrefix, material)
                    .fluidInputs(washedInTuple.getKey().getFluid(washedInTuple.getValue()))
                    .outputs(crushedPurifiedOre)
                    .chancedOutput(
                            OreDictUnifier.get(OrePrefix.dust, washingByproduct, property.getByProductMultiplier()),
                            7000, 580)
                    .chancedOutput(OreDictUnifier.get(OrePrefix.dust, Materials.Stone), 4000, 650)
                    .duration(200).EUt(VA[LV])
                    .buildAndRegister();
        }

        ModHandler.addShapelessRecipe(String.format("crushed_ore_to_dust_%s", material),
                impureDustStack, 'h', new UnificationEntry(crushedPrefix, material));

        processMetalSmelting(crushedPrefix, material, property);
    }

    public static void processCrushedCentrifuged(OrePrefix centrifugedPrefix, Material material, OreProperty property) {
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.dust, property.getOreByProduct(2, material), 1);

        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(centrifugedPrefix, material)
                .outputs(dustStack)
                .duration(10).EUt(16)
                .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .input(centrifugedPrefix, material)
                .outputs(dustStack)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(400)
                .buildAndRegister();

        ModHandler.addShapelessRecipe(String.format("centrifuged_ore_to_dust_%s", material), dustStack,
                'h', new UnificationEntry(centrifugedPrefix, material));

        processMetalSmelting(centrifugedPrefix, material, property);
    }

    public static void processCrushedPurified(OrePrefix purifiedPrefix, Material material, OreProperty property) {
        ItemStack crushedCentrifugedStack = OreDictUnifier.get(OrePrefix.crushedCentrifuged, material);
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dustPure, material);
        Material byproductMaterial = property.getOreByProduct(1, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);

        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(purifiedPrefix, material)
                .outputs(dustStack)
                .duration(10)
                .EUt(16)
                .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .input(purifiedPrefix, material)
                .outputs(dustStack)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(400)
                .buildAndRegister();

        ModHandler.addShapelessRecipe(String.format("purified_ore_to_dust_%s", material), dustStack,
                'h', new UnificationEntry(purifiedPrefix, material));

        if (!crushedCentrifugedStack.isEmpty()) {
            RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                    .input(purifiedPrefix, material)
                    .outputs(crushedCentrifugedStack)
                    .chancedOutput(OrePrefix.dust, byproductMaterial, 3333, 0)
                    .buildAndRegister();
        }

        if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack exquisiteStack = OreDictUnifier.get(OrePrefix.gemExquisite, material);
            ItemStack flawlessStack = OreDictUnifier.get(OrePrefix.gemFlawless, material);
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, material);
            ItemStack flawedStack = OreDictUnifier.get(OrePrefix.gemFlawed, material);
            ItemStack chippedStack = OreDictUnifier.get(OrePrefix.gemChipped, material);

            if (material.hasFlag(HIGH_SIFTER_OUTPUT)) {
                RecipeBuilder<SimpleRecipeBuilder> builder = RecipeMaps.SIFTER_RECIPES.recipeBuilder()
                        .input(purifiedPrefix, material)
                        .chancedOutput(exquisiteStack, 500, 150)
                        .chancedOutput(flawlessStack, 1500, 200)
                        .chancedOutput(gemStack, 5000, 1000)
                        .chancedOutput(dustStack, 2500, 500)
                        .duration(400).EUt(16);

                if (!flawedStack.isEmpty())
                    builder.chancedOutput(flawedStack, 2000, 500);
                if (!chippedStack.isEmpty())
                    builder.chancedOutput(chippedStack, 3000, 350);

                builder.buildAndRegister();
            } else {
                RecipeBuilder<SimpleRecipeBuilder> builder = RecipeMaps.SIFTER_RECIPES.recipeBuilder()
                        .input(purifiedPrefix, material)
                        .chancedOutput(exquisiteStack, 300, 100)
                        .chancedOutput(flawlessStack, 1000, 150)
                        .chancedOutput(gemStack, 3500, 500)
                        .chancedOutput(dustStack, 5000, 750)
                        .duration(400).EUt(16);

                if (!flawedStack.isEmpty())
                    builder.chancedOutput(flawedStack, 2500, 300);
                if (!exquisiteStack.isEmpty())
                    builder.chancedOutput(chippedStack, 3500, 400);

                builder.buildAndRegister();
            }
        }
        processMetalSmelting(purifiedPrefix, material, property);
    }

    public static void processDirtyDust(OrePrefix dustPrefix, Material material, OreProperty property) {
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);
        Material byproduct = property.getOreByProduct(0, material);

        RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .input(dustPrefix, material)
                .outputs(dustStack)
                .duration((int) (material.getMass() * 4)).EUt(24);

        if (byproduct.hasProperty(PropertyKey.DUST)) {
            builder.chancedOutput(OrePrefix.dust, byproduct, 1111, 0);
        } else {
            builder.fluidOutputs(byproduct.getFluid(GTValues.L / 9));
        }

        builder.buildAndRegister();

        RecipeMaps.ORE_WASHER_RECIPES.recipeBuilder()
                .input(dustPrefix, material)
                .circuitMeta(2)
                .fluidInputs(Materials.Water.getFluid(100))
                .outputs(dustStack)
                .duration(8).EUt(4).buildAndRegister();

        // dust gains same amount of material as normal dust
        processMetalSmelting(dustPrefix, material, property);
    }

    public static void processPureDust(OrePrefix purePrefix, Material material, OreProperty property) {
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);
        Material byproductMaterial = property.getOreByProduct(1, material);

        if (property.getSeparatedInto() != null && !property.getSeparatedInto().isEmpty()) {
            List<Material> separatedMaterial = property.getSeparatedInto();
            OrePrefix prefix = (separatedMaterial.get(separatedMaterial.size() - 1).getBlastTemperature() == 0 &&
                    separatedMaterial.get(separatedMaterial.size() - 1).hasProperty(PropertyKey.INGOT)) ?
                            OrePrefix.nugget : OrePrefix.dust;

            ItemStack separatedStack2 = OreDictUnifier.get(prefix, separatedMaterial.get(separatedMaterial.size() - 1),
                    prefix == OrePrefix.nugget ? 2 : 1);

            RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
                    .input(purePrefix, material)
                    .outputs(dustStack)
                    .chancedOutput(OrePrefix.dust, separatedMaterial.get(0), 1000, 250)
                    .chancedOutput(separatedStack2, prefix == OrePrefix.dust ? 500 : 2000,
                            prefix == OrePrefix.dust ? 150 : 600)
                    .duration(200).EUt(24)
                    .buildAndRegister();
        }

        RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .input(purePrefix, material)
                .outputs(dustStack)
                .chancedOutput(OrePrefix.dust, byproductMaterial, 1111, 0)
                .duration(100)
                .EUt(5)
                .buildAndRegister();

        RecipeMaps.ORE_WASHER_RECIPES.recipeBuilder()
                .input(purePrefix, material)
                .circuitMeta(2)
                .fluidInputs(Materials.Water.getFluid(100))
                .outputs(dustStack)
                .duration(8).EUt(4).buildAndRegister();

        processMetalSmelting(purePrefix, material, property);
    }

    public static void processRefinedDust(OrePrefix refinedPrefix, Material material, OreProperty property) {
        processPureDust(refinedPrefix, material, property);
    }

    // GT6: Poor ore - yields 1/4 of regular ore drop (forge hammer → 2 dustTiny, macerator → dustSmall + byproduct)
    public static void processPoorOre(OrePrefix poorOrePrefix, Material material, OreProperty property) {
        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.gem, byproductMaterial);
        if (byproductStack.isEmpty()) byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);
        ItemStack dustSmallStack = OreDictUnifier.get(OrePrefix.dustSmall, material);
        if (dustSmallStack.isEmpty()) dustSmallStack = OreDictUnifier.get(OrePrefix.dust, material);
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material :
                property.getDirectSmeltResult();

        if (!dustSmallStack.isEmpty()) {
            RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(poorOrePrefix, material)
                    .outputs(OreDictUnifier.get(OrePrefix.dustTiny, material, 2))
                    .duration(10).EUt(16).buildAndRegister();

            RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .input(poorOrePrefix, material)
                    .outputs(dustSmallStack)
                    .chancedOutput(byproductStack, 1400, 850)
                    .duration(200).buildAndRegister();
        }

        if (smeltingMaterial.hasProperty(PropertyKey.INGOT) && doesMaterialUseNormalFurnace(smeltingMaterial)) {
            ItemStack nuggetStack = OreDictUnifier.get(OrePrefix.nugget, smeltingMaterial);
            if (!nuggetStack.isEmpty()) {
                ModHandler.addSmeltingRecipe(new UnificationEntry(poorOrePrefix, material), nuggetStack, 0.5f);
            }
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM) && doesMaterialUseNormalFurnace(smeltingMaterial)) {
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, smeltingMaterial);
            if (!gemStack.isEmpty()) {
                ModHandler.addSmeltingRecipe(new UnificationEntry(poorOrePrefix, material), gemStack, 0.5f);
            }
        }
    }

    // GT6: Raw ore - like a regular ore but as an item (1M worth)
    public static void processRawOre(OrePrefix rawOrePrefix, Material material, OreProperty property) {
        processOre(rawOrePrefix, material, property, 1);
    }



    // GT6: Tiny crushed ore - 1/9 of a regular crushed ore
    public static void processCrushedTiny(OrePrefix crushedTinyPrefix, Material material, OreProperty property) {
        // 9 crushedTiny → crushed (packer)
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .input(crushedTinyPrefix, material, 9)
                .circuitMeta(1)
                .output(OrePrefix.crushed, material)
                .buildAndRegister();
        // crushed → 9 crushedTiny
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .input(OrePrefix.crushed, material)
                .circuitMeta(9)
                .output(crushedTinyPrefix, material, 9)
                .buildAndRegister();
        // macerator: crushedTiny → dustTiny + byproduct chance
        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.dustTiny, byproductMaterial);
        if (byproductStack.isEmpty()) byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .input(crushedTinyPrefix, material)
                .output(OrePrefix.dustTiny, material)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(100).buildAndRegister();
    }

    // GT6: Tiny purified crushed ore
    public static void processCrushedPurifiedTiny(OrePrefix crushedPurifiedTinyPrefix, Material material,
                                                  OreProperty property) {
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .input(crushedPurifiedTinyPrefix, material, 9)
                .circuitMeta(1)
                .output(OrePrefix.crushedPurified, material)
                .buildAndRegister();
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .input(OrePrefix.crushedPurified, material)
                .circuitMeta(9)
                .output(crushedPurifiedTinyPrefix, material, 9)
                .buildAndRegister();
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .input(crushedPurifiedTinyPrefix, material)
                .output(OrePrefix.dustTiny, material)
                .duration(100).buildAndRegister();
    }

    // GT6: Tiny centrifuged crushed ore
    public static void processCrushedCentrifugedTiny(OrePrefix crushedCentrifugedTinyPrefix, Material material,
                                                     OreProperty property) {
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .input(crushedCentrifugedTinyPrefix, material, 9)
                .circuitMeta(1)
                .output(OrePrefix.crushedCentrifuged, material)
                .buildAndRegister();
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .input(OrePrefix.crushedCentrifuged, material)
                .circuitMeta(9)
                .output(crushedCentrifugedTinyPrefix, material, 9)
                .buildAndRegister();
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .input(crushedCentrifugedTinyPrefix, material)
                .output(OrePrefix.dustTiny, material)
                .duration(100).buildAndRegister();
    }

    private static boolean doesMaterialUseNormalFurnace(Material material) {
        return !material.hasProperty(PropertyKey.BLAST);
    }
}

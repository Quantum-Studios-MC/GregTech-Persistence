package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockLargeMultiblockCasing.CasingType;
import gregtech.common.blocks.BlockUniqueCasing.UniqueCasingType;
import gregtech.common.blocks.GCYMMetaBlocks;
import gregtech.common.items.MetaItems;

import static gregtech.api.unification.material.Materials.*;

public final class GCYMCasingLoader {

    private GCYMCasingLoader() {}

    public static void init() {
        final int numCasings = ConfigHolder.recipes.casingsPerCraft;

        // ===== Large Multiblock Casings (Shaped) =====
        ModHandler.addShapedRecipe(true, "casing_large_macerator",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.MACERATOR_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, Zeron100),
                'F', new UnificationEntry(OrePrefix.frameGt, Titanium));

        ModHandler.addShapedRecipe(true, "casing_high_temperature",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.HIGH_TEMPERATURE_CASING, numCasings),
                "DhD", "PFP", "DwD",
                'P', new UnificationEntry(OrePrefix.plate, TitaniumCarbide),
                'D', new UnificationEntry(OrePrefix.plate, HSLASteel),
                'F', new UnificationEntry(OrePrefix.frameGt, TungstenCarbide));

        ModHandler.addShapedRecipe(true, "casing_large_assembler",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.ASSEMBLING_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, Stellite100),
                'F', new UnificationEntry(OrePrefix.frameGt, Tungsten));

        ModHandler.addShapedRecipe(true, "casing_stress_proof",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.STRESS_PROOF_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, MaragingSteel300),
                'F', new UnificationEntry(OrePrefix.frameGt, StainlessSteel));

        ModHandler.addShapedRecipe(true, "casing_corrosion_proof",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.CORROSION_PROOF_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, CobaltBrass),
                'F', new UnificationEntry(OrePrefix.frameGt, HSLASteel));

        ModHandler.addShapedRecipe(true, "casing_vibration_safe",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.VIBRATION_SAFE_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, IncoloyMA956),
                'F', new UnificationEntry(OrePrefix.frameGt, IncoloyMA956));

        ModHandler.addShapedRecipe(true, "casing_watertight",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.WATERTIGHT_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, WatertightSteel),
                'F', new UnificationEntry(OrePrefix.frameGt, WatertightSteel));

        ModHandler.addShapedRecipe(true, "casing_large_cutter",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.CUTTER_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, HastelloyC276),
                'F', new UnificationEntry(OrePrefix.frameGt, HastelloyC276));

        ModHandler.addShapedRecipe(true, "casing_nonconducting",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.NONCONDUCTING_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, HSLASteel),
                'F', new UnificationEntry(OrePrefix.frameGt, HSLASteel));

        ModHandler.addShapedRecipe(true, "casing_large_mixer",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.MIXER_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, HastelloyX),
                'F', new UnificationEntry(OrePrefix.frameGt, MaragingSteel300));

        ModHandler.addShapedRecipe(true, "casing_large_engraver",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.ENGRAVER_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, TitaniumTungstenCarbide),
                'F', new UnificationEntry(OrePrefix.frameGt, Titanium));

        ModHandler.addShapedRecipe(true, "casing_atomic",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.ATOMIC_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plateDouble, Trinaquadalloy),
                'F', new UnificationEntry(OrePrefix.frameGt, NaquadahAlloy));

        ModHandler.addShapedRecipe(true, "casing_steam",
                GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.STEAM_CASING, numCasings),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, Brass),
                'F', new UnificationEntry(OrePrefix.frameGt, Brass));

        // ===== Large Multiblock Casings (Assembler) =====
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Zeron100, 6)
                .input(OrePrefix.frameGt, Titanium)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.MACERATOR_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, HSLASteel, 4)
                .input(OrePrefix.plate, TitaniumCarbide, 2)
                .input(OrePrefix.frameGt, TungstenCarbide)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.HIGH_TEMPERATURE_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Stellite100, 6)
                .input(OrePrefix.frameGt, Tungsten)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.ASSEMBLING_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, MaragingSteel300, 6)
                .input(OrePrefix.frameGt, StainlessSteel)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.STRESS_PROOF_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, CobaltBrass, 6)
                .input(OrePrefix.frameGt, HSLASteel)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.CORROSION_PROOF_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, IncoloyMA956, 6)
                .input(OrePrefix.frameGt, IncoloyMA956)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.VIBRATION_SAFE_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, WatertightSteel, 6)
                .input(OrePrefix.frameGt, WatertightSteel)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.WATERTIGHT_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, HastelloyC276, 6)
                .input(OrePrefix.frameGt, HastelloyC276)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.CUTTER_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, HSLASteel, 6)
                .input(OrePrefix.frameGt, HSLASteel)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.NONCONDUCTING_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, HastelloyX, 6)
                .input(OrePrefix.frameGt, MaragingSteel300)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.MIXER_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, TitaniumTungstenCarbide, 6)
                .input(OrePrefix.frameGt, Titanium)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.ENGRAVER_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plateDouble, Trinaquadalloy, 6)
                .input(OrePrefix.frameGt, NaquadahAlloy)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.ATOMIC_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Brass, 6)
                .input(OrePrefix.frameGt, Brass)
                .notConsumable(new IntCircuitIngredient(6))
                .outputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.STEAM_CASING, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        // ===== Unique Casings (Shaped) =====
        ModHandler.addShapedRecipe(true, "casing_crushing_wheels",
                GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.CRUSHING_WHEELS, numCasings),
                "SSS", "GCG", "GMG",
                'S', new UnificationEntry(OrePrefix.gearSmall, TungstenCarbide),
                'G', new UnificationEntry(OrePrefix.gear, Ultimet),
                'C', GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.MACERATOR_CASING),
                'M', MetaItems.ELECTRIC_MOTOR_IV.getStackForm());

        ModHandler.addShapedRecipe(true, "casing_slicing_blades",
                GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.SLICING_BLADES, numCasings),
                "SSS", "GCG", "GMG",
                'S', new UnificationEntry(OrePrefix.plate, TungstenCarbide),
                'G', new UnificationEntry(OrePrefix.gear, Ultimet),
                'C', GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.CUTTER_CASING),
                'M', MetaItems.ELECTRIC_MOTOR_IV.getStackForm());

        ModHandler.addShapedRecipe(true, "casing_electrolytic_cell",
                GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.ELECTROLYTIC_CELL, numCasings),
                "WWW", "WCW", "KAK",
                'W', new UnificationEntry(OrePrefix.wireGtDouble, Platinum),
                'C', GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.NONCONDUCTING_CASING),
                'K', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.IV),
                'A', new UnificationEntry(OrePrefix.cableGtSingle, Tungsten));

        ModHandler.addShapedRecipe(true, "casing_heat_vent",
                GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.HEAT_VENT, numCasings),
                "PDP", "RLR", "PDP",
                'P', new UnificationEntry(OrePrefix.plate, TantalumCarbide),
                'D', new UnificationEntry(OrePrefix.plateDouble, MolybdenumDisilicide),
                'R', new UnificationEntry(OrePrefix.rotor, Titanium),
                'L', new UnificationEntry(OrePrefix.stickLong, MolybdenumDisilicide));

        // ===== Unique Casings (Assembler) =====
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.gearSmall, TungstenCarbide, 3)
                .input(OrePrefix.gear, Ultimet, 4)
                .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                .inputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.MACERATOR_CASING))
                .outputs(GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.CRUSHING_WHEELS, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, TungstenCarbide, 3)
                .input(OrePrefix.gear, Ultimet, 4)
                .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                .inputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.CUTTER_CASING))
                .outputs(GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.SLICING_BLADES, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.wireGtDouble, Platinum, 5)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.IV, 2)
                .input(OrePrefix.cableGtSingle, Tungsten)
                .inputs(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                        .getItemVariant(CasingType.NONCONDUCTING_CASING))
                .outputs(GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.ELECTROLYTIC_CELL, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, TantalumCarbide, 4)
                .input(OrePrefix.rotor, Titanium, 2)
                .input(OrePrefix.plateDouble, MolybdenumDisilicide, 2)
                .input(OrePrefix.stickLong, MolybdenumDisilicide)
                .outputs(GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.HEAT_VENT, numCasings))
                .duration(50).EUt(16).buildAndRegister();

        // Molybdenum Disilicide Coil
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.ring, MolybdenumDisilicide, 32)
                .input(OrePrefix.foil, Graphene, 16)
                .fluidInputs(HSLASteel.getFluid(GTValues.L))
                .outputs(GCYMMetaBlocks.UNIQUE_CASING
                        .getItemVariant(UniqueCasingType.MOLYBDENUM_DISILICIDE_COIL))
                .duration(500).EUt(GTValues.VA[GTValues.EV]).buildAndRegister();
    }
}

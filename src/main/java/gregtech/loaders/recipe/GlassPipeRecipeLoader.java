package gregtech.loaders.recipe;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.glasspipe.BlockGlassPipe;
import gregtech.common.pipelike.glasspipe.GlassPipeType;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class GlassPipeRecipeLoader {

    public static void init() {
        // Basic Glass Pipes - made from Glass
        registerTierRecipes(0, new ItemStack(Blocks.GLASS), Glass, VA[ULV]);

        // Tempered Glass Pipes - made from Glass + Aluminium
        registerTierRecipes(1, new ItemStack(Blocks.GLASS), Aluminium, VA[LV]);

        // Borosilicate Glass Pipes - made from BorosilicateGlass ingots
        registerTierRecipes(2, null, BorosilicateGlass, VA[MV]);

        // Laminated Glass Pipes - made from BorosilicateGlass + Epoxy
        registerLaminatedRecipes();

        // Fusion Glass Pipes - made from BorosilicateGlass + TungstenSteel
        registerFusionRecipes();
    }

    private static void registerTierRecipes(int tierIndex, ItemStack glassInput,
                                            gregtech.api.unification.material.Material material, int voltage) {
        for (int sizeIdx = 0; sizeIdx < GlassPipeType.SIZE_COUNT; sizeIdx++) {
            GlassPipeType type = GlassPipeType.getByTierAndSize(tierIndex, sizeIdx);
            ItemStack pipeStack = getPipeStack(type);
            if (pipeStack.isEmpty()) continue;

            int circuitMeta = getCircuitForSize(sizeIdx);
            int outputCount = getOutputCount(sizeIdx);
            int inputCount = getInputCount(sizeIdx);

            if (tierIndex <= 1 && glassInput != null) {
                // Basic and Tempered: use glass blocks as primary input
                var builder = ASSEMBLER_RECIPES.recipeBuilder()
                        .inputs(new ItemStack(glassInput.getItem(), inputCount, glassInput.getMetadata()))
                        .circuitMeta(circuitMeta)
                        .outputs(new ItemStack(pipeStack.getItem(), outputCount, pipeStack.getMetadata()))
                        .duration(getAssemblerDuration(sizeIdx))
                        .EUt(voltage);

                if (tierIndex == 1) {
                    // Tempered requires aluminium dust
                    builder.input(dust, Aluminium, Math.max(1, inputCount / 2));
                }

                builder.buildAndRegister();
            } else if (tierIndex == 2) {
                // Borosilicate: use BorosilicateGlass ingots
                ASSEMBLER_RECIPES.recipeBuilder()
                        .input(ingot, BorosilicateGlass, inputCount)
                        .circuitMeta(circuitMeta)
                        .outputs(new ItemStack(pipeStack.getItem(), outputCount, pipeStack.getMetadata()))
                        .duration(getAssemblerDuration(sizeIdx))
                        .EUt(voltage)
                        .buildAndRegister();
            }
        }
    }

    private static void registerLaminatedRecipes() {
        for (int sizeIdx = 0; sizeIdx < GlassPipeType.SIZE_COUNT; sizeIdx++) {
            GlassPipeType type = GlassPipeType.getByTierAndSize(3, sizeIdx);
            GlassPipeType boroType = GlassPipeType.getByTierAndSize(2, sizeIdx);
            ItemStack pipeStack = getPipeStack(type);
            ItemStack boroStack = getPipeStack(boroType);
            if (pipeStack.isEmpty() || boroStack.isEmpty()) continue;

            int inputCount = getInputCount(sizeIdx);

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(boroStack.getItem(), 1, boroStack.getMetadata()))
                    .input(dust, Epoxy, Math.max(1, inputCount / 2))
                    .fluidInputs(Polyethylene.getFluid(L * Math.max(1, inputCount / 2)))
                    .outputs(new ItemStack(pipeStack.getItem(), 1, pipeStack.getMetadata()))
                    .duration(getAssemblerDuration(sizeIdx))
                    .EUt(VA[HV])
                    .buildAndRegister();
        }
    }

    private static void registerFusionRecipes() {
        for (int sizeIdx = 0; sizeIdx < GlassPipeType.SIZE_COUNT; sizeIdx++) {
            GlassPipeType type = GlassPipeType.getByTierAndSize(4, sizeIdx);
            GlassPipeType lamType = GlassPipeType.getByTierAndSize(3, sizeIdx);
            ItemStack pipeStack = getPipeStack(type);
            ItemStack lamStack = getPipeStack(lamType);
            if (pipeStack.isEmpty() || lamStack.isEmpty()) continue;

            int inputCount = getInputCount(sizeIdx);

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(lamStack.getItem(), 1, lamStack.getMetadata()))
                    .input(ingot, TungstenSteel, Math.max(1, inputCount / 2))
                    .fluidInputs(Naquadah.getFluid(L * Math.max(1, inputCount)))
                    .outputs(new ItemStack(pipeStack.getItem(), 1, pipeStack.getMetadata()))
                    .duration(getAssemblerDuration(sizeIdx) * 2)
                    .EUt(VA[EV])
                    .buildAndRegister();
        }
    }

    private static ItemStack getPipeStack(GlassPipeType type) {
        BlockGlassPipe block = MetaBlocks.GLASS_PIPES[type.ordinal()];
        if (block == null) return ItemStack.EMPTY;
        return new ItemStack(block);
    }

    private static int getCircuitForSize(int sizeIdx) {
        return switch (sizeIdx) {
            case 0 -> 18; // Tiny
            case 1 -> 12; // Small
            case 2 -> 6;  // Normal
            case 3 -> 3;  // Large
            case 4 -> 1;  // Huge
            default -> 6;
        };
    }

    private static int getOutputCount(int sizeIdx) {
        return switch (sizeIdx) {
            case 0 -> 2;  // Tiny: 2 per craft
            case 1 -> 1;  // Small: 1
            case 2 -> 1;  // Normal: 1
            case 3 -> 1;  // Large: 1
            case 4 -> 1;  // Huge: 1
            default -> 1;
        };
    }

    private static int getInputCount(int sizeIdx) {
        return switch (sizeIdx) {
            case 0 -> 1;  // Tiny: 1 input
            case 1 -> 1;  // Small: 1 input
            case 2 -> 3;  // Normal: 3 inputs
            case 3 -> 6;  // Large: 6 inputs
            case 4 -> 12; // Huge: 12 inputs
            default -> 1;
        };
    }

    private static int getAssemblerDuration(int sizeIdx) {
        return switch (sizeIdx) {
            case 0 -> 40;
            case 1 -> 60;
            case 2 -> 100;
            case 3 -> 200;
            case 4 -> 400;
            default -> 100;
        };
    }
}

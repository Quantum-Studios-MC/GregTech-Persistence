package gregtech.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;

public final class GCYMRecipeMaps {

    public static final RecipeMap<BlastRecipeBuilder> ALLOY_BLAST_RECIPES;

    static {
        ALLOY_BLAST_RECIPES = new RecipeMapBuilder<>(
                "alloy_blast_smelter",
                new BlastRecipeBuilder())
                        .itemInputs(9)
                        .itemOutputs(0)
                        .fluidInputs(3)
                        .fluidOutputs(1)
                        .itemSlotOverlay(GuiTextures.FURNACE_OVERLAY_1, false, false)
                        .itemSlotOverlay(GuiTextures.FURNACE_OVERLAY_1, false, true)
                        .fluidSlotOverlay(GuiTextures.FURNACE_OVERLAY_2, false, false)
                        .fluidSlotOverlay(GuiTextures.FURNACE_OVERLAY_2, false, true)
                        .fluidSlotOverlay(GuiTextures.FURNACE_OVERLAY_2, true, false)
                        .fluidSlotOverlay(GuiTextures.FURNACE_OVERLAY_2, true, true)
                        .sound(GTSoundEvents.FURNACE)
                        .build();
    }

    private GCYMRecipeMaps() {}
}

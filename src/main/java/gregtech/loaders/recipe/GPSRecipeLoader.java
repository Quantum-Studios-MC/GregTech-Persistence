package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.item.ItemStack;

public class GPSRecipeLoader {

    public static void init() {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.LV)
                .input(OrePrefix.plate, Materials.Aluminium, 2)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputs(MetaItems.BATTERY_LV_LITHIUM.getStackForm())
                .inputs(MetaItems.SENSOR_LV.getStackForm())
                .outputs(MetaItems.GPS_DEVICE.getStackForm())
                .duration(200)
                .EUt(30)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.LV)
                .input(OrePrefix.plate, Materials.Steel, 4)
                .inputs(MetaItems.EMITTER_LV.getStackForm())
                .inputs(MetaTileEntities.HULL[GTValues.LV].getStackForm())
                .outputs(new ItemStack(MetaBlocks.SIGNAL_BEACON))
                .duration(100)
                .EUt(30)
                .buildAndRegister();
    }
}

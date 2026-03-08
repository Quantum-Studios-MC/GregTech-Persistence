package gregtech.integration.jei.basic;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.integration.jei.utils.render.DrawableRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialTreeCategory extends BasicRecipeCategory<MaterialTree, MaterialTree> {

    public static final String UID = String.format("%s.material_tree", GTValues.MODID);

    protected String materialName;
    protected String materialFormula;
    protected int materialBFTemp;
    protected String materialAvgM;
    protected String materialAvgP;
    protected String materialAvgN;

    protected final IDrawable slot;
    protected final IDrawable icon;

    protected List<Boolean> itemExists = new ArrayList<>();
    protected List<Boolean> fluidExists = new ArrayList<>();
    protected Map<OrePrefix, Boolean> prefixExistsMap = new HashMap<>();

    public MaterialTreeCategory(IGuiHelper guiHelper) {
        super("material_tree",
                "recipemap.materialtree.name",
                guiHelper.createBlankDrawable(176, MaterialTreeRegistry.getRequiredHeight()),
                guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18)
                .build();
        this.icon = guiHelper.createDrawableIngredient(OreDictUnifier.get(OrePrefix.ingot, Materials.Aluminium));

        /*
         * couldn't think of a better way to register all these
         * generated with bash, requires imagemagick and sed
         * for file in ./*.png; do
         * dimstring=$(identify -ping -format '%w, %h' "$file")
         * basename "$file" .png | sed "s/\(.*\)/registerArrow(guiHelper, \"\1\", $dimstring);/"
         * done
         */
        registerArrow(guiHelper, "2d12", 5, 12);
        registerArrow(guiHelper, "2d16", 5, 16);
        registerArrow(guiHelper, "2r16d37", 18, 40);
        registerArrow(guiHelper, "d14", 5, 14);
        registerArrow(guiHelper, "d7r25u6", 28, 7);
        registerArrow(guiHelper, "d7r50d7", 53, 14);
        registerArrow(guiHelper, "d7r50u6", 53, 7);
        registerArrow(guiHelper, "d7r75d7", 78, 14);
        registerArrow(guiHelper, "d7r75u6", 78, 7);
        registerArrow(guiHelper, "d7r87u22r4", 92, 25);
        registerArrow(guiHelper, "d7r87u46r4", 92, 49);
        registerArrow(guiHelper, "l7", 7, 5);
        registerArrow(guiHelper, "r3d16r4", 7, 19);
        registerArrow(guiHelper, "r3d26r4", 7, 29);
        registerArrow(guiHelper, "r3u15r4", 7, 18);
        registerArrow(guiHelper, "r3u32r4", 7, 35);
        registerArrow(guiHelper, "r3u57r4", 7, 60);
        registerArrow(guiHelper, "r7", 7, 5);
        registerArrow(guiHelper, "u12", 5, 12);
        registerArrow(guiHelper, "u7r25d6", 28, 7);
        registerArrow(guiHelper, "u7r50d6", 53, 7);
        registerArrow(guiHelper, "u7r50u5", 53, 12);
        registerArrow(guiHelper, "u7r75d6", 78, 7);
        registerArrow(guiHelper, "u7r75u5", 78, 12);
        registerArrow(guiHelper, "u7r87d15r4", 92, 18);
        registerArrow(guiHelper, "u7r87u8r4", 92, 17);
        registerArrow(guiHelper, "r3u62r29", 32, 65);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, @NotNull MaterialTree recipeWrapper, IIngredients ingredients) {
        // place and check existence of items
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        List<List<ItemStack>> itemInputs = ingredients.getInputs(VanillaTypes.ITEM);
        List<OrePrefix> prefixes = MaterialTreeRegistry.getPrefixes();
        itemExists.clear();
        prefixExistsMap.clear();
        for (int i = 0; i < prefixes.size(); i++) {
            int[] pos = MaterialTreeRegistry.getPosition(prefixes.get(i));
            itemStackGroup.init(i, true, pos[0], pos[1]);
            boolean exists = i < itemInputs.size() && itemInputs.get(i).size() > 0;
            itemExists.add(exists);
            prefixExistsMap.put(prefixes.get(i), exists);
        }
        itemStackGroup.set(ingredients);

        // place and check existence of fluid(s)
        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
        List<List<FluidStack>> fluidInputs = ingredients.getInputs(VanillaTypes.FLUID);
        fluidExists.clear();
        // fluids annoyingly need to be offset by 1 to fit in the slot graphic
        fluidStackGroup.init(0, true, MaterialTreeRegistry.getFluidX() + 1, MaterialTreeRegistry.getFluidY() + 1);
        fluidExists.add(!fluidInputs.isEmpty() && fluidInputs.get(0).size() > 0);
        fluidStackGroup.set(ingredients);

        // set info of current material
        materialName = recipeWrapper.getMaterialName();
        materialFormula = recipeWrapper.getMaterialFormula();
        materialBFTemp = recipeWrapper.getBlastTemp();
        materialAvgM = I18n.format("gregtech.jei.materials.average_mass", recipeWrapper.getAvgM());
        materialAvgP = I18n.format("gregtech.jei.materials.average_protons", recipeWrapper.getAvgP());
        materialAvgN = I18n.format("gregtech.jei.materials.average_neutrons", recipeWrapper.getAvgN());
    }

    @NotNull
    @Override
    public IRecipeWrapper getRecipeWrapper(@NotNull MaterialTree recipe) {
        return recipe;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        // item slot rendering
        List<OrePrefix> prefixes = MaterialTreeRegistry.getPrefixes();
        for (int i = 0; i < prefixes.size(); i++) {
            if (i < itemExists.size() && itemExists.get(i)) {
                int[] pos = MaterialTreeRegistry.getPosition(prefixes.get(i));
                this.slot.draw(minecraft, pos[0], pos[1]);
            }
        }

        // fluid slot rendering
        if (!fluidExists.isEmpty() && fluidExists.get(0)) {
            this.slot.draw(minecraft, MaterialTreeRegistry.getFluidX(), MaterialTreeRegistry.getFluidY());
        }

        // arrow rendering from registry
        for (MaterialTreeRegistry.ArrowConnection arrow : MaterialTreeRegistry.getArrows()) {
            drawArrow(minecraft, arrow.arrowName, arrow.drawX, arrow.drawY, arrow.condition.test(prefixExistsMap));
        }

        // material info rendering
        int linesDrawn = 0;
        if (minecraft.fontRenderer.getStringWidth(materialName) > 176) {
            minecraft.fontRenderer.drawString(minecraft.fontRenderer.trimStringToWidth(materialName, 171) + "...",
                    0, 0, 0x111111);
            linesDrawn++;
        } else if (materialName.length() != 0) {
            minecraft.fontRenderer.drawString(materialName, 0, 0, 0x111111);
            linesDrawn++;
        }
        if (minecraft.fontRenderer.getStringWidth(materialFormula) > 176) {
            minecraft.fontRenderer.drawString(minecraft.fontRenderer.trimStringToWidth(materialFormula, 171) + "...",
                    0, FONT_HEIGHT * linesDrawn, 0x111111);
            linesDrawn++;
        } else if (materialFormula.length() != 0) {
            minecraft.fontRenderer.drawString(materialFormula, 0, FONT_HEIGHT * linesDrawn, 0x111111);
            linesDrawn++;
        }
        // don't think theres a good way to get the coil tier other than this
        if (materialBFTemp != 0) {
            TemperatureProperty.getInstance().drawInfo(minecraft, 0, FONT_HEIGHT * linesDrawn, 0x111111,
                    materialBFTemp);
            linesDrawn++;
        }
        minecraft.fontRenderer.drawString(materialAvgM, 0, FONT_HEIGHT * linesDrawn, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(materialAvgN, 0, FONT_HEIGHT * linesDrawn, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(materialAvgP, 0, FONT_HEIGHT * linesDrawn, 0x111111);
    }

    // a couple wrappers to make the code look less terrible
    private static void registerArrow(IGuiHelper guiHelper, String name, int width, int height) {
        DrawableRegistry.initDrawable(guiHelper, GTValues.MODID + ":textures/gui/arrows/" + name + ".png", width,
                height, name);
    }

    private static void drawArrow(Minecraft minecraft, String name, int x, int y, boolean shown) {
        if (shown) DrawableRegistry.drawDrawable(minecraft, name, x, y);
    }
}

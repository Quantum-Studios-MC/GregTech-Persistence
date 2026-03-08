package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IParallelMultiblock;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.GCYMMultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.common.metatileentities.multi.multiblockpart.GCYMRecipeMapMultiblockController;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GCYMMultiblockRecipeLogic extends MultiblockRecipeLogic {

    // Tiered casings are currently disabled; change to true to enable the feature.
    private static final boolean ENABLE_TIERED_CASINGS = false;

    public GCYMMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    public GCYMMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
        super(tileEntity, hasPerfectOC);
    }

    @Override
    public int getParallelLimit() {
        if (metaTileEntity instanceof IParallelMultiblock && ((IParallelMultiblock) metaTileEntity).isParallel())
            return ((IParallelMultiblock) metaTileEntity).getMaxParallel();

        return 1;
    }

    @Override
    public @NotNull RecipeMapMultiblockController getMetaTileEntity() {
        return (RecipeMapMultiblockController) super.getMetaTileEntity();
    }

    @Override
    public long getMaxVoltage() {
        if (!ENABLE_TIERED_CASINGS)
            return super.getMaxVoltage();

        if (getMetaTileEntity() instanceof GCYMRecipeMapMultiblockController controller && !controller.isTiered())
            return super.getMaxVoltage();

        List<ITieredMetaTileEntity> list = getMetaTileEntity().getAbilities(GCYMMultiblockAbility.TIERED_HATCH);

        if (list.isEmpty())
            return super.getMaxVoltage();

        return Math.min(GTValues.V[list.get(0).getTier()], super.getMaxVoltage());
    }
}

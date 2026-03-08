package gregtech.integration.groovy;

import gregtech.api.capability.IHeatingCoil;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.logic.old.OCParams;
import gregtech.api.recipes.logic.old.OCResult;
import gregtech.api.recipes.logic.old.OverclockingLogic;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.recipes.properties.impl.TemperatureProperty;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.recipes.logic.old.OverclockingLogic.heatingCoilOC;
import static gregtech.api.recipes.logic.old.OverclockingLogic.standardOC;

/**
 * Custom recipe logic for GroovyScript multiblocks.
 * Supports tier-based speed bonuses, EU discounts, and parallel counts,
 * as well as optional coil/heating logic.
 * <p>
 * The tier-based modifiers are computed from the {@link GroovyMultiblockController}'s
 * current tiered block data, which is set during {@code formStructure()}.
 */
public class GroovyMultiblockRecipeLogic extends MultiblockRecipeLogic {

    public GroovyMultiblockRecipeLogic(RecipeMapMultiblockController metaTileEntity) {
        super(metaTileEntity);
    }

    private GroovyMultiblockController getGroovyController() {
        return (GroovyMultiblockController) metaTileEntity;
    }

    @Override
    public int getParallelLimit() {
        int base = super.getParallelLimit();
        GroovyMultiblockController controller = getGroovyController();
        int tierParallel = controller.getTotalParallelBonus();
        return Math.max(1, base + tierParallel);
    }

    @Override
    protected void modifyOverclockPre(@NotNull OCParams ocParams, @NotNull RecipePropertyStorage storage) {
        super.modifyOverclockPre(ocParams, storage);

        GroovyMultiblockController controller = getGroovyController();

        // Apply coil EU/t discount if coils are enabled
        if (controller.usesCoils() && controller instanceof IHeatingCoil) {
            ocParams.setEut(OverclockingLogic.applyCoilEUtDiscount(ocParams.eut(),
                    ((IHeatingCoil) controller).getCurrentTemperature(),
                    storage.get(TemperatureProperty.getInstance(), 0)));
        }

        // Apply tier-based EU discount (multiplicative with coil discount)
        double euMultiplier = controller.getTotalEuMultiplier();
        if (euMultiplier != 1.0 && euMultiplier > 0.0) {
            ocParams.setEut(Math.max(1L, (long) (ocParams.eut() * euMultiplier)));
        }
    }

    @Override
    protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                        @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
        GroovyMultiblockController controller = getGroovyController();

        // Use heating coil OC if coils are enabled
        if (controller.usesCoils() && controller instanceof IHeatingCoil) {
            heatingCoilOC(ocParams, ocResult, maxVoltage,
                    ((IHeatingCoil) controller).getCurrentTemperature(),
                    propertyStorage.get(TemperatureProperty.getInstance(), 0));
        } else {
            // Standard overclocking
            standardOC(ocParams, ocResult, maxVoltage, getOverclockingDurationFactor(),
                    getOverclockingVoltageFactor());
        }

        // Apply tier-based speed bonus AFTER overclocking
        double durationMultiplier = controller.getTotalDurationMultiplier();
        if (durationMultiplier != 1.0 && durationMultiplier > 0.0) {
            ocResult.setDuration(Math.max(1, (int) (ocResult.duration() * durationMultiplier)));
        }
    }
}

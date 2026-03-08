package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.IParallelMultiblock;
import gregtech.api.capability.impl.GCYMMultiblockRecipeLogic;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.GCYMMultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiMapMultiblockController;
import gregtech.api.metatileentity.multiblock.ui.KeyManager;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.UISyncer;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class GCYMRecipeMapMultiblockController extends MultiMapMultiblockController
                                                        implements IParallelMultiblock {

    private static final boolean ENABLE_TIERED_CASINGS = false;

    public GCYMRecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        this(metaTileEntityId, new RecipeMap<?>[] { recipeMap });
    }

    public GCYMRecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?>[] recipeMaps) {
        super(metaTileEntityId, recipeMaps);
        this.recipeMapWorkable = new GCYMMultiblockRecipeLogic(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (isParallel())
            tooltip.add(I18n.format("gcym.tooltip.parallel_enabled"));
        if (ENABLE_TIERED_CASINGS && isTiered())
            tooltip.add(I18n.format("gcym.tooltip.tiered_hatch_enabled"));
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltage()))
                .addCustom(this::addTieredHatchInfo)
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgress(), recipeMapWorkable.getMaxProgress())
                .addRecipeOutputLine(recipeMapWorkable);
    }

    private void addTieredHatchInfo(KeyManager keyManager, UISyncer syncer) {
        if (isStructureFormed()) {
            List<ITieredMetaTileEntity> list = getAbilities(GCYMMultiblockAbility.TIERED_HATCH);
            if (ENABLE_TIERED_CASINGS && !list.isEmpty()) {
                long maxVoltage = Math.min(GTValues.V[list.get(0).getTier()],
                        Math.max(getEnergyContainer().getInputVoltage(),
                                getEnergyContainer().getOutputVoltage()));
                String voltageName = GTValues.VNF[list.get(0).getTier()];
                keyManager.add(KeyUtil.lang(TextFormatting.GRAY,
                        "gcym.multiblock.tiered_hatch.tooltip", maxVoltage, voltageName));
            }
        }
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public int getMaxParallel() {
        return this.getAbilities(GCYMMultiblockAbility.PARALLEL_HATCH).isEmpty() ? 1 :
                this.getAbilities(GCYMMultiblockAbility.PARALLEL_HATCH).get(0).getCurrentParallel();
    }

    public boolean isTiered() {
        return ENABLE_TIERED_CASINGS;
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn,
                                               boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkEnergyIn, checkMaintenance, checkItemIn,
                checkItemOut, checkFluidIn, checkFluidOut, checkMuffler);
        if (isParallel())
            predicate = predicate
                    .or(abilities(GCYMMultiblockAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1));
        return predicate;
    }

    public static @NotNull TraceabilityPredicate tieredCasing() {
        return new TraceabilityPredicate(abilities(GCYMMultiblockAbility.TIERED_HATCH)
                .setMinGlobalLimited(ENABLE_TIERED_CASINGS ? 1 : 0)
                .setMaxGlobalLimited(1));
    }
}

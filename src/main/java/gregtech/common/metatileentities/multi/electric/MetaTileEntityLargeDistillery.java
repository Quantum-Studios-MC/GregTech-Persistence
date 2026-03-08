package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.IDistillationTower;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.DistillationTowerLogicHandler;
import gregtech.api.capability.impl.GCYMMultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.GCYMMultiblockAbility;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.GCYMTextures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockLargeMultiblockCasing;
import gregtech.common.blocks.GCYMMetaBlocks;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.GCYMRecipeMapMultiblockController;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static gregtech.api.util.RelativeDirection.*;

/**
 * Large Distillery - a larger version of the Distillation Tower that also handles distillery recipes.
 * Uses layer-wise fluid output when running distillation recipes.
 */
public class MetaTileEntityLargeDistillery extends GCYMRecipeMapMultiblockController implements IDistillationTower {

    protected final DistillationTowerLogicHandler handler;

    public MetaTileEntityLargeDistillery(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new RecipeMap<?>[] { RecipeMaps.DISTILLATION_RECIPES, RecipeMaps.DISTILLERY_RECIPES });
        this.recipeMapWorkable = new LargeDistilleryRecipeLogic(this);
        this.handler = new DistillationTowerLogicHandler(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeDistillery(this.metaTileEntityId);
    }

    @Override
    protected Function<BlockPos, Integer> multiblockPartSorter() {
        return RelativeDirection.UP.getSorter(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (!usesAdvHatchLogic() || this.structurePattern == null) return;
        handler.determineLayerCount(this.structurePattern);
        handler.determineOrderedFluidOutputs();
    }

    protected boolean usesAdvHatchLogic() {
        return getCurrentRecipeMap() == RecipeMaps.DISTILLATION_RECIPES;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        if (usesAdvHatchLogic())
            this.handler.invalidate();
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(40);
        TraceabilityPredicate maintenancePredicate = this.hasMaintenanceMechanics() &&
                ConfigHolder.machines.enableMaintenance ?
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setMinGlobalLimited(1).setMaxGlobalLimited(1) :
                        casingPredicate;
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("#####", "#ZZZ#", "#ZCZ#", "#ZZZ#", "#####")
                .aisle("##X##", "#XAX#", "XAPAX", "#XAX#", "##X##").setRepeatable(1, 12)
                .aisle("#YSY#", "YAAAY", "YATAY", "YAAAY", "#YYY#")
                .aisle("#YYY#", "YYYYY", "YYYYY", "YYYYY", "#YYY#")
                .where('S', selfPredicate())
                .where('Y', casingPredicate.or(abilities(MultiblockAbility.IMPORT_ITEMS))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS))
                        .or(abilities(GCYMMultiblockAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(maintenancePredicate))
                .where('X', casingPredicate
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS)
                                .setMinLayerLimited(1)
                                .setMaxLayerLimited(1, 1)))
                .where('Z', casingPredicate)
                .where('P', states(getPipeState()))
                .where('C', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('T', tieredCasing().or(states(getPipeState())))
                .where('A', air())
                .where('#', any())
                .build();
    }

    @Override
    public boolean allowSameFluidFillForOutputs() {
        return !usesAdvHatchLogic();
    }

    private static IBlockState getCasingState() {
        return GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                .getState(BlockLargeMultiblockCasing.CasingType.WATERTIGHT_CASING);
    }

    private static IBlockState getPipeState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return GCYMTextures.WATERTIGHT_CASING;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return GCYMTextures.LARGE_DISTILLERY_OVERLAY;
    }

    @Override
    public int getFluidOutputLimit() {
        if (usesAdvHatchLogic()) return this.handler.getLayerCount();
        else return super.getFluidOutputLimit();
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean isTiered() {
        return false;
    }

    private class LargeDistilleryRecipeLogic extends GCYMMultiblockRecipeLogic {

        public LargeDistilleryRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        protected void outputRecipeOutputs() {
            if (usesAdvHatchLogic()) {
                GTTransferUtils.addItemsToItemHandler(getOutputInventory(), false, itemOutputs);
                handler.applyFluidToOutputs(fluidOutputs, true);
            } else {
                super.outputRecipeOutputs();
            }
        }

        @Override
        protected boolean checkOutputSpaceFluids(@NotNull Recipe recipe,
                                                 @NotNull IMultipleTankHandler exportFluids) {
            if (usesAdvHatchLogic()) {
                if (!metaTileEntity.canVoidRecipeFluidOutputs() &&
                        !handler.applyFluidToOutputs(recipe.getAllFluidOutputs(), false)) {
                    this.isOutputsFull = true;
                    return false;
                }
                return true;
            }
            return super.checkOutputSpaceFluids(recipe, exportFluids);
        }

        @Override
        protected IMultipleTankHandler getOutputTank() {
            if (usesAdvHatchLogic())
                return handler.getFluidTanks();

            return super.getOutputTank();
        }
    }
}

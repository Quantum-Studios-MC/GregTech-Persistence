package gregtech.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.GCYMTextures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockLargeMultiblockCasing;
import gregtech.common.blocks.GCYMMetaBlocks;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.GCYMRecipeMapMultiblockController;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class MetaTileEntityLargeExtractor extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeExtractor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new gregtech.api.recipes.RecipeMap<?>[] {
                RecipeMaps.EXTRACTOR_RECIPES,
                RecipeMaps.CANNER_RECIPES
        });
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeExtractor(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "XXXXX", "XXXXX")
                .aisle("XXSXX", "XCTCX", "XXXXX")
                .aisle("XXXXX", "XXXXX", "XXXXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(25).or(autoAbilities()))
                .where('C', states(getPipeState()))
                .where('T', tieredCasing().or(air()))
                .build();
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
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return GCYMTextures.LARGE_EXTRACTOR_OVERLAY;
    }
}

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
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockLargeMultiblockCasing;
import gregtech.common.blocks.BlockUniqueCasing;
import gregtech.common.blocks.GCYMMetaBlocks;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.GCYMRecipeMapMultiblockController;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class MetaTileEntityLargeCutter extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeCutter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new gregtech.api.recipes.RecipeMap<?>[] {
                RecipeMaps.CUTTER_RECIPES,
                RecipeMaps.LATHE_RECIPES
        });
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeCutter(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXX", "XXXX", "XXXX", "####")
                .aisle("XXXX", "STAX", "XXXX", "####")
                .aisle("XXXX", "XXXX", "XXXX", "XXXX")
                .aisle("XXXX", "GCCX", "GAAX", "XXXX").setRepeatable(3)
                .aisle("XXXX", "XXXX", "XXXX", "XXXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(65).or(autoAbilities()))
                .where('G', states(getGlassState()))
                .where('C', states(getBladeState()))
                .where('T', tieredCasing().or(air()))
                .where('A', air())
                .where('#', any())
                .build();
    }

    private static IBlockState getCasingState() {
        return GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                .getState(BlockLargeMultiblockCasing.CasingType.CUTTER_CASING);
    }

    private static IBlockState getBladeState() {
        return GCYMMetaBlocks.UNIQUE_CASING
                .getState(BlockUniqueCasing.UniqueCasingType.SLICING_BLADES);
    }

    private static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return GCYMTextures.CUTTER_CASING;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return GCYMTextures.LARGE_CUTTER_OVERLAY;
    }
}

package gregtech.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
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

public class MetaTileEntityLargeChemicalBath extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeChemicalBath(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new RecipeMap<?>[] {
                RecipeMaps.CHEMICAL_BATH_RECIPES,
                RecipeMaps.ORE_WASHER_RECIPES
        });
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeChemicalBath(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .aisle("XXSXX", "XCTCX", "XAAAX", "XAAAX", "XAAAX", "XCCCX", "XXXXX")
                .aisle("XXXXX", "XAAAX", "XAAAX", "XAAAX", "XAAAX", "XAAAX", "XXXXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(55).or(autoAbilities()))
                .where('C', states(getPipeState()))
                .where('T', tieredCasing().or(states(getPipeState())))
                .where('A', air())
                .build();
    }

    private static IBlockState getCasingState() {
        return GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                .getState(BlockLargeMultiblockCasing.CasingType.WATERTIGHT_CASING);
    }

    private static IBlockState getPipeState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return GCYMTextures.WATERTIGHT_CASING;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return GCYMTextures.LARGE_CHEMICAL_BATH_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }
}

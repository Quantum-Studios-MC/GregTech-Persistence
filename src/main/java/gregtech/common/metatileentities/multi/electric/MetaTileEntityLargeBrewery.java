package gregtech.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.GCYMTextures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockLargeMultiblockCasing;
import gregtech.common.blocks.BlockUniqueCasing;
import gregtech.common.blocks.GCYMMetaBlocks;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.GCYMRecipeMapMultiblockController;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class MetaTileEntityLargeBrewery extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeBrewery(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new RecipeMap<?>[] {
                RecipeMaps.BREWING_RECIPES,
                RecipeMaps.FERMENTING_RECIPES,
                RecipeMaps.FLUID_HEATER_RECIPES
        });
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeBrewery(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("#XXX#", "#XXX#", "#XXX#", "#XXX#", "#####")
                .aisle("XXXXX", "XCCCX", "XAAAX", "XXAXX", "##X##")
                .aisle("XXXXX", "XCPCX", "XATAX", "XAPAX", "#XMX#")
                .aisle("XXXXX", "XCCCX", "XAAAX", "XXAXX", "##X##")
                .aisle("#XXX#", "#XSX#", "#XXX#", "#XXX#", "#####")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(50)
                        .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('C', states(getCoilState()))
                .where('P', states(getPipeState()))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('T', tieredCasing().or(states(getPipeState())))
                .where('A', air())
                .where('#', any())
                .build();
    }

    private static IBlockState getCasingState() {
        return GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING
                .getState(BlockLargeMultiblockCasing.CasingType.CORROSION_PROOF_CASING);
    }

    private static IBlockState getCoilState() {
        return GCYMMetaBlocks.UNIQUE_CASING
                .getState(BlockUniqueCasing.UniqueCasingType.MOLYBDENUM_DISILICIDE_COIL);
    }

    private static IBlockState getPipeState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return GCYMTextures.CORROSION_PROOF_CASING;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return GCYMTextures.LARGE_BREWERY_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }
}

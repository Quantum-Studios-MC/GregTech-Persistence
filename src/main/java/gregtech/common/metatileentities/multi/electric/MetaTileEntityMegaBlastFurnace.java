package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.IHeatingCoil;
import gregtech.api.capability.IMufflerHatch;
import gregtech.api.capability.impl.HeatingCoilRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.GCYMTextures;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import gregtech.common.blocks.BlockLargeMultiblockCasing.CasingType;
import gregtech.common.blocks.BlockUniqueCasing.UniqueCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.GCYMMetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.GCYMRecipeMapMultiblockController;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityMegaBlastFurnace extends GCYMRecipeMapMultiblockController implements IHeatingCoil {

    private int blastFurnaceTemperature;

    public MetaTileEntityMegaBlastFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.BLAST_RECIPES);
        this.recipeMapWorkable = new HeatingCoilRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityMegaBlastFurnace(this.metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltage()))
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgress(), recipeMapWorkable.getMaxProgress())
                .addRecipeOutputLine(recipeMapWorkable);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoilType");
        if (type instanceof IHeatingCoilBlockStats stats) {
            this.blastFurnaceTemperature = stats.getCoilTemperature();
        } else {
            this.blastFurnaceTemperature = CoilType.CUPRONICKEL.getCoilTemperature();
        }
        this.blastFurnaceTemperature += 100 *
                Math.max(0, GTUtility.getFloorTierByVoltage(getEnergyContainer().getInputVoltage()) - GTValues.MV);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.blastFurnaceTemperature = 0;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        return this.blastFurnaceTemperature >= recipe.getProperty(TemperatureProperty.getInstance(), 0);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        TraceabilityPredicate casing = states(getCasingState()).setMinGlobalLimited(360);
        return FactoryBlockPattern.start()
                .aisle("##XXXXXXXXX##", "##XXXXXXXXX##", "#############", "#############", "#############",
                        "#############", "#############", "#############", "#############", "#############",
                        "#############", "#############", "#############", "#############", "#############",
                        "#############", "#############")
                .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###",
                        "#############", "#############", "#############", "#############", "#############",
                        "####FFFFF####", "#############", "#############", "#############", "#############",
                        "#############", "#############")
                .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFHHHFFF##",
                        "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##",
                        "##FFFHHHFFF##", "#############", "#############", "#############", "#############",
                        "#############", "###TTTTTTT###")
                .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFHHHPHHHFF#",
                        "######P######", "######P######", "######P######", "######P######", "######P######",
                        "##FHHHPHHHF##", "######P######", "######P######", "######P######", "######P######",
                        "######P######", "##TTTTPTTTT##")
                .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFHHHHHHHFF#",
                        "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####",
                        "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####",
                        "####BITIB####", "##TTTTPTTTT##")
                .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FHHHAAAHHHF#",
                        "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####",
                        "#FHHHAAAHHHF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####",
                        "####IAAAI####", "##TTTTPTTTT##")
                .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "###PPAAAPP###", "###PTAAATP###", "#FHPHAAAHPHF#",
                        "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###", "###PTAAATP###",
                        "#FHPHAAAHPHF#", "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###",
                        "###PTAAATP###", "##TPPPMPPPT##")
                .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FHHHAAAHHHF#",
                        "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####",
                        "#FHHHAAAHHHF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####",
                        "####IAAAI####", "##TTTTPTTTT##")
                .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFHHHHHHHFF#",
                        "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####",
                        "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####",
                        "####BITIB####", "##TTTTPTTTT##")
                .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFHHHPHHHFF#",
                        "######P######", "######P######", "######P######", "######P######", "######P######",
                        "##FHHHPHHHF##", "######P######", "######P######", "######P######", "######P######",
                        "######P######", "##TTTTPTTTT##")
                .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFHHHFFF##",
                        "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##",
                        "##FFFHHHFFF##", "#############", "#############", "#############", "#############",
                        "#############", "###TTTTTTT###")
                .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###",
                        "#############", "#############", "#############", "#############", "#############",
                        "####FFFFF####", "#############", "#############", "#############", "#############",
                        "#############", "#############")
                .aisle("##XXXXXXXXX##", "##XXXXSXXXX##", "#############", "#############", "#############",
                        "#############", "#############", "#############", "#############", "#############",
                        "#############", "#############", "#############", "#############", "#############",
                        "#############", "#############")
                .where('S', selfPredicate())
                .where('X', casing.or(autoAbilities(true, true, true, true, true, true, false)))
                .where('F', frames(Materials.NaquadahAlloy))
                .where('H', casing)
                .where('P', states(getPipeState()))
                .where('B', states(getFireboxState()))
                .where('I', states(getIntakeState()))
                .where('T', states(getCasingState2()))
                .where('V', states(getVentState()))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('C', heatingCoils())
                .where('A', air())
                .where('#', any())
                .build();
    }

    private static IBlockState getCasingState() {
        return GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(CasingType.HIGH_TEMPERATURE_CASING);
    }

    private static IBlockState getCasingState2() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
    }

    private static IBlockState getFireboxState() {
        return MetaBlocks.BOILER_FIREBOX_CASING
                .getState(BlockFireboxCasing.FireboxCasingType.TUNGSTENSTEEL_FIREBOX);
    }

    private static IBlockState getIntakeState() {
        return MetaBlocks.MULTIBLOCK_CASING
                .getState(BlockMultiblockCasing.MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING);
    }

    private static IBlockState getPipeState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE);
    }

    private static IBlockState getVentState() {
        return GCYMMetaBlocks.UNIQUE_CASING.getState(UniqueCasingType.HEAT_VENT);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.3"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return iMultiblockPart instanceof IMufflerHatch ? Textures.ROBUST_TUNGSTENSTEEL_CASING :
                GCYMTextures.BLAST_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return GCYMTextures.MEGA_BLAST_FURNACE_OVERLAY;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public int getCurrentTemperature() {
        return this.blastFurnaceTemperature;
    }
}

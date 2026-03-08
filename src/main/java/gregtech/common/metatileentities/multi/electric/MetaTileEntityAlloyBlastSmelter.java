package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.IHeatingCoil;
import gregtech.api.capability.impl.HeatingCoilRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.ui.KeyManager;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.UISyncer;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.GCYMRecipeMaps;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.GCYMTextures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockLargeMultiblockCasing.CasingType;
import gregtech.common.blocks.BlockUniqueCasing.UniqueCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.GCYMMetaBlocks;
import gregtech.common.metatileentities.GCYMMetaTileEntities;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MetaTileEntityAlloyBlastSmelter extends RecipeMapMultiblockController implements IHeatingCoil {

    private int blastFurnaceTemperature;

    public MetaTileEntityAlloyBlastSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GCYMRecipeMaps.ALLOY_BLAST_RECIPES);
        this.recipeMapWorkable = new HeatingCoilRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAlloyBlastSmelter(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(this.getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltage()))
                .addCustom(this::addHeatCapacity)
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgress(), recipeMapWorkable.getMaxProgress())
                .addRecipeOutputLine(recipeMapWorkable);
    }

    private void addHeatCapacity(KeyManager keyManager, UISyncer syncer) {
        if (isStructureFormed()) {
            var heatString = KeyUtil.number(TextFormatting.RED,
                    syncer.syncInt(getCurrentTemperature()), "K");

            keyManager.add(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.blast_furnace.max_temperature", heatString));
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        IHeatingCoilBlockStats type = context.getOrDefault("CoilType", CoilType.CUPRONICKEL);
        this.blastFurnaceTemperature = type.getCoilTemperature();
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
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("#XXX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXMXX")
                .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                .aisle("#XSX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                .where('S', selfPredicate())
                .where('X',
                        states(getCasingState()).setMinGlobalLimited(30)
                                .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('C', heatingCoils())
                .where('G', states(getCasingState2()))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('A', air())
                .where('#', any())
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("#XEX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                .aisle("XXXXX", "C###C", "G###G", "C###C", "XXXXX")
                .aisle("XXXXX", "C###C", "G###G", "C###C", "XXMXX")
                .aisle("FXXXH", "C###C", "G###G", "C###C", "XXXXX")
                .aisle("#ISO#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                .where('S', GCYMMetaTileEntities.ALLOY_BLAST_SMELTER, EnumFacing.SOUTH)
                .where('X', getCasingState())
                .where('G', getCasingState2())
                .where('M', MetaTileEntities.MUFFLER_HATCH[GTValues.HV], EnumFacing.UP)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.HV], EnumFacing.SOUTH)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.HV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.HV], EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.EV], EnumFacing.NORTH)
                .where('H',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                getCasingState(),
                        EnumFacing.SOUTH)
                .where('#', Blocks.AIR.getDefaultState());

        GregTechAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('C', entry.getKey()).build()));
        return shapeInfo;
    }

    private static IBlockState getCasingState() {
        return GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(CasingType.HIGH_TEMPERATURE_CASING);
    }

    private static IBlockState getCasingState2() {
        return GCYMMetaBlocks.UNIQUE_CASING.getState(UniqueCasingType.HEAT_VENT);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.3"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return GCYMTextures.BLAST_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return GCYMTextures.ALLOY_BLAST_SMELTER_OVERLAY;
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

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = super.getDataInfo();
        list.add(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(blastFurnaceTemperature) + "K")
                        .setStyle(new Style().setColor(TextFormatting.RED))));
        return list;
    }
}

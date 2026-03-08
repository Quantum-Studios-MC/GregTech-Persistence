package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IParallelHatch;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.GCYMMultiblockAbility;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.client.renderer.texture.GCYMTextures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class MetaTileEntityParallelHatch extends MetaTileEntityMultiblockPart
                                         implements IMultiblockAbilityPart<IParallelHatch>, IParallelHatch {

    private static final int MIN_PARALLEL = 1;

    private final int maxParallel;

    private int currentParallel;

    public MetaTileEntityParallelHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.maxParallel = (int) Math.pow(4, tier - GTValues.EV);
        this.currentParallel = this.maxParallel;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityParallelHatch(this.metaTileEntityId, this.getTier());
    }

    @Override
    public int getCurrentParallel() {
        return currentParallel;
    }

    public void setCurrentParallel(int parallelAmount) {
        this.currentParallel = MathHelper.clamp(this.currentParallel + parallelAmount, 1, this.maxParallel);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(MetaTileEntityGuiData guiData, PanelSyncManager panelSyncManager,
                                UISettings settings) {
        IntSyncValue parallelSync = new IntSyncValue(
                () -> currentParallel,
                val -> this.currentParallel = MathHelper.clamp(val, 1, maxParallel));
        panelSyncManager.syncValue("parallel", parallelSync);

        InteractionSyncHandler decreaseHandler = new InteractionSyncHandler()
                .setOnMousePressed(mouse -> {
                    if (panelSyncManager.isClient()) return;
                    this.currentParallel = MathHelper.clamp(currentParallel - 1, 1, maxParallel);
                });
        InteractionSyncHandler increaseHandler = new InteractionSyncHandler()
                .setOnMousePressed(mouse -> {
                    if (panelSyncManager.isClient()) return;
                    this.currentParallel = MathHelper.clamp(currentParallel + 1, 1, maxParallel);
                });

        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(GTGuiTextures.DISPLAY.asWidget()
                        .pos(62, 36)
                        .size(53, 20))
                .child(new TextFieldWidget()
                        .pos(63, 38)
                        .size(51, 16)
                        .setNumbers(1, maxParallel)
                        .setMaxLength(String.valueOf(maxParallel).length())
                        .value(parallelSync))
                .child(new ButtonWidget<>()
                        .pos(29, 36)
                        .size(30, 20)
                        .overlay(IKey.str("-"))
                        .syncHandler(decreaseHandler))
                .child(new ButtonWidget<>()
                        .pos(118, 36)
                        .size(30, 20)
                        .overlay(IKey.str("+"))
                        .syncHandler(increaseHandler))
                .bindPlayerInventory();
    }

    public String getParallelAmountToString() {
        return Integer.toString(this.currentParallel);
    }

    public static @NotNull Function<String, String> getTextFieldValidator(IntSupplier maxSupplier) {
        return val -> {
            if (val.isEmpty())
                return String.valueOf(MIN_PARALLEL);
            int max = maxSupplier.getAsInt();
            int num;
            try {
                num = Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
                return String.valueOf(max);
            }
            if (num < MIN_PARALLEL)
                return String.valueOf(MIN_PARALLEL);
            if (num > max)
                return String.valueOf(max);
            return val;
        };
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gcym.machine.parallel_hatch.tooltip", this.maxParallel));
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }

    @Override
    public MultiblockAbility<IParallelHatch> getAbility() {
        return GCYMMultiblockAbility.PARALLEL_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            OrientedOverlayRenderer overlayRenderer;
            if (getTier() == GTValues.IV)
                overlayRenderer = GCYMTextures.PARALLEL_HATCH_MK1_OVERLAY;
            else if (getTier() == GTValues.LuV)
                overlayRenderer = GCYMTextures.PARALLEL_HATCH_MK2_OVERLAY;
            else if (getTier() == GTValues.ZPM)
                overlayRenderer = GCYMTextures.PARALLEL_HATCH_MK3_OVERLAY;
            else
                overlayRenderer = GCYMTextures.PARALLEL_HATCH_MK4_OVERLAY;

            if (getController() != null && getController() instanceof RecipeMapMultiblockController) {
                overlayRenderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                        getController().isActive(),
                        getController().getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null)
                                .isWorkingEnabled());
            } else {
                overlayRenderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), false, false);
            }
        }
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        data.setInteger("currentParallel", this.currentParallel);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.currentParallel = data.getInteger("currentParallel");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.currentParallel);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.currentParallel = buf.readInt();
    }
}

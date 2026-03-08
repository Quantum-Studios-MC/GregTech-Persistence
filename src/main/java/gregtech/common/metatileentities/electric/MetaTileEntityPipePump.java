package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.fluidpipe.net.FluidPressureData;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Pipe Pump - a tiered machine that connects to fluid pipe networks and injects pressure.
 * <p>
 * Placed adjacent to fluid pipes, it maintains a configurable target pressure (in mBar).
 * It only consumes EU when the connected network pressure drops below the target,
 * making it energy-efficient by design.
 * <p>
 * Higher tiers can produce higher maximum pressure and are more energy-efficient.
 * <ul>
 *   <li>LV: max 2000 mBar, 8 EU/t when active</li>
 *   <li>MV: max 4000 mBar, 28 EU/t when active</li>
 *   <li>HV: max 8000 mBar, 100 EU/t when active</li>
 *   <li>EV: max 16000 mBar, 360 EU/t when active</li>
 *   <li>IV: max 32000 mBar, 1200 EU/t when active</li>
 * </ul>
 * The pump injects pressure into the front-facing pipe neighbor each tick.
 */
public class MetaTileEntityPipePump extends TieredMetaTileEntity {

    private static final int PRESSURE_INCREMENT_STEP = 100;

    private int targetPressure;
    private boolean isActive;

    public MetaTileEntityPipePump(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.targetPressure = getMaxPressure() / 2;
        this.isActive = false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPipePump(metaTileEntityId, getTier());
    }

    /**
     * Maximum pressure this tier of pump can produce.
     */
    public int getMaxPressure() {
        return 2000 * (1 << (getTier() - 1));
    }

    /**
     * EU/t cost when actively pumping.
     */
    public int getEnergyPerTick() {
        return (int) (GTValues.V[getTier()] / 4);
    }

    public int getTargetPressure() {
        return targetPressure;
    }

    public boolean isPumpActive() {
        return isActive;
    }

    private void setTargetPressureClamped(int value) {
        targetPressure = Math.max(0, Math.min(getMaxPressure(), value));
        markDirty();
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote || !ConfigHolder.machines.pressure.enablePressureSystem) return;

        if (getOffsetTimer() % 5 != 0) return;

        isActive = false;

        TileEntityFluidPipeTickable pipe = getConnectedPipe();
        if (pipe == null) return;

        FluidPressureData pipePresure = pipe.getPressureData();
        int currentPressure = pipePresure.getPressure();

        // Only activate when network pressure is below target
        if (currentPressure >= targetPressure) return;

        int energyCost = getEnergyPerTick() * 5;
        if (energyContainer.getEnergyStored() < energyCost) return;

        int pressureToAdd = Math.min(targetPressure - currentPressure, getMaxPressure() / 10);
        pipePresure.addPressure(pressureToAdd);
        energyContainer.removeEnergy(energyCost);
        isActive = true;
    }

    @Nullable
    private TileEntityFluidPipeTickable getConnectedPipe() {
        if (getWorld() == null) return null;
        TileEntity te = getWorld().getTileEntity(getPos().offset(getFrontFacing()));
        if (te instanceof TileEntityFluidPipeTickable pipe) {
            return pipe;
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public @NotNull ModularPanel buildUI(MetaTileEntityGuiData guiData, PanelSyncManager panelSyncManager,
                                         UISettings settings) {
        int maxP = getMaxPressure();
        IntSyncValue targetSync = new IntSyncValue(() -> targetPressure, this::setTargetPressureClamped);
        IntSyncValue currentPressureSync = new IntSyncValue(() -> {
            TileEntityFluidPipeTickable pipe = getConnectedPipe();
            return pipe != null ? pipe.getPressureData().getPressure() : -1;
        });
        IntSyncValue burstPressureSync = new IntSyncValue(() -> {
            TileEntityFluidPipeTickable pipe = getConnectedPipe();
            return pipe != null ? pipe.getNodeData().getBurstPressure() : 0;
        });
        BooleanSyncValue activeSync = new BooleanSyncValue(this::isPumpActive);

        panelSyncManager.syncValue("target", targetSync);
        panelSyncManager.syncValue("current", currentPressureSync);
        panelSyncManager.syncValue("burst", burstPressureSync);
        panelSyncManager.syncValue("active", activeSync);

        InteractionSyncHandler decreaseHandler = new InteractionSyncHandler()
                .setOnMousePressed(mouse -> {
                    if (panelSyncManager.isClient()) return;
                    setTargetPressureClamped(targetPressure - PRESSURE_INCREMENT_STEP);
                });
        InteractionSyncHandler increaseHandler = new InteractionSyncHandler()
                .setOnMousePressed(mouse -> {
                    if (panelSyncManager.isClient()) return;
                    setTargetPressureClamped(targetPressure + PRESSURE_INCREMENT_STEP);
                });

        return GTGuis.createPanel(this, 176, 192)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(new RichTextWidget()
                        .size(156, 47)
                        .background(GTGuiTextures.DISPLAY.asIcon().size(162, 55))
                        .pos(10, 22)
                        .textColor(Color.WHITE.main)
                        .alignment(Alignment.TopLeft)
                        .autoUpdate(true)
                        .textBuilder(richText -> {
                            int target = targetSync.getIntValue();
                            int targetPercent = maxP > 0 ? (int) ((long) target * 100 / maxP) : 0;
                            richText.addLine(IKey.lang("gregtech.machine.pipe_pump.target_detail",
                                    TextFormatting.AQUA.toString() + target + TextFormatting.RESET,
                                    TextFormatting.AQUA.toString() + targetPercent + "%" + TextFormatting.RESET));

                            int current = currentPressureSync.getIntValue();
                            if (current >= 0) {
                                int burst = burstPressureSync.getIntValue();
                                int fillPercent = burst > 0 ? (int) ((long) current * 100 / burst) : 0;
                                TextFormatting riskColor;
                                String riskKey;
                                if (current > burst) {
                                    riskColor = TextFormatting.DARK_RED;
                                    riskKey = "gregtech.fluid_pipe.risk.critical";
                                } else if (current > burst * 3 / 4) {
                                    riskColor = TextFormatting.RED;
                                    riskKey = "gregtech.fluid_pipe.risk.danger";
                                } else if (current > burst / 2) {
                                    riskColor = TextFormatting.YELLOW;
                                    riskKey = "gregtech.fluid_pipe.risk.warning";
                                } else {
                                    riskColor = TextFormatting.GREEN;
                                    riskKey = "gregtech.fluid_pipe.risk.nominal";
                                }
                                richText.addLine(IKey.lang("gregtech.machine.pipe_pump.pipe_status",
                                        riskColor.toString() + current + TextFormatting.RESET,
                                        riskColor.toString() + fillPercent + "%" + TextFormatting.RESET,
                                        IKey.lang(riskKey).getFormatted()));

                                if (activeSync.getBoolValue()) {
                                    richText.addLine(KeyUtil.lang(TextFormatting.GREEN,
                                            "gregtech.machine.pipe_pump.pumping"));
                                } else if (current >= target) {
                                    richText.addLine(KeyUtil.lang(TextFormatting.AQUA,
                                            "gregtech.machine.pipe_pump.target_reached"));
                                } else {
                                    richText.addLine(KeyUtil.lang(TextFormatting.GRAY,
                                            "gregtech.machine.pipe_pump.idle"));
                                }
                            } else {
                                richText.addLine(KeyUtil.lang(TextFormatting.YELLOW,
                                        "gregtech.machine.pipe_pump.no_pipe"));
                            }
                        }))
                .child(IKey.str("Target (mBar):").asWidget().pos(7, 78))
                .child(new TextFieldWidget()
                        .pos(7, 90).size(120, 14)
                        .setNumbers(0, maxP)
                        .setMaxLength(8)
                        .value(targetSync))
                .child(new ButtonWidget<>()
                        .pos(131, 88).size(18, 18)
                        .overlay(IKey.str("-"))
                        .syncHandler(decreaseHandler))
                .child(new ButtonWidget<>()
                        .pos(151, 88).size(18, 18)
                        .overlay(IKey.str("+"))
                        .syncHandler(increaseHandler))
                .bindPlayerInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.pipe_pump.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.pipe_pump.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.pipe_pump.tooltip.max", getMaxPressure()));
        tooltip.add(I18n.format("gregtech.machine.pipe_pump.tooltip.cost", getEnergyPerTick()));
        tooltip.add(I18n.format("gregtech.machine.pipe_pump.tooltip.hint"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    @NotNull
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("TargetPressure", targetPressure);
        return data;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("TargetPressure")) {
            targetPressure = data.getInteger("TargetPressure");
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(targetPressure);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        targetPressure = buf.readVarInt();
    }

    @Override
    protected boolean isEnergyEmitter() {
        return false;
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 2L;
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return false;
    }
}

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
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Pressure Sensor - outputs a comparator signal proportional to the connected pipe's pressure.
 * <p>
 * Place with the front facing toward a fluid pipe. The back and sides output a redstone
 * signal from 0-15 based on (current pressure / burst pressure) of the connected pipe.
 * <p>
 * Costs negligible power (1 EU/t at LV) and serves as the primary way for players to
 * monitor pipe networks and build circuit-controlled pump shutoffs.
 */
public class MetaTileEntityPressureSensor extends TieredMetaTileEntity {

    private int currentSignal = 0;
    private int lastReadPressure = 0;
    private int lastReadBurst = 0;

    public MetaTileEntityPressureSensor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPressureSensor(metaTileEntityId, getTier());
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote || !ConfigHolder.machines.pressure.enablePressureSystem) return;

        if (getOffsetTimer() % 5 != 0) return;

        int newSignal = 0;
        lastReadPressure = 0;
        lastReadBurst = 0;

        TileEntity te = getWorld().getTileEntity(getPos().offset(getFrontFacing()));
        if (te instanceof TileEntityFluidPipeTickable pipe) {
            lastReadPressure = pipe.getPressureData().getPressure();
            lastReadBurst = pipe.getNodeData().getBurstPressure();
            if (lastReadBurst > 0) {
                newSignal = (int) ((long) lastReadPressure * 15 / lastReadBurst);
                newSignal = Math.max(0, Math.min(15, newSignal));
            }
        }

        if (newSignal != currentSignal) {
            currentSignal = newSignal;
            // Update redstone output on all non-front sides
            for (EnumFacing side : EnumFacing.VALUES) {
                if (side != getFrontFacing()) {
                    setOutputRedstoneSignal(side, currentSignal);
                }
            }
            markDirty();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.SCREEN.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public @NotNull ModularPanel buildUI(MetaTileEntityGuiData guiData, PanelSyncManager panelSyncManager,
                                         UISettings settings) {
        IntSyncValue pressureSync = new IntSyncValue(() -> lastReadPressure);
        IntSyncValue burstSync = new IntSyncValue(() -> lastReadBurst);
        IntSyncValue signalSync = new IntSyncValue(() -> currentSignal);

        panelSyncManager.syncValue("pressure", pressureSync);
        panelSyncManager.syncValue("burst", burstSync);
        panelSyncManager.syncValue("signal", signalSync);

        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(new RichTextWidget()
                        .size(156, 47)
                        .background(GTGuiTextures.DISPLAY.asIcon().size(162, 55))
                        .pos(10, 22)
                        .textColor(Color.WHITE.main)
                        .alignment(Alignment.TopLeft)
                        .autoUpdate(true)
                        .textBuilder(richText -> {
                            int burst = burstSync.getIntValue();
                            if (burst > 0) {
                                int pressure = pressureSync.getIntValue();
                                int percent = (int) ((long) pressure * 100 / burst);
                                TextFormatting riskColor;
                                String riskKey;
                                if (pressure > burst) {
                                    riskColor = TextFormatting.DARK_RED;
                                    riskKey = "gregtech.fluid_pipe.risk.critical";
                                } else if (pressure > burst * 3 / 4) {
                                    riskColor = TextFormatting.RED;
                                    riskKey = "gregtech.fluid_pipe.risk.danger";
                                } else if (pressure > burst / 2) {
                                    riskColor = TextFormatting.YELLOW;
                                    riskKey = "gregtech.fluid_pipe.risk.warning";
                                } else if (pressure > burst / 4) {
                                    riskColor = TextFormatting.GREEN;
                                    riskKey = "gregtech.fluid_pipe.risk.nominal";
                                } else {
                                    riskColor = TextFormatting.GRAY;
                                    riskKey = "gregtech.fluid_pipe.risk.idle";
                                }

                                richText.addLine(IKey.lang("gregtech.machine.pressure_sensor.reading",
                                        riskColor.toString() + pressure + TextFormatting.RESET,
                                        riskColor.toString() + percent + "%" + TextFormatting.RESET));
                                richText.addLine(IKey.lang("gregtech.machine.pressure_sensor.risk_level",
                                        IKey.lang(riskKey).getFormatted()));

                                int sig = signalSync.getIntValue();
                                StringBuilder bar = new StringBuilder();
                                for (int i = 0; i < 15; i++) {
                                    bar.append(i < sig ? "\u2588" : "\u2591");
                                }
                                richText.addLine(IKey.lang("gregtech.machine.pressure_sensor.signal_bar",
                                        TextFormatting.RED.toString() + bar + TextFormatting.RESET,
                                        TextFormatting.RED.toString() + sig + TextFormatting.RESET));
                            } else {
                                richText.addLine(KeyUtil.lang(TextFormatting.YELLOW,
                                        "gregtech.machine.pressure_sensor.no_pipe"));
                                richText.addLine(KeyUtil.lang(TextFormatting.GRAY,
                                        "gregtech.machine.pressure_sensor.hint"));
                            }
                        }))
                .bindPlayerInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.pressure_sensor.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.pressure_sensor.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.pressure_sensor.tooltip.3"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected boolean isEnergyEmitter() {
        return false;
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return side != null && side != getFrontFacing();
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 1L;
    }
}

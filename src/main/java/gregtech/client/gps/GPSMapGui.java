package gregtech.client.gps;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.common.gps.BeaconTracker;
import gregtech.common.gps.MapDataManager;
import gregtech.common.gps.UpgradeHandler;
import gregtech.common.gps.WaypointManager;
import gregtech.common.items.MetaItems;
import gregtech.core.network.packets.PacketGPSAddWaypoint;
import gregtech.core.network.packets.PacketGPSDismissDeathPoint;
import gregtech.core.network.packets.PacketGPSRemoveWaypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class GPSMapGui extends GuiScreen {

    private final EntityPlayer player;
    private final EnumHand hand;
    private int mapCenterX;
    private int mapCenterZ;
    private int zoom = 4;
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 16;
    private static final int TEX_SIZE = 256;

    private DynamicTexture fullMapTexture;
    private ResourceLocation fullMapTextureLocation;
    private int[] fullMapPixels;

    private boolean needsTextureUpdate = true;
    private int dragStartX, dragStartZ;
    private int dragMouseX, dragMouseY;
    private boolean dragging;

    private GuiTextField waypointNameField;
    private int selectedWaypoint = -1;
    private int pendingWaypointX, pendingWaypointZ;
    private boolean showWaypointDialog;
    private int selectedDimension;

    /** Ticks since last auto-refresh of the texture. */
    private int autoRefreshTimer;
    private static final int AUTO_REFRESH_INTERVAL = 10;

    /** Sidebar scroll offset for waypoint list. */
    private int waypointScrollOffset;

    private static final int BTN_ZOOM_IN = 0;
    private static final int BTN_ZOOM_OUT = 1;
    private static final int BTN_CENTER = 2;
    private static final int BTN_NORTH_LOCK = 3;
    private static final int BTN_DISMISS_DEATH = 4;
    private static final int BTN_DIM_PREV = 5;
    private static final int BTN_DIM_NEXT = 6;
    private static final int BTN_CONFIRM_WP = 7;
    private static final int BTN_CANCEL_WP = 8;

    // Theme colours
    private static final int COL_BG = 0xFF0D1117;
    private static final int COL_PANEL = 0xFF161B22;
    private static final int COL_BORDER = 0xFF1A8A6E;
    private static final int COL_TEXT = 0xFFC9D1D9;
    private static final int COL_TEXT_DIM = 0xFF8B949E;
    private static final int COL_ACCENT = 0xFF1A8A6E;
    private static final int COL_DANGER = 0xFFDA3633;

    public GPSMapGui(EntityPlayer player, EnumHand hand) {
        this.player = player;
        this.hand = hand;
        this.mapCenterX = (int) player.posX;
        this.mapCenterZ = (int) player.posZ;
        this.selectedDimension = player.dimension;
    }

    private int sidebarWidth() { return 130; }
    private int mapLeft() { return 6; }
    private int mapTop() { return 6; }
    private int mapWidth() { return width - sidebarWidth() - 12; }
    private int mapHeight() { return height - 12; }

    @Override
    public void initGui() {
        buttonList.clear();

        int sw = sidebarWidth();
        int sx = width - sw;
        int by = 8;

        // Zoom row
        buttonList.add(new GuiButton(BTN_ZOOM_IN, sx + 4, by, sw / 2 - 6, 16, "\u00a7l+"));
        buttonList.add(new GuiButton(BTN_ZOOM_OUT, sx + sw / 2 + 2, by, sw / 2 - 6, 16, "\u00a7l-"));
        by += 20;
        buttonList.add(new GuiButton(BTN_CENTER, sx + 4, by, sw - 8, 16, "Re-center"));
        by += 20;
        buttonList.add(new GuiButton(BTN_NORTH_LOCK, sx + 4, by, sw - 8, 16, "North Lock"));
        by += 20;

        ItemStack gps = player.getHeldItem(hand);
        if (WaypointManager.getDeathPoint(gps) != null) {
            buttonList.add(new GuiButton(BTN_DISMISS_DEATH, sx + 4, by, sw - 8, 16, "\u00a7cDismiss Death"));
            by += 20;
        }

        if (UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.DIMENSION_MAP)) {
            buttonList.add(new GuiButton(BTN_DIM_PREV, sx + 4, by, sw / 2 - 6, 16, "< Dim"));
            buttonList.add(new GuiButton(BTN_DIM_NEXT, sx + sw / 2 + 2, by, sw / 2 - 6, 16, "Dim >"));
            by += 20;
        }

        waypointNameField = new GuiTextField(100, fontRenderer, width / 2 - 80, height / 2 - 10, 160, 18);
        waypointNameField.setMaxStringLength(32);
        waypointNameField.setVisible(false);

        ensureFullMapTexture();
        needsTextureUpdate = true;
        // Immediately scan the viewport on open and invalidate nearby stale data
        MapDataManager.invalidateNearby(player.chunkCoordX, player.chunkCoordZ, 3);
        triggerViewportScan();
    }

    private void ensureFullMapTexture() {
        if (fullMapTexture == null) {
            fullMapTexture = new DynamicTexture(TEX_SIZE, TEX_SIZE);
            fullMapTextureLocation = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("gtgps_fullmap", fullMapTexture);
            fullMapPixels = fullMapTexture.getTextureData();
        }
    }

    @Override
    public void updateScreen() {
        // Drain the scan queue every tick so chunks load while GUI is open
        // (MinimapRenderer's tick handler skips when a screen is open)
        if (selectedDimension == player.dimension) {
            MapDataManager.processScanQueue(player.world, 16);
            // If chunks just finished scanning, repaint the texture
            if (!needsTextureUpdate && !MapDataManager.hasPendingScans()) {
                // Check and refresh once scans settle
            }
        }

        autoRefreshTimer++;
        if (autoRefreshTimer >= AUTO_REFRESH_INTERVAL) {
            autoRefreshTimer = 0;
            triggerViewportScan();
            needsTextureUpdate = true;
        }
        // Also refresh whenever pending scans have been processed
        if (MapDataManager.hasPendingScans()) {
            // Still scanning - refresh texture every 3 ticks to show progress
            if (autoRefreshTimer % 3 == 0) {
                needsTextureUpdate = true;
            }
        }
    }

    private void triggerViewportScan() {
        if (selectedDimension != player.dimension) return;
        int blocksVisible = zoom * TEX_SIZE;
        int halfBlocks = blocksVisible / 2;
        int startCX = (mapCenterX - halfBlocks) >> 4;
        int endCX = (mapCenterX + halfBlocks) >> 4;
        int startCZ = (mapCenterZ - halfBlocks) >> 4;
        int endCZ = (mapCenterZ + halfBlocks) >> 4;
        // Limit the scan region to avoid lag
        int maxChunks = 12;
        startCX = Math.max(startCX, (mapCenterX >> 4) - maxChunks);
        endCX = Math.min(endCX, (mapCenterX >> 4) + maxChunks);
        startCZ = Math.max(startCZ, (mapCenterZ >> 4) - maxChunks);
        endCZ = Math.min(endCZ, (mapCenterZ >> 4) + maxChunks);

        ItemStack gps = player.getHeldItem(hand);
        boolean underground = UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.UNDERGROUND)
                && player.posY < 60;
        boolean oreOverlay = UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.ORE_OVERLAY);

        MapDataManager.checkDimensionChange(player.dimension);
        MapDataManager.updateSurroundingChunks(player.world,
                mapCenterX >> 4, mapCenterZ >> 4, maxChunks,
                underground, (int) player.posY, oreOverlay);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Full dark background
        drawRect(0, 0, width, height, COL_BG);

        ItemStack gps = player.getHeldItem(hand);
        if (!MetaItems.GPS_DEVICE.isItemEqual(gps)) {
            mc.displayGuiScreen(null);
            return;
        }

        if (needsTextureUpdate) {
            updateFullMapTexture(gps);
            needsTextureUpdate = false;
        }

        int mL = mapLeft(), mT = mapTop(), mW = mapWidth(), mH = mapHeight();

        // Map border
        drawRect(mL - 1, mT - 1, mL + mW + 1, mT + mH + 1, COL_BORDER);
        drawRect(mL, mT, mL + mW, mT + mH, COL_BG);

        mc.getTextureManager().bindTexture(fullMapTextureLocation);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawScaledTexture(mL, mT, mW, mH);

        renderMapWaypoints(gps, mW, mH);
        renderMapDeathPoint(gps, mW, mH);
        renderMapBeacons(gps, mW, mH);
        renderPlayerMarker(mW, mH);

        GlStateManager.disableBlend();

        // --- Sidebar ---
        int sw = sidebarWidth();
        int sx = width - sw;
        drawRect(sx, 0, width, height, COL_PANEL);
        drawRect(sx, 0, sx + 1, height, COL_BORDER);

        // Status info at bottom of sidebar
        int infoY = height - 60;
        drawHLine(sx + 4, sx + sw - 4, infoY - 4, COL_BORDER);

        IElectricItem electric = gps.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electric != null) {
            float pct = (float) electric.getCharge() / electric.getMaxCharge();
            String chargeStr = String.format("%,d / %,d EU", electric.getCharge(), electric.getMaxCharge());
            fontRenderer.drawStringWithShadow(chargeStr, sx + 6, infoY, COL_TEXT_DIM);
            // Bar
            int barW = sw - 12;
            drawRect(sx + 6, infoY + 12, sx + 6 + barW, infoY + 15, 0xFF21262D);
            int fillColor = pct > 0.3f ? COL_ACCENT : COL_DANGER;
            drawRect(sx + 6, infoY + 12, sx + 6 + (int) (barW * pct), infoY + 15, fillColor);
        }

        String coordStr = String.format("X: %d  Z: %d", mapCenterX, mapCenterZ);
        fontRenderer.drawStringWithShadow(coordStr, sx + 6, infoY + 20, COL_TEXT_DIM);
        String zoomStr = String.format("Zoom: %dx  Dim: %d", zoom, selectedDimension);
        fontRenderer.drawStringWithShadow(zoomStr, sx + 6, infoY + 32, COL_TEXT_DIM);

        // Waypoint list
        int wpListTop = getWaypointListTop();
        int wpListBottom = infoY - 8;
        drawHLine(sx + 4, sx + sw - 4, wpListTop - 4, 0xFF30363D);
        fontRenderer.drawStringWithShadow("\u00a7lWaypoints", sx + 6, wpListTop - 14, COL_ACCENT);
        fontRenderer.drawStringWithShadow("\u00a77(Right-click map to add)", sx + 6, wpListTop - 3, 0xFF484F58);

        NBTTagList waypoints = WaypointManager.getWaypoints(gps);
        int visibleSlots = (wpListBottom - wpListTop - 8) / 12;
        waypointScrollOffset = MathHelper.clamp(waypointScrollOffset, 0,
                Math.max(0, waypoints.tagCount() - visibleSlots));

        for (int i = waypointScrollOffset; i < waypoints.tagCount(); i++) {
            int yPos = wpListTop + 8 + (i - waypointScrollOffset) * 12;
            if (yPos + 10 > wpListBottom) break;

            NBTTagCompound wp = waypoints.getCompoundTagAt(i);
            String name = WaypointManager.getWaypointName(wp);
            int wx = WaypointManager.getWaypointX(wp);
            int wz = WaypointManager.getWaypointZ(wp);
            int color = WaypointManager.getWaypointColor(wp) | 0xFF000000;
            boolean hovered = mouseX >= sx + 4 && mouseX <= sx + sw - 4
                    && mouseY >= yPos && mouseY < yPos + 12;

            // Color indicator dot
            drawRect(sx + 6, yPos + 2, sx + 10, yPos + 6, color);
            String line = String.format("%s (%d,%d)", name, wx, wz);
            if (line.length() > 18) line = line.substring(0, 17) + "..";
            fontRenderer.drawStringWithShadow(line, sx + 13, yPos,
                    hovered ? 0xFFFFFFFF : COL_TEXT);

            if (hovered && i == selectedWaypoint) {
                fontRenderer.drawStringWithShadow("\u00a7c\u2716", sx + sw - 14, yPos, COL_DANGER);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Waypoint creation dialog overlay
        if (showWaypointDialog) {
            // Dim background
            drawRect(0, 0, width, height, 0x88000000);
            int dw = 200, dh = 70;
            int dx = width / 2 - dw / 2, dy = height / 2 - dh / 2;
            drawRect(dx, dy, dx + dw, dy + dh, COL_PANEL);
            drawRect(dx, dy, dx + dw, dy + 1, COL_BORDER);
            drawRect(dx, dy + dh - 1, dx + dw, dy + dh, COL_BORDER);
            drawRect(dx, dy, dx + 1, dy + dh, COL_BORDER);
            drawRect(dx + dw - 1, dy, dx + dw, dy + dh, COL_BORDER);
            fontRenderer.drawString("New Waypoint at " + pendingWaypointX + ", " + pendingWaypointZ,
                    dx + 8, dy + 6, COL_TEXT);
            waypointNameField.x = dx + 8;
            waypointNameField.y = dy + 20;
            waypointNameField.width = dw - 16;
            waypointNameField.setVisible(true);
            waypointNameField.drawTextBox();
        }
    }

    private int getWaypointListTop() {
        // Find the Y position below the last button
        int maxBtnY = 0;
        for (GuiButton btn : buttonList) {
            if (btn.id < 10) {
                int bottom = btn.y + btn.height;
                if (bottom > maxBtnY) maxBtnY = bottom;
            }
        }
        return maxBtnY + 18;
    }

    private void updateFullMapTexture(ItemStack gps) {
        ensureFullMapTexture();
        java.util.Arrays.fill(fullMapPixels, 0xFF0D1117);

        int blocksVisible = zoom * TEX_SIZE;
        int halfBlocks = blocksVisible / 2;
        int startX = mapCenterX - halfBlocks;
        int startZ = mapCenterZ - halfBlocks;

        boolean underground = UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.UNDERGROUND)
                && player.posY < 60
                && selectedDimension == player.dimension;
        boolean oreOverlay = UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.ORE_OVERLAY);

        for (int px = 0; px < TEX_SIZE; px++) {
            for (int py = 0; py < TEX_SIZE; py++) {
                int worldX = startX + px * zoom;
                int worldZ = startZ + py * zoom;
                int chunkX = worldX >> 4;
                int chunkZ = worldZ >> 4;
                int localX = ((worldX % 16) + 16) % 16;
                int localZ = ((worldZ % 16) + 16) % 16;

                int[] colors = underground ?
                        MapDataManager.getUndergroundColors(chunkX, chunkZ) :
                        MapDataManager.getChunkColors(chunkX, chunkZ);

                int color = 0xFF0D1117;
                if (colors != null) {
                    color = colors[localZ * 16 + localX];
                }

                if (oreOverlay) {
                    int[] oreColors = MapDataManager.getOreOverlayColors(chunkX, chunkZ);
                    if (oreColors != null && oreColors[localZ * 16 + localX] != 0) {
                        color = blendColor(color, oreColors[localZ * 16 + localX], 0.5f);
                    }
                }

                // Chunk grid lines (only at zoom where they aren't too dense)
                if (zoom <= 4) {
                    if (Math.floorMod(worldX, 16) < zoom || Math.floorMod(worldZ, 16) < zoom) {
                        color = blendColor(color, 0xFF888888, 0.2f);
                    }
                }

                // Ore vein grid lines (48-block grid, visible at all zoom levels) when ore overlay active
                if (oreOverlay) {
                    if (Math.floorMod(worldX, 48) < zoom || Math.floorMod(worldZ, 48) < zoom) {
                        color = blendColor(color, 0xFF1A8A6E, 0.55f);
                    }

                    // Vein center cross marker (small cross at center of each 48x48 cell)
                    int veinOffX = Math.floorMod(worldX, 48);
                    int veinOffZ = Math.floorMod(worldZ, 48);
                    int crossHalf = Math.max(1, zoom);
                    if (veinOffX >= 24 - crossHalf && veinOffX <= 24 + crossHalf
                            && veinOffZ >= 24 - crossHalf && veinOffZ <= 24 + crossHalf) {
                        if (Math.abs(veinOffX - 24) < zoom || Math.abs(veinOffZ - 24) < zoom) {
                            color = blendColor(color, 0xFFFFAA00, 0.7f);
                        }
                    }
                }

                fullMapPixels[py * TEX_SIZE + px] = color;
            }
        }

        fullMapTexture.updateDynamicTexture();
    }

    private void renderMapWaypoints(ItemStack gps, int mapW, int mapH) {
        NBTTagList waypoints = WaypointManager.getWaypoints(gps);
        for (int i = 0; i < waypoints.tagCount(); i++) {
            NBTTagCompound wp = waypoints.getCompoundTagAt(i);
            if (WaypointManager.getWaypointDim(wp) != selectedDimension) continue;

            int wx = WaypointManager.getWaypointX(wp);
            int wz = WaypointManager.getWaypointZ(wp);
            int[] screenPos = worldToScreen(wx, wz, mapW, mapH);
            if (screenPos == null) continue;

            int color = WaypointManager.getWaypointColor(wp) | 0xFF000000;
            drawColoredDiamond(screenPos[0], screenPos[1], 4, color);
            fontRenderer.drawStringWithShadow(WaypointManager.getWaypointName(wp),
                    screenPos[0] + 6, screenPos[1] - 4, color);
        }
    }

    private void renderMapDeathPoint(ItemStack gps, int mapW, int mapH) {
        NBTTagCompound death = WaypointManager.getDeathPoint(gps);
        if (death == null || death.getInteger("dim") != selectedDimension) return;

        int dx = death.getInteger("x");
        int dz = death.getInteger("z");
        int[] screenPos = worldToScreen(dx, dz, mapW, mapH);
        if (screenPos == null) return;

        drawColoredRect(screenPos[0] - 4, screenPos[1] - 4, 8, 8, COL_DANGER);
        drawColoredRect(screenPos[0] - 3, screenPos[1] - 3, 6, 6, 0xFFFFFFFF);
        fontRenderer.drawStringWithShadow("\u2620", screenPos[0] - 3, screenPos[1] - 4, COL_DANGER);
    }

    private void renderMapBeacons(ItemStack gps, int mapW, int mapH) {
        if (selectedDimension != player.dimension) return;
        int radius = UpgradeHandler.getMapRadius(gps);
        List<BeaconTracker.BeaconData> beacons = BeaconTracker.findBeacons(
                player.world, player.getPosition(), radius);

        for (BeaconTracker.BeaconData beacon : beacons) {
            int[] screenPos = worldToScreen(beacon.pos.getX(), beacon.pos.getZ(), mapW, mapH);
            if (screenPos == null) continue;

            drawColoredRect(screenPos[0] - 3, screenPos[1] - 3, 6, 6, 0xFF00FFFF);
            if (!beacon.name.isEmpty()) {
                fontRenderer.drawStringWithShadow(beacon.name,
                        screenPos[0] + 5, screenPos[1] - 4, 0xFF00FFFF);
            }
        }
    }

    private void renderPlayerMarker(int mapW, int mapH) {
        if (selectedDimension != player.dimension) return;
        int[] pos = worldToScreen((int) player.posX, (int) player.posZ, mapW, mapH);
        if (pos == null) return;

        GlStateManager.disableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float yaw = player.rotationYaw;
        double rad = Math.toRadians(yaw);
        int size = 6;

        buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        buf.pos(pos[0] + Math.sin(rad) * size, pos[1] - Math.cos(rad) * size, 0).endVertex();
        buf.pos(pos[0] + Math.sin(rad + 2.4) * size * 0.6, pos[1] - Math.cos(rad + 2.4) * size * 0.6, 0)
                .endVertex();
        buf.pos(pos[0] + Math.sin(rad - 2.4) * size * 0.6, pos[1] - Math.cos(rad - 2.4) * size * 0.6, 0)
                .endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();
    }

    private int[] worldToScreen(int worldX, int worldZ, int mapW, int mapH) {
        int mL = mapLeft(), mT = mapTop();
        int blocksVisible = zoom * TEX_SIZE;
        int halfBlocks = blocksVisible / 2;
        double scaleX = (double) mapW / blocksVisible;
        double scaleZ = (double) mapH / blocksVisible;
        int sx = mL + (int) ((worldX - mapCenterX + halfBlocks) * scaleX);
        int sy = mT + (int) ((worldZ - mapCenterZ + halfBlocks) * scaleZ);
        if (sx < mL || sx > mL + mapW || sy < mT || sy > mT + mapH) return null;
        return new int[]{ sx, sy };
    }

    private int[] screenToWorld(int screenX, int screenY, int mapW, int mapH) {
        int mL = mapLeft(), mT = mapTop();
        int blocksVisible = zoom * TEX_SIZE;
        int halfBlocks = blocksVisible / 2;
        double scaleX = (double) blocksVisible / mapW;
        double scaleZ = (double) blocksVisible / mapH;
        int wx = (int) ((screenX - mL) * scaleX) + mapCenterX - halfBlocks;
        int wz = (int) ((screenY - mT) * scaleZ) + mapCenterZ - halfBlocks;
        return new int[]{ wx, wz };
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        ItemStack gps = player.getHeldItem(hand);
        switch (button.id) {
            case BTN_ZOOM_IN:
                if (zoom > MIN_ZOOM) { zoom--; needsTextureUpdate = true; }
                break;
            case BTN_ZOOM_OUT:
                if (zoom < MAX_ZOOM) { zoom++; needsTextureUpdate = true; }
                break;
            case BTN_CENTER:
                mapCenterX = (int) player.posX;
                mapCenterZ = (int) player.posZ;
                needsTextureUpdate = true;
                break;
            case BTN_NORTH_LOCK:
                UpgradeHandler.toggleNorthLock(gps);
                break;
            case BTN_DISMISS_DEATH:
                GregTechAPI.networkHandler.sendToServer(
                        new PacketGPSDismissDeathPoint(hand == EnumHand.MAIN_HAND ? 0 : 1));
                WaypointManager.dismissDeathPoint(gps);
                initGui();
                break;
            case BTN_DIM_PREV:
                selectedDimension--;
                needsTextureUpdate = true;
                break;
            case BTN_DIM_NEXT:
                selectedDimension++;
                needsTextureUpdate = true;
                break;
            case BTN_CONFIRM_WP:
                confirmWaypoint(gps);
                break;
            case BTN_CANCEL_WP:
                closeWaypointDialog();
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (showWaypointDialog) {
            waypointNameField.mouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        int mL = mapLeft(), mT = mapTop(), mW = mapWidth(), mH = mapHeight();

        if (mouseX >= mL && mouseX <= mL + mW && mouseY >= mT && mouseY <= mT + mH) {
            if (mouseButton == 0) {
                dragging = true;
                dragMouseX = mouseX;
                dragMouseY = mouseY;
                dragStartX = mapCenterX;
                dragStartZ = mapCenterZ;
            } else if (mouseButton == 1) {
                int[] world = screenToWorld(mouseX, mouseY, mW, mH);
                pendingWaypointX = world[0];
                pendingWaypointZ = world[1];
                showWaypointDialog = true;
                waypointNameField.setVisible(true);
                waypointNameField.setFocused(true);
                waypointNameField.setText("");
                addDialogButtons();
            }
        }

        // Waypoint list clicks
        int sw = sidebarWidth();
        int sx = width - sw;
        int wpListTop = getWaypointListTop() + 8;
        NBTTagList waypoints = WaypointManager.getWaypoints(player.getHeldItem(hand));
        for (int i = waypointScrollOffset; i < waypoints.tagCount(); i++) {
            int yPos = wpListTop + (i - waypointScrollOffset) * 12;
            if (mouseX >= sx + 4 && mouseX <= sx + sw - 4
                    && mouseY >= yPos && mouseY < yPos + 12) {
                if (selectedWaypoint == i && mouseButton == 0) {
                    GregTechAPI.networkHandler.sendToServer(
                            new PacketGPSRemoveWaypoint(hand == EnumHand.MAIN_HAND ? 0 : 1, i));
                    WaypointManager.removeWaypoint(player.getHeldItem(hand), i);
                    selectedWaypoint = -1;
                } else {
                    selectedWaypoint = i;
                    NBTTagCompound wp = waypoints.getCompoundTagAt(i);
                    mapCenterX = WaypointManager.getWaypointX(wp);
                    mapCenterZ = WaypointManager.getWaypointZ(wp);
                    needsTextureUpdate = true;
                }
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            int blocksVisible = zoom * TEX_SIZE;
            int mW = mapWidth(), mH = mapHeight();
            double scaleX = (double) blocksVisible / mW;
            double scaleZ = (double) blocksVisible / mH;
            mapCenterX = dragStartX - (int) ((mouseX - dragMouseX) * scaleX);
            mapCenterZ = dragStartZ - (int) ((mouseY - dragMouseY) * scaleZ);
            needsTextureUpdate = true;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging) {
            // Trigger a fresh scan for the new viewport area after drag
            triggerViewportScan();
        }
        dragging = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int mx = Mouse.getEventX() * width / mc.displayWidth;
            int sx = width - sidebarWidth();
            if (mx >= sx) {
                // Scroll waypoint list
                if (scroll > 0) waypointScrollOffset = Math.max(0, waypointScrollOffset - 2);
                else waypointScrollOffset += 2;
            } else {
                // Scroll zoom
                if (scroll > 0 && zoom > MIN_ZOOM) zoom--;
                else if (scroll < 0 && zoom < MAX_ZOOM) zoom++;
                needsTextureUpdate = true;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (showWaypointDialog) {
            waypointNameField.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == 28) { // Enter
                confirmWaypoint(player.getHeldItem(hand));
            } else if (keyCode == 1) { // Escape
                closeWaypointDialog();
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void confirmWaypoint(ItemStack gps) {
        String name = waypointNameField.getText().trim();
        if (name.isEmpty()) name = "Waypoint";
        int color = new Random().nextInt(0xFFFFFF);

        IElectricItem electric = gps.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electric != null && electric.getCharge() >= 500) {
            GregTechAPI.networkHandler.sendToServer(
                    new PacketGPSAddWaypoint(hand == EnumHand.MAIN_HAND ? 0 : 1,
                            pendingWaypointX, pendingWaypointZ, selectedDimension, name, color));
            WaypointManager.addWaypoint(gps, pendingWaypointX, pendingWaypointZ,
                    selectedDimension, name, color);
        }
        closeWaypointDialog();
        needsTextureUpdate = true;
    }

    private void closeWaypointDialog() {
        showWaypointDialog = false;
        waypointNameField.setVisible(false);
        removeDialogButtons();
    }

    private void addDialogButtons() {
        int dy = height / 2 + 14;
        buttonList.add(new GuiButton(BTN_CONFIRM_WP, width / 2 - 82, dy, 78, 18, "\u00a7aConfirm"));
        buttonList.add(new GuiButton(BTN_CANCEL_WP, width / 2 + 4, dy, 78, 18, "\u00a7cCancel"));
    }

    private void removeDialogButtons() {
        buttonList.removeIf(b -> b.id == BTN_CONFIRM_WP || b.id == BTN_CANCEL_WP);
    }

    private void drawScaledTexture(int x, int y, int w, int h) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x, y + h, 0).tex(0, 1).endVertex();
        buf.pos(x + w, y + h, 0).tex(1, 1).endVertex();
        buf.pos(x + w, y, 0).tex(1, 0).endVertex();
        buf.pos(x, y, 0).tex(0, 0).endVertex();
        tess.draw();
    }

    private void drawHLine(int x1, int x2, int y, int color) {
        drawRect(x1, y, x2, y + 1, color);
    }

    private void drawColoredDiamond(int cx, int cy, int size, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        GlStateManager.disableTexture2D();
        GlStateManager.color(r, g, b, 1);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        buf.pos(cx, cy - size, 0).endVertex();
        buf.pos(cx + size, cy, 0).endVertex();
        buf.pos(cx, cy + size, 0).endVertex();
        buf.pos(cx - size, cy, 0).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    private void drawColoredRect(int x, int y, int w, int h, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        GlStateManager.disableTexture2D();
        GlStateManager.color(r, g, b, a);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buf.pos(x + w, y, 0).endVertex();
        buf.pos(x, y, 0).endVertex();
        buf.pos(x, y + h, 0).endVertex();
        buf.pos(x + w, y + h, 0).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    private static int blendColor(int bg, int fg, float alpha) {
        int br = (bg >> 16) & 0xFF, bgr = (bg >> 8) & 0xFF, bb = bg & 0xFF;
        int fr = (fg >> 16) & 0xFF, fgr = (fg >> 8) & 0xFF, fb = fg & 0xFF;
        int r = (int) (br * (1 - alpha) + fr * alpha);
        int g = (int) (bgr * (1 - alpha) + fgr * alpha);
        int b = (int) (bb * (1 - alpha) + fb * alpha);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        if (fullMapTexture != null) {
            fullMapTexture.deleteGlTexture();
            fullMapTexture = null;
        }
    }
}

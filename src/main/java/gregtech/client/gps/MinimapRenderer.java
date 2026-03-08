package gregtech.client.gps;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.common.gps.BeaconTracker;
import gregtech.common.gps.MapDataManager;
import gregtech.common.gps.UpgradeHandler;
import gregtech.common.gps.WaypointManager;
import gregtech.common.items.MetaItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.lwjgl.opengl.GL11;

import java.util.List;

@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public class MinimapRenderer {

    private static final int MAP_SIZE = 128;
    private static final int DISPLAY_SIZE = 96;
    private static final int MARGIN = 14;

    private static DynamicTexture mapTexture;
    private static ResourceLocation mapTextureLocation;
    private static int[] mapPixels;
    private static int tickCounter;
    /** Update every 2 ticks for smooth feel. */
    private static final int UPDATE_INTERVAL = 2;
    /** How many chunks of the texture update we do per tick-update cycle. */
    private static final int TEX_ROWS_PER_UPDATE = MAP_SIZE;
    /** Counter for periodic nearby-chunk invalidation so terrain changes show. */
    private static int rescanCounter;
    private static final int RESCAN_INTERVAL = 60;

    // Smoothed player position for interpolation
    private static double smoothX, smoothZ;

    private static void ensureTexture() {
        if (mapTexture == null) {
            mapTexture = new DynamicTexture(MAP_SIZE, MAP_SIZE);
            mapTextureLocation = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("gtgps_minimap", mapTexture);
            mapPixels = mapTexture.getTextureData();
        }
    }

    private static ItemStack findGPSInHotbar(EntityPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (MetaItems.GPS_DEVICE.isItemEqual(stack)) {
                IElectricItem electric = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electric != null && electric.getCharge() > 0) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        ItemStack gps = findGPSInHotbar(mc.player);
        if (gps.isEmpty()) return;

        tickCounter++;
        if (tickCounter % UPDATE_INTERVAL != 0) return;

        int radius = UpgradeHandler.getMapRadius(gps);
        int radiusChunks = radius / 16;
        int playerCX = mc.player.chunkCoordX;
        int playerCZ = mc.player.chunkCoordZ;

        // Periodically invalidate nearby chunks so terrain changes appear live
        rescanCounter++;
        if (rescanCounter >= RESCAN_INTERVAL) {
            rescanCounter = 0;
            MapDataManager.invalidateNearby(playerCX, playerCZ, 2);
        }

        boolean underground = UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.UNDERGROUND)
                && mc.player.posY < 60 && !mc.player.world.canSeeSky(mc.player.getPosition());
        boolean oreOverlay = UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.ORE_OVERLAY);

        MapDataManager.checkDimensionChange(mc.player.dimension);
        MapDataManager.updateSurroundingChunks(mc.world, playerCX, playerCZ, radiusChunks,
                underground, (int) mc.player.posY, oreOverlay);

        // Smooth position tracking
        smoothX = mc.player.posX;
        smoothZ = mc.player.posZ;

        updateMapTexture(mc, gps, radiusChunks, underground, oreOverlay);
    }

    private static void updateMapTexture(Minecraft mc, ItemStack gps, int radiusChunks,
                                         boolean underground, boolean oreOverlay) {
        ensureTexture();
        int halfMap = MAP_SIZE / 2;
        int totalBlocks = radiusChunks * 2 * 16;
        double pixelsPerBlock = (double) MAP_SIZE / totalBlocks;

        java.util.Arrays.fill(mapPixels, 0xFF0D1117);

        double cenX = smoothX;
        double cenZ = smoothZ;
        int playerCX = MathHelper.floor(cenX) >> 4;
        int playerCZ = MathHelper.floor(cenZ) >> 4;

        for (int cx = playerCX - radiusChunks; cx <= playerCX + radiusChunks; cx++) {
            for (int cz = playerCZ - radiusChunks; cz <= playerCZ + radiusChunks; cz++) {
                int[] colors = underground ?
                        MapDataManager.getUndergroundColors(cx, cz) :
                        MapDataManager.getChunkColors(cx, cz);
                if (colors == null) continue;

                int[] oreColors = oreOverlay ? MapDataManager.getOreOverlayColors(cx, cz) : null;

                for (int bx = 0; bx < 16; bx++) {
                    for (int bz = 0; bz < 16; bz++) {
                        double worldX = (cx * 16 + bx) - cenX;
                        double worldZ = (cz * 16 + bz) - cenZ;
                        int px = (int) (halfMap + worldX * pixelsPerBlock);
                        int py = (int) (halfMap + worldZ * pixelsPerBlock);
                        if (px >= 0 && px < MAP_SIZE && py >= 0 && py < MAP_SIZE) {
                            int color = colors[bz * 16 + bx];
                            if (oreColors != null && oreColors[bz * 16 + bx] != 0) {
                                color = blendColor(color, oreColors[bz * 16 + bx], 0.5f);
                            }

                            int absX = cx * 16 + bx;
                            int absZ = cz * 16 + bz;

                            // Chunk grid lines (subtle)
                            if (bx == 0 || bz == 0) {
                                color = blendColor(color, 0xFF888888, 0.2f);
                            }

                            // Ore vein grid lines (48-block, prominent) when ore overlay active
                            if (oreOverlay && (Math.floorMod(absX, 48) == 0 || Math.floorMod(absZ, 48) == 0)) {
                                color = blendColor(color, 0xFF1A8A6E, 0.55f);
                            }

                            // Vein center marker (small cross at center of each 48x48 cell)
                            if (oreOverlay && Math.floorMod(absX, 48) == 24 && Math.abs(Math.floorMod(absZ, 48) - 24) <= 1) {
                                color = blendColor(color, 0xFFFFAA00, 0.7f);
                            } else if (oreOverlay && Math.floorMod(absZ, 48) == 24 && Math.abs(Math.floorMod(absX, 48) - 24) <= 1) {
                                color = blendColor(color, 0xFFFFAA00, 0.7f);
                            }

                            mapPixels[py * MAP_SIZE + px] = color;
                        }
                    }
                }
            }
        }

        // Player crosshair
        mapPixels[halfMap * MAP_SIZE + halfMap] = 0xFFFFFFFF;
        if (halfMap - 1 >= 0) mapPixels[(halfMap - 1) * MAP_SIZE + halfMap] = 0xFFFFFFFF;
        if (halfMap + 1 < MAP_SIZE) mapPixels[(halfMap + 1) * MAP_SIZE + halfMap] = 0xFFFFFFFF;
        mapPixels[halfMap * MAP_SIZE + halfMap - 1] = 0xFFFFFFFF;
        mapPixels[halfMap * MAP_SIZE + halfMap + 1] = 0xFFFFFFFF;

        mapTexture.updateDynamicTexture();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.currentScreen != null) return;

        ItemStack gps = findGPSInHotbar(mc.player);
        if (gps.isEmpty() || mapTexture == null) return;

        ScaledResolution res = new ScaledResolution(mc);
        int screenW = res.getScaledWidth();
        int scaleFactor = res.getScaleFactor();
        int x = screenW - DISPLAY_SIZE - MARGIN;
        int y = MARGIN;
        int cx = x + DISPLAY_SIZE / 2;
        int cy = y + DISPLAY_SIZE / 2;
        int halfSize = DISPLAY_SIZE / 2;

        boolean northLocked = UpgradeHandler.isNorthLocked(gps);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Draw a fixed border/frame around the minimap - thin futuristic frame
        drawRect(x - 2, y - 2, x + DISPLAY_SIZE + 2, y + DISPLAY_SIZE + 2, 0xDD0D1117);
        drawRect(x - 1, y - 1, x + DISPLAY_SIZE + 1, y + DISPLAY_SIZE + 1, 0xFF1A8A6E);
        drawRect(x, y, x + DISPLAY_SIZE, y + DISPLAY_SIZE, 0xFF0D1117);

        // Use GL scissor to clip the map content to the minimap square
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        // Use mc.displayHeight directly for pixel-perfect scissor (avoids scaled rounding)
        GL11.glScissor(x * scaleFactor,
                mc.displayHeight - (y + DISPLAY_SIZE) * scaleFactor,
                DISPLAY_SIZE * scaleFactor, DISPLAY_SIZE * scaleFactor);

        // Rotate the map texture around its center if not north-locked
        GlStateManager.pushMatrix();
        if (!northLocked) {
            float yaw = mc.player.rotationYaw;
            GlStateManager.translate(cx, cy, 0);
            GlStateManager.rotate(-yaw, 0, 0, 1);
            GlStateManager.translate(-cx, -cy, 0);
        }

        mc.getTextureManager().bindTexture(mapTextureLocation);
        // Draw map larger than the display to fill corners during rotation
        int oversize = (int) (DISPLAY_SIZE * 0.42); // sqrt(2)-1 ~ 0.414
        drawTexturedRect(x - oversize, y - oversize,
                DISPLAY_SIZE + oversize * 2, DISPLAY_SIZE + oversize * 2);

        GlStateManager.popMatrix();

        // Render blips (not rotated by GL - we rotate coords manually)
        renderEntityBlips(mc, gps, x, y, northLocked);
        renderWaypointIndicators(mc, gps, x, y, northLocked);
        renderBeaconIndicators(mc, gps, x, y, northLocked);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        // Compass direction above minimap
        String dir = getCardinalDirection(mc.player.rotationYaw);
        mc.fontRenderer.drawStringWithShadow(dir,
                cx - mc.fontRenderer.getStringWidth(dir) / 2.0f,
                y - 11, 0xFF1A8A6E);

        // Coordinates below minimap
        String coords = String.format("%d, %d", MathHelper.floor(mc.player.posX), MathHelper.floor(mc.player.posZ));
        mc.fontRenderer.drawStringWithShadow(coords,
                cx - mc.fontRenderer.getStringWidth(coords) / 2.0f,
                y + DISPLAY_SIZE + 4, 0xFF888888);

        // EU charge bar under coords
        IElectricItem electric = gps.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electric != null) {
            float pct = (float) electric.getCharge() / electric.getMaxCharge();
            int barWidth = DISPLAY_SIZE - 4;
            int barX = x + 2;
            int barY = y + DISPLAY_SIZE + 15;
            drawRect(barX, barY, barX + barWidth, barY + 3, 0xFF1A1A2E);
            int fillColor = pct > 0.3f ? 0xFF1A8A6E : 0xFFAA3333;
            drawRect(barX, barY, barX + (int) (barWidth * pct), barY + 3, fillColor);
        }
    }

    private static void renderEntityBlips(Minecraft mc, ItemStack gps, int x, int y, boolean northLocked) {
        boolean entityRadar = UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.ENTITY_RADAR);
        boolean playerRadar = UpgradeHandler.hasUpgrade(gps, UpgradeHandler.Upgrade.PLAYER_RADAR);
        if (!entityRadar && !playerRadar) return;

        int radius = UpgradeHandler.getMapRadius(gps);
        double scale = (double) DISPLAY_SIZE / (radius * 2);
        float yaw = northLocked ? 0 : mc.player.rotationYaw;

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == mc.player) continue;
            double dx = entity.posX - mc.player.posX;
            double dz = entity.posZ - mc.player.posZ;
            if (Math.abs(dx) > radius || Math.abs(dz) > radius) continue;

            int color = 0;
            if (entityRadar && entity instanceof IMob) {
                color = 0xFFFF4444;
            } else if (entityRadar && entity instanceof IAnimals) {
                color = 0xFF44FF44;
            } else if (playerRadar && entity instanceof EntityPlayer) {
                color = 0xFF44AAFF;
            }
            if (color == 0) continue;

            double rotDx = dx, rotDz = dz;
            if (!northLocked) {
                double rad = Math.toRadians(yaw);
                double cos = Math.cos(rad), sin = Math.sin(rad);
                rotDx = dx * cos + dz * sin;
                rotDz = -dx * sin + dz * cos;
            }

            int px = x + DISPLAY_SIZE / 2 + (int) (rotDx * scale);
            int py = y + DISPLAY_SIZE / 2 + (int) (rotDz * scale);
            if (px >= x && px < x + DISPLAY_SIZE && py >= y && py < y + DISPLAY_SIZE) {
                drawRect(px - 1, py - 1, px + 2, py + 2, color);
            }
        }
    }

    private static void renderWaypointIndicators(Minecraft mc, ItemStack gps, int x, int y, boolean northLocked) {
        NBTTagList waypoints = WaypointManager.getWaypoints(gps);
        int radius = UpgradeHandler.getMapRadius(gps);
        double scale = (double) DISPLAY_SIZE / (radius * 2);
        float yaw = northLocked ? 0 : mc.player.rotationYaw;

        for (int i = 0; i < waypoints.tagCount(); i++) {
            NBTTagCompound wp = waypoints.getCompoundTagAt(i);
            if (WaypointManager.getWaypointDim(wp) != mc.player.dimension) continue;

            double dx = WaypointManager.getWaypointX(wp) - mc.player.posX;
            double dz = WaypointManager.getWaypointZ(wp) - mc.player.posZ;

            double rotDx = dx, rotDz = dz;
            if (!northLocked) {
                double rad = Math.toRadians(yaw);
                double cos = Math.cos(rad), sin = Math.sin(rad);
                rotDx = dx * cos + dz * sin;
                rotDz = -dx * sin + dz * cos;
            }

            int px = x + DISPLAY_SIZE / 2 + (int) (rotDx * scale);
            int py = y + DISPLAY_SIZE / 2 + (int) (rotDz * scale);

            int color = WaypointManager.getWaypointColor(wp) | 0xFF000000;
            if (px >= x && px < x + DISPLAY_SIZE && py >= y && py < y + DISPLAY_SIZE) {
                drawDiamond(px, py, 3, color);
            } else {
                // Edge arrow pointing toward waypoint
                double angle = Math.atan2(rotDz, rotDx);
                int edgeX = x + DISPLAY_SIZE / 2 + (int) (Math.cos(angle) * (DISPLAY_SIZE / 2 - 5));
                int edgeY = y + DISPLAY_SIZE / 2 + (int) (Math.sin(angle) * (DISPLAY_SIZE / 2 - 5));
                edgeX = MathHelper.clamp(edgeX, x + 3, x + DISPLAY_SIZE - 3);
                edgeY = MathHelper.clamp(edgeY, y + 3, y + DISPLAY_SIZE - 3);
                drawTriangleArrow(edgeX, edgeY, (float) Math.toDegrees(angle), color);
            }
        }

        NBTTagCompound death = WaypointManager.getDeathPoint(gps);
        if (death != null && death.getInteger("dim") == mc.player.dimension) {
            double dx = death.getInteger("x") - mc.player.posX;
            double dz = death.getInteger("z") - mc.player.posZ;
            double rotDx = dx, rotDz = dz;
            if (!northLocked) {
                double rad = Math.toRadians(yaw);
                double cos = Math.cos(rad), sin = Math.sin(rad);
                rotDx = dx * cos + dz * sin;
                rotDz = -dx * sin + dz * cos;
            }
            int px = x + DISPLAY_SIZE / 2 + (int) (rotDx * scale);
            int py = y + DISPLAY_SIZE / 2 + (int) (rotDz * scale);
            px = MathHelper.clamp(px, x + 3, x + DISPLAY_SIZE - 3);
            py = MathHelper.clamp(py, y + 3, y + DISPLAY_SIZE - 3);
            drawSkull(px, py);
        }
    }

    private static void renderBeaconIndicators(Minecraft mc, ItemStack gps, int x, int y, boolean northLocked) {
        int radius = UpgradeHandler.getMapRadius(gps);
        List<BeaconTracker.BeaconData> beacons = BeaconTracker.findBeacons(
                mc.world, mc.player.getPosition(), radius);
        double scale = (double) DISPLAY_SIZE / (radius * 2);
        float yaw = northLocked ? 0 : mc.player.rotationYaw;

        for (BeaconTracker.BeaconData beacon : beacons) {
            double dx = beacon.pos.getX() - mc.player.posX;
            double dz = beacon.pos.getZ() - mc.player.posZ;
            double rotDx = dx, rotDz = dz;
            if (!northLocked) {
                double rad = Math.toRadians(yaw);
                double cos = Math.cos(rad), sin = Math.sin(rad);
                rotDx = dx * cos + dz * sin;
                rotDz = -dx * sin + dz * cos;
            }

            int px = x + DISPLAY_SIZE / 2 + (int) (rotDx * scale);
            int py = y + DISPLAY_SIZE / 2 + (int) (rotDz * scale);
            px = MathHelper.clamp(px, x + 3, x + DISPLAY_SIZE - 3);
            py = MathHelper.clamp(py, y + 3, y + DISPLAY_SIZE - 3);

            drawRect(px - 2, py - 2, px + 3, py + 3, 0xFF00FFFF);
        }
    }

    private static void drawTexturedRect(int x, int y, int width, int height) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x, y + height, 0).tex(0, 1).endVertex();
        buf.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        buf.pos(x + width, y, 0).tex(1, 0).endVertex();
        buf.pos(x, y, 0).tex(0, 0).endVertex();
        tess.draw();
    }

    private static void drawRect(int x1, int y1, int x2, int y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        GlStateManager.disableTexture2D();
        GlStateManager.color(r, g, b, a);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buf.pos(x2, y1, 0).endVertex();
        buf.pos(x1, y1, 0).endVertex();
        buf.pos(x1, y2, 0).endVertex();
        buf.pos(x2, y2, 0).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    private static void drawDiamond(int cx, int cy, int size, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        GlStateManager.disableTexture2D();
        GlStateManager.color(r, g, b, a);
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

    private static void drawTriangleArrow(int cx, int cy, float angle, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        double rad = Math.toRadians(angle);
        int size = 4;

        GlStateManager.disableTexture2D();
        GlStateManager.color(r, g, b, a);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        buf.pos(cx + Math.cos(rad) * size, cy + Math.sin(rad) * size, 0).endVertex();
        buf.pos(cx + Math.cos(rad + 2.4) * size * 0.6, cy + Math.sin(rad + 2.4) * size * 0.6, 0).endVertex();
        buf.pos(cx + Math.cos(rad - 2.4) * size * 0.6, cy + Math.sin(rad - 2.4) * size * 0.6, 0).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    private static void drawSkull(int cx, int cy) {
        drawRect(cx - 3, cy - 3, cx + 4, cy + 4, 0xFFAA0000);
        drawRect(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFFFFFFF);
        drawRect(cx - 1, cy - 1, cx, cy, 0xFF000000);
        drawRect(cx + 1, cy - 1, cx + 2, cy, 0xFF000000);
        drawRect(cx, cy + 1, cx + 1, cy + 2, 0xFF000000);
    }

    private static int blendColor(int bg, int fg, float alpha) {
        int br = (bg >> 16) & 0xFF, bgr = (bg >> 8) & 0xFF, bb = bg & 0xFF;
        int fr = (fg >> 16) & 0xFF, fgr = (fg >> 8) & 0xFF, fb = fg & 0xFF;
        int r = (int) (br * (1 - alpha) + fr * alpha);
        int g = (int) (bgr * (1 - alpha) + fgr * alpha);
        int b = (int) (bb * (1 - alpha) + fb * alpha);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static String getCardinalDirection(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw >= 337.5 || yaw < 22.5) return "S";
        if (yaw < 67.5) return "SW";
        if (yaw < 112.5) return "W";
        if (yaw < 157.5) return "NW";
        if (yaw < 202.5) return "N";
        if (yaw < 247.5) return "NE";
        if (yaw < 292.5) return "E";
        return "SE";
    }
}

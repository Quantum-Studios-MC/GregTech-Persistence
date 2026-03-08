package gregtech.client;

import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.client.utils.formula.MaterialColorUtil;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders rich tooltips for material items (ores, ingots, dusts, etc.)
 * with a colored border matching the material color, similar to
 * {@link MachineTooltipRenderer} for machines.
 * <p>
 * All formula coloring and composition info is added as regular tooltip text
 * by {@link gregtech.client.utils.formula.FormulaTooltipHandler} - this class
 * only handles the visual border and text drawing. No raw GL overlays.
 */
@SideOnly(Side.CLIENT)
public class MaterialTooltipRenderer {

    private static final int BG_COLOR = 0xF0100010;
    private static final int MAX_BODY_WIDTH = 210;
    private static final int Z_LEVEL = 300;
    private static final int PADDING = 4;
    private static final int SCREEN_EDGE_PAD = 4;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        Material material = extractMaterial(stack);
        if (material == null) return;

        event.setCanceled(true);
        renderTooltip(event.getLines(), event.getX(), event.getY(),
                event.getScreenWidth(), event.getScreenHeight(),
                event.getFontRenderer(), material);
    }

    private static void renderTooltip(List<String> lines, int mouseX, int mouseY,
                                      int screenWidth, int screenHeight,
                                      FontRenderer font, Material material) {
        if (lines.isEmpty()) return;

        // Compute material-tinted border colors
        int rawColor = material.getMaterialRGB();
        int r = (rawColor >> 16) & 0xFF;
        int g = (rawColor >> 8) & 0xFF;
        int b = rawColor & 0xFF;
        float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
        hsb[1] = Math.min(1.0f, hsb[1] * 1.2f);
        hsb[2] = Math.max(0.45f, Math.min(1.0f, hsb[2] * 1.1f + 0.05f));
        int boosted = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0x00FFFFFF;
        int borderStart = 0xC0000000 | boosted;
        int borderEnd = 0xC0000000 | ((boosted & 0xFEFEFE) >> 1);

        // Layout: wrap body text
        String title = lines.get(0);
        List<String> bodyWrapped = wrapBody(lines, font);

        int titleW = font.getStringWidth(title);
        int maxW = titleW;
        for (String s : bodyWrapped) {
            int w = font.getStringWidth(s);
            if (w > maxW) maxW = w;
        }

        int tooltipWidth = maxW + PADDING;
        int tooltipHeight = 10;
        if (!bodyWrapped.isEmpty()) {
            tooltipHeight += 4 + bodyWrapped.size() * 10;
        }

        // Position - clamp to screen edges
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        if (tooltipX + tooltipWidth + PADDING + SCREEN_EDGE_PAD > screenWidth) {
            tooltipX = mouseX - tooltipWidth - 16;
        }
        if (tooltipX < SCREEN_EDGE_PAD) tooltipX = SCREEN_EDGE_PAD;
        if (tooltipY + tooltipHeight + PADDING + SCREEN_EDGE_PAD > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - PADDING - SCREEN_EDGE_PAD;
        }
        if (tooltipY < SCREEN_EDGE_PAD) tooltipY = SCREEN_EDGE_PAD;

        // Draw
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        int left = tooltipX - PADDING;
        int top = tooltipY - PADDING;
        int right = tooltipX + tooltipWidth + PADDING;
        int bottom = tooltipY + tooltipHeight + PADDING;

        // Background
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, top - 1, right + 1, top, BG_COLOR, BG_COLOR);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, bottom, right + 1, bottom + 1, BG_COLOR, BG_COLOR);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, top, right + 1, bottom, BG_COLOR, BG_COLOR);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 2, top, left - 1, bottom, BG_COLOR, BG_COLOR);
        GuiUtils.drawGradientRect(Z_LEVEL, right + 1, top, right + 2, bottom, BG_COLOR, BG_COLOR);

        // Colored border
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, top, left, bottom, borderStart, borderEnd);
        GuiUtils.drawGradientRect(Z_LEVEL, right, top, right + 1, bottom, borderStart, borderEnd);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, top - 1, right + 1, top, borderStart, borderStart);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, bottom, right + 1, bottom + 1, borderEnd, borderEnd);

        // Text
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, Z_LEVEL);

        int titleColor = MaterialColorUtil.getTooltipColor(material);
        font.drawStringWithShadow(title, tooltipX, tooltipY, titleColor);

        if (!bodyWrapped.isEmpty()) {
            int sepY = tooltipY + 11;
            GuiUtils.drawGradientRect(0, left + 2, sepY, right - 2, sepY + 1, borderStart, borderEnd);

            int textY = tooltipY + 14;
            for (String line : bodyWrapped) {
                font.drawStringWithShadow(line, tooltipX, textY, 0xFFAAAAAA);
                textY += 10;
            }
        }

        GlStateManager.popMatrix();

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
    }

    @Nullable
    private static Material extractMaterial(ItemStack stack) {
        Material mat = MetaPrefixItem.tryGetMaterial(stack);
        if (mat != null) return mat;
        UnificationEntry entry = OreDictUnifier.getUnificationEntry(stack);
        if (entry != null && entry.material != null) return entry.material;
        return null;
    }

    private static List<String> wrapBody(List<String> lines, FontRenderer font) {
        List<String> bodyWrapped = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            wrapLine(font, lines.get(i), MAX_BODY_WIDTH, bodyWrapped);
        }
        return bodyWrapped;
    }

    private static void wrapLine(FontRenderer font, String line, int maxWidth, List<String> output) {
        if (font.getStringWidth(line) <= maxWidth) {
            output.add(line);
            return;
        }

        StringBuilder current = new StringBuilder();
        StringBuilder fmt = new StringBuilder();
        for (String word : line.split(" ")) {
            String test = current.length() == 0 ? fmt.toString() + word : current + " " + word;
            if (font.getStringWidth(test) > maxWidth && current.length() > 0) {
                output.add(current.toString());
                current = new StringBuilder(fmt.toString());
                current.append(word);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(word);
            }

            for (int i = 0; i < word.length() - 1; i++) {
                if (word.charAt(i) == '\u00a7') {
                    char code = word.charAt(i + 1);
                    if (code == 'r' || code == 'R') {
                        fmt.setLength(0);
                    } else {
                        fmt.append('\u00a7').append(code);
                    }
                    i++;
                }
            }
        }
        if (current.length() > 0) {
            output.add(current.toString());
        }
    }
}

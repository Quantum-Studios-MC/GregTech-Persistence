package gregtech.client;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MachineTooltipRenderer {

    private static final int BG_COLOR = 0xF0100010;
    private static final int MAX_BODY_WIDTH = 210;
    private static final int Z_LEVEL = 300;
    private static final int ICON_SIZE = 12;
    private static final int ICON_PAD = 2;

    private static final int[] TIER_COLORS = {
            0x555555, 0xAAAAAA, 0x55FFFF, 0xFFAA00,
            0xAA00AA, 0x5555FF, 0xFF55FF, 0xFF5555,
            0x00AAAA, 0xAA0000, 0x00AA00, 0x006600,
            0xFFFF55, 0x5555FF, 0xFF5555,
    };

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        MetaTileEntity mte = GTUtility.getMetaTileEntity(stack);
        if (mte == null) return;

        event.setCanceled(true);
        renderTooltip(stack, event.getLines(), event.getX(), event.getY(),
                event.getScreenWidth(), event.getScreenHeight(),
                event.getFontRenderer(), mte);
    }

    private static void renderTooltip(ItemStack stack, List<String> lines, int mouseX, int mouseY,
                                      int screenWidth, int screenHeight,
                                      FontRenderer font, MetaTileEntity mte) {
        if (lines.isEmpty()) return;

        int tier = mte instanceof ITieredMetaTileEntity t ? t.getTier() : -1;
        int rawColor = tier >= 0 && tier < TIER_COLORS.length ? TIER_COLORS[tier] : 0x808080;
        int borderStart = 0xC0000000 | rawColor;
        int borderEnd = 0xC0000000 | ((rawColor & 0xFEFEFE) >> 1);

        String title = lines.get(0);
        String tierLabel = tier >= 0 ? GTValues.VNF[tier] : null;

        List<String> bodyWrapped = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            wrapLine(font, lines.get(i), MAX_BODY_WIDTH, bodyWrapped);
        }

        int iconSpace = ICON_SIZE + ICON_PAD;
        int titleW = font.getStringWidth(title) + iconSpace;
        int tierLabelW = tierLabel != null ? font.getStringWidth(tierLabel) : 0;
        int headerW = tierLabel != null ? titleW + 8 + tierLabelW : titleW;

        int maxW = headerW;
        for (String s : bodyWrapped) {
            int w = font.getStringWidth(s);
            if (w > maxW) maxW = w;
        }

        int tooltipWidth = maxW + 4;
        int tooltipHeight = 10;
        if (!bodyWrapped.isEmpty()) {
            tooltipHeight += 4 + bodyWrapped.size() * 10;
        }

        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        if (tooltipX + tooltipWidth + 8 > screenWidth) tooltipX = mouseX - tooltipWidth - 16;
        if (tooltipX < 4) tooltipX = 4;
        if (tooltipY + tooltipHeight + 8 > screenHeight) tooltipY = screenHeight - tooltipHeight - 8;
        if (tooltipY < 4) tooltipY = 4;

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        int left = tooltipX - 4;
        int top = tooltipY - 4;
        int right = tooltipX + tooltipWidth + 4;
        int bottom = tooltipY + tooltipHeight + 4;

        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, top - 1, right + 1, top, BG_COLOR, BG_COLOR);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, bottom, right + 1, bottom + 1, BG_COLOR, BG_COLOR);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, top, right + 1, bottom, BG_COLOR, BG_COLOR);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 2, top, left - 1, bottom, BG_COLOR, BG_COLOR);
        GuiUtils.drawGradientRect(Z_LEVEL, right + 1, top, right + 2, bottom, BG_COLOR, BG_COLOR);

        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, top, left, bottom, borderStart, borderEnd);
        GuiUtils.drawGradientRect(Z_LEVEL, right, top, right + 1, bottom, borderStart, borderEnd);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, top - 1, right + 1, top, borderStart, borderStart);
        GuiUtils.drawGradientRect(Z_LEVEL, left - 1, bottom, right + 1, bottom + 1, borderEnd, borderEnd);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, Z_LEVEL);

        int iconY = tooltipY + (8 - ICON_SIZE) / 2;
        renderMachineIcon(stack, tooltipX, iconY);

        font.drawStringWithShadow(title, tooltipX + iconSpace, tooltipY, 0xFFFFFFFF);
        if (tierLabel != null) {
            font.drawStringWithShadow(tierLabel, tooltipX + tooltipWidth - tierLabelW, tooltipY, 0xFFFFFFFF);
        }

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

    private static void renderMachineIcon(ItemStack stack, int x, int y) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderItem = mc.getRenderItem();
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        float scale = ICON_SIZE / 16.0f;
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, scale);
        renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
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

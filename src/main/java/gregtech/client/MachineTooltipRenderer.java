package gregtech.client;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MachineTooltipRenderer {

    private static final int PADDING_H = 5;
    private static final int PADDING_V = 4;
    private static final int LINE_SPACING = 11;
    private static final int HEADER_HEIGHT = 14;
    private static final int SEPARATOR_HEIGHT = 4;
    private static final int MAX_LINE_WIDTH = 220;
    private static final int BORDER_WIDTH = 1;

    private static final int[] TIER_COLORS = {
            0xFF555555,
            0xFFAAAAAA,
            0xFF55FFFF,
            0xFFFFAA00,
            0xFFAA00AA,
            0xFF5555FF,
            0xFFFF55FF,
            0xFFFF5555,
            0xFF00AAAA,
            0xFFAA0000,
            0xFF00AA00,
            0xFF006600,
            0xFFFFFF55,
            0xFF5555FF,
            0xFFFF5555,
    };

    private static final int BG_COLOR = 0xF0100010;
    private static final int BG_HEADER = 0xF0180020;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        MetaTileEntity mte = GTUtility.getMetaTileEntity(stack);
        if (mte == null) return;

        event.setCanceled(true);
        renderMachineTooltip(event.getLines(), event.getX(), event.getY(),
                event.getScreenWidth(), event.getScreenHeight(),
                event.getFontRenderer(), mte);
    }

    private static void renderMachineTooltip(List<String> lines, int mouseX, int mouseY,
                                             int screenWidth, int screenHeight,
                                             FontRenderer font, MetaTileEntity mte) {
        if (lines.isEmpty()) return;

        int tier = mte instanceof ITieredMetaTileEntity tiered ? tiered.getTier() : -1;
        int borderColor = tier >= 0 && tier < TIER_COLORS.length ? TIER_COLORS[tier] : 0xFF808080;
        int borderColorDark = darken(borderColor, 0.6f);

        List<String> wrappedLines = new ArrayList<>();
        for (String line : lines) {
            wrapLine(font, line, MAX_LINE_WIDTH, wrappedLines);
        }

        int maxWidth = 0;
        for (String line : wrappedLines) {
            int w = font.getStringWidth(line);
            if (w > maxWidth) maxWidth = w;
        }
        maxWidth = Math.min(maxWidth, MAX_LINE_WIDTH);

        int tooltipWidth = maxWidth + PADDING_H * 2;
        int bodyHeight = (wrappedLines.size() - 1) * LINE_SPACING;
        int tooltipHeight = HEADER_HEIGHT + SEPARATOR_HEIGHT + bodyHeight + PADDING_V * 2;

        int x = mouseX + 12;
        int y = mouseY - 12;

        if (x + tooltipWidth > screenWidth) {
            x = mouseX - tooltipWidth - 4;
        }
        if (x < 4) x = 4;

        if (y + tooltipHeight > screenHeight) {
            y = screenHeight - tooltipHeight - 4;
        }
        if (y < 4) y = 4;

        GlStateManager.pushMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        float zLevel = 300.0f;
        GlStateManager.translate(0, 0, zLevel);

        Gui.drawRect(x - BORDER_WIDTH, y - BORDER_WIDTH,
                x + tooltipWidth + BORDER_WIDTH, y + tooltipHeight + BORDER_WIDTH, borderColor);

        Gui.drawRect(x, y, x + tooltipWidth, y + HEADER_HEIGHT, BG_HEADER);

        Gui.drawRect(x, y + HEADER_HEIGHT, x + tooltipWidth, y + tooltipHeight, BG_COLOR);

        drawGradientLine(x, y + HEADER_HEIGHT, x + tooltipWidth, borderColor, borderColorDark);

        if (!wrappedLines.isEmpty()) {
            font.drawStringWithShadow(wrappedLines.get(0), x + PADDING_H, y + 3, 0xFFFFFF);
        }

        if (tier >= 0) {
            String tierLabel = GTValues.VNF[tier];
            int tierWidth = font.getStringWidth(tierLabel);
            font.drawStringWithShadow(tierLabel, x + tooltipWidth - tierWidth - PADDING_H, y + 3, 0xFFFFFF);
        }

        int textY = y + HEADER_HEIGHT + SEPARATOR_HEIGHT + PADDING_V;
        for (int i = 1; i < wrappedLines.size(); i++) {
            String line = wrappedLines.get(i);
            font.drawStringWithShadow(line, x + PADDING_H, textY, 0xFFCCCCCC);
            textY += LINE_SPACING;
        }

        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawGradientLine(int x1, int y, int x2, int colorLeft, int colorRight) {
        Gui.drawRect(x1, y, x2, y + 1, colorLeft);
    }

    private static int darken(int argbColor, float factor) {
        int a = (argbColor >> 24) & 0xFF;
        int r = (int) (((argbColor >> 16) & 0xFF) * factor);
        int g = (int) (((argbColor >> 8) & 0xFF) * factor);
        int b = (int) ((argbColor & 0xFF) * factor);
        return (a << 24) | (MathHelper.clamp(r, 0, 255) << 16) |
                (MathHelper.clamp(g, 0, 255) << 8) | MathHelper.clamp(b, 0, 255);
    }

    private static void wrapLine(FontRenderer font, String line, int maxWidth, List<String> output) {
        if (font.getStringWidth(line) <= maxWidth) {
            output.add(line);
            return;
        }

        StringBuilder currentLine = new StringBuilder();
        StringBuilder formatting = new StringBuilder();
        String[] words = line.split(" ");

        for (String word : words) {
            String test = currentLine.length() == 0 ?
                    formatting + word : currentLine + " " + word;
            if (font.getStringWidth(test) > maxWidth && currentLine.length() > 0) {
                output.add(currentLine.toString());
                currentLine = new StringBuilder(formatting.toString());
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }

            String stripped = net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(word);
            if (stripped != null && !stripped.equals(word)) {
                formatting.setLength(0);
                for (int i = 0; i < word.length() - 1; i++) {
                    if (word.charAt(i) == '\u00a7') {
                        formatting.append(word.charAt(i));
                        formatting.append(word.charAt(i + 1));
                        i++;
                    }
                }
            }
        }

        if (currentLine.length() > 0) {
            output.add(currentLine.toString());
        }
    }
}

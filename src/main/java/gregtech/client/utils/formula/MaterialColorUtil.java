package gregtech.client.utils.formula;

import gregtech.api.unification.material.Material;

import net.minecraft.util.text.TextFormatting;

public final class MaterialColorUtil {

    private static final int[][] MC_COLORS = {
            { 0, 0, 0 }, { 0, 0, 170 }, { 0, 170, 0 }, { 0, 170, 170 },
            { 170, 0, 0 }, { 170, 0, 170 }, { 255, 170, 0 }, { 170, 170, 170 },
            { 85, 85, 85 }, { 85, 85, 255 }, { 85, 255, 85 }, { 85, 255, 255 },
            { 255, 85, 85 }, { 255, 85, 255 }, { 255, 255, 85 }, { 255, 255, 255 }
    };

    private static final TextFormatting[] MC_FMTS = {
            TextFormatting.BLACK, TextFormatting.DARK_BLUE, TextFormatting.DARK_GREEN,
            TextFormatting.DARK_AQUA, TextFormatting.DARK_RED, TextFormatting.DARK_PURPLE,
            TextFormatting.GOLD, TextFormatting.GRAY, TextFormatting.DARK_GRAY,
            TextFormatting.BLUE, TextFormatting.GREEN, TextFormatting.AQUA,
            TextFormatting.RED, TextFormatting.LIGHT_PURPLE, TextFormatting.YELLOW,
            TextFormatting.WHITE
    };

    private MaterialColorUtil() {}

    public static int getTooltipColor(Material material) {
        int rgb = material.getMaterialRGB();
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
        hsb[1] = Math.min(1.0f, hsb[1] * 1.3f);
        hsb[2] = Math.max(0.55f, Math.min(1.0f, hsb[2] * 1.2f + 0.1f));
        int boosted = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return 0xFF000000 | (boosted & 0x00FFFFFF);
    }

    public static int getDimColor(Material material) {
        int rgb = material.getMaterialRGB();
        int r = Math.max((int) (((rgb >> 16) & 0xFF) * 0.45f), 40);
        int g = Math.max((int) (((rgb >> 8) & 0xFF) * 0.45f), 40);
        int b = Math.max((int) ((rgb & 0xFF) * 0.45f), 40);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int getGlowColor(Material material) {
        int full = getTooltipColor(material);
        return (0x40 << 24) | (full & 0x00FFFFFF);
    }

    public static int getBorderColor(Material material) {
        int rgb = material.getMaterialRGB();
        int r = Math.min(255, (int) (((rgb >> 16) & 0xFF) * 0.7f + 30));
        int g = Math.min(255, (int) (((rgb >> 8) & 0xFF) * 0.7f + 30));
        int b = Math.min(255, (int) ((rgb & 0xFF) * 0.7f + 30));
        return (0x60 << 24) | (r << 16) | (g << 8) | b;
    }

    public static int lerpColor(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        return ((int) (a1 + (a2 - a1) * t) << 24) |
                ((int) (r1 + (r2 - r1) * t) << 16) |
                ((int) (g1 + (g2 - g1) * t) << 8) |
                (int) (b1 + (b2 - b1) * t);
    }

    public static TextFormatting nearestFormatting(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int best = 15;
        double bestDist = Double.MAX_VALUE;
        for (int i = 1; i < 16; i++) {
            double dr = r - MC_COLORS[i][0];
            double dg = g - MC_COLORS[i][1];
            double db = b - MC_COLORS[i][2];
            double dist = dr * dr + dg * dg + db * db;
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return MC_FMTS[best];
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}

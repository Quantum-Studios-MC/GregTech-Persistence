package gregtech.client.utils.formula;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * Atom renderer that draws everything using the exact same pattern as vanilla
 * {@link Gui#drawRect}: POSITION vertex format with GlStateManager.color(),
 * using only proper (non-degenerate) quads. This is the only rendering path
 * that performance mods (Nothirium/Vintagium) reliably support.
 * <p>
 * All coordinates must be in screen space - no GL matrix transforms.
 */
public final class AtomRenderer {

    private static final int SEG_HI = 32;
    private static final int SEG_LO = 12;
    private static final float TWO_PI = (float) (Math.PI * 2.0);
    /** Reference node radius at zoom=1 - used for LOD thickness scaling. */
    private static final float NODE_R_REF = 40.0f;

    private AtomRenderer() {}

    // ── high-level API ───────────────────────────────────────────────────

    /**
     * Draws an atom with adaptive level-of-detail based on screen-space radius.
     * <ul>
     * <li>{@code r < 5}: filled disc only (dot)</li>
     * <li>{@code r < 12}: filled disc + outline ring</li>
     * <li>{@code r < 25}: disc + ring + shell orbits (no particles)</li>
     * <li>{@code r >= 25}: full detail with nucleus, electrons, orbits</li>
     * </ul>
     */
    public static void drawAtom(float cx, float cy, float r, int color, long protons, long neutrons,
                                float animPhase) {
        // LOD 0: tiny dot - just a filled circle
        if (r < 5) {
            int seg = Math.max(8, (int) (r * 3));
            fillCircle(cx, cy, r, withAlpha(color, 0xC0), seg);
            return;
        }

        int seg = r < 15 ? SEG_LO : SEG_HI;

        // Always: filled body + outline ring
        fillCircle(cx, cy, r * 0.9f, withAlpha(color, 0x70), seg);
        float ringThick = Math.max(1.5f, 2.0f * r / NODE_R_REF);
        drawCircleRing(cx, cy, r, ringThick, withAlpha(color, 0xB0), seg);

        // LOD 1: small - no internal detail
        if (r < 12) return;

        int shells = protons <= 2 ? 1 : protons <= 10 ? 2 : protons <= 18 ? 3 : protons <= 36 ? 4 : 5;

        // LOD 2: medium - shell orbit rings only, no particles
        if (r < 25) {
            for (int s = 1; s <= shells; s++) {
                float shellR = r * (0.25f + 0.12f * s);
                int shellAlpha = Math.max(0x25, 0x50 - s * 8);
                drawCircleRing(cx, cy, shellR, 1.0f, withAlpha(color, shellAlpha), SEG_LO);
            }
            return;
        }

        // LOD 3: full detail - nucleus, orbit rings, electron dots
        if (protons > 0) {
            drawNucleus(cx, cy, r * 0.3f, protons, neutrons, color, animPhase);
        }

        for (int s = 1; s <= shells; s++) {
            float shellR = r * (0.25f + 0.12f * s);
            int shellAlpha = Math.max(0x30, 0x60 - s * 8);
            drawCircleRing(cx, cy, shellR, 1.0f, withAlpha(color, shellAlpha), SEG_HI);
            int electrons = electronsInShell(s, protons);
            float dotR = Math.max(1.5f, r * 0.05f * (1.0f - s * 0.08f));
            for (int e = 0; e < electrons; e++) {
                float angle = TWO_PI * e / electrons + animPhase * TWO_PI + s * 1.2f;
                float ex = cx + (float) Math.cos(angle) * shellR;
                float ey = cy + (float) Math.sin(angle) * shellR;
                fillCircle(ex, ey, dotR, withAlpha(color, 0xDD), SEG_LO);
            }
        }
    }

    private static void drawNucleus(float cx, float cy, float r, long protons, long neutrons, int color,
                                    float animPhase) {
        long total = Math.min(protons + neutrons, 40);
        float spacing = Math.max(r / (float) Math.sqrt(total) * 1.4f, 1.5f);
        int placed = 0;
        for (int ring = 0; placed < total; ring++) {
            if (ring == 0) {
                int c = placed % 2 == 0 ? withAlpha(color, 0x90) : withAlpha(color, 0x55);
                fillCircle(cx, cy, spacing * 0.4f, c, SEG_LO);
                placed++;
            } else {
                int count = Math.min(ring * 6, (int) (total - placed));
                for (int i = 0; i < count && placed < total; i++) {
                    float angle = TWO_PI * i / count + animPhase * 0.3f + ring * 0.5f;
                    float px = cx + (float) Math.cos(angle) * spacing * ring;
                    float py = cy + (float) Math.sin(angle) * spacing * ring;
                    boolean isProton = placed < protons;
                    int c = isProton ? withAlpha(color, 0x90) : withAlpha(color, 0x50);
                    fillCircle(px, py, spacing * 0.35f, c, SEG_LO);
                    placed++;
                }
            }
        }
    }

    private static int electronsInShell(int shell, long protons) {
        int[] maxPerShell = { 2, 8, 18, 32, 32 };
        long remaining = protons;
        for (int s = 1; s < shell; s++) {
            if (s - 1 < maxPerShell.length) remaining -= maxPerShell[s - 1];
        }
        if (remaining <= 0) return 0;
        int max = shell - 1 < maxPerShell.length ? maxPerShell[shell - 1] : 32;
        return (int) Math.min(remaining, max);
    }

    // ── primitives using VANILLA drawRect pattern ────────────────────────
    // Each call: color() → begin(7, POSITION) → 4 distinct vertices → draw()
    // This exactly matches Gui.drawRect which is known to work.

    /**
     * Draws a filled circle as a series of horizontal rect slices (scanlines).
     * Each slice is a proper quad with 4 distinct vertices.
     */
    public static void fillCircle(float cx, float cy, float r, int color, int slices) {
        if (r < 0.5f) return;
        float a = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, a);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Draw circle as horizontal slices from top to bottom
        float step = (2 * r) / slices;
        buf.begin(7, DefaultVertexFormats.POSITION);
        for (int i = 0; i < slices; i++) {
            float y1 = cy - r + i * step;
            float y2 = cy - r + (i + 1) * step;
            // half-width at y1 and y2
            float dy1 = y1 - cy;
            float dy2 = y2 - cy;
            float hw1 = (float) Math.sqrt(Math.max(0, r * r - dy1 * dy1));
            float hw2 = (float) Math.sqrt(Math.max(0, r * r - dy2 * dy2));
            // proper quad: 4 distinct vertices (trapezoid slice)
            buf.pos(cx - hw1, y1, 0.0).endVertex();
            buf.pos(cx - hw2, y2, 0.0).endVertex();
            buf.pos(cx + hw2, y2, 0.0).endVertex();
            buf.pos(cx + hw1, y1, 0.0).endVertex();
        }
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Draws a circle ring as a series of annulus quads (4 distinct vertices each).
     */
    public static void drawCircleRing(float cx, float cy, float r, float thickness, int color, int segments) {
        if (r < 0.5f) return;
        float a = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;
        float inner = r - thickness * 0.5f;
        float outer = r + thickness * 0.5f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, a);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(7, DefaultVertexFormats.POSITION);
        for (int i = 0; i < segments; i++) {
            float a1 = TWO_PI * i / segments;
            float a2 = TWO_PI * (i + 1) / segments;
            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);
            // proper quad: inner1 → outer1 → outer2 → inner2
            buf.pos(cx + cos1 * inner, cy + sin1 * inner, 0.0).endVertex();
            buf.pos(cx + cos1 * outer, cy + sin1 * outer, 0.0).endVertex();
            buf.pos(cx + cos2 * outer, cy + sin2 * outer, 0.0).endVertex();
            buf.pos(cx + cos2 * inner, cy + sin2 * inner, 0.0).endVertex();
        }
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Draws a gradient bond between two nodes. Two quads (one per half).
     * Enforces a minimum thickness of 1.5 screen pixels so bonds remain
     * visible when zoomed out.
     */
    public static void drawGradientBond(float x1, float y1, float x2, float y2,
                                        float r1, float r2, float thickness,
                                        int color1, int color2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        // Only skip if atoms are literally overlapping (gap < 1 pixel inward)
        float gap = len - (r1 + r2);
        if (gap < 1.0f) return;

        // Enforce minimum visible thickness
        float effThick = Math.max(thickness, 1.5f);

        float nx = -dy / len * effThick * 0.5f;
        float ny = dx / len * effThick * 0.5f;

        float t1 = r1 / len;
        float t2 = 1.0f - r2 / len;
        float sx = x1 + dx * t1, sy = y1 + dy * t1;
        float ex = x1 + dx * t2, ey = y1 + dy * t2;
        float mx = (sx + ex) * 0.5f, my = (sy + ey) * 0.5f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        // first half
        float a1f = ((color1 >> 24) & 0xFF) / 255f;
        float r1f = ((color1 >> 16) & 0xFF) / 255f;
        float g1f = ((color1 >> 8) & 0xFF) / 255f;
        float b1f = (color1 & 0xFF) / 255f;
        GlStateManager.color(r1f, g1f, b1f, a1f);
        buf.begin(7, DefaultVertexFormats.POSITION);
        buf.pos(sx + nx, sy + ny, 0.0).endVertex();
        buf.pos(sx - nx, sy - ny, 0.0).endVertex();
        buf.pos(mx - nx, my - ny, 0.0).endVertex();
        buf.pos(mx + nx, my + ny, 0.0).endVertex();
        tess.draw();

        // second half
        float a2f = ((color2 >> 24) & 0xFF) / 255f;
        float r2f = ((color2 >> 16) & 0xFF) / 255f;
        float g2f = ((color2 >> 8) & 0xFF) / 255f;
        float b2f = (color2 & 0xFF) / 255f;
        GlStateManager.color(r2f, g2f, b2f, a2f);
        buf.begin(7, DefaultVertexFormats.POSITION);
        buf.pos(mx + nx, my + ny, 0.0).endVertex();
        buf.pos(mx - nx, my - ny, 0.0).endVertex();
        buf.pos(ex - nx, ey - ny, 0.0).endVertex();
        buf.pos(ex + nx, ey + ny, 0.0).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    // ── batch stubs (kept for API compat, now no-ops) ────────────────────

    public static void beginBatch() {
        // no-op: each primitive does its own begin/draw
    }

    public static void endBatch() {
        // no-op
    }

    // ── colour utility ───────────────────────────────────────────────────

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}

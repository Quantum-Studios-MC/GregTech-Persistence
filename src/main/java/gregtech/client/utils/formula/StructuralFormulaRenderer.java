package gregtech.client.utils.formula;

import gregtech.api.unification.Element;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public final class StructuralFormulaRenderer {

    private static final float NODE_R = 7.0f;
    private static final float BOND_T = 2.0f;

    private StructuralFormulaRenderer() {}

    public static void draw(FontRenderer fr, Material material, float cx, float cy, float scale) {
        List<SNode> nodes = new ArrayList<>();
        List<SBond> bonds = new ArrayList<>();
        buildStructure(material, nodes, bonds);
        if (nodes.isEmpty()) return;
        layout(nodes, bonds);

        // Compute centroid so we can centre the structure at (cx, cy)
        float avgX = 0, avgY = 0;
        for (SNode n : nodes) {
            avgX += n.x;
            avgY += n.y;
        }
        avgX /= nodes.size();
        avgY /= nodes.size();

        // All coordinates are transformed to screen space manually
        // (performance mods bypass GL modelview for BufferBuilder geometry).

        AtomRenderer.beginBatch();

        for (SBond b : bonds) {
            int c1 = MaterialColorUtil.getTooltipColor(b.a.material);
            int c2 = MaterialColorUtil.getTooltipColor(b.b.material);
            float sx1 = cx + (b.a.x - avgX) * scale;
            float sy1 = cy + (b.a.y - avgY) * scale;
            float sx2 = cx + (b.b.x - avgX) * scale;
            float sy2 = cy + (b.b.y - avgY) * scale;
            AtomRenderer.drawGradientBond(sx1, sy1, sx2, sy2,
                    NODE_R * scale, NODE_R * scale, BOND_T * b.order * scale, c1, c2);
        }

        for (SNode n : nodes) {
            int color = MaterialColorUtil.getTooltipColor(n.material);
            float sx = cx + (n.x - avgX) * scale;
            float sy = cy + (n.y - avgY) * scale;
            AtomRenderer.fillCircle(sx, sy, NODE_R * scale, color, 24);
        }

        AtomRenderer.endBatch();

        // text labels (FontRenderer handles its own pipeline, matrix is fine)
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        for (SNode n : nodes) {
            String sym = getSymbol(n.material);
            if (sym != null && !sym.isEmpty()) {
                float sx = cx + (n.x - avgX) * scale;
                float sy = cy + (n.y - avgY) * scale;
                GlStateManager.pushMatrix();
                GlStateManager.translate(sx, sy, 0);
                GlStateManager.scale(scale, scale, 1);
                int sw = fr.getStringWidth(sym);
                fr.drawStringWithShadow(sym, -sw / 2f, -fr.FONT_HEIGHT / 2f, 0xFFFFFFFF);
                GlStateManager.popMatrix();
            }
        }
    }

    private static float bondLength(SNode a, SNode b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static String getSymbol(Material mat) {
        Element el = mat.getElement();
        if (el != null) return el.getSymbol();
        return mat.getChemicalFormula();
    }

    private static void buildStructure(Material material, List<SNode> nodes, List<SBond> bonds) {
        ImmutableList<MaterialStack> comps = material.getMaterialComponents();
        if (comps.isEmpty()) {
            nodes.add(new SNode(material, 0, 0));
            return;
        }
        SNode center = new SNode(comps.get(0).material, 0, 0);
        nodes.add(center);
        for (int i = 1; i < comps.size(); i++) {
            for (int j = 0; j < comps.get(i).amount; j++) {
                SNode outer = new SNode(comps.get(i).material, 0, 0);
                nodes.add(outer);
                bonds.add(new SBond(center, outer, inferBondOrder(comps.get(i).material)));
            }
        }
        for (int j = 1; j < comps.get(0).amount; j++) {
            SNode extra = new SNode(comps.get(0).material, 0, 0);
            nodes.add(extra);
            bonds.add(new SBond(center, extra, 1));
        }
    }

    private static int inferBondOrder(Material mat) {
        Element el = mat.getElement();
        if (el == null) return 1;
        long p = el.getProtons();
        if (p == 8 || p == 16) return 2;
        if (p == 7 && el.getNeutrons() == 7) return 3;
        return 1;
    }

    private static void layout(List<SNode> nodes, List<SBond> bonds) {
        if (nodes.size() <= 1) return;
        float restLen = Math.max(36.0f, Math.min(64.0f, 120.0f / (float) Math.sqrt(nodes.size())));
        float angleStep = (float) (2 * Math.PI / (nodes.size() - 1));
        for (int i = 1; i < nodes.size(); i++) {
            float angle = angleStep * (i - 1);
            nodes.get(i).x = (float) Math.cos(angle) * restLen;
            nodes.get(i).y = (float) Math.sin(angle) * restLen;
        }
        for (int iter = 0; iter < 150; iter++) {
            float t = 1.0f - (float) iter / 150;
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    SNode a = nodes.get(i), b = nodes.get(j);
                    float dx = b.x - a.x, dy = b.y - a.y;
                    float d = Math.max((float) Math.sqrt(dx * dx + dy * dy), 1f);
                    float f = 2000f / (d * d) * t;
                    a.x -= dx / d * f;
                    a.y -= dy / d * f;
                    b.x += dx / d * f;
                    b.y += dy / d * f;
                }
            }
            for (SBond bond : bonds) {
                float dx = bond.b.x - bond.a.x, dy = bond.b.y - bond.a.y;
                float d = Math.max((float) Math.sqrt(dx * dx + dy * dy), 1f);
                float f = (d - restLen) * 0.1f * t;
                bond.a.x += dx / d * f;
                bond.a.y -= dy / d * f;
                bond.b.x -= dx / d * f;
                bond.b.y += dy / d * f;
            }
        }
    }

    static final class SNode {

        final Material material;
        float x, y;

        SNode(Material m, float x, float y) {
            this.material = m;
            this.x = x;
            this.y = y;
        }
    }

    static final class SBond {

        final SNode a, b;
        final int order;

        SBond(SNode a, SNode b, int order) {
            this.a = a;
            this.b = b;
            this.order = order;
        }
    }
}

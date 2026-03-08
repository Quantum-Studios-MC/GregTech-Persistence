package gregtech.client.utils.formula;

import gregtech.api.unification.Element;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.client.utils.formula.MaterialCompositionHelper.ComponentEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

public class MoleculeViewerScreen extends GuiScreen {

    private static final float NODE_R = 40.0f;
    private static final float REST_LEN = 160.0f;
    private static final float BOND_T = 3.0f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 5.0f;
    private static final int LAYOUT_ITERS = 250;
    private static final long DOUBLE_CLICK_MS = 350;
    private static final int SIDE_W = 180;
    private static final int BG_COLOR = 0xFF0A0A12;
    private static final int PANEL_COLOR = 0xFF0F0F1A;
    private static final int DIVIDER_COLOR = 0xFF333355;

    private final Material rootMaterial;
    private Material currentMaterial;
    private final Deque<Material> navStack = new ArrayDeque<>();

    private final List<VAtom> atoms = new ArrayList<>();
    private final List<VBond> bonds = new ArrayList<>();

    private float camX, camY;
    private float zoom = 1.0f;
    private float tick;

    private boolean dragging;
    private int dragSX, dragSY;
    private float dragCX, dragCY;

    private long lastClickTime;
    private int lastClickMX, lastClickMY;

    @Nullable
    private VAtom hoveredAtom;

    public MoleculeViewerScreen(Material material) {
        this.rootMaterial = material;
        this.currentMaterial = material;
        rebuildGraph(material);
    }

    public static void open(Material material) {
        if (material != null) {
            Minecraft.getMinecraft().displayGuiScreen(new MoleculeViewerScreen(material));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        centerView();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void rebuildGraph(Material mat) {
        atoms.clear();
        bonds.clear();
        currentMaterial = mat;
        ImmutableList<MaterialStack> comps = mat.getMaterialComponents();
        if (comps.isEmpty()) {
            atoms.add(new VAtom(mat, 0, 0, 1));
            return;
        }
        Map<Material, VAtom> seen = new LinkedHashMap<>();
        for (MaterialStack stack : comps) {
            VAtom existing = seen.get(stack.material);
            if (existing != null) {
                existing.amount += stack.amount;
            } else {
                VAtom atom = new VAtom(stack.material, 0, 0, stack.amount);
                seen.put(stack.material, atom);
                atoms.add(atom);
            }
        }
        if (atoms.size() > 1) {
            VAtom central = atoms.get(0);
            for (int i = 1; i < atoms.size(); i++) {
                bonds.add(new VBond(central, atoms.get(i)));
            }
            for (int i = 1; i < atoms.size(); i++) {
                for (int j = i + 1; j < atoms.size(); j++) {
                    if (shouldBond(atoms.get(i).material, atoms.get(j).material)) {
                        bonds.add(new VBond(atoms.get(i), atoms.get(j)));
                    }
                }
            }
        }
        runLayout();
    }

    private boolean shouldBond(Material a, Material b) {
        Element ea = a.getElement();
        Element eb = b.getElement();
        if (ea == null || eb == null) return false;
        long pa = ea.getProtons(), pb = eb.getProtons();
        boolean aElectroneg = pa == 8 || pa == 9 || pa == 17 || pa == 7 || pa == 16;
        boolean bElectroneg = pb == 8 || pb == 9 || pb == 17 || pb == 7 || pb == 16;
        return aElectroneg != bElectroneg;
    }

    private void runLayout() {
        if (atoms.size() <= 1) return;
        int n = atoms.size();
        // Initial placement: distribute evenly around a circle scaled to atom count
        float initR = REST_LEN * (0.5f + 0.35f * n);
        for (int i = 0; i < n; i++) {
            float angle = (float) (2 * Math.PI * i / n);
            atoms.get(i).x = (float) Math.cos(angle) * initR;
            atoms.get(i).y = (float) Math.sin(angle) * initR;
        }
        for (int iter = 0; iter < LAYOUT_ITERS; iter++) {
            float t = 1.0f - (float) iter / LAYOUT_ITERS;
            // Repulsion between all pairs
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    VAtom a = atoms.get(i), b = atoms.get(j);
                    float dx = b.x - a.x, dy = b.y - a.y;
                    float d = Math.max((float) Math.sqrt(dx * dx + dy * dy), 1f);
                    // Stronger repulsion ensures atoms stay well-separated
                    float f = 60000f / (d * d) * t;
                    float fx = dx / d * f, fy = dy / d * f;
                    a.x -= fx;
                    a.y -= fy;
                    b.x += fx;
                    b.y += fy;
                }
            }
            // Spring attraction along bonds
            for (VBond bond : bonds) {
                float dx = bond.b.x - bond.a.x, dy = bond.b.y - bond.a.y;
                float d = Math.max((float) Math.sqrt(dx * dx + dy * dy), 1f);
                float f = (d - REST_LEN) * 0.1f * t;
                float fx = dx / d * f, fy = dy / d * f;
                bond.a.x += fx;
                bond.a.y += fy;
                bond.b.x -= fx;
                bond.b.y -= fy;
            }
        }
    }

    private void centerView() {
        if (atoms.isEmpty()) return;
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (VAtom a : atoms) {
            minX = Math.min(minX, a.x);
            maxX = Math.max(maxX, a.x);
            minY = Math.min(minY, a.y);
            maxY = Math.max(maxY, a.y);
        }
        camX = (minX + maxX) / 2f;
        camY = (minY + maxY) / 2f;
        float spanX = maxX - minX + NODE_R * 6;
        float spanY = maxY - minY + NODE_R * 6;
        int gw = width - SIDE_W;
        if (spanX > 0 && spanY > 0) {
            zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, Math.min(gw / spanX, height / spanY) * 0.75f));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick = (tick + 0.015f) % 1.0f;
        FontRenderer fr = mc.fontRenderer;
        int gw = width - SIDE_W;

        // Draw solid background first to prevent bleedthrough from world rendering
        this.drawDefaultBackground();
        Gui.drawRect(0, 0, width, height, BG_COLOR);

        drawGrid(gw);
        drawGraph(gw, mouseX, mouseY, fr);
        drawSidePanel(gw, fr);
        drawTopBar(fr);
        drawBottomHints(fr);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawGrid(int gw) {
        float cx = gw / 2f;
        float cy = height / 2f;
        int gridColor = 0x08FFFFFF;
        float step = 100 * zoom;
        if (step < 20) return;
        float offX = (cx - camX * zoom) % step;
        float offY = (cy - camY * zoom) % step;
        for (float x = offX; x < gw; x += step) {
            Gui.drawRect((int) x, 0, (int) x + 1, height, gridColor);
        }
        for (float y = offY; y < height; y += step) {
            Gui.drawRect(0, (int) y, gw, (int) y + 1, gridColor);
        }
    }

    /** Transform world X to screen X. */
    private float toScreenX(float worldX, float centerX) {
        return centerX + (worldX - camX) * zoom;
    }

    /** Transform world Y to screen Y. */
    private float toScreenY(float worldY, float centerY) {
        return centerY + (worldY - camY) * zoom;
    }

    private void drawGraph(int gw, int mouseX, int mouseY, FontRenderer fr) {
        float centerX = gw / 2f;
        float centerY = height / 2f;

        // hit-test in world space
        float wx = (mouseX - centerX) / zoom + camX;
        float wy = (mouseY - centerY) / zoom + camY;
        hoveredAtom = null;
        if (mouseX < gw) {
            for (VAtom atom : atoms) {
                float dx = atom.x - wx, dy = atom.y - wy;
                if (dx * dx + dy * dy <= NODE_R * NODE_R * 1.3f) {
                    hoveredAtom = atom;
                }
            }
        }

        // ── all geometry drawn in SCREEN SPACE (no GL matrix) ────────────
        // Performance mods bypass the GL modelview matrix for BufferBuilder,
        // so we must transform every coordinate ourselves.

        float zr = NODE_R * zoom;   // node radius in screen pixels
        float zt = Math.max(2.0f, BOND_T * zoom);   // bond thickness in screen pixels (min 2px)

        AtomRenderer.beginBatch();

        for (VBond bond : bonds) {
            int c1 = MaterialColorUtil.getTooltipColor(bond.a.material);
            int c2 = MaterialColorUtil.getTooltipColor(bond.b.material);
            AtomRenderer.drawGradientBond(
                    toScreenX(bond.a.x, centerX), toScreenY(bond.a.y, centerY),
                    toScreenX(bond.b.x, centerX), toScreenY(bond.b.y, centerY),
                    zr, zr, zt, c1, c2);
        }

        for (VAtom atom : atoms) {
            boolean hovered = atom == hoveredAtom;
            int color = MaterialColorUtil.getTooltipColor(atom.material);
            long protons = 0, neutrons = 0;
            Element el = atom.material.getElement();
            if (el != null) {
                protons = el.getProtons();
                neutrons = el.getNeutrons();
            }
            float sx = toScreenX(atom.x, centerX);
            float sy = toScreenY(atom.y, centerY);
            if (hovered) {
                AtomRenderer.drawCircleRing(sx, sy, (NODE_R + 5) * zoom, 2.5f * zoom,
                        AtomRenderer.withAlpha(color, 0xAA), 48);
            }
            AtomRenderer.drawAtom(sx, sy, zr, color, protons, neutrons, tick);
        }

        AtomRenderer.endBatch();

        // ── text labels (screen space, FontRenderer handles its own pipeline) ──
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        for (VAtom atom : atoms) {
            float sx = toScreenX(atom.x, centerX);
            float sy = toScreenY(atom.y, centerY);

            // Skip labels if the node is too small to read them
            if (zr < 6) continue;

            Element el = atom.material.getElement();
            String sym = el != null ? el.getSymbol() : abbreviate(atom.material.getChemicalFormula());
            if (sym != null && !sym.isEmpty()) {
                float s = Math.max(0.5f, 1.4f * zoom);
                float tw = fr.getStringWidth(sym) * s;
                float th = fr.FONT_HEIGHT * s;
                GlStateManager.pushMatrix();
                GlStateManager.translate(sx - tw / 2f, sy - th / 2f, 0);
                GlStateManager.scale(s, s, 1);
                fr.drawStringWithShadow(sym, 0, 0, 0xFFFFFFFF);
                GlStateManager.popMatrix();
            }
            if (atom.amount > 1 && zr >= 10) {
                float amtS = Math.max(0.4f, 0.85f * zoom);
                GlStateManager.pushMatrix();
                GlStateManager.translate(sx + zr * 0.6f, sy + zr * 0.5f, 0);
                GlStateManager.scale(amtS, amtS, 1);
                fr.drawStringWithShadow("\u00d7" + atom.amount, 0, 0, 0xFFBBBBBB);
                GlStateManager.popMatrix();
            }
        }

        if (hoveredAtom != null) {
            drawHoverPanel(hoveredAtom, mouseX, mouseY, fr);
        }
    }

    private void drawHoverPanel(VAtom atom, int mx, int my, FontRenderer fr) {
        int px = mx + 12;
        int py = my - 10;
        int pw = 150;
        int ph = 10;

        List<String> lines = new ArrayList<>();
        int color = MaterialColorUtil.getTooltipColor(atom.material);
        lines.add(atom.material.getLocalizedName());
        Element el = atom.material.getElement();
        if (el != null) {
            lines.add("Z:" + el.getProtons() + "  N:" + el.getNeutrons() + "  A:" + el.getMass());
        }
        if (atom.amount > 1) {
            lines.add("Count: " + atom.amount);
        }
        if (!atom.material.getMaterialComponents().isEmpty()) {
            lines.add(I18n.format("gregtech.jei.formula.click_navigate"));
        }

        ph = lines.size() * 11 + 6;
        pw = 0;
        for (String line : lines) {
            pw = Math.max(pw, fr.getStringWidth(line) + 8);
        }
        if (px + pw > width - SIDE_W) px = mx - pw - 8;
        if (py + ph > height) py = height - ph;
        if (py < 0) py = 0;

        Gui.drawRect(px - 3, py - 3, px + pw + 3, py + ph + 1, 0xE0080818);
        Gui.drawRect(px - 3, py - 3, px + pw + 3, py - 2, MaterialColorUtil.getBorderColor(atom.material));

        GlStateManager.enableTexture2D();
        int ty = py;
        for (int i = 0; i < lines.size(); i++) {
            int c = i == 0 ? color : i == lines.size() - 1 &&
                    !atom.material.getMaterialComponents().isEmpty() ? 0xFF6688CC : 0xFFAAAAAA;
            fr.drawStringWithShadow(lines.get(i), px, ty, c);
            ty += 11;
        }
    }

    private void drawSidePanel(int gw, FontRenderer fr) {
        Gui.drawRect(gw, 0, width, height, PANEL_COLOR);
        Gui.drawRect(gw, 0, gw + 1, height, DIVIDER_COLOR);

        int tx = gw + 8;
        int ty = 10;
        int maxW = SIDE_W - 16;

        int nameColor = MaterialColorUtil.getTooltipColor(currentMaterial);
        fr.drawStringWithShadow(currentMaterial.getLocalizedName(), tx, ty, nameColor);
        ty += 12;

        String formula = currentMaterial.getChemicalFormula();
        if (formula != null && !formula.isEmpty()) {
            fr.drawStringWithShadow(formula, tx, ty, 0xFF999999);
            ty += 12;
        }

        if (!navStack.isEmpty()) {
            StringBuilder bc = new StringBuilder();
            Iterator<Material> it = navStack.descendingIterator();
            while (it.hasNext()) {
                if (bc.length() > 0) bc.append(" > ");
                bc.append(it.next().getLocalizedName());
            }
            bc.append(" > ").append(currentMaterial.getLocalizedName());
            String bcStr = bc.toString();
            if (fr.getStringWidth(bcStr) > maxW) {
                bcStr = "... > " + currentMaterial.getLocalizedName();
            }
            fr.drawStringWithShadow(bcStr, tx, ty, 0xFF666666);
            ty += 12;
        }

        ty += 4;
        Gui.drawRect(tx, ty, gw + SIDE_W - 8, ty + 1, DIVIDER_COLOR);
        ty += 6;

        fr.drawStringWithShadow(I18n.format("gregtech.jei.composition.title"), tx, ty, 0xFF8888CC);
        ty += 14;

        List<ComponentEntry> entries = MaterialCompositionHelper.computeComposition(currentMaterial);
        int barW = maxW - 4;

        for (ComponentEntry entry : entries) {
            if (ty > height - 30) break;
            int entryColor = MaterialColorUtil.getTooltipColor(entry.material);
            String name = entry.material.getLocalizedName();
            String pct = MaterialCompositionHelper.formatPercentage(entry.massFraction);
            int pctW = fr.getStringWidth(pct);

            fr.drawStringWithShadow(name, tx, ty, entryColor);
            fr.drawStringWithShadow(pct, gw + SIDE_W - 8 - pctW, ty, 0xFFBBBBBB);
            ty += 10;

            int filled = (int) (barW * entry.massFraction);
            Gui.drawRect(tx, ty, tx + barW, ty + 3, 0xFF1A1A2A);
            if (filled > 0) {
                Gui.drawRect(tx, ty, tx + filled, ty + 3, MaterialColorUtil.getDimColor(entry.material));
            }
            ty += 8;
        }

        ty += 4;
        long totalParts = 0;
        for (ComponentEntry e : entries) totalParts += e.stoichiometricAmount;
        if (totalParts > 0) {
            fr.drawStringWithShadow(
                    I18n.format("gregtech.jei.composition.total_parts", totalParts),
                    tx, ty, 0xFF666666);
        }
    }

    private void drawTopBar(FontRenderer fr) {
        Gui.drawRect(0, 0, width - SIDE_W, 20, 0xC0080818);
        fr.drawStringWithShadow(
                I18n.format("gregtech.jei.formula.viewing", currentMaterial.getLocalizedName()),
                6, 6, 0xFFCCCCCC);
    }

    private void drawBottomHints(FontRenderer fr) {
        String hint = I18n.format("gregtech.jei.formula.right_click_back") +
                "  |  Scroll: Zoom  |  Drag: Pan  |  Double-click: Explore";
        fr.drawStringWithShadow(hint, 6, height - 12, 0xFF444444);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            long now = System.currentTimeMillis();
            int dist = Math.abs(mouseX - lastClickMX) + Math.abs(mouseY - lastClickMY);
            if (now - lastClickTime < DOUBLE_CLICK_MS && dist < 8) {
                onDoubleClick();
                lastClickTime = 0;
                return;
            }
            lastClickTime = now;
            lastClickMX = mouseX;
            lastClickMY = mouseY;
            dragging = true;
            dragSX = mouseX;
            dragSY = mouseY;
            dragCX = camX;
            dragCY = camY;
        } else if (mouseButton == 1) {
            navigateBack();
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging && clickedMouseButton == 0) {
            camX = dragCX - (mouseX - dragSX) / zoom;
            camY = dragCY - (mouseY - dragSY) / zoom;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = false;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int mx = Mouse.getEventX() * width / mc.displayWidth;
            int my = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            int gw = width - SIDE_W;
            if (mx < gw) {
                float cx = gw / 2f;
                float cy = height / 2f;
                float wxBefore = (mx - cx) / zoom + camX;
                float wyBefore = (my - cy) / zoom + camY;
                float factor = scroll > 0 ? 1.15f : 1f / 1.15f;
                zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * factor));
                float wxAfter = (mx - cx) / zoom + camX;
                float wyAfter = (my - cy) / zoom + camY;
                camX += wxBefore - wxAfter;
                camY += wyBefore - wyAfter;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_E) {
            mc.displayGuiScreen(null);
        }
    }

    private void onDoubleClick() {
        if (hoveredAtom == null) return;
        if (!hoveredAtom.material.getMaterialComponents().isEmpty()) {
            navStack.push(hoveredAtom.material);
            rebuildGraph(hoveredAtom.material);
            centerView();
        } else {
            camX = hoveredAtom.x;
            camY = hoveredAtom.y;
            zoom = Math.min(MAX_ZOOM, zoom * 2);
        }
    }

    private void navigateBack() {
        if (!navStack.isEmpty()) {
            navStack.pop();
            Material target = navStack.isEmpty() ? rootMaterial : navStack.peek();
            rebuildGraph(target);
            centerView();
        }
    }

    private static String abbreviate(String s) {
        if (s == null) return "";
        return s.length() > 6 ? s.substring(0, 6) : s;
    }

    static final class VAtom {

        final Material material;
        float x, y;
        long amount;

        VAtom(Material m, float x, float y, long amount) {
            this.material = m;
            this.x = x;
            this.y = y;
            this.amount = amount;
        }
    }

    static final class VBond {

        final VAtom a, b;

        VBond(VAtom a, VAtom b) {
            this.a = a;
            this.b = b;
        }
    }
}

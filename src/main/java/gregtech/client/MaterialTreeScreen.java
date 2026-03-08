package gregtech.client;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.utils.formula.MaterialColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

/**
 * Standalone fullscreen GUI for viewing all ore prefix forms of a material.
 * Replaces the old JEI Material Tree category with a scrollable, grouped display.
 * <p>
 * Opened via Shift+T while hovering a material item.
 */
@SideOnly(Side.CLIENT)
public class MaterialTreeScreen extends GuiScreen {

    // ── Layout constants ────────────────────────────────────────────────
    private static final int BG_COLOR = 0xFF0A0A12;
    private static final int HEADER_BG = 0xFF0F0F1A;
    private static final int DIVIDER_COLOR = 0xFF333355;
    private static final int GROUP_HEADER_COLOR = 0xFF8888CC;
    private static final int INFO_COLOR = 0xFFAAAAAA;
    private static final int SLOT_BG = 0xFF1A1A2E;
    private static final int SLOT_BORDER = 0xFF333366;
    private static final int SLOT_HOVER = 0xFF444488;

    private static final int HEADER_HEIGHT = 36;
    private static final int MARGIN = 8;
    private static final int SLOT_SIZE = 18;      // slot visual size (item is 16x16 centered)
    private static final int CELL_H = 20;         // height of one item row (slot + padding)
    private static final int CELL_W = 20;         // width of one cell (icon only, no text)
    private static final int CELL_GAP_X = 2;      // horizontal gap between cells
    private static final int CELL_GAP_Y = 1;      // vertical gap between item rows
    private static final int GROUP_VGAP = 6;      // gap between groups
    private static final int GROUP_HEADER_H = 12;  // height of group label

    // ── Prefix groups ───────────────────────────────────────────────────
    // Explicit groups in display order. Each group is a (name, prefixNames[]) pair.
    private static final String[][] PREFIX_GROUPS = {
            { "Ingots", "ingot", "ingotHot", "ingotDouble", "ingotTriple", "ingotQuadruple", "ingotQuintuple",
                    "billet", "chunkGt", "nugget" },
            { "Dusts", "dust", "dustSmall", "dustTiny", "dustDiv72", "dustImpure", "dustPure" },
            { "Gems", "gem", "gemChipped", "gemFlawed", "gemFlawless", "gemExquisite", "gemLegendary" },
            { "Plates", "plate", "plateDouble", "plateTriple", "plateQuadruple", "plateQuintuple",
                    "plateDense", "plateTiny", "plateCurved", "foil", "casingSmall" },
            { "Wires & Cables",
                    "wireGtSingle", "wireGtDouble", "wireGtQuadruple", "wireGtOctal", "wireGtHex",
                    "cableGtSingle", "cableGtDouble", "cableGtQuadruple", "cableGtOctal", "cableGtHex",
                    "wireFine" },
            { "Pipes",
                    "pipeTinyFluid", "pipeSmallFluid", "pipeNormalFluid", "pipeLargeFluid", "pipeHugeFluid",
                    "pipeQuadrupleFluid", "pipeNonupleFluid",
                    "pipeTinyItem", "pipeSmallItem", "pipeNormalItem", "pipeLargeItem", "pipeHugeItem",
                    "pipeSmallRestrictive", "pipeNormalRestrictive", "pipeLargeRestrictive", "pipeHugeRestrictive" },
            { "Rods & Fasteners", "stick", "stickLong", "bolt", "screw" },
            { "Gears", "gear", "gearSmall" },
            { "Springs & Rings", "spring", "springSmall", "ring", "chain" },
            { "Other", "round", "lens", "rotor", "block", "frameGt",
                    "turbineBlade" },
            { "Ores", "ore", "oreGranite", "oreDiorite", "oreAndesite", "oreBlackgranite", "oreRedgranite",
                    "oreMarble", "oreBasalt", "oreSand", "oreRedSand", "oreNetherrack", "oreEndstone",
                    "orePoor" },
            { "Crushed", "crushed", "crushedPurified", "crushedCentrifuged",
                    "crushedTiny", "crushedPurifiedTiny", "crushedCentrifugedTiny" },
            { "Tool Heads", "toolHeadBuzzSaw", "toolHeadScrewdriver", "toolHeadDrill",
                    "toolHeadChainsaw", "toolHeadWrench" },
            { "Nuclear", "fuelRod", "fuelRodDepleted", "fuelRodHotDepleted",
                    "fuelPellet", "fuelPelletDepleted", "dustSpentFuel", "dustBredFuel" },
    };

    // ── State ───────────────────────────────────────────────────────────
    private final Material material;

    /** Each display group has a name + list of (OrePrefix, ItemStack) entries. */
    private final List<DisplayGroup> displayGroups = new ArrayList<>();
    @Nullable
    private FluidStack fluidStack;

    private float scrollOffset = 0;
    private float maxScroll = 0;

    @Nullable
    private ItemStack hoveredStack;
    private int hoveredSlotX, hoveredSlotY;

    // ── Construction ────────────────────────────────────────────────────

    public MaterialTreeScreen(Material material) {
        this.material = material;
        buildDisplayData();
    }

    public static void open(Material material) {
        if (material != null) {
            Minecraft.getMinecraft().displayGuiScreen(new MaterialTreeScreen(material));
        }
    }

    // ── Data building ───────────────────────────────────────────────────

    private void buildDisplayData() {
        displayGroups.clear();

        // Build a lookup of all OrePrefix by name
        Map<String, OrePrefix> prefixByName = new LinkedHashMap<>();
        for (OrePrefix p : OrePrefix.values()) {
            prefixByName.put(p.name, p);
        }

        // Track which prefixes have been placed in explicit groups
        Set<String> placed = new HashSet<>();

        // Process explicit groups
        for (String[] groupDef : PREFIX_GROUPS) {
            String groupName = groupDef[0];
            List<PrefixEntry> entries = new ArrayList<>();
            for (int i = 1; i < groupDef.length; i++) {
                OrePrefix prefix = prefixByName.get(groupDef[i]);
                if (prefix == null) continue;
                placed.add(groupDef[i]);
                ItemStack stack = OreDictUnifier.get(prefix, material);
                if (stack.isEmpty()) continue;
                entries.add(new PrefixEntry(prefix, stack));
            }
            if (!entries.isEmpty()) {
                displayGroups.add(new DisplayGroup(groupName, entries));
            }
        }

        // Catch-all: any OrePrefix not in an explicit group that produces an item
        List<PrefixEntry> otherEntries = new ArrayList<>();
        for (OrePrefix prefix : OrePrefix.values()) {
            if (placed.contains(prefix.name)) continue;
            if (!prefix.isUnificationEnabled) continue;
            if (prefix.isSelfReferencing) continue;
            ItemStack stack = OreDictUnifier.get(prefix, material);
            if (stack.isEmpty()) continue;
            otherEntries.add(new PrefixEntry(prefix, stack));
        }
        if (!otherEntries.isEmpty()) {
            displayGroups.add(new DisplayGroup("Uncategorized", otherEntries));
        }

        // Fluid
        if (material.hasProperty(PropertyKey.FLUID)) {
            Fluid fluid = material.getFluid();
            if (fluid != null) {
                fluidStack = new FluidStack(fluid, 1000);
            }
        }
    }

    // ── GuiScreen overrides ─────────────────────────────────────────────

    @Override
    public void initGui() {
        super.initGui();
        scrollOffset = 0;
        recalcMaxScroll();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Background
        Gui.drawRect(0, 0, width, height, BG_COLOR);

        // Header
        drawHeader();

        // Scrollable content area (clipped)
        int contentTop = HEADER_HEIGHT;
        int contentBottom = height;

        // Enable scissor to clip scrollable area
        double scale = mc.displayWidth / (double) width;
        int sx = 0;
        int sy = (int) ((height - contentBottom) * scale);
        int sw = mc.displayWidth;
        int sh = (int) ((contentBottom - contentTop) * scale);
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        org.lwjgl.opengl.GL11.glScissor(sx, sy, sw, sh);

        hoveredStack = null;
        drawContent(mouseX, mouseY, contentTop);

        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        // Scrollbar
        drawScrollbar(contentTop, contentBottom);

        // Tooltip (drawn last, on top of everything)
        if (hoveredStack != null && !hoveredStack.isEmpty()) {
            renderToolTip(hoveredStack, mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ── Header ──────────────────────────────────────────────────────────

    private void drawHeader() {
        FontRenderer fr = mc.fontRenderer;
        Gui.drawRect(0, 0, width, HEADER_HEIGHT, HEADER_BG);
        Gui.drawRect(0, HEADER_HEIGHT - 1, width, HEADER_HEIGHT, DIVIDER_COLOR);

        int tx = MARGIN;
        int ty = 6;

        // Material name (colored)
        int nameColor = MaterialColorUtil.getTooltipColor(material);
        String title = I18n.format("gregtech.material_tree.title", material.getLocalizedName());
        fr.drawStringWithShadow(title, tx, ty, nameColor);

        // Formula next to title
        String formula = material.getChemicalFormula();
        if (formula != null && !formula.isEmpty()) {
            int titleW = fr.getStringWidth(title);
            fr.drawStringWithShadow(" - " + formula, tx + titleW, ty, 0xFF999999);
        }

        ty += 14;

        // Compact properties line
        StringBuilder props = new StringBuilder();
        if (material.hasProperty(PropertyKey.BLAST)) {
            props.append(I18n.format("gregtech.material_tree.blast_temp", material.getBlastTemperature()));
            props.append("  ");
        }
        props.append(I18n.format("gregtech.jei.materials.average_mass", material.getMass()));
        fr.drawStringWithShadow(props.toString(), tx, ty, INFO_COLOR);
    }

    // ── Content ─────────────────────────────────────────────────────────

    private void drawContent(int mouseX, int mouseY, int contentTop) {
        FontRenderer fr = mc.fontRenderer;
        RenderItem itemRenderer = mc.getRenderItem();

        int y = contentTop + MARGIN - (int) scrollOffset;
        int areaLeft = MARGIN;
        int areaWidth = width - MARGIN * 2 - 8; // 8px for scrollbar

        int colsPerRow = Math.max(1, (areaWidth + CELL_GAP_X) / (CELL_W + CELL_GAP_X));

        // Fluid display (if present)
        if (fluidStack != null) {
            // Draw a fluid "slot"
            boolean fluidHovered = mouseX >= areaLeft && mouseX < areaLeft + SLOT_SIZE &&
                    mouseY >= y && mouseY < y + SLOT_SIZE &&
                    mouseY >= HEADER_HEIGHT;
            Gui.drawRect(areaLeft, y, areaLeft + SLOT_SIZE, y + SLOT_SIZE,
                    fluidHovered ? SLOT_HOVER : SLOT_BG);
            drawSlotBorder(areaLeft, y, fluidHovered ? 0xFF6666AA : SLOT_BORDER);
            int fluidColor = fluidStack.getFluid().getColor(fluidStack);
            Gui.drawRect(areaLeft + 1, y + 1, areaLeft + SLOT_SIZE - 1, y + SLOT_SIZE - 1,
                    fluidColor | 0xFF000000);
            y += CELL_H + GROUP_VGAP;
        }

        // Prefix groups
        for (DisplayGroup group : displayGroups) {
            // Group header
            fr.drawStringWithShadow(group.name, areaLeft, y, GROUP_HEADER_COLOR);
            y += GROUP_HEADER_H;

            // Items in multi-column list: each cell = [icon] [full name]
            int col = 0;
            for (PrefixEntry entry : group.entries) {
                int cellX = areaLeft + col * (CELL_W + CELL_GAP_X);
                int cellY = y;
                int slotX = cellX;
                int slotY = cellY + (CELL_H - SLOT_SIZE) / 2;

                // Hover detection over the whole cell
                boolean hovered = mouseX >= cellX && mouseX < cellX + CELL_W &&
                        mouseY >= cellY && mouseY < cellY + CELL_H &&
                        mouseY >= HEADER_HEIGHT;

                // Draw slot background
                Gui.drawRect(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE,
                        hovered ? SLOT_HOVER : SLOT_BG);
                drawSlotBorder(slotX, slotY, hovered ? 0xFF6666AA : SLOT_BORDER);

                // Render item
                GlStateManager.enableDepth();
                RenderHelper.enableGUIStandardItemLighting();
                itemRenderer.renderItemAndEffectIntoGUI(entry.stack, slotX + 1, slotY + 1);
                itemRenderer.renderItemOverlayIntoGUI(fr, entry.stack, slotX + 1, slotY + 1, null);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableDepth();

                // Track hovered
                if (hovered) {
                    hoveredStack = entry.stack;
                    hoveredSlotX = slotX;
                    hoveredSlotY = slotY;
                }

                col++;
                if (col >= colsPerRow) {
                    col = 0;
                    y += CELL_H + CELL_GAP_Y;
                }
            }

            // Finish any partial row
            if (col > 0) {
                y += CELL_H + CELL_GAP_Y;
            }

            y += GROUP_VGAP;
        }

        // Update max scroll
        int totalContentH = y + (int) scrollOffset - contentTop;
        int viewH = height - contentTop;
        maxScroll = Math.max(0, totalContentH - viewH + MARGIN);
    }

    // ── Scrollbar ───────────────────────────────────────────────────────

    private void drawScrollbar(int contentTop, int contentBottom) {
        if (maxScroll <= 0) return;
        int barX = width - 6;
        int barH = contentBottom - contentTop;
        // Background track
        Gui.drawRect(barX, contentTop, barX + 4, contentBottom, 0xFF111122);

        // Thumb
        float viewFraction = (float) barH / (barH + maxScroll);
        int thumbH = Math.max(20, (int) (barH * viewFraction));
        float scrollFraction = scrollOffset / maxScroll;
        int thumbY = contentTop + (int) (scrollFraction * (barH - thumbH));
        Gui.drawRect(barX, thumbY, barX + 4, thumbY + thumbH, 0xFF555588);
    }

    // ── Input handling ──────────────────────────────────────────────────

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            scrollOffset -= scroll * 0.5f;
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_E) {
            mc.displayGuiScreen(null);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private void recalcMaxScroll() {
        // Approximate max scroll based on content
        int y = MARGIN;
        if (fluidStack != null) {
            y += GROUP_HEADER_H + 2 + CELL_H + GROUP_VGAP;
        }
        int areaWidth = width - MARGIN * 2 - 8;
        int colsPerRow = Math.max(1, (areaWidth + CELL_GAP_X) / (CELL_W + CELL_GAP_X));
        for (DisplayGroup group : displayGroups) {
            y += GROUP_HEADER_H + 2;
            int rows = (group.entries.size() + colsPerRow - 1) / colsPerRow;
            y += rows * (CELL_H + CELL_GAP_Y);
            y += GROUP_VGAP;
        }
        int viewH = height - HEADER_HEIGHT;
        maxScroll = Math.max(0, y - viewH + MARGIN);
    }

    private static void drawSlotBorder(int x, int y, int color) {
        // Top
        Gui.drawRect(x, y, x + SLOT_SIZE, y + 1, color);
        // Bottom
        Gui.drawRect(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, color);
        // Left
        Gui.drawRect(x, y, x + 1, y + SLOT_SIZE, color);
        // Right
        Gui.drawRect(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, color);
    }



    // ── Data classes ────────────────────────────────────────────────────

    private static class DisplayGroup {

        final String name;
        final List<PrefixEntry> entries;

        DisplayGroup(String name, List<PrefixEntry> entries) {
            this.name = name;
            this.entries = entries;
        }
    }

    private static class PrefixEntry {

        final OrePrefix prefix;
        final ItemStack stack;

        PrefixEntry(OrePrefix prefix, ItemStack stack) {
            this.prefix = prefix;
            this.stack = stack;
        }
    }
}

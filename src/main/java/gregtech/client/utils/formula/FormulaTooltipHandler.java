package gregtech.client.utils.formula;

import gregtech.api.GTValues;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.client.utils.formula.MaterialCompositionHelper.ComponentEntry;
import gregtech.client.utils.formula.MaterialFormulaBuilder.FormulaData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * Handles material formula tooltip enrichment. All rendering is done through
 * regular tooltip text lines with formatting codes - no raw GL overlays.
 * <p>
 * Shift+N opens the molecule viewer screen for detailed exploration.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = GTValues.MODID)
public final class FormulaTooltipHandler {

    @Nullable
    private static Material activeMaterial;
    @Nullable
    private static FormulaData activeFormula;

    private static boolean shiftNDown;
    private static boolean shiftTDown;

    private FormulaTooltipHandler() {}

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().player == null) {
            reset();
            return;
        }

        boolean sn = Keyboard.isKeyDown(Keyboard.KEY_N) &&
                (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
        if (sn && !shiftNDown && activeMaterial != null) {
            MoleculeViewerScreen.open(activeMaterial);
        }
        shiftNDown = sn;

        boolean st = Keyboard.isKeyDown(Keyboard.KEY_T) &&
                (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
        if (st && !shiftTDown && activeMaterial != null) {
            gregtech.client.MaterialTreeScreen.open(activeMaterial);
        }
        shiftTDown = st;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        Material mat = extractMaterial(event.getItemStack());

        // Update tracking - set immediately, clear immediately
        if (mat != null) {
            if (mat != activeMaterial) {
                activeMaterial = mat;
                activeFormula = MaterialFormulaBuilder.build(mat);
            }
        } else {
            activeMaterial = null;
            activeFormula = null;
            return;
        }

        FormulaData data = activeFormula;
        if (data == null || data.isEmpty()) return;

        List<String> tips = event.getToolTip();
        String formulaText = data.getText();

        // Find the formula line in the tooltip
        int formulaLineIdx = -1;
        for (int i = 0; i < tips.size(); i++) {
            String stripped = TextFormatting.getTextWithoutFormattingCodes(tips.get(i));
            if (stripped != null && stripped.equals(formulaText)) {
                formulaLineIdx = i;
                break;
            }
        }

        if (formulaLineIdx < 0) return;

        // Replace the formula line with a colorized version
        String colorizedFormula = buildColorizedFormula(data);
        tips.set(formulaLineIdx, colorizedFormula);

        // Insert additional info below the formula
        int ins = formulaLineIdx + 1;
        tips.add(ins++, "\u00a78" + I18n.format("gregtech.jei.formula.shift_n_viewer", "Shift+N"));
        tips.add(ins++, "\u00a78" + I18n.format("gregtech.material_tree.shift_t_hint", "Shift+T"));

        // Add composition breakdown
        List<ComponentEntry> entries = MaterialCompositionHelper.computeComposition(mat);
        if (entries.size() > 1) {
            tips.add(ins++, "");
            tips.add(ins++, "\u00a78\u00a7n" + I18n.format("gregtech.jei.composition.title"));
            for (ComponentEntry entry : entries) {
                TextFormatting color = MaterialColorUtil.nearestFormatting(entry.material.getMaterialRGB());
                String name = entry.material.getLocalizedName();
                String pct = MaterialCompositionHelper.formatPercentage(entry.massFraction);
                tips.add(ins++, " " + color + "\u2022 " + name + " \u00a77" + pct);
            }
        }
    }

    /**
     * @return the currently tracked material, or null
     */
    @Nullable
    public static Material getActiveMaterial() {
        return activeMaterial;
    }

    /**
     * Build a colorized formula string where each element segment uses the
     * nearest Minecraft formatting color for its material.
     */
    private static String buildColorizedFormula(FormulaData data) {
        StringBuilder sb = new StringBuilder();
        Material lastMat = null;
        for (int i = 0; i < data.length(); i++) {
            Material charMat = data.getMaterialAt(i);
            if (charMat != lastMat) {
                if (charMat != null) {
                    sb.append(MaterialColorUtil.nearestFormatting(charMat.getMaterialRGB()));
                } else {
                    sb.append(TextFormatting.GRAY);
                }
                lastMat = charMat;
            }
            sb.append(data.getSequence().charAt(i));
        }
        return sb.toString();
    }

    @Nullable
    private static Material extractMaterial(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Material mat = MetaPrefixItem.tryGetMaterial(stack);
        if (mat != null) return mat;
        UnificationEntry entry = OreDictUnifier.getUnificationEntry(stack);
        if (entry != null && entry.material != null) return entry.material;
        return null;
    }

    private static void reset() {
        activeMaterial = null;
        activeFormula = null;
        shiftNDown = false;
        shiftTDown = false;
    }
}

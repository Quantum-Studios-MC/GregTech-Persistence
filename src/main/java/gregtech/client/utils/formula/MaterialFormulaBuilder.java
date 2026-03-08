package gregtech.client.utils.formula;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.SmallDigits;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

public final class MaterialFormulaBuilder {

    private MaterialFormulaBuilder() {}

    public static FormulaData build(@Nullable Material material) {
        if (material == null) return FormulaData.EMPTY;
        String formula = material.getChemicalFormula();
        if (formula == null || formula.isEmpty()) return FormulaData.EMPTY;
        ImmutableList<MaterialStack> components = material.getMaterialComponents();
        if (components.isEmpty()) {
            MaterialCharSequence.Builder b = new MaterialCharSequence.Builder(formula.length());
            b.append(formula, material);
            return new FormulaData(b.build(), material);
        }
        if (components.size() == 1 && components.get(0).amount == 1) {
            MaterialCharSequence.Builder b = new MaterialCharSequence.Builder(formula.length());
            appendMaterial(b, components.get(0).material);
            return new FormulaData(b.build(), material);
        }
        MaterialCharSequence.Builder b = new MaterialCharSequence.Builder(formula.length() + 16);
        for (MaterialStack stack : components) {
            appendStack(b, stack);
        }
        MaterialCharSequence seq = b.build();
        if (!seq.getText().equals(formula)) {
            MaterialCharSequence.Builder fb = new MaterialCharSequence.Builder(formula.length());
            fb.append(formula, material);
            return new FormulaData(fb.build(), material);
        }
        return new FormulaData(seq, material);
    }

    private static void appendStack(MaterialCharSequence.Builder b, MaterialStack stack) {
        Material mat = stack.material;
        String childFormula = mat.getChemicalFormula();
        if (childFormula.isEmpty()) {
            b.append('?', mat);
        } else if (mat.getMaterialComponents().size() > 1) {
            b.append('(', mat);
            appendMaterial(b, mat);
            b.append(')', mat);
        } else {
            appendMaterial(b, mat);
        }
        if (stack.amount > 1) {
            b.append(SmallDigits.toSmallDownNumbers(String.valueOf(stack.amount)), mat);
        }
    }

    private static void appendMaterial(MaterialCharSequence.Builder b, Material mat) {
        ImmutableList<MaterialStack> components = mat.getMaterialComponents();
        if (components.isEmpty()) {
            String formula = mat.getChemicalFormula();
            b.append(formula != null ? formula : "?", mat);
            return;
        }
        if (components.size() == 1 && components.get(0).amount == 1) {
            appendMaterial(b, components.get(0).material);
            return;
        }
        for (MaterialStack stack : components) {
            appendStack(b, stack);
        }
    }

    public static final class FormulaData {

        public static final FormulaData EMPTY = new FormulaData(
                new MaterialCharSequence.Builder(0).build(), null);

        private final MaterialCharSequence seq;
        @Nullable
        private final Material owner;

        FormulaData(MaterialCharSequence seq, @Nullable Material owner) {
            this.seq = seq;
            this.owner = owner;
        }

        public String getText() {
            return seq.getText();
        }

        @Nullable
        public Material getOwner() {
            return owner;
        }

        @Nullable
        public Material getMaterialAt(int i) {
            return seq.materialAt(i);
        }

        public int length() {
            return seq.length();
        }

        public boolean isEmpty() {
            return seq.length() == 0;
        }

        public MaterialCharSequence getSequence() {
            return seq;
        }
    }
}

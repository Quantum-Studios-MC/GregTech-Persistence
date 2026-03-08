package gregtech.client.utils.formula;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MaterialCompositionHelper {

    private MaterialCompositionHelper() {}

    public static List<ComponentEntry> computeComposition(Material material) {
        if (material == null) return Collections.emptyList();
        ImmutableList<MaterialStack> components = material.getMaterialComponents();
        if (components.isEmpty()) {
            return Collections.singletonList(new ComponentEntry(material, 1.0, 1));
        }
        double totalMass = 0;
        for (MaterialStack stack : components) {
            totalMass += effectiveMass(stack.material) * stack.amount;
        }
        if (totalMass <= 0) {
            List<ComponentEntry> entries = new ArrayList<>();
            double frac = 1.0 / components.size();
            for (MaterialStack stack : components) {
                entries.add(new ComponentEntry(stack.material, frac, stack.amount));
            }
            return Collections.unmodifiableList(entries);
        }
        List<ComponentEntry> entries = new ArrayList<>();
        for (MaterialStack stack : components) {
            double mass = effectiveMass(stack.material) * stack.amount;
            entries.add(new ComponentEntry(stack.material, mass / totalMass, stack.amount));
        }
        entries.sort((a, b) -> Double.compare(b.massFraction, a.massFraction));
        return Collections.unmodifiableList(entries);
    }

    private static double effectiveMass(Material mat) {
        if (mat.getElement() != null) return mat.getElement().getMass();
        ImmutableList<MaterialStack> components = mat.getMaterialComponents();
        if (components.isEmpty()) return Math.max(mat.getMass(), 1);
        double mass = 0;
        for (MaterialStack stack : components) {
            mass += effectiveMass(stack.material) * stack.amount;
        }
        return mass;
    }

    public static String formatPercentage(double fraction) {
        double pct = fraction * 100.0;
        if (pct >= 99.95) return "100%";
        if (pct < 0.05) return "<0.1%";
        if (pct >= 10.0) return String.format("%.1f%%", pct);
        return String.format("%.2f%%", pct);
    }

    public static final class ComponentEntry {

        public final Material material;
        public final double massFraction;
        public final long stoichiometricAmount;

        public ComponentEntry(Material material, double massFraction, long stoichiometricAmount) {
            this.material = material;
            this.massFraction = massFraction;
            this.stoichiometricAmount = stoichiometricAmount;
        }
    }
}

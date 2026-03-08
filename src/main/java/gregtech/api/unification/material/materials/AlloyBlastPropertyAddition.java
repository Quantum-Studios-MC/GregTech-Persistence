package gregtech.api.unification.material.materials;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.GCYMMaterialFlags;
import gregtech.api.unification.material.properties.AlloyBlastProperty;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.GCYMPropertyKey;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Automatically adds {@link AlloyBlastProperty} to all qualifying materials.
 * A material qualifies if it:
 * - Has BLAST property
 * - Has FLUID property
 * - Has 2+ material components (is an alloy)
 * - Does not have the DISABLE_ALLOY_PROPERTY flag
 * - Has at most 2 fluid-only components
 */
public final class AlloyBlastPropertyAddition {

    private AlloyBlastPropertyAddition() {}

    public static void init() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (!material.hasFlag(GCYMMaterialFlags.DISABLE_ALLOY_PROPERTY)) {
                addAlloyBlastProperty(material);
            }
        }
    }

    private static void addAlloyBlastProperty(@NotNull Material material) {
        final List<MaterialStack> components = material.getMaterialComponents();
        // ignore materials which are not alloys
        if (components.size() < 2) return;

        BlastProperty blastProperty = material.getProperty(PropertyKey.BLAST);
        if (blastProperty == null) return;

        if (!material.hasProperty(PropertyKey.FLUID)) return;

        // if there are more than 2 fluid-only components in the material, do not generate
        if (components.stream().filter(AlloyBlastPropertyAddition::isMaterialStackFluidOnly).limit(3).count() > 2) {
            return;
        }

        material.setProperty(GCYMPropertyKey.ALLOY_BLAST, new AlloyBlastProperty(material.getBlastTemperature()));
    }

    private static boolean isMaterialStackFluidOnly(@NotNull MaterialStack ms) {
        return !ms.material.hasProperty(PropertyKey.DUST) && ms.material.hasProperty(PropertyKey.FLUID);
    }
}

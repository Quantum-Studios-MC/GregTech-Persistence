package gregtech.api.unification.material.materials;

import gregtech.api.recipes.alloyblast.CustomAlloyBlastRecipeProducer;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.AlloyBlastProperty;
import gregtech.api.unification.material.properties.GCYMPropertyKey;

public final class GCYMMaterialFlagAddition {

    private GCYMMaterialFlagAddition() {}

    public static void register() {
        // Frames
        Materials.TungstenCarbide.addFlags(MaterialFlags.GENERATE_FRAME);
        Materials.Tungsten.addFlags(MaterialFlags.GENERATE_FRAME);
        Materials.Brass.addFlags(MaterialFlags.GENERATE_FRAME);

        // Small Gears
        Materials.TungstenCarbide.addFlags(MaterialFlags.GENERATE_SMALL_GEAR);

        // Long Rods
        Materials.Neutronium.addFlags(MaterialFlags.GENERATE_LONG_ROD);

        // Rotors
        Materials.Iridium.addFlags(MaterialFlags.GENERATE_RING, MaterialFlags.GENERATE_ROTOR);

        // Springs
        Materials.Neutronium.addFlags(MaterialFlags.GENERATE_SPRING);

        // Dense Plates
        Materials.Neutronium.addFlags(MaterialFlags.GENERATE_DENSE);

        // Foils
        Materials.Graphene.addFlags(MaterialFlags.GENERATE_FOIL);
    }

    /**
     * Called after AlloyBlastPropertyAddition.init() to override custom recipe producers.
     */
    public static void initLate() {
        // Alloy Blast Overriding
        AlloyBlastProperty property = Materials.NiobiumNitride.getProperty(GCYMPropertyKey.ALLOY_BLAST);
        if (property != null) {
            property.setRecipeProducer(new CustomAlloyBlastRecipeProducer(1, 11, -1));
        }

        property = Materials.IndiumTinBariumTitaniumCuprate.getProperty(GCYMPropertyKey.ALLOY_BLAST);
        if (property != null) {
            property.setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 16));
        }
    }
}

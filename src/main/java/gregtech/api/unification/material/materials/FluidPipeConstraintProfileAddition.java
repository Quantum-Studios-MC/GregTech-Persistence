package gregtech.api.unification.material.materials;

import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;

import static gregtech.api.unification.material.Materials.*;

/**
 * Adds handcrafted fluid transport constraints for each material pipe.
 *
 * This profile layer augments base fluid pipe properties with pH windows and
 * non-default containment flags for modern fluid hazards.
 */
public final class FluidPipeConstraintProfileAddition {

    private FluidPipeConstraintProfileAddition() {}

    public static void init() {
        // Entry level pipes
        profile(Wood, 6.0, 8.0, false, false, false, false, false, true);
        profile(TreatedWood, 5.5, 8.5, false, false, false, false, false, true);

        // Common metals
        profile(Copper, 4.5, 9.5, false, false, false, false, false, true);
        profile(Bronze, 3.5, 10.5, false, false, false, false, false, true);
        profile(TinAlloy, 4.0, 10.0, false, false, false, false, true, false);
        profile(Aluminium, 4.0, 10.5, false, false, false, false, false, true);
        profile(Lead, 4.0, 9.5, false, false, true, false, false, false);
        profile(Steel, 2.5, 11.5, false, true, true, false, true, true);

        // Reinforced industrial alloys
        profile(Potin, 3.0, 11.0, true, true, true, false, true, true);
        profile(Chrome, 1.5, 12.5, true, true, false, false, true, true);
        profile(Gold, 2.0, 12.0, true, true, true, false, true, true);
        profile(StainlessSteel, 1.0, 13.0, true, true, true, false, true, true);
        profile(NiobiumNitride, 1.5, 12.5, true, true, true, false, true, true);
        profile(Titanium, 1.0, 13.0, true, true, true, false, true, true);
        profile(VanadiumSteel, 1.0, 13.0, true, true, true, false, true, true);
        profile(TungstenSteel, 0.8, 13.2, true, true, true, true, true, true);
        profile(TungstenCarbide, 0.8, 13.2, true, true, true, false, true, true);
        profile(Europium, 1.0, 13.0, true, true, true, true, true, true);
        profile(Iridium, 0.8, 13.5, true, true, true, true, true, true);
        profile(Tungsten, 0.5, 13.5, true, true, true, true, true, true);

        // Advanced and endgame
        profile(Naquadah, 0.3, 13.7, true, true, true, true, true, true);
        profile(Duranium, 0.0, 14.0, true, true, true, true, true, true);
        profile(Neutronium, 0.0, 14.0, true, true, true, true, true, true);

        // High-tier polymers
        profile(Polyethylene, 5.0, 9.0, false, false, false, false, true, false);
        profile(Polytetrafluoroethylene, 0.0, 14.0, true, true, true, false, true, true);
        profile(Polybenzimidazole, 1.5, 12.5, true, true, true, false, true, true);
    }

    private static void profile(Material material, double minPH, double maxPH,
                                boolean acidProof, boolean corrosiveProof, boolean toxicProof,
                                boolean radioactiveProof, boolean flammableProof, boolean sludgeProof) {
        if (material == null || !material.hasProperty(PropertyKey.FLUID_PIPE)) return;

        FluidPipeProperties props = material.getProperty(PropertyKey.FLUID_PIPE);
        props.setPHRange(minPH, maxPH);
        props.setCanContain(FluidAttributes.ACID, acidProof);
        props.setCanContain(FluidAttributes.CORROSIVE, corrosiveProof);
        props.setCanContain(FluidAttributes.TOXIC, toxicProof);
        props.setCanContain(FluidAttributes.RADIOACTIVE, radioactiveProof);
        props.setCanContain(FluidAttributes.FLAMMABLE, flammableProof);
        props.setCanContain(FluidAttributes.SLUDGE, sludgeProof);
    }
}

package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidDataProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import static gregtech.api.unification.material.Materials.*;

/**
 * Assigns physical fluid data (viscosity, pH, density, etc.) to all materials
 * that have a fluid property. These values are used for:
 * <ul>
 * <li>Fluid tooltips in JEI and tanks</li>
 * <li>Pipe flow behavior (viscosity affects throughput, surface tension affects leak rate)</li>
 * </ul>
 * <p>
 * Values are based on real-world data where available, and reasonable
 * estimates for fictional/exotic materials.
 * <p>
 * Parameters: viscosity (cP), pH, heat capacity (J/(g·K)),
 * electrical conductivity (S/m), surface tension (mN/m), density (g/cm³)
 */
public final class FluidDataPropertyAddition {

    private FluidDataPropertyAddition() {}

    // ============================= Helper Methods =============================

    /** Set all fluid data properties directly. */
    private static void set(Material m, double viscosity, double pH, double heatCap,
                            double conductivity, double surfaceTension, double density) {
        m.setProperty(PropertyKey.FLUID_DATA,
                new FluidDataProperty(viscosity, pH, heatCap, conductivity, surfaceTension, density));
    }

    /** Molten metal - neutral pH. */
    private static void molten(Material m, double viscosity, double heatCap,
                               double conductivity, double surfaceTension, double density) {
        set(m, viscosity, 7.0, heatCap, conductivity, surfaceTension, density);
    }

    /** Gas - no surface tension, no conductivity. pH provided for reactive gases. */
    private static void gas(Material m, double viscosity, double pH, double heatCap, double density) {
        set(m, viscosity, pH, heatCap, 0.0, 0.0, density);
    }

    /** Inert gas - neutral pH, no surface tension, no conductivity. */
    private static void inertGas(Material m, double viscosity, double heatCap, double density) {
        set(m, viscosity, 7.0, heatCap, 0.0, 0.0, density);
    }

    /** Organic liquid - neutral pH, non-conducting. */
    private static void organic(Material m, double viscosity, double heatCap,
                                double surfaceTension, double density) {
        set(m, viscosity, 7.0, heatCap, 0.0, surfaceTension, density);
    }

    /** Fuel/petroleum - neutral pH, non-conducting. */
    private static void fuel(Material m, double viscosity, double heatCap,
                             double surfaceTension, double density) {
        set(m, viscosity, 7.0, heatCap, 0.0, surfaceTension, density);
    }

    /** Aqueous dye solution - water-based, neutral. */
    private static void dye(Material m) {
        set(m, 1.5, 7.0, 3.5, 0.01, 45.0, 1.05);
    }

    // ============================== Init Entry ================================

    public static void init() {
        initElementalMetals();
        initElementalGases();
        initFirstDegree();
        initSecondDegree();
        initOrganicChemistry();
        initUnknownComposition();
        initHigherDegree();
        initGCYM();
    }

    // ========================= Elemental Metals (molten) ======================

    private static void initElementalMetals() {
        // Material η(cP) Cp σ(S/m) γ(mN/m) ρ(g/cm³)
        molten(Aluminium, 1.3, 1.18, 3770000, 868, 2.375);
        molten(Americium, 3.0, 0.14, 1500000, 550, 11.7);
        molten(Antimony, 1.3, 0.21, 330000, 371, 6.53);
        molten(Arsenic, 1.0, 0.33, 34000, 370, 5.22);
        molten(Beryllium, 1.0, 2.40, 2500000, 1100, 1.69);
        molten(Bismuth, 1.6, 0.15, 7700, 378, 10.05);
        molten(Carbon, 0.5, 2.00, 30000, 100, 1.8);
        molten(Cerium, 2.9, 0.19, 760000, 706, 6.55);
        molten(Chrome, 0.7, 0.56, 7700000, 1590, 6.3);
        molten(Cobalt, 4.2, 0.56, 1500000, 1873, 7.75);
        molten(Copper, 3.36, 0.49, 4000000, 1285, 8.0);
        molten(Darmstadtium, 5.0, 0.13, 1000000, 1000, 34.8);
        molten(Europium, 2.5, 0.18, 1100000, 264, 4.87);
        molten(Gallium, 2.04, 0.37, 3700000, 718, 6.1);
        molten(Gold, 5.0, 0.149, 7800000, 1140, 17.31);
        molten(Indium, 1.7, 0.23, 1200000, 556, 7.02);
        molten(Iridium, 5.3, 0.131, 1200000, 2250, 20.0);
        molten(Iron, 5.5, 0.82, 710000, 1872, 7.0);
        molten(Lanthanum, 2.6, 0.195, 780000, 718, 5.94);
        molten(Lead, 2.65, 0.16, 1000000, 458, 10.66);
        molten(Lithium, 0.56, 4.38, 3800000, 398, 0.512);
        molten(Lutetium, 3.8, 0.154, 1800000, 940, 9.3);
        molten(Magnesium, 1.25, 1.36, 3700000, 559, 1.584);
        molten(Manganese, 5.9, 0.83, 690000, 1090, 5.95);
        set(Mercury, 1.526, 7.0, 0.14, 1040000, 480, 13.534);
        molten(Molybdenum, 3.0, 0.36, 1800000, 2250, 9.33);
        molten(Neodymium, 2.7, 0.19, 1600000, 687, 6.89);
        molten(Nickel, 4.9, 0.56, 1300000, 1778, 7.81);
        molten(Niobium, 2.0, 0.32, 2500000, 1900, 7.83);
        molten(Osmium, 4.4, 0.130, 850000, 2500, 20.0);
        molten(Palladium, 4.22, 0.244, 5300000, 1500, 10.38);
        molten(Platinum, 6.74, 0.159, 4600000, 1800, 19.77);
        molten(Plutonium239, 6.0, 0.14, 670000, 550, 16.6);
        molten(Plutonium241, 6.0, 0.14, 670000, 550, 16.6);
        molten(Potassium, 0.42, 0.75, 7400000, 110, 0.828);
        molten(Rhodium, 4.8, 0.243, 1600000, 2000, 10.7);
        molten(Ruthenium, 4.0, 0.24, 1400000, 2250, 10.65);
        molten(Samarium, 2.8, 0.197, 1060000, 680, 7.16);
        molten(Silicon, 0.8, 0.71, 1200000, 865, 2.57);
        molten(Silver, 3.88, 0.283, 6100000, 903, 9.32);
        molten(Tantalum, 7.1, 0.14, 570000, 2150, 15.0);
        molten(Thorium, 5.0, 0.118, 540000, 978, 10.6);
        molten(Tin, 1.85, 0.228, 2100000, 560, 6.99);
        molten(Titanium, 2.2, 0.69, 2400000, 1650, 4.11);
        molten(Tungsten, 8.0, 0.18, 1800000, 2500, 17.6);
        molten(Uranium, 5.5, 0.12, 340000, 1550, 17.3);
        molten(Uranium235, 5.5, 0.12, 340000, 1550, 17.3);
        molten(Vanadium, 3.5, 0.49, 5000000, 1950, 5.5);
        molten(Yttrium, 3.7, 0.30, 1800000, 871, 4.24);
        molten(Zinc, 3.85, 0.48, 2800000, 782, 6.57);
        // Fictional / exotic elements
        molten(Naquadah, 3.5, 0.15, 20000000, 1800, 24.0);
        molten(NaquadahEnriched, 3.2, 0.15, 25000000, 1850, 25.0);
        molten(Naquadria, 2.8, 0.16, 30000000, 1900, 26.0);
        molten(Neutronium, 1.0, 0.05, 500000000, 5000, 100.0);
        molten(Tritanium, 4.0, 0.20, 15000000, 2200, 18.0);
        molten(Duranium, 5.0, 0.18, 12000000, 2400, 20.0);
        molten(Trinium, 2.5, 0.22, 18000000, 2000, 15.0);
    }

    // ========================== Elemental Gases ===============================

    private static void initElementalGases() {
        // Material η(cP) pH Cp ρ(g/cm³)
        inertGas(Argon, 0.023, 0.52, 0.00164);
        inertGas(Deuterium, 0.013, 7.20, 0.000164);
        inertGas(Helium, 0.020, 5.19, 0.000164);
        inertGas(Helium3, 0.020, 5.19, 0.000123);
        inertGas(Hydrogen, 0.009, 14.3, 0.000082);
        inertGas(Krypton, 0.025, 0.248, 0.00343);
        inertGas(Neon, 0.032, 1.03, 0.000825);
        inertGas(Radon, 0.018, 0.094, 0.00911);
        inertGas(Tritium, 0.009, 10.0, 0.00024);
        inertGas(Xenon, 0.023, 0.158, 0.00539);
        // Reactive elemental gases
        gas(Chlorine, 0.013, 1.0, 0.48, 0.00295);
        gas(Fluorine, 0.024, 0.0, 0.82, 0.00155);
        gas(Nitrogen, 0.018, 7.0, 1.04, 0.00115);
        gas(Oxygen, 0.020, 7.0, 0.92, 0.00131);
    }

    // ===================== First Degree Materials =============================

    private static void initFirstDegree() {
        // --- Alloys (molten) ---
        // Material η(cP) Cp σ(S/m) γ(mN/m) ρ(g/cm³)
        molten(AnnealedCopper, 3.2, 0.49, 4200000, 1280, 8.0);
        molten(BatteryAlloy, 2.5, 0.16, 500000, 450, 10.2);
        molten(Brass, 3.0, 0.38, 3500000, 1050, 8.0);
        molten(Bronze, 3.8, 0.38, 2500000, 1100, 8.3);
        molten(Cupronickel, 4.5, 0.45, 2000000, 1400, 8.5);
        molten(Electrum, 4.5, 0.20, 6500000, 1020, 13.5);
        molten(Invar, 5.5, 0.50, 1000000, 1800, 8.0);
        molten(Kanthal, 4.0, 0.52, 720000, 1500, 7.1);
        molten(Magnalium, 1.2, 1.20, 3500000, 700, 2.0);
        molten(Nichrome, 5.0, 0.46, 900000, 1750, 8.2);
        molten(NiobiumNitride, 3.0, 0.35, 1000000, 1200, 8.4);
        molten(NiobiumTitanium, 2.5, 0.40, 2000000, 1700, 6.0);
        molten(SterlingSilver, 3.9, 0.28, 5800000, 900, 9.3);
        molten(RoseGold, 4.8, 0.17, 7000000, 1100, 15.0);
        molten(BlackBronze, 3.5, 0.35, 2000000, 1000, 8.5);
        molten(BismuthBronze, 3.0, 0.30, 1500000, 950, 8.8);
        molten(Ruridit, 5.0, 0.18, 1400000, 2100, 15.0);
        molten(SolderingAlloy, 2.0, 0.20, 1500000, 500, 8.5);
        molten(StainlessSteel, 6.5, 0.50, 1300000, 1500, 6.9);
        molten(Steel, 6.0, 0.46, 800000, 1800, 7.0);
        molten(TinAlloy, 2.0, 0.23, 2000000, 550, 7.0);
        molten(Ultimet, 5.5, 0.42, 900000, 1700, 8.5);
        molten(VanadiumGallium, 3.0, 0.40, 3000000, 1000, 6.0);
        molten(WroughtIron, 5.5, 0.82, 700000, 1870, 7.0);
        molten(Osmiridium, 5.0, 0.13, 1000000, 2350, 20.0);
        molten(ManganesePhosphide, 4.5, 0.50, 200000, 800, 5.5);
        molten(MagnesiumDiboride, 2.0, 0.80, 3500000, 900, 2.6);
        molten(RTMAlloy, 5.0, 0.20, 1500000, 1500, 12.0);

        // --- Superconductor compounds (molten) ---
        molten(YttriumBariumCuprate, 5.0, 0.30, 10000, 800, 6.3);
        molten(MercuryBariumCalciumCuprate, 5.0, 0.30, 100000, 500, 7.0);
        molten(UraniumTriplatinum, 6.5, 0.15, 3000000, 1700, 18.0);
        molten(SamariumIronArsenicOxide, 5.0, 0.35, 5000, 500, 6.5);
        molten(IndiumTinBariumTitaniumCuprate, 4.5, 0.30, 50000, 500, 6.8);
        molten(UraniumRhodiumDinaquadide, 5.5, 0.15, 5000000, 1800, 22.0);
        molten(EnrichedNaquadahTriniumEuropiumDuranide, 4.0, 0.16, 8000000, 1900, 20.0);
        molten(RutheniumTriniumAmericiumNeutronate, 4.5, 0.14, 10000000, 2000, 23.0);

        // --- Semiconductors (molten) ---
        molten(GalliumArsenide, 1.0, 0.33, 1, 600, 5.3);
        molten(IndiumGalliumPhosphide, 1.0, 0.35, 1, 600, 4.8);
        molten(NickelZincFerrite, 8.0, 0.60, 0.01, 600, 5.4);

        // --- Compounds & Chemicals (liquid) ---
        set(Water, 1.002, 7.0, 4.186, 0.0055, 72.8, 1.0);
        set(DistilledWater, 1.002, 7.0, 4.186, 0.000005, 72.8, 1.0);
        set(Ice, 1.002, 7.0, 4.186, 0.0055, 72.8, 0.917);
        set(SodiumPotassium, 0.5, 7.0, 1.0, 2000000, 120, 0.866);
        molten(TungstenCarbide, 7.0, 0.20, 3300000, 2000, 15.6);

        // --- Aqueous / Acid compounds ---
        set(SulfuricAcid, 26.7, 0.3, 1.34, 0.83, 55.1, 1.84);
        set(HydrochloricAcid, 1.9, 0.1, 2.94, 0.39, 65.0, 1.18);
        set(NitricAcid, 1.092, 1.0, 1.72, 0.0, 43.6, 1.51);
        set(HydrofluoricAcid, 0.256, 1.5, 2.53, 0.0, 10.1, 1.15);
        set(PhosphoricAcid, 2.4, 1.5, 1.43, 0.058, 75.0, 1.69);
        set(FluoroantimonicAcid, 5.4, -31.3, 1.36, 1.0, 45.0, 2.885);
        set(SodiumPersulfate, 1.5, 4.0, 2.8, 0.12, 68.0, 1.12);
        set(Iron3Chloride, 12.0, 2.0, 2.55, 0.25, 54.0, 1.82);
        set(RhodiumSulfate, 2.5, 2.0, 2.5, 0.5, 60.0, 1.5);
        set(HypochlorousAcid, 1.0, 4.0, 2.6, 0.01, 65.0, 1.05);
        organic(TitaniumTetrachloride, 0.84, 0.81, 32, 1.73);

        // --- Gas-phase compounds ---
        gas(CarbonDioxide, 0.015, 3.7, 0.844, 0.00181);
        gas(NitrogenDioxide, 0.013, 2.0, 0.79, 0.00189);
        gas(HydrogenSulfide, 0.012, 4.0, 1.0, 0.00141);
        gas(SulfurTrioxide, 0.013, 0.0, 0.64, 0.00328);
        gas(SulfurDioxide, 0.013, 1.5, 0.62, 0.00263);
        gas(CarbonMonoxide, 0.018, 7.0, 1.04, 0.00115);
        gas(Ammonia, 0.010, 11.6, 2.06, 0.000694);
        gas(NitricOxide, 0.019, 7.0, 0.995, 0.00123);
        gas(NitrousOxide, 0.015, 7.0, 0.88, 0.00180);
        gas(Steam, 0.012, 7.0, 2.08, 0.000598);
        gas(UraniumHexafluoride, 0.017, 7.0, 0.45, 0.01442);
        gas(EnrichedUraniumHexafluoride, 0.017, 7.0, 0.45, 0.01442);
        gas(DepletedUraniumHexafluoride, 0.017, 7.0, 0.45, 0.01442);
    }

    // ===================== Second Degree Materials ============================

    private static void initSecondDegree() {
        // --- Molten alloys / glasses ---
        // Material η(cP) Cp σ(S/m) γ(mN/m) ρ(g/cm³)
        molten(BlackSteel, 6.5, 0.48, 750000, 1750, 7.5);
        molten(DamascusSteel, 6.2, 0.47, 800000, 1800, 7.3);
        molten(TungstenSteel, 7.5, 0.25, 1500000, 2200, 12.0);
        molten(CobaltBrass, 4.0, 0.40, 2000000, 1200, 8.2);
        molten(VanadiumSteel, 6.0, 0.48, 1000000, 1850, 7.2);
        molten(Potin, 3.5, 0.28, 1000000, 800, 8.5);
        molten(NaquadahAlloy, 4.5, 0.18, 15000000, 2000, 22.0);
        molten(RhodiumPlatedPalladium, 5.0, 0.22, 4000000, 1700, 11.0);
        // Glass (molten, very viscous at working temperature)
        set(Glass, 500, 7.0, 0.84, 0.001, 300, 2.2);
        set(BorosilicateGlass, 500, 7.0, 0.83, 0.001, 280, 2.23);
        // Concrete (slurry/liquid, very viscous)
        set(Concrete, 5000, 12.0, 0.88, 0.1, 300, 2.3);
        // Magical / MC fluids
        set(Blaze, 1.0, 7.0, 2.0, 0.0, 20, 0.5);
        set(Redstone, 3.0, 7.0, 1.5, 100000, 200, 4.0);
        // Aqueous solutions
        set(SaltWater, 1.08, 7.5, 3.9, 4.8, 72, 1.025);
        set(SulfuricNickelSolution, 1.5, 1.5, 2.8, 15.0, 65, 1.20);
        set(SulfuricCopperSolution, 1.5, 1.5, 2.6, 12.0, 65, 1.25);
        set(LeadZincSolution, 1.5, 2.0, 2.5, 8.0, 60, 1.3);
        set(NitrationMixture, 2.5, 0.5, 1.5, 0.5, 55, 1.5);
        set(DilutedSulfuricAcid, 5.0, 1.0, 2.0, 0.3, 60, 1.3);
        set(DilutedHydrochloricAcid, 1.2, 0.5, 3.0, 0.2, 65, 1.05);
        set(AquaRegia, 1.5, -0.5, 2.0, 0.5, 55, 1.15);
        set(AcidicOsmiumSolution, 1.8, 1.0, 2.0, 0.3, 55, 1.5);
        // Air types (gas)
        inertGas(Air, 0.018, 1.0, 0.00119);
        inertGas(NetherAir, 0.020, 1.1, 0.00125);
        inertGas(EnderAir, 0.015, 1.2, 0.00100);
        // Liquid air (cryogenic liquids)
        set(LiquidAir, 0.17, 7.0, 1.0, 0.0, 8.4, 0.87);
        set(LiquidNetherAir, 0.20, 7.0, 1.1, 0.0, 8.0, 0.85);
        set(LiquidEnderAir, 0.15, 7.0, 1.2, 0.0, 7.0, 0.80);
    }

    // ==================== Organic Chemistry Materials =========================

    private static void initOrganicChemistry() {
        // --- Simple organic liquids ---
        // Material η(cP) Cp γ(mN/m) ρ(g/cm³)
        set(AceticAcid, 1.22, 2.4, 2.05, 0.0006, 27.6, 1.049);
        set(Phenol, 3.437, 4.0, 2.24, 0.00001, 38.2, 1.07);
        organic(Methanol, 0.544, 2.53, 22.7, 0.792);
        organic(Ethanol, 1.074, 2.44, 22.1, 0.789);
        organic(Acetone, 0.306, 2.17, 25.2, 0.784);
        organic(Benzene, 0.604, 1.74, 28.9, 0.879);
        organic(Toluene, 0.560, 1.71, 28.4, 0.867);
        organic(Glycerol, 1412, 2.43, 63.0, 1.261);
        organic(Nitrobenzene, 1.81, 1.50, 43.4, 1.199);
        set(GlycerylTrinitrate, 36.0, 7.0, 1.45, 0.0, 58, 1.59);
        organic(Cumene, 0.78, 1.70, 28.2, 0.862);
        organic(Epichlorohydrin, 1.12, 1.50, 37.0, 1.18);
        organic(Chloroform, 0.57, 0.95, 27.1, 1.489);
        organic(AllylChloride, 0.32, 1.30, 23.5, 0.938);
        organic(Isoprene, 0.22, 1.60, 17.5, 0.681);
        organic(VinylAcetate, 0.43, 1.60, 24.0, 0.932);
        organic(MethylAcetate, 0.36, 2.00, 24.7, 0.934);
        organic(Tetranitromethane, 1.65, 1.00, 30.0, 1.64);
        organic(Dimethyldichlorosilane, 0.59, 1.10, 22.0, 1.06);
        organic(Styrene, 0.72, 1.70, 32.0, 0.906);
        organic(Dichlorobenzene, 1.32, 1.13, 37.0, 1.30);
        organic(BisphenolA, 6.0, 1.90, 38.0, 1.08);
        organic(DiphenylIsophtalate, 5.0, 1.30, 40.0, 1.28);
        organic(PhthalicAcid, 4.0, 1.35, 45.0, 1.59);
        organic(Dimethylbenzene, 0.61, 1.72, 28.5, 0.864);
        organic(Nitrochlorobenzene, 2.0, 1.20, 40.0, 1.37);
        organic(Chlorobenzene, 0.80, 1.33, 33.0, 1.106);
        organic(Octane, 0.54, 2.23, 21.6, 0.703);
        organic(EthylTertButylEther, 0.42, 2.20, 20.0, 0.742);
        organic(Ethylbenzene, 0.67, 1.73, 29.2, 0.867);
        organic(Naphthalene, 0.97, 1.30, 28.0, 0.98);
        organic(Cyclohexane, 0.98, 1.85, 25.5, 0.779);
        organic(Butyraldehyde, 0.43, 2.00, 24.0, 0.802);
        set(Dimethylhydrazine, 0.492, 12.5, 2.56, 0.0001, 26.0, 0.791);
        set(DinitrogenTetroxide, 0.44, 7.0, 1.47, 0.0, 26.0, 1.45);
        set(Diaminobenzidine, 8.0, 7.0, 1.50, 0.0, 48.0, 1.2);
        set(Dichlorobenzidine, 5.0, 7.0, 1.20, 0.0, 42.0, 1.6);
        // Aqueous organic
        set(DissolvedCalciumAcetate, 1.2, 8.5, 3.5, 0.01, 65.0, 1.05);
        set(Monochloramine, 0.8, 7.0, 2.00, 0.0, 50.0, 1.02);

        // --- Organic gases ---
        inertGas(Methane, 0.011, 2.22, 0.000656);
        inertGas(Propane, 0.008, 1.67, 0.00183);
        inertGas(Propene, 0.009, 1.53, 0.00175);
        inertGas(Ethane, 0.009, 1.75, 0.00124);
        inertGas(Butene, 0.007, 1.53, 0.00230);
        inertGas(Butane, 0.007, 1.67, 0.00239);
        inertGas(Ethylene, 0.010, 1.53, 0.00115);
        inertGas(Butadiene, 0.008, 1.48, 0.00222);
        inertGas(Tetrafluoroethylene, 0.012, 0.83, 0.004);
        inertGas(VinylChloride, 0.010, 0.86, 0.00256);
        gas(Chloromethane, 0.011, 7.0, 0.75, 0.00207);
        gas(Dimethylamine, 0.013, 11.0, 1.60, 0.00184);
        gas(Ethenone, 0.010, 7.0, 1.20, 0.00172);
        gas(NitrosylChloride, 0.013, 2.0, 0.68, 0.00268);

        // --- Polymers (molten/liquid, very viscous) ---
        organic(SiliconeRubber, 5000, 1.46, 20, 1.1);
        organic(StyreneButadieneRubber, 8000, 1.90, 30, 0.94);
        organic(PolyvinylAcetate, 3000, 1.47, 36, 1.19);
        organic(ReinforcedEpoxyResin, 15000, 1.00, 40, 1.8);
        organic(PolyvinylChloride, 10000, 0.84, 40, 1.4);
        organic(PolyphenyleneSulfide, 20000, 1.09, 45, 1.35);
        organic(Polybenzimidazole, 50000, 1.30, 48, 1.3);
        organic(Polyethylene, 5000, 2.30, 31, 0.92);
        organic(Epoxy, 10000, 1.10, 42, 1.2);
        organic(Polycaprolactam, 8000, 1.70, 38, 1.14);
        organic(Polytetrafluoroethylene, 12000, 1.00, 18, 2.15);
        organic(Rubber, 15000, 2.00, 25, 0.92);
        organic(PolyvinylButyral, 6000, 1.50, 35, 1.1);
        organic(PolychlorinatedBiphenyl, 300, 1.20, 44, 1.5);
    }

    // =================== Unknown Composition Materials ========================

    private static void initUnknownComposition() {
        // --- Petroleum / fuel products ---
        // Material η(cP) Cp γ(mN/m) ρ(g/cm³)
        fuel(Diesel, 3.5, 2.05, 28, 0.832);
        fuel(Naphtha, 0.5, 2.10, 24, 0.75);
        fuel(Lubricant, 220, 1.67, 32, 0.88);
        fuel(Creosote, 12.0, 1.47, 38, 1.08);
        fuel(BioDiesel, 4.5, 2.10, 28, 0.88);
        fuel(RocketFuel, 0.8, 2.00, 22, 0.82);
        fuel(CetaneBoostedDiesel, 3.5, 2.10, 27, 0.835);
        fuel(RawGasoline, 0.7, 2.10, 22, 0.73);
        fuel(Gasoline, 0.6, 2.10, 22, 0.74);
        fuel(HighOctaneGasoline, 0.55, 2.15, 21, 0.73);

        // Heavy fuels
        fuel(SulfuricHeavyFuel, 20.0, 1.80, 30, 0.95);
        fuel(HeavyFuel, 15.0, 1.85, 30, 0.94);
        fuel(LightlyHydroCrackedHeavyFuel, 10.0, 1.90, 28, 0.90);
        fuel(SeverelyHydroCrackedHeavyFuel, 5.0, 2.00, 26, 0.85);
        fuel(LightlySteamCrackedHeavyFuel, 10.0, 1.90, 28, 0.90);
        fuel(SeverelySteamCrackedHeavyFuel, 5.0, 2.00, 26, 0.85);

        // Light fuels
        fuel(SulfuricLightFuel, 3.0, 2.00, 26, 0.85);
        fuel(LightFuel, 2.5, 2.05, 25, 0.82);
        fuel(LightlyHydroCrackedLightFuel, 2.0, 2.10, 24, 0.80);
        fuel(SeverelyHydroCrackedLightFuel, 1.5, 2.15, 23, 0.78);
        fuel(LightlySteamCrackedLightFuel, 2.0, 2.10, 24, 0.80);
        fuel(SeverelySteamCrackedLightFuel, 1.5, 2.15, 23, 0.78);

        // Naphtha variants
        fuel(SulfuricNaphtha, 0.6, 2.10, 24, 0.76);
        fuel(LightlyHydroCrackedNaphtha, 0.45, 2.15, 23, 0.73);
        fuel(SeverelyHydroCrackedNaphtha, 0.40, 2.20, 22, 0.70);
        fuel(LightlySteamCrackedNaphtha, 0.45, 2.15, 23, 0.73);
        fuel(SeverelySteamCrackedNaphtha, 0.40, 2.20, 22, 0.70);

        // Petroleum gases
        inertGas(SulfuricGas, 0.013, 1.20, 0.0015);
        inertGas(RefineryGas, 0.010, 1.60, 0.0012);
        inertGas(LightlyHydroCrackedGas, 0.009, 1.70, 0.0011);
        inertGas(SeverelyHydroCrackedGas, 0.008, 1.80, 0.0010);
        inertGas(LightlySteamCrackedGas, 0.009, 1.70, 0.0011);
        inertGas(SeverelySteamCrackedGas, 0.008, 1.80, 0.0010);
        inertGas(NaturalGas, 0.011, 2.20, 0.00072);
        inertGas(WoodGas, 0.011, 1.50, 0.0012);
        inertGas(CoalGas, 0.012, 1.40, 0.0013);
        inertGas(LPG, 0.008, 1.65, 0.0020);

        // Cracked specific chemicals (gas)
        inertGas(HydroCrackedEthane, 0.009, 1.75, 0.00124);
        inertGas(HydroCrackedEthylene, 0.010, 1.53, 0.00115);
        inertGas(HydroCrackedPropene, 0.008, 1.55, 0.00175);
        inertGas(HydroCrackedPropane, 0.008, 1.67, 0.00183);
        inertGas(HydroCrackedButane, 0.007, 1.65, 0.00240);
        inertGas(HydroCrackedButene, 0.007, 1.50, 0.00230);
        inertGas(HydroCrackedButadiene, 0.007, 1.50, 0.00222);
        inertGas(SteamCrackedEthane, 0.009, 1.75, 0.00124);
        inertGas(SteamCrackedEthylene, 0.010, 1.53, 0.00115);
        inertGas(SteamCrackedPropene, 0.008, 1.55, 0.00175);
        inertGas(SteamCrackedPropane, 0.008, 1.67, 0.00183);
        inertGas(SteamCrackedButane, 0.007, 1.65, 0.00240);
        inertGas(SteamCrackedButene, 0.007, 1.50, 0.00230);
        inertGas(SteamCrackedButadiene, 0.007, 1.50, 0.00222);
        inertGas(CharcoalByproducts, 0.012, 1.30, 0.0015);

        // --- Organic / bio liquids ---
        set(Glue, 800, 7.0, 2.10, 0.0, 42, 1.05);
        organic(WoodVinegar, 1.1, 3.50, 50, 1.02);
        fuel(WoodTar, 300, 1.40, 35, 1.1);
        fuel(CoalTar, 200, 1.50, 38, 1.1);
        set(Biomass, 5.0, 6.0, 3.50, 0.001, 50, 1.02);
        set(FermentedBiomass, 3.0, 4.0, 3.20, 0.001, 45, 1.05);
        fuel(SeedOil, 35.0, 2.00, 33, 0.92);
        fuel(FishOil, 50.0, 2.00, 33, 0.93);
        set(Milk, 2.1, 6.6, 3.93, 0.046, 47, 1.03);

        // --- Crude oils ---
        fuel(Oil, 30.0, 1.90, 28, 0.87);
        fuel(OilHeavy, 50.0, 1.80, 30, 0.93);
        fuel(RawOil, 40.0, 1.85, 29, 0.90);
        fuel(OilLight, 10.0, 2.00, 25, 0.80);

        // --- Biological / exotic ---
        set(Bacteria, 1.2, 6.5, 3.50, 0.01, 60, 1.05);
        set(BacterialSludge, 100, 5.5, 3.00, 0.02, 50, 1.1);
        set(EnrichedBacterialSludge, 80, 5.0, 3.00, 0.03, 50, 1.1);
        set(Mutagen, 5.0, 6.0, 2.50, 0.001, 45, 1.2);
        set(GelatinMixture, 300, 5.5, 3.00, 0.001, 40, 1.1);
        set(RawGrowthMedium, 5.0, 6.8, 3.50, 0.01, 50, 1.03);
        set(SterileGrowthMedium, 5.0, 7.0, 3.50, 0.01, 50, 1.03);
        set(UUMatter, 1.0, 7.0, 5.00, 0.0, 50, 1.0);

        // --- Industrial / processing ---
        set(DrillingFluid, 50.0, 9.0, 2.50, 0.1, 40, 1.15);
        set(ConstructionFoam, 800, 12.0, 1.50, 0.001, 35, 1.5);
        set(McGuffium239, 0.5, 7.0, 10.0, 1000000, 50, 5.0);
        set(IndiumConcentrate, 2.0, 1.5, 2.50, 0.5, 55, 1.3);
        organic(PCBCoolant, 0.5, 1.80, 16, 1.2);

        // --- MC / magical ---
        set(Lava, 1500, 7.0, 1.00, 0.0, 400, 3.1);
        set(Glowstone, 0.5, 7.0, 1.50, 100, 30, 2.0);

        // --- Slurries ---
        set(RubySlurry, 100, 4.0, 2.00, 0.1, 50, 1.5);
        set(SapphireSlurry, 100, 4.0, 2.00, 0.1, 50, 1.5);
        set(GreenSapphireSlurry, 100, 4.0, 2.00, 0.1, 50, 1.5);
        set(BauxiteSlurry, 80, 13.0, 2.50, 0.3, 55, 1.4);
        set(CrackedBauxiteSlurry, 50, 12.0, 2.50, 0.2, 55, 1.35);
        set(BauxiteSludge, 200, 12.5, 2.30, 0.3, 50, 1.5);
        set(DecalcifiedBauxiteSludge, 150, 11.0, 2.30, 0.2, 50, 1.45);

        // --- Dyes (all water-based solutions) ---
        dye(DyeBlack);
        dye(DyeRed);
        dye(DyeGreen);
        dye(DyeBrown);
        dye(DyeBlue);
        dye(DyePurple);
        dye(DyeCyan);
        dye(DyeLightGray);
        dye(DyeGray);
        dye(DyePink);
        dye(DyeLime);
        dye(DyeYellow);
        dye(DyeLightBlue);
        dye(DyeMagenta);
        dye(DyeOrange);
        dye(DyeWhite);

        // --- Naquadah processing solutions ---
        set(ImpureEnrichedNaquadahSolution, 3.0, 1.0, 1.5, 5.0, 55, 2.5);
        set(EnrichedNaquadahSolution, 2.5, 1.5, 1.5, 8.0, 55, 2.5);
        set(AcidicEnrichedNaquadahSolution, 2.8, 0.5, 1.5, 3.0, 50, 2.8);
        set(EnrichedNaquadahWaste, 5.0, 2.0, 1.5, 1.0, 45, 2.0);
        set(ImpureNaquadriaSolution, 3.5, 0.5, 1.5, 8.0, 55, 3.0);
        set(NaquadriaSolution, 3.0, 1.0, 1.5, 12.0, 55, 3.0);
        set(AcidicNaquadriaSolution, 3.2, 0.0, 1.5, 5.0, 50, 3.2);
        set(NaquadriaWaste, 6.0, 2.0, 1.5, 2.0, 45, 2.5);
    }

    // ===================== Higher Degree Materials ============================

    private static void initHigherDegree() {
        // Material η(cP) Cp σ(S/m) γ(mN/m) ρ(g/cm³)
        molten(RedSteel, 6.5, 0.48, 750000, 1750, 7.6);
        molten(BlueSteel, 6.5, 0.48, 750000, 1750, 7.6);
        molten(HSSG, 7.0, 0.35, 1000000, 2000, 9.0);
        molten(HSSE, 7.0, 0.35, 1000000, 2000, 9.2);
        molten(HSSS, 7.0, 0.35, 1000000, 2000, 9.5);
        molten(RedAlloy, 4.0, 0.40, 5000000, 1000, 7.5);
        molten(BlueAlloy, 4.0, 0.40, 5000000, 1000, 7.5);
    }

    // ============================ GCYM Materials ==============================

    private static void initGCYM() {
        // Material η(cP) Cp σ(S/m) γ(mN/m) ρ(g/cm³)
        molten(Stellite100, 6.0, 0.42, 1200000, 1800, 8.7);
        molten(WatertightSteel, 6.2, 0.46, 800000, 1750, 7.8);
        molten(MaragingSteel300, 6.0, 0.45, 900000, 1800, 8.0);
        molten(HastelloyC276, 6.5, 0.43, 800000, 1700, 8.9);
        molten(HastelloyX, 6.5, 0.43, 800000, 1700, 8.2);
        molten(Trinaquadalloy, 4.0, 0.18, 20000000, 2100, 20.0);
        molten(Zeron100, 6.0, 0.50, 1000000, 1750, 7.8);
        molten(TitaniumCarbide, 5.0, 0.25, 3000000, 1800, 4.9);
        molten(TantalumCarbide, 6.0, 0.20, 500000, 2000, 14.3);
        molten(MolybdenumDisilicide, 4.0, 0.30, 2000000, 1500, 6.3);
        // GCYM Second Degree
        molten(HSLASteel, 6.0, 0.48, 850000, 1780, 7.85);
        molten(TitaniumTungstenCarbide, 6.5, 0.22, 2500000, 1900, 11.0);
        molten(IncoloyMA956, 5.5, 0.52, 750000, 1700, 7.2);
    }
}

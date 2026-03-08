package gregtech.api.recipes.ingredients;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class providing pre-built and custom property-based fluid recipe inputs.
 * <p>
 * Registered as a GroovyScript global binding so pack scripts can use it directly:
 * <pre>
 * // Pre-built presets:
 * mods.gregtech.chemical_bath.recipeBuilder()
 *         .input(ore, 'dustWood')
 *         .fluidInputs(FluidPropertyInputs.anyWater(100))
 *         .output(item('minecraft:paper'))
 *         .duration(200).EUt(4).buildAndRegister()
 *
 * // Custom property builder:
 * mods.gregtech.chemical_reactor.recipeBuilder()
 *         .fluidInputs(FluidPropertyInputs.custom(1000)
 *                 .pHBelow(3.0)
 *                 .viscosityAbove(5.0)
 *                 .description("Thick Acid")
 *                 .build())
 *         .outputs(...)
 *         .buildAndRegister()
 *
 * // Builder shorthand with auto-generated description:
 * def acidInput = FluidPropertyInputs.custom(500).pHBelow(2.0).conductivityAbove(0.1).build()
 * </pre>
 *
 * @see GTRecipeFluidPropertyInput
 */
public final class FluidPropertyInputs {

    private FluidPropertyInputs() {}

    // ---- Pre-built presets ----

    /**
     * Any water-like fluid (neutral pH 6-8, low viscosity &lt;2 cP, high heat capacity &gt;3 J/g·K).
     * Matches: Water, DistilledWater, SaltWater, Ice.
     */
    public static @NotNull GTRecipeFluidPropertyInput anyWater(int amount) {
        return GTRecipeFluidPropertyInput.anyWater(amount);
    }

    /**
     * Any acidic fluid (pH &lt;4, liquid density &gt;0.5 g/cm³).
     * Matches: SulfuricAcid, HydrochloricAcid, NitricAcid, HydrofluoricAcid, PhosphoricAcid,
     * FluoroantimonicAcid, Iron3Chloride, RhodiumSulfate, AquaRegia, etc.
     */
    public static @NotNull GTRecipeFluidPropertyInput anyAcid(int amount) {
        return GTRecipeFluidPropertyInput.anyAcid(amount);
    }

    /**
     * Any strongly acidic fluid (pH &lt;2, liquid density &gt;0.5 g/cm³).
     * Matches: SulfuricAcid, HydrochloricAcid, NitricAcid, HydrofluoricAcid, FluoroantimonicAcid, AquaRegia.
     */
    public static @NotNull GTRecipeFluidPropertyInput anyStrongAcid(int amount) {
        return GTRecipeFluidPropertyInput.anyStrongAcid(amount);
    }

    /**
     * Any good coolant fluid (heat capacity &gt;1.5, viscosity &lt;5, liquid density &gt;0.5).
     * Matches: Water, DistilledWater, SaltWater, and similar high heat-capacity liquids.
     */
    public static @NotNull GTRecipeFluidPropertyInput anyCoolant(int amount) {
        return GTRecipeFluidPropertyInput.anyCoolant(amount);
    }

    /**
     * Any lubricating fluid (viscosity &gt;30, neutral pH 5-9, liquid density &gt;0.5).
     * Matches: Lubricant, SeedOil, Glycerol, and similar thick neutral fluids.
     */
    public static @NotNull GTRecipeFluidPropertyInput anyLubricant(int amount) {
        return GTRecipeFluidPropertyInput.anyLubricant(amount);
    }

    /**
     * Any electrically conductive fluid (conductivity &gt;1000 S/m).
     * Matches: most molten metals (Copper, Gold, Aluminium, etc.) and conductive solutions.
     */
    public static @NotNull GTRecipeFluidPropertyInput anyConductiveFluid(int amount) {
        return GTRecipeFluidPropertyInput.anyConductiveFluid(amount);
    }

    /**
     * Any inert gas (density &lt;0.01 g/cm³, neutral pH 6-8).
     * Matches: Helium, Argon, Neon, Nitrogen, Hydrogen, and noble/inert gases.
     */
    public static @NotNull GTRecipeFluidPropertyInput anyInertGas(int amount) {
        return GTRecipeFluidPropertyInput.anyInertGas(amount);
    }

    /**
     * Any solvent - low viscosity (&lt;3 cP), non-gaseous liquid (density 0.5-2.0 g/cm³).
     * Matches: Water, Methanol, Ethanol, Acetone, Benzene, Chloroform, and similar.
     */
    public static @NotNull GTRecipeFluidPropertyInput anySolvent(int amount) {
        return GTRecipeFluidPropertyInput.anySolvent(amount);
    }

    // ---- Custom builder ----

    /**
     * Start building a custom property-based fluid input.
     * <p>
     * Chain property constraints and call {@code .build()} to create the input:
     * <pre>
     * FluidPropertyInputs.custom(1000)
     *         .pHBelow(3.0)
     *         .viscosityAbove(5.0)
     *         .description("Thick Acid")
     *         .build()
     * </pre>
     *
     * Available builder methods:
     * <ul>
     *   <li>{@code viscosityBelow/Above/Between(double...)} - viscosity in cP</li>
     *   <li>{@code pHBelow/Above/Between(double...)} - pH (0-14)</li>
     *   <li>{@code heatCapacityBelow/Above/Between(double...)} - specific heat capacity in J/(g·K)</li>
     *   <li>{@code conductivityBelow/Above/Between(double...)} - electrical conductivity in S/m</li>
     *   <li>{@code surfaceTensionBelow/Above/Between(double...)} - surface tension in mN/m</li>
     *   <li>{@code densityBelow/Above/Between(double...)} - density in g/cm³</li>
     *   <li>{@code description(String)} - set a custom human-readable description</li>
     *   <li>{@code where(Predicate, String)} - add a custom predicate with a description</li>
     * </ul>
     *
     * @param amount the amount of fluid required in mB
     * @return a new Builder
     */
    public static GTRecipeFluidPropertyInput.@NotNull Builder custom(int amount) {
        return GTRecipeFluidPropertyInput.builder(amount);
    }
}

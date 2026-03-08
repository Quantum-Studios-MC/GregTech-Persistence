package gregtech.api.recipes.ingredients;

import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidDataProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A recipe fluid input that matches any fluid whose {@link FluidDataProperty} satisfies a given predicate.
 * <p>
 * Unlike {@link GTRecipeFluidInput} which matches a specific fluid identity, this input matches
 * <b>any</b> fluid whose physical properties meet the specified criteria.
 * <p>
 * Recipes using this input are stored in a separate list on the RecipeMap and checked via linear scan,
 * rather than being indexed in the ingredient tree (since they cannot be keyed by a single fluid identity).
 * <p>
 * Example usage:
 * <pre>
 * // Match any fluid with pH below 3 (strongly acidic)
 * new GTRecipeFluidPropertyInput(p -> p.getPH() < 3.0, 1000, "Strongly Acidic Fluid")
 *
 * // Match any fluid with viscosity below 2 cP and high heat capacity
 * GTRecipeFluidPropertyInput.builder(1000)
 *         .viscosityBelow(2.0)
 *         .heatCapacityAbove(3.0)
 *         .description("Thin, High Heat Capacity Fluid")
 *         .build()
 * </pre>
 */
public class GTRecipeFluidPropertyInput extends GTRecipeInput {

    private final Predicate<FluidDataProperty> propertyMatcher;
    private final String description;

    /**
     * Create a property-based fluid input.
     *
     * @param propertyMatcher predicate that tests a fluid's {@link FluidDataProperty}
     * @param amount          the amount of fluid required in mB
     * @param description     human-readable description for JEI/tooltip display
     */
    public GTRecipeFluidPropertyInput(@NotNull Predicate<FluidDataProperty> propertyMatcher, int amount,
                                      @NotNull String description) {
        this.propertyMatcher = propertyMatcher;
        this.amount = amount;
        this.description = description;
    }

    /**
     * @return a builder for constructing property-based fluid inputs with combined criteria
     */
    public static @NotNull Builder builder(int amount) {
        return new Builder(amount);
    }

    // ---- Common presets ----

    /**
     * Returns a property input matching any water-like fluid (neutral pH, low viscosity, high heat capacity).
     * Matches Water, DistilledWater, SaltWater, Ice, and any modded fluid with similar properties.
     *
     * @param amount the amount of fluid required in mB
     */
    public static @NotNull GTRecipeFluidPropertyInput anyWater(int amount) {
        return builder(amount)
                .pHBetween(6.0, 8.0)
                .viscosityBelow(2.0)
                .heatCapacityAbove(3.0)
                .description("Any Neutral Water")
                .build();
    }

    /**
     * Returns a property input matching any acidic fluid (pH below 4, liquid-density).
     * Matches SulfuricAcid, HydrochloricAcid, NitricAcid, HydrofluoricAcid, PhosphoricAcid,
     * FluoroantimonicAcid, Iron3Chloride, RhodiumSulfate, AquaRegia, etc.
     *
     * @param amount the amount of fluid required in mB
     */
    public static @NotNull GTRecipeFluidPropertyInput anyAcid(int amount) {
        return builder(amount)
                .pHBelow(4.0)
                .densityAbove(0.5)
                .description("Any Acidic Fluid")
                .build();
    }

    /**
     * Returns a property input matching any strongly acidic fluid (pH below 2).
     * Matches SulfuricAcid, HydrochloricAcid, NitricAcid, HydrofluoricAcid, FluoroantimonicAcid, AquaRegia.
     *
     * @param amount the amount of fluid required in mB
     */
    public static @NotNull GTRecipeFluidPropertyInput anyStrongAcid(int amount) {
        return builder(amount)
                .pHBelow(2.0)
                .densityAbove(0.5)
                .description("Any Strongly Acidic Fluid")
                .build();
    }

    /**
     * Returns a property input matching any good coolant (high heat capacity, low viscosity liquid).
     * Matches Water, DistilledWater, SaltWater, Ice, and similar fluids.
     *
     * @param amount the amount of fluid required in mB
     */
    public static @NotNull GTRecipeFluidPropertyInput anyCoolant(int amount) {
        return builder(amount)
                .heatCapacityAbove(1.5)
                .viscosityBelow(5.0)
                .densityAbove(0.5)
                .description("Any Coolant")
                .build();
    }

    /**
     * Returns a property input matching any lubricating fluid (high viscosity, neutral pH).
     * Matches Lubricant, SeedOil, Glycerol, and similar thick neutral fluids.
     *
     * @param amount the amount of fluid required in mB
     */
    public static @NotNull GTRecipeFluidPropertyInput anyLubricant(int amount) {
        return builder(amount)
                .viscosityAbove(30.0)
                .pHBetween(5.0, 9.0)
                .densityAbove(0.5)
                .description("Any Lubricant")
                .build();
    }

    /**
     * Returns a property input matching any electrically conductive fluid (conductivity above 1000 S/m).
     * Matches most molten metals (Copper, Gold, Aluminium, etc.) and conductive solutions.
     *
     * @param amount the amount of fluid required in mB
     */
    public static @NotNull GTRecipeFluidPropertyInput anyConductiveFluid(int amount) {
        return builder(amount)
                .conductivityAbove(1000.0)
                .description("Any Conductive Fluid")
                .build();
    }

    /**
     * Returns a property input matching any inert gas (very low density, neutral pH).
     * Matches Helium, Argon, Neon, Nitrogen, Hydrogen, and noble/inert gases.
     *
     * @param amount the amount of fluid required in mB
     */
    public static @NotNull GTRecipeFluidPropertyInput anyInertGas(int amount) {
        return builder(amount)
                .densityBelow(0.01)
                .pHBetween(6.0, 8.0)
                .description("Any Inert Gas")
                .build();
    }

    /**
     * Returns a property input matching any solvent - low viscosity, non-gaseous liquid.
     * Matches Water, DistilledWater, Methanol, Ethanol, Acetone, Benzene, Chloroform, and similar.
     *
     * @param amount the amount of fluid required in mB
     */
    public static @NotNull GTRecipeFluidPropertyInput anySolvent(int amount) {
        return builder(amount)
                .viscosityBelow(3.0)
                .densityBetween(0.5, 2.0)
                .description("Any Solvent")
                .build();
    }

    @Override
    protected GTRecipeFluidPropertyInput copy() {
        GTRecipeFluidPropertyInput copy = new GTRecipeFluidPropertyInput(this.propertyMatcher, this.amount,
                this.description);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public GTRecipeInput copyWithAmount(int amount) {
        GTRecipeFluidPropertyInput copy = new GTRecipeFluidPropertyInput(this.propertyMatcher, amount,
                this.description);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    /**
     * Returns null - this input does not represent a specific fluid.
     * Use {@link #acceptsFluid(FluidStack)} for matching.
     */
    @Override
    public @Nullable FluidStack getInputFluidStack() {
        return null;
    }

    /**
     * Tests whether the given fluid matches the property criteria.
     * Looks up the fluid's material via {@link FluidUnifier}, then checks its {@link FluidDataProperty}.
     */
    @Override
    public boolean acceptsFluid(@Nullable FluidStack input) {
        if (input == null || input.amount == 0) return false;

        Material material = FluidUnifier.getMaterialFromFluid(input.getFluid());
        if (material == null) return false;

        FluidDataProperty fluidData = material.getProperty(PropertyKey.FLUID_DATA);
        if (fluidData == null) return false;

        return propertyMatcher.test(fluidData);
    }

    /**
     * @return true - this is a property-based fluid input, not an identity-based one
     */
    public boolean isPropertyBased() {
        return true;
    }

    /**
     * @return the human-readable description of the property criteria
     */
    public @NotNull String getDescription() {
        return description;
    }

    /**
     * @return the predicate used for matching
     */
    public @NotNull Predicate<FluidDataProperty> getPropertyMatcher() {
        return propertyMatcher;
    }

    @Override
    protected int computeHash() {
        return Objects.hash(description, amount, isConsumable);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeFluidPropertyInput other)) return false;
        if (this.amount != other.amount || this.isConsumable != other.isConsumable) return false;
        return Objects.equals(this.description, other.description);
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) return true;
        if (!(input instanceof GTRecipeFluidPropertyInput other)) return false;
        return Objects.equals(this.description, other.description);
    }

    @Override
    public String toString() {
        return amount + "x[" + description + "]";
    }

    /**
     * Builder for composing multiple property predicates into a single {@link GTRecipeFluidPropertyInput}.
     * <p>
     * All conditions are combined with AND logic.
     */
    public static class Builder {

        private final int amount;
        private Predicate<FluidDataProperty> predicate = p -> true;
        private final StringBuilder desc = new StringBuilder();
        private String customDescription;

        private Builder(int amount) {
            this.amount = amount;
        }

        private void appendDesc(String part) {
            if (desc.length() > 0) desc.append(", ");
            desc.append(part);
        }

        // ---- Viscosity ----

        public @NotNull Builder viscosityBelow(double max) {
            predicate = predicate.and(p -> p.getViscosity() < max);
            appendDesc("viscosity<" + max + "cP");
            return this;
        }

        public @NotNull Builder viscosityAbove(double min) {
            predicate = predicate.and(p -> p.getViscosity() > min);
            appendDesc("viscosity>" + min + "cP");
            return this;
        }

        public @NotNull Builder viscosityBetween(double min, double max) {
            predicate = predicate.and(p -> p.getViscosity() >= min && p.getViscosity() <= max);
            appendDesc("viscosity=" + min + "-" + max + "cP");
            return this;
        }

        // ---- pH ----

        public @NotNull Builder pHBelow(double max) {
            predicate = predicate.and(p -> p.getPH() < max);
            appendDesc("pH<" + max);
            return this;
        }

        public @NotNull Builder pHAbove(double min) {
            predicate = predicate.and(p -> p.getPH() > min);
            appendDesc("pH>" + min);
            return this;
        }

        public @NotNull Builder pHBetween(double min, double max) {
            predicate = predicate.and(p -> p.getPH() >= min && p.getPH() <= max);
            appendDesc("pH=" + min + "-" + max);
            return this;
        }

        // ---- Heat Capacity ----

        public @NotNull Builder heatCapacityBelow(double max) {
            predicate = predicate.and(p -> p.getSpecificHeatCapacity() < max);
            appendDesc("heatCap<" + max + "J/gK");
            return this;
        }

        public @NotNull Builder heatCapacityAbove(double min) {
            predicate = predicate.and(p -> p.getSpecificHeatCapacity() > min);
            appendDesc("heatCap>" + min + "J/gK");
            return this;
        }

        public @NotNull Builder heatCapacityBetween(double min, double max) {
            predicate = predicate.and(p -> p.getSpecificHeatCapacity() >= min && p.getSpecificHeatCapacity() <= max);
            appendDesc("heatCap=" + min + "-" + max + "J/gK");
            return this;
        }

        // ---- Electrical Conductivity ----

        public @NotNull Builder conductivityBelow(double max) {
            predicate = predicate.and(p -> p.getElectricalConductivity() < max);
            appendDesc("conductivity<" + max + "S/m");
            return this;
        }

        public @NotNull Builder conductivityAbove(double min) {
            predicate = predicate.and(p -> p.getElectricalConductivity() > min);
            appendDesc("conductivity>" + min + "S/m");
            return this;
        }

        public @NotNull Builder conductivityBetween(double min, double max) {
            predicate = predicate.and(p -> p.getElectricalConductivity() >= min &&
                    p.getElectricalConductivity() <= max);
            appendDesc("conductivity=" + min + "-" + max + "S/m");
            return this;
        }

        // ---- Surface Tension ----

        public @NotNull Builder surfaceTensionBelow(double max) {
            predicate = predicate.and(p -> p.getSurfaceTension() < max);
            appendDesc("surfTension<" + max + "mN/m");
            return this;
        }

        public @NotNull Builder surfaceTensionAbove(double min) {
            predicate = predicate.and(p -> p.getSurfaceTension() > min);
            appendDesc("surfTension>" + min + "mN/m");
            return this;
        }

        public @NotNull Builder surfaceTensionBetween(double min, double max) {
            predicate = predicate.and(p -> p.getSurfaceTension() >= min && p.getSurfaceTension() <= max);
            appendDesc("surfTension=" + min + "-" + max + "mN/m");
            return this;
        }

        // ---- Density ----

        public @NotNull Builder densityBelow(double max) {
            predicate = predicate.and(p -> p.getDensity() < max);
            appendDesc("density<" + max + "g/cm³");
            return this;
        }

        public @NotNull Builder densityAbove(double min) {
            predicate = predicate.and(p -> p.getDensity() > min);
            appendDesc("density>" + min + "g/cm³");
            return this;
        }

        public @NotNull Builder densityBetween(double min, double max) {
            predicate = predicate.and(p -> p.getDensity() >= min && p.getDensity() <= max);
            appendDesc("density=" + min + "-" + max + "g/cm³");
            return this;
        }

        // ---- Custom ----

        /**
         * Add a custom property predicate.
         */
        public @NotNull Builder where(@NotNull Predicate<FluidDataProperty> condition, @NotNull String conditionDesc) {
            predicate = predicate.and(condition);
            appendDesc(conditionDesc);
            return this;
        }

        /**
         * Set a custom description, overriding the auto-generated one.
         */
        public @NotNull Builder description(@NotNull String description) {
            this.customDescription = description;
            return this;
        }

        /**
         * Build the {@link GTRecipeFluidPropertyInput}.
         */
        public @NotNull GTRecipeFluidPropertyInput build() {
            String finalDesc = customDescription != null ? customDescription : desc.toString();
            return new GTRecipeFluidPropertyInput(predicate, amount, finalDesc);
        }
    }
}

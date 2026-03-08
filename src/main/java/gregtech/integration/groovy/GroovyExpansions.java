package gregtech.integration.groovy;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeFluidPropertyInput;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;
import gregtech.api.unification.material.properties.ExtraToolProperty;
import gregtech.api.unification.material.properties.FluidDataProperty;
import gregtech.api.unification.material.properties.MaterialToolProperty;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;

public class GroovyExpansions {

    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> property(RecipeBuilder<R> builder, String key,
                                                                         Object value) {
        if (!builder.applyPropertyCT(key, value)) {
            GroovyLog.get().error("Failed to add property '{}' with '{}' to recipe", key, value);
        }
        return builder;
    }

    public static Material.Builder materialBuilder(MaterialEvent event, int id, ResourceLocation resourceLocation) {
        return Material.builder(id, resourceLocation);
    }

    public static Material.Builder materialBuilder(MaterialEvent event, int id, String domain, String path) {
        return materialBuilder(event, id, new ResourceLocation(domain, path));
    }

    public static Material.Builder materialBuilder(MaterialEvent event, int id, String s) {
        String domain, path;
        if (s.contains(":")) {
            String[] parts = s.split(":", 2);
            domain = parts[0];
            path = parts[1];
        } else {
            domain = GroovyScript.getRunConfig().getPackId();
            path = s;
        }
        return materialBuilder(event, id, new ResourceLocation(domain, path));
    }

    public static MaterialToolProperty.Builder toolBuilder(MaterialEvent event, float harvestSpeed, float attackDamage,
                                                           int durability, int harvestLevel) {
        return MaterialToolProperty.Builder.of(harvestSpeed, attackDamage, durability, harvestLevel);
    }

    public static MaterialToolProperty.Builder toolBuilder(MaterialEvent event) {
        return toolBuilder(event, 1.0F, 1.0F, 100, 2);
    }

    public static ExtraToolProperty.Builder overrideToolBuilder(MaterialEvent event) {
        return ExtraToolProperty.Builder.of();
    }

    public static ExtraToolProperty.Builder overrideToolBuilder(MaterialEvent event, float harvestSpeed,
                                                                float attackDamage,
                                                                int durability, int harvestLevel) {
        return ExtraToolProperty.Builder.of(harvestSpeed, attackDamage, durability, harvestLevel);
    }

    public static FluidBuilder fluidBuilder(MaterialEvent event) {
        return new FluidBuilder();
    }

    public static Element addElement(MaterialEvent event, long protons, long neutrons, long halfLifeSeconds,
                                     String decayTo, String name, String symbol, boolean isIsotope) {
        return Elements.add(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
    }

    public static Element addElement(MaterialEvent event, long protons, long neutrons, String name, String symbol,
                                     boolean isIsotope) {
        return Elements.add(protons, neutrons, name, symbol, isIsotope);
    }

    public static Element addElement(MaterialEvent event, long protons, long neutrons, String name, String symbol) {
        return Elements.add(protons, neutrons, name, symbol);
    }

    public static FluidBuilder acidic(FluidBuilder builder) {
        return builder.attributes(FluidAttributes.ACID);
    }

    /**
     * Create a new FluidDataProperty builder with default (water-like) values.
     * <p>
     * Usage in GroovyScript:
     * 
     * <pre>
     * def fdBuilder = event.fluidDataBuilder()
     * fdBuilder.viscosity(1.002).pH(7.0).density(1.0).build()
     * </pre>
     */
    public static FluidDataProperty.Builder fluidDataBuilder(MaterialEvent event) {
        return FluidDataProperty.Builder.create();
    }

    /**
     * Create a new FluidDataProperty builder with default (water-like) values.
     * Available in PostMaterialEvent as well.
     */
    public static FluidDataProperty.Builder fluidDataBuilder(PostMaterialEvent event) {
        return FluidDataProperty.Builder.create();
    }

    /**
     * Create a new property-based fluid input builder for use in recipes.
     * <p>
     * Usage in GroovyScript:
     * 
     * <pre>
     * def acidInput = builder.fluidPropertyInput(1000)
     *         .pHBelow(3.0)
     *         .description("Strongly Acidic Fluid")
     *         .build()
     * mods.gregtech.chemical_reactor.recipeBuilder()
     *         .fluidInputs(acidInput)
     *         .outputs(...)
     *         .buildAndRegister()
     * </pre>
     */
    public static <R extends RecipeBuilder<R>> GTRecipeFluidPropertyInput.Builder fluidPropertyInput(
            RecipeBuilder<R> builder, int amount) {
        return GTRecipeFluidPropertyInput.builder(amount);
    }

    // ---- Preset fluid property inputs (convenience methods on RecipeBuilder) ----

    /**
     * Add an "Any Neutral Water" property-based fluid input (pH 6-8, viscosity &lt;2 cP, heat capacity &gt;3 J/gK).
     * <p>
     * Matches Water, DistilledWater, SaltWater, Ice, and similar water-like fluids.
     * <pre>
     * builder.anyWaterInput(100)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> anyWaterInput(RecipeBuilder<R> builder, int amount) {
        builder.fluidInputs(GTRecipeFluidPropertyInput.anyWater(amount));
        return builder;
    }

    /**
     * Add an "Any Acidic Fluid" property-based fluid input (pH &lt;4, density &gt;0.5).
     * <p>
     * Matches SulfuricAcid, HydrochloricAcid, NitricAcid, HydrofluoricAcid, PhosphoricAcid,
     * FluoroantimonicAcid, Iron3Chloride, RhodiumSulfate, AquaRegia, etc.
     * <pre>
     * builder.anyAcidInput(500)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> anyAcidInput(RecipeBuilder<R> builder, int amount) {
        builder.fluidInputs(GTRecipeFluidPropertyInput.anyAcid(amount));
        return builder;
    }

    /**
     * Add an "Any Strongly Acidic Fluid" property-based fluid input (pH &lt;2, density &gt;0.5).
     * <pre>
     * builder.anyStrongAcidInput(1000)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> anyStrongAcidInput(RecipeBuilder<R> builder,
                                                                                   int amount) {
        builder.fluidInputs(GTRecipeFluidPropertyInput.anyStrongAcid(amount));
        return builder;
    }

    /**
     * Add an "Any Coolant" property-based fluid input (heat capacity &gt;1.5, viscosity &lt;5, density &gt;0.5).
     * <pre>
     * builder.anyCoolantInput(100)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> anyCoolantInput(RecipeBuilder<R> builder, int amount) {
        builder.fluidInputs(GTRecipeFluidPropertyInput.anyCoolant(amount));
        return builder;
    }

    /**
     * Add an "Any Lubricant" property-based fluid input (viscosity &gt;30, pH 5-9, density &gt;0.5).
     * <pre>
     * builder.anyLubricantInput(250)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> anyLubricantInput(RecipeBuilder<R> builder,
                                                                                  int amount) {
        builder.fluidInputs(GTRecipeFluidPropertyInput.anyLubricant(amount));
        return builder;
    }

    /**
     * Add an "Any Conductive Fluid" property-based fluid input (conductivity &gt;1000 S/m).
     * <pre>
     * builder.anyConductiveFluidInput(1000)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> anyConductiveFluidInput(RecipeBuilder<R> builder,
                                                                                        int amount) {
        builder.fluidInputs(GTRecipeFluidPropertyInput.anyConductiveFluid(amount));
        return builder;
    }

    /**
     * Add an "Any Inert Gas" property-based fluid input (density &lt;0.01, pH 6-8).
     * <pre>
     * builder.anyInertGasInput(1000)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> anyInertGasInput(RecipeBuilder<R> builder, int amount) {
        builder.fluidInputs(GTRecipeFluidPropertyInput.anyInertGas(amount));
        return builder;
    }

    /**
     * Add an "Any Solvent" property-based fluid input (viscosity &lt;3, density 0.5-2.0).
     * <pre>
     * builder.anySolventInput(500)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> anySolventInput(RecipeBuilder<R> builder, int amount) {
        builder.fluidInputs(GTRecipeFluidPropertyInput.anySolvent(amount));
        return builder;
    }
}

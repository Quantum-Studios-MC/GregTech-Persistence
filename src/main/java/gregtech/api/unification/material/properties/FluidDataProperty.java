package gregtech.api.unification.material.properties;

import org.jetbrains.annotations.NotNull;

public class FluidDataProperty implements IMaterialProperty {

    private double viscosity;
    private double pH;
    private double specificHeatCapacity;
    private double electricalConductivity;
    private double surfaceTension;
    private double density;

    public FluidDataProperty() {
        this.viscosity = 1.0;
        this.pH = 7.0;
        this.specificHeatCapacity = 4.186;
        this.electricalConductivity = 0.0;
        this.surfaceTension = 72.8;
        this.density = 1.0;
    }

    public FluidDataProperty(double viscosity, double pH, double specificHeatCapacity,
                             double electricalConductivity, double surfaceTension, double density) {
        this.viscosity = viscosity;
        this.pH = pH;
        this.specificHeatCapacity = specificHeatCapacity;
        this.electricalConductivity = electricalConductivity;
        this.surfaceTension = surfaceTension;
        this.density = density;
    }

    public double getViscosity() {
        return viscosity;
    }

    public FluidDataProperty setViscosity(double viscosity) {
        this.viscosity = viscosity;
        return this;
    }

    public double getPH() {
        return pH;
    }

    public FluidDataProperty setPH(double pH) {
        this.pH = pH;
        return this;
    }

    public double getSpecificHeatCapacity() {
        return specificHeatCapacity;
    }

    public FluidDataProperty setSpecificHeatCapacity(double specificHeatCapacity) {
        this.specificHeatCapacity = specificHeatCapacity;
        return this;
    }

    public double getElectricalConductivity() {
        return electricalConductivity;
    }

    public FluidDataProperty setElectricalConductivity(double electricalConductivity) {
        this.electricalConductivity = electricalConductivity;
        return this;
    }

    public double getSurfaceTension() {
        return surfaceTension;
    }

    public FluidDataProperty setSurfaceTension(double surfaceTension) {
        this.surfaceTension = surfaceTension;
        return this;
    }

    public double getDensity() {
        return density;
    }

    public FluidDataProperty setDensity(double density) {
        this.density = density;
        return this;
    }

    public String getViscosityCategory() {
        if (viscosity < 0.5) return "gregtech.fluid_data.viscosity.very_thin";
        if (viscosity < 2.0) return "gregtech.fluid_data.viscosity.thin";
        if (viscosity < 10.0) return "gregtech.fluid_data.viscosity.moderate";
        if (viscosity < 100.0) return "gregtech.fluid_data.viscosity.thick";
        if (viscosity < 1000.0) return "gregtech.fluid_data.viscosity.very_thick";
        return "gregtech.fluid_data.viscosity.paste";
    }

    public String getPHCategory() {
        if (pH < 2.0) return "gregtech.fluid_data.ph.strongly_acidic";
        if (pH < 5.0) return "gregtech.fluid_data.ph.acidic";
        if (pH < 6.5) return "gregtech.fluid_data.ph.weakly_acidic";
        if (pH <= 7.5) return "gregtech.fluid_data.ph.neutral";
        if (pH < 9.0) return "gregtech.fluid_data.ph.weakly_alkaline";
        if (pH < 12.0) return "gregtech.fluid_data.ph.alkaline";
        return "gregtech.fluid_data.ph.strongly_alkaline";
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.FLUID, true);
    }

    /**
     * Builder for {@link FluidDataProperty} using a fluent API.
     * <p>
     * Example usage:
     * 
     * <pre>
     * FluidDataProperty.Builder.create()
     *         .viscosity(1.002)
     *         .pH(7.0)
     *         .density(1.0)
     *         .heatCapacity(4.186)
     *         .build();
     * </pre>
     */
    public static class Builder {

        private double viscosity = 1.0;
        private double pH = 7.0;
        private double specificHeatCapacity = 4.186;
        private double electricalConductivity = 0.0;
        private double surfaceTension = 72.8;
        private double density = 1.0;

        private Builder() {}

        /**
         * Create a new FluidDataProperty builder with default values (water-like).
         */
        public static @NotNull Builder create() {
            return new Builder();
        }

        /**
         * Create a new FluidDataProperty builder with the given viscosity and pH,
         * using defaults for other values.
         */
        public static @NotNull Builder of(double viscosity, double pH) {
            Builder b = new Builder();
            b.viscosity = viscosity;
            b.pH = pH;
            return b;
        }

        /**
         * @param viscosity the viscosity in cP (centipoise). Default: 1.0 (water-like)
         */
        public @NotNull Builder viscosity(double viscosity) {
            this.viscosity = viscosity;
            return this;
        }

        /**
         * @param pH the pH value. Default: 7.0 (neutral)
         */
        public @NotNull Builder pH(double pH) {
            this.pH = pH;
            return this;
        }

        /**
         * @param heatCapacity the specific heat capacity in J/(g*K). Default: 4.186 (water)
         */
        public @NotNull Builder heatCapacity(double heatCapacity) {
            this.specificHeatCapacity = heatCapacity;
            return this;
        }

        /**
         * @param conductivity the electrical conductivity in S/m. Default: 0.0
         */
        public @NotNull Builder conductivity(double conductivity) {
            this.electricalConductivity = conductivity;
            return this;
        }

        /**
         * @param surfaceTension the surface tension in mN/m. Default: 72.8 (water)
         */
        public @NotNull Builder surfaceTension(double surfaceTension) {
            this.surfaceTension = surfaceTension;
            return this;
        }

        /**
         * @param density the density in g/cm^3. Default: 1.0 (water)
         */
        public @NotNull Builder density(double density) {
            this.density = density;
            return this;
        }

        /**
         * Build the {@link FluidDataProperty}.
         */
        public @NotNull FluidDataProperty build() {
            return new FluidDataProperty(viscosity, pH, specificHeatCapacity,
                    electricalConductivity, surfaceTension, density);
        }
    }
}

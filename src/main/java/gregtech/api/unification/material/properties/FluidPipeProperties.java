package gregtech.api.unification.material.properties;

import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Objects;

public class FluidPipeProperties implements IMaterialProperty, IPropertyFluidFilter<FluidPipeProperties> {

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    private int throughput;
    private final int tanks;

    private int maxFluidTemperature;
    private boolean gasProof;
    private boolean cryoProof;
    private boolean plasmaProof;
    private double minPH;
    private double maxPH;
    private int burstPressure;
    private int friction;

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, 1);
    }

    /**
     * Should only be called from
     * {@link gregtech.common.pipelike.fluidpipe.FluidPipeType#modifyProperties(FluidPipeProperties)}
     */
    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof, int tanks) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, tanks, 0.0, 14.0);
    }

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof, int tanks, double minPH, double maxPH) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, tanks, minPH, maxPH, 5000, 10);
    }

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof, int tanks, double minPH, double maxPH,
                               int burstPressure, int friction) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.tanks = tanks;
        this.burstPressure = burstPressure;
        this.friction = friction;
        setPHRange(minPH, maxPH);
    }

    /**
     * Default property constructor.
     */
    public FluidPipeProperties() {
        this(300, 1, false, false, false, false);
    }

    public FluidPipeProperties copyWith(int throughput, int tanks) {
        FluidPipeProperties copy = new FluidPipeProperties(maxFluidTemperature, throughput, gasProof,
                isAcidProof(), cryoProof, plasmaProof, tanks, minPH, maxPH, burstPressure, friction);
        for (FluidAttribute attribute : containmentPredicate.keySet()) {
            copy.setCanContain(attribute, containmentPredicate.getBoolean(attribute));
        }
        return copy;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }

        if (properties.hasProperty(PropertyKey.ITEM_PIPE)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Fluid and Item Pipe Property, which is not allowed!");
        }
    }

    public int getTanks() {
        return tanks;
    }

    public int getThroughput() {
        return throughput;
    }

    public FluidPipeProperties setThroughput(int throughput) {
        this.throughput = throughput;
        return this;
    }

    public int getBurstPressure() {
        return burstPressure;
    }

    public FluidPipeProperties setBurstPressure(int burstPressure) {
        this.burstPressure = burstPressure;
        return this;
    }

    public int getFriction() {
        return friction;
    }

    public FluidPipeProperties setFriction(int friction) {
        this.friction = friction;
        return this;
    }

    @Override
    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    public FluidPipeProperties setMaxFluidTemperature(int maxFluidTemperature) {
        this.maxFluidTemperature = maxFluidTemperature;
        return this;
    }

    public double getMinPH() {
        return minPH;
    }

    public double getMaxPH() {
        return maxPH;
    }

    public boolean canContainPH(double pH) {
        return pH >= minPH && pH <= maxPH;
    }

    public FluidPipeProperties setPHRange(double minPH, double maxPH) {
        this.minPH = Math.max(0.0, Math.min(14.0, minPH));
        this.maxPH = Math.max(0.0, Math.min(14.0, maxPH));
        if (this.minPH > this.maxPH) {
            double tmp = this.minPH;
            this.minPH = this.maxPH;
            this.maxPH = tmp;
        }
        return this;
    }

    public String getPHResistanceTierKey() {
        if (minPH <= 0.0 && maxPH >= 14.0) return "gregtech.fluid_pipe.ph_tier.universal";
        if (minPH <= 1.0 && maxPH >= 13.0) return "gregtech.fluid_pipe.ph_tier.extreme";
        if (minPH <= 2.0 && maxPH >= 12.0) return "gregtech.fluid_pipe.ph_tier.industrial";
        if (minPH <= 4.0 && maxPH >= 10.0) return "gregtech.fluid_pipe.ph_tier.reinforced";
        return "gregtech.fluid_pipe.ph_tier.standard";
    }

    @Override
    public boolean canContain(@NotNull FluidState state) {
        return switch (state) {
            case LIQUID -> true;
            case GAS -> gasProof;
            case PLASMA -> plasmaProof;
        };
    }

    @Override
    public boolean canContain(@NotNull FluidAttribute attribute) {
        return containmentPredicate.getBoolean(attribute);
    }

    @Override
    public FluidPipeProperties setCanContain(@NotNull FluidAttribute attribute, boolean canContain) {
        this.containmentPredicate.put(attribute, canContain);
        return this;
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }

    public boolean isGasProof() {
        return gasProof;
    }

    public FluidPipeProperties setGasProof(boolean gasProof) {
        this.gasProof = gasProof;
        return this;
    }

    public boolean isAcidProof() {
        return canContain(FluidAttributes.ACID);
    }

    public boolean isCorrosiveProof() {
        return canContain(FluidAttributes.CORROSIVE);
    }

    public boolean isToxicProof() {
        return canContain(FluidAttributes.TOXIC);
    }

    public boolean isRadioactiveProof() {
        return canContain(FluidAttributes.RADIOACTIVE);
    }

    public boolean isFlammableProof() {
        return canContain(FluidAttributes.FLAMMABLE);
    }

    public boolean isSludgeProof() {
        return canContain(FluidAttributes.SLUDGE);
    }

    public boolean isCryoProof() {
        return cryoProof;
    }

    public FluidPipeProperties setCryoProof(boolean cryoProof) {
        this.cryoProof = cryoProof;
        return this;
    }

    public boolean isPlasmaProof() {
        return plasmaProof;
    }

    public FluidPipeProperties setPlasmaProof(boolean plasmaProof) {
        this.plasmaProof = plasmaProof;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidPipeProperties that)) return false;
        return getThroughput() == that.getThroughput() &&
                getTanks() == that.getTanks() &&
                getMaxFluidTemperature() == that.getMaxFluidTemperature() &&
                isGasProof() == that.isGasProof() &&
                isCryoProof() == that.isCryoProof() &&
                isPlasmaProof() == that.isPlasmaProof() &&
            Double.compare(that.getMinPH(), getMinPH()) == 0 &&
            Double.compare(that.getMaxPH(), getMaxPH()) == 0 &&
                getBurstPressure() == that.getBurstPressure() &&
                getFriction() == that.getFriction() &&
                containmentPredicate.equals(that.containmentPredicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getThroughput(), getTanks(), getMaxFluidTemperature(), gasProof, cryoProof, plasmaProof,
            getMinPH(), getMaxPH(), getBurstPressure(), getFriction(), containmentPredicate);
    }

    @Override
    public String toString() {
        return "FluidPipeProperties{" +
                "throughput=" + throughput +
                ", tanks=" + tanks +
                ", maxFluidTemperature=" + maxFluidTemperature +
                ", gasProof=" + gasProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                ", minPH=" + minPH +
                ", maxPH=" + maxPH +
                ", burstPressure=" + burstPressure +
                ", friction=" + friction +
                ", containmentPredicate=" + containmentPredicate +
                '}';
    }
}

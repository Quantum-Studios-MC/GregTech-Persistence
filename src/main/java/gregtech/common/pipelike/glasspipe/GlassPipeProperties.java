package gregtech.common.pipelike.glasspipe;

import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public class GlassPipeProperties implements IPropertyFluidFilter<GlassPipeProperties> {

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    private final int maxFluidTemperature;
    private final int throughput;
    private final boolean gasProof;
    private final boolean cryoProof;
    private final boolean plasmaProof;
    private final String tierName;

    public GlassPipeProperties(String tierName, int maxFluidTemperature, int throughput,
                               boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        this.tierName = tierName;
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
    }

    public String getTierName() {
        return tierName;
    }

    public int getThroughput() {
        return throughput;
    }

    @Override
    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
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
    public GlassPipeProperties setCanContain(@NotNull FluidAttribute attribute, boolean canContain) {
        this.containmentPredicate.put(attribute, canContain);
        return this;
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }

    @Override
    public boolean isGasProof() {
        return gasProof;
    }

    @Override
    public boolean isCryoProof() {
        return cryoProof;
    }

    @Override
    public boolean isPlasmaProof() {
        return plasmaProof;
    }

    public boolean isAcidProof() {
        return canContain(FluidAttributes.ACID);
    }
}

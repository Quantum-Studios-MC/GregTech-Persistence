package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;

import net.minecraft.nbt.NBTTagCompound;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> {

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    @Override
    protected void writeNodeData(FluidPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.setInteger("throughput", nodeData.getThroughput());
        tagCompound.setBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.setBoolean("acid_proof", nodeData.isAcidProof());
        tagCompound.setBoolean("corrosive_proof", nodeData.isCorrosiveProof());
        tagCompound.setBoolean("toxic_proof", nodeData.isToxicProof());
        tagCompound.setBoolean("radioactive_proof", nodeData.isRadioactiveProof());
        tagCompound.setBoolean("flammable_proof", nodeData.isFlammableProof());
        tagCompound.setBoolean("sludge_proof", nodeData.isSludgeProof());
        tagCompound.setBoolean("cryo_proof", nodeData.isCryoProof());
        tagCompound.setBoolean("plasma_proof", nodeData.isPlasmaProof());
        tagCompound.setInteger("channels", nodeData.getTanks());
        tagCompound.setDouble("min_ph", nodeData.getMinPH());
        tagCompound.setDouble("max_ph", nodeData.getMaxPH());
        tagCompound.setInteger("burst_pressure", nodeData.getBurstPressure());
        tagCompound.setInteger("friction", nodeData.getFriction());
    }

    @Override
    protected FluidPipeProperties readNodeData(NBTTagCompound tagCompound) {
        int maxTemperature = tagCompound.getInteger("max_temperature");
        int throughput = tagCompound.getInteger("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        boolean acidProof = tagCompound.getBoolean("acid_proof");
        boolean corrosiveProof = tagCompound.getBoolean("corrosive_proof");
        boolean toxicProof = tagCompound.getBoolean("toxic_proof");
        boolean radioactiveProof = tagCompound.getBoolean("radioactive_proof");
        boolean flammableProof = tagCompound.getBoolean("flammable_proof");
        boolean sludgeProof = tagCompound.getBoolean("sludge_proof");
        boolean cryoProof = tagCompound.getBoolean("cryo_proof");
        boolean plasmaProof = tagCompound.getBoolean("plasma_proof");
        int channels = tagCompound.getInteger("channels");
        double minPH = tagCompound.hasKey("min_ph") ? tagCompound.getDouble("min_ph") : 0.0;
        double maxPH = tagCompound.hasKey("max_ph") ? tagCompound.getDouble("max_ph") : 14.0;
        int burstPressure = tagCompound.hasKey("burst_pressure") ? tagCompound.getInteger("burst_pressure") : 5000;
        int friction = tagCompound.hasKey("friction") ? tagCompound.getInteger("friction") : 10;

        FluidPipeProperties properties = new FluidPipeProperties(maxTemperature, throughput, gasProof, acidProof,
            cryoProof, plasmaProof, channels, minPH, maxPH, burstPressure, friction);
        properties.setCanContain(gregtech.api.fluids.attribute.FluidAttributes.CORROSIVE, corrosiveProof);
        properties.setCanContain(gregtech.api.fluids.attribute.FluidAttributes.TOXIC, toxicProof);
        properties.setCanContain(gregtech.api.fluids.attribute.FluidAttributes.RADIOACTIVE, radioactiveProof);
        properties.setCanContain(gregtech.api.fluids.attribute.FluidAttributes.FLAMMABLE, flammableProof);
        properties.setCanContain(gregtech.api.fluids.attribute.FluidAttributes.SLUDGE, sludgeProof);
        return properties;
    }
}

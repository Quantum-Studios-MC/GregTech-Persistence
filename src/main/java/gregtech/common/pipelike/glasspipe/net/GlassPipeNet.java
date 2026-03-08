package gregtech.common.pipelike.glasspipe.net;

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.common.pipelike.glasspipe.GlassPipeProperties;

import net.minecraft.nbt.NBTTagCompound;

public class GlassPipeNet extends PipeNet<GlassPipeProperties> {

    public GlassPipeNet(WorldPipeNet<GlassPipeProperties, GlassPipeNet> world) {
        super(world);
    }

    @Override
    protected void writeNodeData(GlassPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setString("tier", nodeData.getTierName());
        tagCompound.setInteger("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.setInteger("throughput", nodeData.getThroughput());
        tagCompound.setBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.setBoolean("acid_proof", nodeData.isAcidProof());
        tagCompound.setBoolean("cryo_proof", nodeData.isCryoProof());
        tagCompound.setBoolean("plasma_proof", nodeData.isPlasmaProof());
    }

    @Override
    protected GlassPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return new GlassPipeProperties(
                tagCompound.getString("tier"),
                tagCompound.getInteger("max_temperature"),
                tagCompound.getInteger("throughput"),
                tagCompound.getBoolean("gas_proof"),
                tagCompound.getBoolean("acid_proof"),
                tagCompound.getBoolean("cryo_proof"),
                tagCompound.getBoolean("plasma_proof"));
    }
}

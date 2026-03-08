package gregtech.common.pipelike.glasspipe.net;

import gregtech.api.pipenet.WorldPipeNet;
import gregtech.common.pipelike.glasspipe.GlassPipeProperties;

import net.minecraft.world.World;

public class WorldGlassPipeNet extends WorldPipeNet<GlassPipeProperties, GlassPipeNet> {

    private static final String DATA_ID_BASE = "gregtech.glass_pipe_net";

    public static WorldGlassPipeNet getWorldPipeNet(World world) {
        String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldGlassPipeNet netWorldData = (WorldGlassPipeNet) world.loadData(WorldGlassPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldGlassPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }

    public WorldGlassPipeNet(String name) {
        super(name);
    }

    @Override
    protected GlassPipeNet createNetInstance() {
        return new GlassPipeNet(this);
    }
}

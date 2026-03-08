package gregtech.common.gps;

import gregtech.common.blocks.BlockSignalBeacon;
import gregtech.common.blocks.TileEntitySignalBeacon;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class BeaconTracker {

    public static List<BeaconData> findBeacons(World world, BlockPos center, int range) {
        List<BeaconData> result = new ArrayList<>();
        int chunkRange = (range >> 4) + 1;
        int centerCX = center.getX() >> 4;
        int centerCZ = center.getZ() >> 4;

        for (int cx = centerCX - chunkRange; cx <= centerCX + chunkRange; cx++) {
            for (int cz = centerCZ - chunkRange; cz <= centerCZ + chunkRange; cz++) {
                if (!world.isChunkGeneratedAt(cx, cz)) continue;
                Chunk chunk = world.getChunk(cx, cz);
                for (TileEntity te : chunk.getTileEntityMap().values()) {
                    if (!(te instanceof TileEntitySignalBeacon)) continue;
                    TileEntitySignalBeacon beacon = (TileEntitySignalBeacon) te;
                    if (!beacon.isPowered()) continue;

                    double dist = center.getDistance(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
                    if (dist <= range && dist <= beacon.getBroadcastRange()) {
                        result.add(new BeaconData(
                                te.getPos(),
                                beacon.getBeaconName(),
                                (int) dist));
                    }
                }
            }
        }
        return result;
    }

    public static class BeaconData {

        public final BlockPos pos;
        public final String name;
        public final int distance;

        public BeaconData(BlockPos pos, String name, int distance) {
            this.pos = pos;
            this.name = name;
            this.distance = distance;
        }
    }
}

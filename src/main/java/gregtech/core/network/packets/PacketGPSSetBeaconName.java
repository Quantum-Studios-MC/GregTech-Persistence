package gregtech.core.network.packets;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.common.blocks.TileEntitySignalBeacon;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

public class PacketGPSSetBeaconName implements IPacket, IServerExecutor {

    private BlockPos pos;
    private String name;

    @SuppressWarnings("unused")
    public PacketGPSSetBeaconName() {}

    public PacketGPSSetBeaconName(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    @Override
    public void encode(@NotNull PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeString(name);
    }

    @Override
    public void decode(@NotNull PacketBuffer buf) {
        pos = buf.readBlockPos();
        name = buf.readString(32);
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.player;
        if (player.getDistanceSq(pos) > 64) return;
        TileEntity te = player.world.getTileEntity(pos);
        if (!(te instanceof TileEntitySignalBeacon)) return;
        String safeName = name;
        if (safeName.length() > 32) safeName = safeName.substring(0, 32);
        ((TileEntitySignalBeacon) te).setBeaconName(safeName);
    }
}

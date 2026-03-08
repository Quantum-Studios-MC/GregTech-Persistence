package gregtech.core.network.packets;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.common.gps.WaypointManager;
import gregtech.common.items.MetaItems;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import org.jetbrains.annotations.NotNull;

public class PacketGPSRemoveWaypoint implements IPacket, IServerExecutor {

    private int hand;
    private int index;

    @SuppressWarnings("unused")
    public PacketGPSRemoveWaypoint() {}

    public PacketGPSRemoveWaypoint(int hand, int index) {
        this.hand = hand;
        this.index = index;
    }

    @Override
    public void encode(@NotNull PacketBuffer buf) {
        buf.writeVarInt(hand);
        buf.writeVarInt(index);
    }

    @Override
    public void decode(@NotNull PacketBuffer buf) {
        hand = buf.readVarInt();
        index = buf.readVarInt();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.player;
        EnumHand enumHand = hand == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        ItemStack gps = player.getHeldItem(enumHand);
        if (!MetaItems.GPS_DEVICE.isItemEqual(gps)) return;
        if (index < 0 || index >= 64) return;
        WaypointManager.removeWaypoint(gps, index);
    }
}

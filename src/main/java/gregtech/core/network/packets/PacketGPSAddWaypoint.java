package gregtech.core.network.packets;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
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

public class PacketGPSAddWaypoint implements IPacket, IServerExecutor {

    private int hand;
    private int x, z, dimension;
    private String name;
    private int color;

    @SuppressWarnings("unused")
    public PacketGPSAddWaypoint() {}

    public PacketGPSAddWaypoint(int hand, int x, int z, int dimension, String name, int color) {
        this.hand = hand;
        this.x = x;
        this.z = z;
        this.dimension = dimension;
        this.name = name;
        this.color = color;
    }

    @Override
    public void encode(@NotNull PacketBuffer buf) {
        buf.writeVarInt(hand);
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(dimension);
        buf.writeString(name);
        buf.writeInt(color);
    }

    @Override
    public void decode(@NotNull PacketBuffer buf) {
        hand = buf.readVarInt();
        x = buf.readInt();
        z = buf.readInt();
        dimension = buf.readInt();
        name = buf.readString(32);
        color = buf.readInt();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.player;
        EnumHand enumHand = hand == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        ItemStack gps = player.getHeldItem(enumHand);
        if (!MetaItems.GPS_DEVICE.isItemEqual(gps)) return;

        IElectricItem electric = gps.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electric == null || electric.getCharge() < 500) return;

        String safeName = name;
        if (safeName.length() > 32) safeName = safeName.substring(0, 32);

        electric.discharge(500, 1, true, false, false);
        WaypointManager.addWaypoint(gps, x, z, dimension, safeName, color);
    }
}

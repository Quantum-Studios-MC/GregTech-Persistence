package gregtech.common.blocks;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.Nullable;

public class TileEntitySignalBeacon extends TileEntity implements ITickable, IEnergyContainer {

    private long energyStored;
    private static final long CAPACITY = 32000L;
    private static final long DRAIN_PER_TICK = 1L;
    private static final int BROADCAST_RANGE = 256;
    private String beaconName = "";

    @Override
    public void update() {
        if (world.isRemote) return;
        if (energyStored >= DRAIN_PER_TICK) {
            energyStored -= DRAIN_PER_TICK;
        }
    }

    public boolean isPowered() {
        return energyStored > 0;
    }

    public String getBeaconName() {
        return beaconName;
    }

    public void setBeaconName(String name) {
        this.beaconName = name;
        markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    public int getBroadcastRange() {
        return BROADCAST_RANGE;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        long canAccept = CAPACITY - energyStored;
        if (canAccept <= 0) return 0;
        long energyReceived = Math.min(voltage * amperage, canAccept);
        energyStored += energyReceived;
        return amperage;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public long changeEnergy(long amount) {
        long old = energyStored;
        energyStored = Math.max(0, Math.min(CAPACITY, energyStored + amount));
        return energyStored - old;
    }

    @Override
    public long getEnergyStored() {
        return energyStored;
    }

    @Override
    public long getEnergyCapacity() {
        return CAPACITY;
    }

    @Override
    public long getInputAmperage() {
        return 1;
    }

    @Override
    public long getInputVoltage() {
        return 32;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong("Energy", energyStored);
        compound.setString("BeaconName", beaconName);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        energyStored = compound.getLong("Energy");
        beaconName = compound.getString("BeaconName");
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setString("BeaconName", beaconName);
        tag.setLong("Energy", energyStored);
        return tag;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.getNbtCompound();
        beaconName = tag.getString("BeaconName");
        energyStored = tag.getLong("Energy");
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        beaconName = tag.getString("BeaconName");
        energyStored = tag.getLong("Energy");
    }
}

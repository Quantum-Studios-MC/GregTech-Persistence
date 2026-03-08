package gregtech.common.gps;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class WaypointManager {

    private static final String WAYPOINTS_TAG = "GPSWaypoints";
    private static final String DEATH_TAG = "GPSDeathPoint";
    private static final int MAX_WAYPOINTS = 64;

    public static NBTTagList getWaypoints(ItemStack stack) {
        if (!stack.hasTagCompound()) return new NBTTagList();
        return stack.getTagCompound().getTagList(WAYPOINTS_TAG, Constants.NBT.TAG_COMPOUND);
    }

    public static boolean addWaypoint(ItemStack stack, int x, int z, int dimension, String name, int color) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagList list = stack.getTagCompound().getTagList(WAYPOINTS_TAG, Constants.NBT.TAG_COMPOUND);
        if (list.tagCount() >= MAX_WAYPOINTS) return false;

        NBTTagCompound wp = new NBTTagCompound();
        wp.setInteger("x", x);
        wp.setInteger("z", z);
        wp.setInteger("dim", dimension);
        wp.setString("name", name);
        wp.setInteger("color", color);
        list.appendTag(wp);
        stack.getTagCompound().setTag(WAYPOINTS_TAG, list);
        return true;
    }

    public static void removeWaypoint(ItemStack stack, int index) {
        if (!stack.hasTagCompound()) return;
        NBTTagList list = stack.getTagCompound().getTagList(WAYPOINTS_TAG, Constants.NBT.TAG_COMPOUND);
        if (index >= 0 && index < list.tagCount()) {
            list.removeTag(index);
            stack.getTagCompound().setTag(WAYPOINTS_TAG, list);
        }
    }

    public static void setDeathPoint(ItemStack stack, int x, int z, int dimension) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound death = new NBTTagCompound();
        death.setInteger("x", x);
        death.setInteger("z", z);
        death.setInteger("dim", dimension);
        death.setBoolean("active", true);
        stack.getTagCompound().setTag(DEATH_TAG, death);
    }

    public static NBTTagCompound getDeathPoint(ItemStack stack) {
        if (!stack.hasTagCompound()) return null;
        if (!stack.getTagCompound().hasKey(DEATH_TAG)) return null;
        NBTTagCompound death = stack.getTagCompound().getCompoundTag(DEATH_TAG);
        if (!death.getBoolean("active")) return null;
        return death;
    }

    public static void dismissDeathPoint(ItemStack stack) {
        if (!stack.hasTagCompound()) return;
        if (stack.getTagCompound().hasKey(DEATH_TAG)) {
            stack.getTagCompound().getCompoundTag(DEATH_TAG).setBoolean("active", false);
        }
    }

    public static int getWaypointX(NBTTagCompound wp) { return wp.getInteger("x"); }
    public static int getWaypointZ(NBTTagCompound wp) { return wp.getInteger("z"); }
    public static int getWaypointDim(NBTTagCompound wp) { return wp.getInteger("dim"); }
    public static String getWaypointName(NBTTagCompound wp) { return wp.getString("name"); }
    public static int getWaypointColor(NBTTagCompound wp) { return wp.getInteger("color"); }
}

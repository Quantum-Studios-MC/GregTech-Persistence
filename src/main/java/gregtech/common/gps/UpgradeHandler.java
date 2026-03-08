package gregtech.common.gps;

import gregtech.common.items.MetaItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

public class UpgradeHandler {

    public enum Upgrade {

        RANGE_1("range_1", 0, 128),
        RANGE_2("range_2", 0, 256),
        RANGE_3("range_3", 0, 512),
        ENTITY_RADAR("entity_radar", 5, 0),
        PLAYER_RADAR("player_radar", 5, 0),
        UNDERGROUND("underground", 15, 0),
        ORE_OVERLAY("ore_overlay", 20, 0),
        DIMENSION_MAP("dimension_map", 0, 0);

        public final String nbtKey;
        public final int extraDrain;
        public final int radius;

        Upgrade(String nbtKey, int extraDrain, int radius) {
            this.nbtKey = nbtKey;
            this.extraDrain = extraDrain;
            this.radius = radius;
        }
    }

    private static final String UPGRADE_TAG = "GPSUpgrades";
    private static final long BASE_DRAIN = 10;

    public static NBTTagCompound getUpgradeTag(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(UPGRADE_TAG)) return new NBTTagCompound();
        return root.getCompoundTag(UPGRADE_TAG);
    }

    private static void setUpgradeTag(ItemStack stack, NBTTagCompound upgrades) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setTag(UPGRADE_TAG, upgrades);
    }

    public static boolean hasUpgrade(ItemStack stack, Upgrade upgrade) {
        return getUpgradeTag(stack).getBoolean(upgrade.nbtKey);
    }

    public static int getMapRadius(ItemStack stack) {
        NBTTagCompound tag = getUpgradeTag(stack);
        if (tag.getBoolean(Upgrade.RANGE_3.nbtKey)) return 512;
        if (tag.getBoolean(Upgrade.RANGE_2.nbtKey)) return 256;
        if (tag.getBoolean(Upgrade.RANGE_1.nbtKey)) return 128;
        return 64;
    }

    public static long calculateEUDrain(ItemStack stack) {
        NBTTagCompound tag = getUpgradeTag(stack);
        long drain = BASE_DRAIN;
        for (Upgrade upgrade : Upgrade.values()) {
            if (tag.getBoolean(upgrade.nbtKey)) {
                drain += upgrade.extraDrain;
            }
        }
        return drain;
    }

    public static boolean tryInstallUpgrade(ItemStack gps, ItemStack component, EntityPlayer player) {
        Upgrade target = matchUpgrade(component);
        if (target == null) {
            player.sendMessage(new TextComponentTranslation("gtgps.message.invalid_upgrade"));
            return false;
        }

        NBTTagCompound tag = getUpgradeTag(gps);
        if (tag.getBoolean(target.nbtKey)) {
            player.sendMessage(new TextComponentTranslation("gtgps.message.already_installed"));
            return false;
        }

        tag.setBoolean(target.nbtKey, true);
        setUpgradeTag(gps, tag);
        component.shrink(1);
        player.sendMessage(new TextComponentTranslation("gtgps.message.upgrade_installed",
                target.nbtKey));
        return true;
    }

    private static Upgrade matchUpgrade(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.isItemEqual(MetaItems.SENSOR_LV.getStackForm())) return Upgrade.RANGE_1;
        if (stack.isItemEqual(MetaItems.SENSOR_MV.getStackForm())) return Upgrade.RANGE_2;
        if (stack.isItemEqual(MetaItems.SENSOR_HV.getStackForm())) return Upgrade.RANGE_3;
        if (stack.isItemEqual(MetaItems.EMITTER_MV.getStackForm())) return Upgrade.ENTITY_RADAR;
        if (stack.isItemEqual(MetaItems.EMITTER_HV.getStackForm())) return Upgrade.PLAYER_RADAR;
        if (stack.isItemEqual(MetaItems.FIELD_GENERATOR_HV.getStackForm())) return Upgrade.UNDERGROUND;
        if (stack.isItemEqual(MetaItems.FIELD_GENERATOR_EV.getStackForm())) return Upgrade.ORE_OVERLAY;
        if (stack.isItemEqual(MetaItems.TOOL_DATA_ORB.getStackForm())) return Upgrade.DIMENSION_MAP;
        return null;
    }

    public static boolean isNorthLocked(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean("NorthLocked");
    }

    public static void toggleNorthLock(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        boolean current = stack.getTagCompound().getBoolean("NorthLocked");
        stack.getTagCompound().setBoolean("NorthLocked", !current);
    }
}

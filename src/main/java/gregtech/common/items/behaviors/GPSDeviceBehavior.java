package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.gps.UpgradeHandler;
import gregtech.common.gps.WaypointManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class GPSDeviceBehavior implements IItemBehaviour {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            ItemStack offhand = player.getHeldItem(hand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            if (!offhand.isEmpty() && !world.isRemote) {
                if (UpgradeHandler.tryInstallUpgrade(stack, offhand, player)) {
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            }
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        IElectricItem electric = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electric == null || electric.getCharge() <= 0) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (world.isRemote) {
            openMapGui(player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @SideOnly(Side.CLIENT)
    private void openMapGui(EntityPlayer player, EnumHand hand) {
        Minecraft.getMinecraft().displayGuiScreen(
                new gregtech.client.gps.GPSMapGui(player, hand));
    }

    @Override
    public void onUpdate(ItemStack stack, Entity entity) {
        if (entity.world.isRemote || !(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        // Only drain EU when in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (player.inventory.getStackInSlot(i) == stack) {
                slot = i;
                break;
            }
        }
        if (slot < 0) return;

        IElectricItem electric = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electric == null || electric.getCharge() <= 0) return;

        long drain = UpgradeHandler.calculateEUDrain(stack);
        if (drain > 0) {
            electric.discharge(drain, GTValues.LV, true, false, false);
        }
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        lines.add(I18n.format("gtgps.tooltip.drain",
                UpgradeHandler.calculateEUDrain(stack)));

        NBTTagCompound tag = UpgradeHandler.getUpgradeTag(stack);
        for (UpgradeHandler.Upgrade upgrade : UpgradeHandler.Upgrade.values()) {
            if (tag.getBoolean(upgrade.nbtKey)) {
                lines.add("\u00a7a\u2714 " + I18n.format("gtgps.upgrade." + upgrade.nbtKey));
            }
        }

        int wpCount = WaypointManager.getWaypoints(stack).tagCount();
        lines.add(I18n.format("gtgps.tooltip.waypoints", wpCount));

        lines.add(I18n.format("gtgps.tooltip.sneak_install"));
    }
}

package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Flint and Tinder behavior ported from GT6 - ignites fire on right-click.
 */
public class IgniteBehavior implements IToolBehavior {

    public static final IgniteBehavior INSTANCE = new IgniteBehavior();

    private IgniteBehavior() {/**/}

    @NotNull
    @Override
    public EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                      @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        BlockPos offset = pos.offset(facing);
        ItemStack stack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(offset, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        if (world.isAirBlock(offset)) {
            if (!world.isRemote) {
                world.setBlockState(offset, Blocks.FIRE.getDefaultState());
                ToolHelper.damageItem(stack, player, 1);
            }
            world.playSound(null, offset, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS,
                    1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
            player.swingArm(hand);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.ignite"));
    }
}

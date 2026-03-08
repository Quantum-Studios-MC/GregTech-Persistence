package gregtech.common.items.tool;

import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
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
 * Chisel behavior ported from GT6 - cycles stone block variants on right-click.
 */
public class ChiselBehavior implements IToolBehavior {

    public static final ChiselBehavior INSTANCE = new ChiselBehavior();

    private ChiselBehavior() {/**/}

    @NotNull
    @Override
    public EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                      @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        ItemStack stack = player.getHeldItem(hand);
        IBlockState newState = getChiseledState(state, block);

        if (newState != null) {
            if (!world.isRemote) {
                world.setBlockState(pos, newState);
                ToolHelper.damageItem(stack, player, 1);
            }
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
            player.swingArm(hand);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    private static IBlockState getChiseledState(IBlockState state, Block block) {
        if (block == Blocks.STONE) {
            // stone -> chiseled stone brick
            return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT,
                    BlockStoneBrick.EnumType.CHISELED);
        }
        if (block == Blocks.STONEBRICK) {
            int meta = block.getMetaFromState(state);
            switch (meta) {
                case 0: // normal -> chiseled
                    return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT,
                            BlockStoneBrick.EnumType.CHISELED);
                case 1: // mossy -> mossy cobble
                    return Blocks.MOSSY_COBBLESTONE.getDefaultState();
                case 2: // cracked -> cobble
                    return Blocks.COBBLESTONE.getDefaultState();
                case 3: // chiseled -> cracked
                    return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT,
                            BlockStoneBrick.EnumType.CRACKED);
                default:
                    return null;
            }
        }
        if (block instanceof BlockSilverfish) {
            // reveal silverfish blocks
            return Blocks.STONEBRICK.getDefaultState();
        }
        return null;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.chisel"));
    }
}

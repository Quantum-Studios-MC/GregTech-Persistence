package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.event.world.BlockEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Plow behavior ported from GT6 - area harvests snow, fire, and lightweight plant blocks in a 3x3x3 area.
 */
public class PlowBehavior implements IToolBehavior {

    public static final PlowBehavior INSTANCE = new PlowBehavior();

    private PlowBehavior() {/**/}

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.plow"));
    }
}

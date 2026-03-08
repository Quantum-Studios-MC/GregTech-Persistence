package gregtech.common.blocks;

import gregtech.api.GTValues;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class BlockSignalBeacon extends Block {

    public BlockSignalBeacon() {
        super(Material.IRON);
        setTranslationKey("gtgps.signal_beacon");
        setHardness(3.0F);
        setResistance(10.0F);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.REDSTONE);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntitySignalBeacon();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntitySignalBeacon)) return false;
        TileEntitySignalBeacon beacon = (TileEntitySignalBeacon) te;

        if (player.isSneaking()) {
            String currentName = beacon.getBeaconName();
            boolean powered = beacon.isPowered();
            player.sendMessage(new TextComponentTranslation("gtgps.message.beacon_status",
                    currentName.isEmpty() ? "Unnamed" : currentName,
                    powered ? "\u00a7aPowered" : "\u00a7cUnpowered",
                    beacon.getEnergyStored()));
        } else {
            net.minecraft.item.ItemStack held = player.getHeldItem(hand);
            if (held.hasDisplayName()) {
                beacon.setBeaconName(held.getDisplayName());
                player.sendMessage(new TextComponentString(
                        "\u00a7bBeacon named: \u00a7f" + held.getDisplayName()));
            } else {
                player.sendMessage(new TextComponentTranslation("gtgps.message.beacon_name_hint"));
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
    }
}

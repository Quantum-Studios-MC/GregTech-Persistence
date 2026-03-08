package gregtech.common.pipelike.glasspipe;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.client.renderer.pipe.PipeRenderProperties;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelike.glasspipe.net.WorldGlassPipeNet;
import gregtech.common.pipelike.glasspipe.tile.TileEntityGlassPipe;
import gregtech.common.pipelike.glasspipe.tile.TileEntityGlassPipeTickable;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockGlassPipe extends BlockPipe<GlassPipeType, GlassPipeProperties, WorldGlassPipeNet> {

    private final GlassPipeType pipeType;

    public BlockGlassPipe(@NotNull GlassPipeType pipeType) {
        this.pipeType = pipeType;
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
        setHarvestLevel(ToolClasses.PICKAXE, 1);
        setSoundType(SoundType.GLASS);
        setHardness(1.0F);
        setResistance(2.0F);
        this.useNeighborBrightness = true;
    }

    @Override
    public Class<GlassPipeType> getPipeTypeClass() {
        return GlassPipeType.class;
    }

    @Override
    public WorldGlassPipeNet getWorldPipeNet(World world) {
        return WorldGlassPipeNet.getWorldPipeNet(world);
    }

    @Override
    public TileEntityPipeBase<GlassPipeType, GlassPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return new TileEntityGlassPipeTickable(); // glass pipes are always ticking for fluid transfer
    }

    @Override
    public GlassPipeProperties createProperties(@NotNull IPipeTile<GlassPipeType, GlassPipeProperties> pipeTile) {
        GlassPipeType type = pipeTile.getPipeType();
        if (type == null) return getFallbackType();
        return type.getProperties();
    }

    @Override
    public GlassPipeProperties createItemProperties(@NotNull ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlockGlassPipe pipe) {
            return ((BlockGlassPipe) pipe.getBlock()).pipeType.getProperties();
        }
        return null;
    }

    @Override
    public ItemStack getDropItem(IPipeTile<GlassPipeType, GlassPipeProperties> pipeTile) {
        return new ItemStack(this, 1, pipeType.ordinal());
    }

    @Override
    protected GlassPipeProperties getFallbackType() {
        return GlassPipeType.BASIC_NORMAL.getProperties();
    }

    @Override
    public GlassPipeType getPipeType() {
        return pipeType;
    }

    @Override
    public void setTileEntityData(@NotNull TileEntityPipeBase<GlassPipeType, GlassPipeProperties> pipeTile,
                                  ItemStack itemStack) {
        pipeTile.setPipeData(this, pipeType);
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, this.pipeType.ordinal()));
    }

    @Override
    public boolean canPipesConnect(IPipeTile<GlassPipeType, GlassPipeProperties> selfTile, EnumFacing side,
                                   IPipeTile<GlassPipeType, GlassPipeProperties> sideTile) {
        return selfTile instanceof TileEntityGlassPipe && sideTile instanceof TileEntityGlassPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<GlassPipeType, GlassPipeProperties> selfTile, EnumFacing side,
                                         @Nullable TileEntity tile) {
        if (tile == null) return false;
        return tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) return false;
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockGlassPipe;
    }

    @Override
    protected @NotNull BlockStateContainer.Builder constructState(BlockStateContainer.@NotNull Builder builder) {
        return super.constructState(builder).add(PipeRenderProperties.FLUID_COLOR_PROPERTY);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    @NotNull
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean shouldSideBeRendered(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                        @NotNull BlockPos pos, @NotNull EnumFacing side) {
        IBlockState sideState = world.getBlockState(pos.offset(side));
        if (sideState.getBlock() instanceof BlockGlassPipe) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }
}

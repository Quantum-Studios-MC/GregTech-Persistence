package gregtech.common.blocks;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTUtility;
import gregtech.client.model.MaterialStateMapper;
import gregtech.client.model.modelfactories.MaterialBlockModelLoader;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.properties.PropertyMaterial;
import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Compressed dust block - 9 dusts compressed into one block.
 * Extends BlockFalling so it has gravity like sand.
 */
public abstract class BlockDustCompressed extends BlockFalling {

    public static BlockDustCompressed create(Material[] materials) {
        PropertyMaterial property = PropertyMaterial.create("variant", materials);
        return new BlockDustCompressed() {

            @NotNull
            @Override
            public PropertyMaterial getVariantProperty() {
                return property;
            }
        };
    }

    private BlockDustCompressed() {
        super(net.minecraft.block.material.Material.SAND);
        setTranslationKey("dust_compressed");
        setHardness(2.0f);
        setResistance(3.0f);
        setSoundType(SoundType.SAND);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_MATERIALS);
    }

    @NotNull
    public abstract PropertyMaterial getVariantProperty();

    @NotNull
    public ItemStack getItem(@NotNull Material material) {
        return GTUtility.toItem(getDefaultState().withProperty(getVariantProperty(), material));
    }

    @NotNull
    public Material getGtMaterial(int meta) {
        if (meta >= getVariantProperty().getAllowedValues().size()) {
            meta = 0;
        }
        return getVariantProperty().getAllowedValues().get(meta);
    }

    @NotNull
    public Material getGtMaterial(@NotNull ItemStack stack) {
        return getGtMaterial(stack.getMetadata());
    }

    @NotNull
    public Material getGtMaterial(@NotNull IBlockState state) {
        return state.getValue(getVariantProperty());
    }

    @NotNull
    public IBlockState getBlock(@NotNull Material material) {
        return getDefaultState().withProperty(getVariantProperty(), material);
    }

    @Override
    public String getHarvestTool(@NotNull IBlockState state) {
        return ToolClasses.SHOVEL;
    }

    @Override
    public int getHarvestLevel(@NotNull IBlockState state) {
        return getGtMaterial(state).getBlockHarvestLevel();
    }

    @NotNull
    @Override
    public SoundType getSoundType(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                  @Nullable Entity entity) {
        return SoundType.SAND;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: blockDust" + getGtMaterial(stack).toCamelCaseString());
        }
    }

    // ── BlockStateContainer / meta methods (from BlockMaterialBase) ─────

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getVariantProperty());
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getVariantProperty(), getGtMaterial(meta));
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState state) {
        return getVariantProperty().getAllowedValues().indexOf(state.getValue(getVariantProperty()));
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        for (IBlockState state : blockState.getValidStates()) {
            if (getGtMaterial(state) != Materials.NULL) {
                list.add(GTUtility.toItem(state));
            }
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public MapColor getMapColor(@NotNull IBlockState state, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return getMaterial(state).getMaterialMapColor();
    }

    @Override
    public int getFlammability(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        Material material = getGtMaterial(world.getBlockState(pos));
        if (material.hasFlag(MaterialFlags.FLAMMABLE)) {
            return 20;
        }
        return super.getFlammability(world, pos, face);
    }

    @Override
    public int getFireSpreadSpeed(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        Material material = getGtMaterial(world.getBlockState(pos));
        if (material.hasFlag(MaterialFlags.FLAMMABLE)) {
            return 5;
        }
        return super.getFireSpreadSpeed(world, pos, face);
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, new MaterialStateMapper(
                MaterialIconType.blockDust, s -> getGtMaterial(s).getMaterialIconSet()));
        for (IBlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state),
                    MaterialBlockModelLoader.registerItemModel(
                            MaterialIconType.blockDust,
                            getGtMaterial(state).getMaterialIconSet()));
        }
    }
}

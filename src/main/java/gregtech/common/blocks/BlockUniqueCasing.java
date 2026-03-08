package gregtech.common.blocks;

import gregtech.api.block.VariantActiveBlock;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public class BlockUniqueCasing extends VariantActiveBlock<BlockUniqueCasing.UniqueCasingType> {

    public BlockUniqueCasing() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("unique_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(UniqueCasingType.CRUSHING_WHEELS));
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        UniqueCasingType type = getState(state);
        if (type == UniqueCasingType.MOLYBDENUM_DISILICIDE_COIL || type == UniqueCasingType.HEAT_VENT) {
            if (layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT)
                return true;
        }
        return layer == BlockRenderLayer.SOLID;
    }

    public enum UniqueCasingType implements IStringSerializable {

        CRUSHING_WHEELS("crushing_wheels"),
        SLICING_BLADES("slicing_blades"),
        ELECTROLYTIC_CELL("electrolytic_cell"),
        HEAT_VENT("heat_vent"),
        MOLYBDENUM_DISILICIDE_COIL("molybdenum_disilicide_coil");

        private final String name;

        UniqueCasingType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }
    }
}

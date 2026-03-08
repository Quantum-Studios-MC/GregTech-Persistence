package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public class BlockLargeMultiblockCasing extends VariantBlock<BlockLargeMultiblockCasing.CasingType> {

    public BlockLargeMultiblockCasing() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("large_multiblock_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(net.minecraft.block.SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CasingType.MACERATOR_CASING));
    }

    public enum CasingType implements IStringSerializable {

        MACERATOR_CASING("macerator_casing"),
        HIGH_TEMPERATURE_CASING("high_temperature_casing"),
        ASSEMBLING_CASING("assembling_casing"),
        STRESS_PROOF_CASING("stress_proof_casing"),
        CORROSION_PROOF_CASING("corrosion_proof_casing"),
        VIBRATION_SAFE_CASING("vibration_safe_casing"),
        WATERTIGHT_CASING("watertight_casing"),
        CUTTER_CASING("cutter_casing"),
        NONCONDUCTING_CASING("nonconducting_casing"),
        MIXER_CASING("mixer_casing"),
        ENGRAVER_CASING("engraver_casing"),
        ATOMIC_CASING("atomic_casing"),
        STEAM_CASING("steam_casing");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }
    }
}

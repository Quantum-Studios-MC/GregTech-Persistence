package gregtech.common.worldgen.stoneLayer;

import gregtech.api.unification.material.Material;

import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Defines a stone layer type, ported from GT6's StoneLayer.
 * Each layer has a base stone block, its cobblestone and mossy variants,
 * an associated Material, and a list of ores that can spawn within it.
 */
public class StoneLayer {

    /** All registered stone layers. These are what the noise generator selects from. */
    public static final List<StoneLayer> LAYERS = new ArrayList<>();

    /**
     * Boundary ore definitions: when a layer of material A touches material B, these ores can spawn.
     * Key = MaterialPair (ordered), Value = list of ores that spawn at the boundary.
     */
    public static final Map<MaterialPair, List<StoneLayerOres>> BOUNDARY_ORES = new HashMap<>();

    /** Random small gem ore materials that can appear in any stone layer. */
    public static final List<Material> RANDOM_SMALL_GEM_ORES = new ArrayList<>();

    public final IBlockState stone;
    public final IBlockState cobble;
    public final IBlockState mossy;
    public final Material material;
    /** Ores that spawn within this layer */
    public final List<StoneLayerOres> ores = new ArrayList<>();

    /**
     * Creates and auto-registers a stone layer.
     */
    public StoneLayer(@NotNull IBlockState stone, @Nullable IBlockState cobble, @Nullable IBlockState mossy,
                      @NotNull Material material, StoneLayerOres... layerOres) {
        this.stone = stone;
        this.cobble = cobble;
        this.mossy = mossy;
        this.material = material;
        for (StoneLayerOres ore : layerOres) {
            if (ore != null) this.ores.add(ore);
        }
        LAYERS.add(this);
    }

    /**
     * Defines boundary ores between two material types. Ores spawn on BOTH sides.
     * Matches GT6's StoneLayer.bothsides(Material, Material, ...) semantics.
     */
    public static void bothSides(Material matA, Material matB, StoneLayerOres... ores) {
        MaterialPair ab = new MaterialPair(matA, matB);
        MaterialPair ba = new MaterialPair(matB, matA);
        for (StoneLayerOres ore : ores) {
            if (ore != null) {
                BOUNDARY_ORES.computeIfAbsent(ab, k -> new ArrayList<>()).add(ore);
                BOUNDARY_ORES.computeIfAbsent(ba, k -> new ArrayList<>()).add(ore);
            }
        }
    }

    /**
     * Defines boundary ores that only spawn when matTop is above matBottom.
     * Matches GT6's StoneLayer.topbottom(Material, Material, ...) semantics.
     */
    public static void topBottom(Material matTop, Material matBottom, StoneLayerOres... ores) {
        MaterialPair pair = new MaterialPair(matTop, matBottom);
        for (StoneLayerOres ore : ores) {
            if (ore != null) {
                BOUNDARY_ORES.computeIfAbsent(pair, k -> new ArrayList<>()).add(ore);
            }
        }
    }

    /**
     * Key for boundary ore lookup. Ordered pair of materials.
     */
    public static class MaterialPair {

        public final Material a;
        public final Material b;

        public MaterialPair(Material a, Material b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MaterialPair other)) return false;
            return a == other.a && b == other.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(a), System.identityHashCode(b));
        }
    }
}

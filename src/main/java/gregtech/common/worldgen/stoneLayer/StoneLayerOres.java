package gregtech.common.worldgen.stoneLayer;

import gregtech.api.unification.material.Material;

import net.minecraft.world.biome.Biome;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Defines an ore that can spawn within a stone layer, ported from GT6's StoneLayerOres.
 */
public class StoneLayerOres {

    public final Material material;
    /** Chance per block, out of 10000. E.g., 48 = 48/10000 = 0.48% */
    public final int chance;
    public final int minY;
    public final int maxY;
    /** If non-null, ore only generates in these biomes */
    @Nullable
    public final Set<Biome> targetBiomes;

    public StoneLayerOres(Material material, int chance, int minY, int maxY) {
        this(material, chance, minY, maxY, null);
    }

    public StoneLayerOres(Material material, int chance, int minY, int maxY, @Nullable Set<Biome> targetBiomes) {
        this.material = material;
        this.chance = chance;
        this.minY = minY;
        this.maxY = maxY;
        this.targetBiomes = targetBiomes;
    }

    /**
     * Checks if this ore should generate at the given Y level and biome.
     */
    public boolean check(int y, Biome biome) {
        if (y < minY || y > maxY) return false;
        return targetBiomes == null || targetBiomes.contains(biome);
    }
}

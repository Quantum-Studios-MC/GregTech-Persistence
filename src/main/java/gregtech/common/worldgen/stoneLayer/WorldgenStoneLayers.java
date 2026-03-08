package gregtech.common.worldgen.stoneLayer;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.ore.StoneTypes;
import gregtech.api.worldgen.config.OreConfigUtils;
import gregtech.common.blocks.BlockStoneLayerRock;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/**
 * World generator that replaces vanilla stone with GT6-style stone layers.
 * Must run at priority 0 (before ore gen at priority 1).
 *
 * Uses 3D cellular noise to create natural-looking boundaries between different
 * stone types (komatiite, basalt, marble, limestone, granite variants, etc.).
 * Within each layer, specific ores can spawn. At layer boundaries, additional
 * boundary ores appear.
 */
public class WorldgenStoneLayers implements IWorldGenerator {

    public static final WorldgenStoneLayers INSTANCE = new WorldgenStoneLayers();

    private static boolean initialized = false;

    /** Per-world noise generators (weak-referenced to avoid leaks) */
    private final WeakHashMap<World, NoiseGenerator> noiseCache = new WeakHashMap<>();

    /** Material → StoneType mapping for ore block lookups */
    private static final Map<Material, StoneType> MATERIAL_STONE_TYPE = new HashMap<>();

    /** Cached ore block states: oreMaterial → (stoneType → blockState) */
    private static final Map<Material, Map<StoneType, IBlockState>> ORE_STATE_CACHE = new HashMap<>();

    /** Cached poor ore block states: oreMaterial → (stoneType → blockState) */
    private static final Map<Material, Map<StoneType, IBlockState>> POOR_ORE_STATE_CACHE = new HashMap<>();

    static {
        MATERIAL_STONE_TYPE.put(Materials.Stone, StoneTypes.STONE);
        MATERIAL_STONE_TYPE.put(Materials.Granite, StoneTypes.GRANITE);
        MATERIAL_STONE_TYPE.put(Materials.Diorite, StoneTypes.DIORITE);
        MATERIAL_STONE_TYPE.put(Materials.Andesite, StoneTypes.ANDESITE);
        MATERIAL_STONE_TYPE.put(Materials.GraniteBlack, StoneTypes.BLACK_GRANITE);
        MATERIAL_STONE_TYPE.put(Materials.GraniteRed, StoneTypes.RED_GRANITE);
        MATERIAL_STONE_TYPE.put(Materials.Marble, StoneTypes.MARBLE);
        MATERIAL_STONE_TYPE.put(Materials.Basalt, StoneTypes.BASALT);
        MATERIAL_STONE_TYPE.put(Materials.Komatiite, StoneTypes.KOMATIITE);
        MATERIAL_STONE_TYPE.put(Materials.Kimberlite, StoneTypes.KIMBERLITE);
        MATERIAL_STONE_TYPE.put(Materials.Limestone, StoneTypes.LIMESTONE);
        MATERIAL_STONE_TYPE.put(Materials.Quartzite, StoneTypes.QUARTZITE_STONE);
        MATERIAL_STONE_TYPE.put(Materials.GreenSchist, StoneTypes.GREEN_SCHIST);
        MATERIAL_STONE_TYPE.put(Materials.BlueSchist, StoneTypes.BLUE_SCHIST);
        MATERIAL_STONE_TYPE.put(Materials.Shale, StoneTypes.SHALE);
        MATERIAL_STONE_TYPE.put(Materials.Slate, StoneTypes.SLATE);
        MATERIAL_STONE_TYPE.put(Materials.Gneiss, StoneTypes.GNEISS);
    }

    /**
     * Initialize stone layer definitions. Must be called after MetaBlocks registration.
     */
    public static void init() {
        if (!initialized) {
            StoneLayerDefinitions.init();
            initialized = true;
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return; // Overworld only
        if (StoneLayer.LAYERS.isEmpty()) return;

        NoiseGenerator noise = noiseCache.computeIfAbsent(world, NoiseGenerator::new);
        int layerCount = StoneLayer.LAYERS.size();

        int startX = chunkX * 16;
        int startZ = chunkZ * 16;
        Biome biome = world.getBiome(new BlockPos(startX + 8, 64, startZ + 8));

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                int prevLayerIdx = -1;
                Material lastRockMaterial = null;
                Material lastOreMaterial = null;

                for (int y = 1; y < 128; y++) {
                    pos.setPos(worldX, y, worldZ);
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    // Only process vanilla stone, cobblestone, or mossy cobblestone
                    boolean isStone = block == Blocks.STONE;
                    boolean isCobble = block == Blocks.COBBLESTONE;
                    boolean isMossy = block == Blocks.MOSSY_COBBLESTONE;

                    if (!isStone && !isCobble && !isMossy) {
                        prevLayerIdx = -1;
                        continue;
                    }

                    int layerIdx = noise.get(worldX, y, worldZ, layerCount);
                    StoneLayer layer = StoneLayer.LAYERS.get(layerIdx);

                    // Track the stone layer material (topmost value used for surface rocks)
                    lastRockMaterial = layer.material;

                    // Replace block with layer variant
                    if (isStone) {
                        world.setBlockState(pos, layer.stone, 16);
                    } else if (isCobble && layer.cobble != null) {
                        world.setBlockState(pos, layer.cobble, 16);
                    } else if (isMossy && layer.mossy != null) {
                        world.setBlockState(pos, layer.mossy, 16);
                    }

                    // Intra-layer ore spawning (only in stone, not cobble)
                    if (isStone && !layer.ores.isEmpty()) {
                        boolean placedOre = false;
                        for (StoneLayerOres ore : layer.ores) {
                            if (ore.check(y, biome) && random.nextInt(10000) < ore.chance) {
                                IBlockState oreState = getOreState(ore.material, layer.material);
                                if (oreState != null) {
                                    world.setBlockState(pos, oreState, 16);
                                    lastOreMaterial = ore.material;
                                    placedOre = true;
                                }
                                break; // one ore per position
                            }
                        }

                        // Poor ore spawning - 2x more common than regular ores
                        if (!placedOre) {
                            for (StoneLayerOres ore : layer.ores) {
                                if (ore.check(y, biome) && random.nextInt(10000) < ore.chance * 2) {
                                    IBlockState poorState = getPoorOreState(ore.material, layer.material);
                                    if (poorState != null) {
                                        world.setBlockState(pos, poorState, 16);
                                        lastOreMaterial = ore.material;
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    // Random small gem ore (1/400 chance per stone block)
                    if (isStone && !StoneLayer.RANDOM_SMALL_GEM_ORES.isEmpty() &&
                            random.nextInt(400) == 0) {
                        Material gem = StoneLayer.RANDOM_SMALL_GEM_ORES.get(
                                random.nextInt(StoneLayer.RANDOM_SMALL_GEM_ORES.size()));
                        IBlockState gemState = getOreState(gem, layer.material);
                        if (gemState != null) {
                            world.setBlockState(pos, gemState, 16);
                        }
                    }

                    // Boundary ore check
                    if (isStone && prevLayerIdx >= 0 && prevLayerIdx != layerIdx) {
                        StoneLayer prevLayer = StoneLayer.LAYERS.get(prevLayerIdx);
                        List<StoneLayerOres> boundaryOres = StoneLayer.BOUNDARY_ORES.get(
                                new StoneLayer.MaterialPair(layer.material, prevLayer.material));
                        if (boundaryOres != null) {
                            boolean placedBoundaryOre = false;
                            for (StoneLayerOres ore : boundaryOres) {
                                if (ore.check(y, biome) && random.nextInt(10000) < ore.chance) {
                                    IBlockState oreState = getOreState(ore.material, layer.material);
                                    if (oreState != null) {
                                        world.setBlockState(pos, oreState, 16);
                                        lastOreMaterial = ore.material;
                                        placedBoundaryOre = true;
                                    }
                                    break;
                                }
                            }
                            // Poor ore at boundaries - 2x more common
                            if (!placedBoundaryOre) {
                                for (StoneLayerOres ore : boundaryOres) {
                                    if (ore.check(y, biome) && random.nextInt(10000) < ore.chance * 2) {
                                        IBlockState poorState = getPoorOreState(ore.material, layer.material);
                                        if (poorState != null) {
                                            world.setBlockState(pos, poorState, 16);
                                            lastOreMaterial = ore.material;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    prevLayerIdx = layerIdx;
                }

                // GT6-style surface rock placement: small rocks on the surface indicate
                // underground stone composition and ore deposits
                if (lastRockMaterial != null && world.getWorldType() != WorldType.FLAT
                        && random.nextInt(64) == 0) {
                    placeStoneLayerRock(world, pos, worldX, worldZ, lastRockMaterial,
                            lastOreMaterial, random);
                }
            }
        }
    }

    /**
     * Gets the ore block state for a given ore material in the context of a layer material.
     * Returns null if no ore block exists for this combination.
     */
    private static IBlockState getOreState(Material oreMaterial, Material layerMaterial) {
        StoneType stoneType = MATERIAL_STONE_TYPE.get(layerMaterial);
        if (stoneType == null) stoneType = StoneTypes.STONE;

        Map<StoneType, IBlockState> oreMap = ORE_STATE_CACHE.computeIfAbsent(oreMaterial, m -> {
            try {
                return OreConfigUtils.getOreForMaterial(m);
            } catch (IllegalArgumentException e) {
                return Collections.emptyMap();
            }
        });

        IBlockState result = oreMap.get(stoneType);
        // Fallback to vanilla stone ore if specific stone type not found
        if (result == null) {
            result = oreMap.get(StoneTypes.STONE);
        }
        return result;
    }

    /**
     * Gets the poor ore block state for a given ore material in the context of a layer material.
     * Returns null if no poor ore block exists for this combination.
     */
    private static IBlockState getPoorOreState(Material oreMaterial, Material layerMaterial) {
        StoneType stoneType = MATERIAL_STONE_TYPE.get(layerMaterial);
        if (stoneType == null) stoneType = StoneTypes.STONE;

        Map<StoneType, IBlockState> poorOreMap = POOR_ORE_STATE_CACHE.computeIfAbsent(oreMaterial, m -> {
            try {
                return OreConfigUtils.getPoorOreForMaterial(m);
            } catch (IllegalArgumentException e) {
                return Collections.emptyMap();
            }
        });

        IBlockState result = poorOreMap.get(stoneType);
        if (result == null) {
            result = poorOreMap.get(StoneTypes.STONE);
        }
        return result;
    }

    /**
     * Places a GT6-style surface rock indicator on the terrain surface.
     * The rock material is chosen as either the most recent ore material (50%)
     * or the topmost stone layer material (50%).
     */
    private static void placeStoneLayerRock(World world, BlockPos.MutableBlockPos pos,
                                            int worldX, int worldZ,
                                            Material lastRockMaterial,
                                            Material lastOreMaterial,
                                            Random random) {
        // Choose material: 50% ore (if found), otherwise stone layer
        Material rockMat = (random.nextBoolean() && lastOreMaterial != null) ?
                lastOreMaterial : lastRockMaterial;

        BlockStoneLayerRock rockBlock = MetaBlocks.STONE_LAYER_ROCK.get(rockMat);
        if (rockBlock == null) return;

        // Find the surface: scan downward from sky to find air above solid ground
        int surfaceY = world.getHeight(worldX, worldZ);
        if (surfaceY <= 1 || surfaceY >= 255) return;

        pos.setPos(worldX, surfaceY, worldZ);
        IBlockState above = world.getBlockState(pos);

        // Must be air above the surface
        if (!above.getBlock().isAir(above, world, pos)) return;

        pos.setPos(worldX, surfaceY - 1, worldZ);
        IBlockState ground = world.getBlockState(pos);

        // Ground must have a solid top face (no water, leaves, etc.)
        if (ground.getBlockFaceShape(world, pos, EnumFacing.UP) != BlockFaceShape.SOLID) return;

        // Place the rock
        pos.setPos(worldX, surfaceY, worldZ);
        world.setBlockState(pos, rockBlock.getBlock(rockMat), 16);
    }
}

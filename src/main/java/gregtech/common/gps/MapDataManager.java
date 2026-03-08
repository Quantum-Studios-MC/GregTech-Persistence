package gregtech.common.gps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SideOnly(Side.CLIENT)
public class MapDataManager {

    private static final Map<Long, int[]> chunkColorCache = new ConcurrentHashMap<>();
    private static final Map<Long, int[]> undergroundCache = new ConcurrentHashMap<>();
    private static final Map<Long, int[]> oreCache = new ConcurrentHashMap<>();
    private static int lastDimension = Integer.MIN_VALUE;

    /** Pending chunk coordinates to scan incrementally. */
    private static final Deque<long[]> scanQueue = new ArrayDeque<>();
    /** Track what's already queued to avoid duplicate entries. */
    private static final java.util.Set<Long> queuedKeys = new java.util.HashSet<>();
    /** Max chunks to scan per tick to avoid lag spikes. */
    private static final int MAX_SCANS_PER_TICK = 32;

    public static void clearCache() {
        chunkColorCache.clear();
        undergroundCache.clear();
        oreCache.clear();
        scanQueue.clear();
        queuedKeys.clear();
    }

    /** Invalidate cached data for chunks near the given chunk coords so they get re-scanned. */
    public static void invalidateNearby(int chunkX, int chunkZ, int radius) {
        for (int cx = chunkX - radius; cx <= chunkX + radius; cx++) {
            for (int cz = chunkZ - radius; cz <= chunkZ + radius; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                chunkColorCache.remove(key);
                undergroundCache.remove(key);
                oreCache.remove(key);
            }
        }
    }

    public static void checkDimensionChange(int dim) {
        if (dim != lastDimension) {
            clearCache();
            lastDimension = dim;
        }
    }

    public static int[] getChunkColors(int chunkX, int chunkZ) {
        return chunkColorCache.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    public static boolean hasChunkData(int chunkX, int chunkZ) {
        return chunkColorCache.containsKey(ChunkPos.asLong(chunkX, chunkZ));
    }

    public static int[] getUndergroundColors(int chunkX, int chunkZ) {
        return undergroundCache.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    public static int[] getOreOverlayColors(int chunkX, int chunkZ) {
        return oreCache.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    public static void scanChunk(World world, int chunkX, int chunkZ) {
        if (!world.isChunkGeneratedAt(chunkX, chunkZ)) return;
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        if (chunk.isEmpty()) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int[] colors = new int[256];
        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                int worldX = chunkX * 16 + bx;
                int worldZ = chunkZ * 16 + bz;
                int topY = getTopSolidY(world, worldX, worldZ, pos);
                pos.setPos(worldX, topY, worldZ);
                IBlockState state = world.getBlockState(pos);
                colors[bz * 16 + bx] = getBlockColor(state, world, pos);
            }
        }
        chunkColorCache.put(ChunkPos.asLong(chunkX, chunkZ), colors);
    }

    public static void scanUnderground(World world, int chunkX, int chunkZ, int playerY) {
        if (!world.isChunkGeneratedAt(chunkX, chunkZ)) return;
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        if (chunk.isEmpty()) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int[] colors = new int[256];
        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                int worldX = chunkX * 16 + bx;
                int worldZ = chunkZ * 16 + bz;
                pos.setPos(worldX, playerY, worldZ);
                IBlockState state = world.getBlockState(pos);
                Block blk = state.getBlock();
                Material mat = state.getMaterial();
                if (blk == Blocks.AIR) {
                    colors[bz * 16 + bx] = 0xFF111111;
                } else if (mat == Material.WATER) {
                    colors[bz * 16 + bx] = 0xFF2244AA;
                } else if (mat == Material.LAVA) {
                    colors[bz * 16 + bx] = 0xFFDD4400;
                } else if (!state.isOpaqueCube()) {
                    colors[bz * 16 + bx] = 0xFF222222;
                } else {
                    colors[bz * 16 + bx] = getBlockColor(state, world, pos);
                }
            }
        }
        undergroundCache.put(ChunkPos.asLong(chunkX, chunkZ), colors);
    }

    public static void scanOreOverlay(World world, int chunkX, int chunkZ) {
        if (!world.isChunkGeneratedAt(chunkX, chunkZ)) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int[] colors = new int[256];
        boolean hasOres = false;
        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                int worldX = chunkX * 16 + bx;
                int worldZ = chunkZ * 16 + bz;
                int oreColor = findOreColor(world, worldX, worldZ, pos);
                colors[bz * 16 + bx] = oreColor;
                if (oreColor != 0) hasOres = true;
            }
        }
        if (hasOres) {
            oreCache.put(ChunkPos.asLong(chunkX, chunkZ), colors);
        }
    }

    private static int findOreColor(World world, int x, int z, BlockPos.MutableBlockPos pos) {
        for (int y = 1; y < 80; y++) {
            pos.setPos(x, y, z);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block.getRegistryName() != null) {
                String path = block.getRegistryName().getPath();
                if (path.startsWith("ore_") || path.startsWith("poor_ore_")) {
                    return state.getMapColor(world, pos).colorValue | 0xFF000000;
                }
            }
        }
        return 0;
    }

    private static int getTopSolidY(World world, int x, int z, BlockPos.MutableBlockPos pos) {
        int y = world.getHeight(x, z);
        while (y > 0) {
            pos.setPos(x, y, z);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.AIR) {
                y--;
                continue;
            }
            // Accept opaque blocks, fluids, and any block with a visible material
            Material mat = state.getMaterial();
            if (state.isOpaqueCube() || mat == Material.WATER || mat == Material.LAVA
                    || mat == Material.ICE || mat == Material.PACKED_ICE
                    || mat == Material.LEAVES || mat == Material.GLASS) {
                return y;
            }
            y--;
        }
        return 0;
    }

    private static int getBlockColor(IBlockState state, World world, BlockPos pos) {
        Material mat = state.getMaterial();
        // Give fluids recognizable colors even if mapColor is wrong
        if (mat == Material.WATER) {
            return 0xFF2244AA;
        }
        if (mat == Material.LAVA) {
            return 0xFFDD4400;
        }

        int mapColor = state.getMapColor(world, pos).colorValue;
        if (mapColor == 0) {
            mapColor = 0x7F7F7F;
        }

        int shade = 220;
        IBlockState above = world.getBlockState(pos.up());
        Block aboveBlock = above.getBlock();
        if (aboveBlock == Blocks.AIR) {
            shade = 255;
        } else if (above.getMaterial() == Material.WATER) {
            // Darken blocks under water slightly for depth effect
            shade = 180;
        }

        int r = ((mapColor >> 16) & 0xFF) * shade / 255;
        int g = ((mapColor >> 8) & 0xFF) * shade / 255;
        int b = (mapColor & 0xFF) * shade / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Queue chunks that need scanning. Actually scan only a limited number per call
     * to spread work across ticks and avoid frame drops.
     */
    public static void updateSurroundingChunks(World world, int playerChunkX, int playerChunkZ, int radiusChunks,
                                               boolean underground, int playerY, boolean oreOverlay) {
        // Queue any chunks that don't have data yet, prioritizing closest first
        for (int cx = playerChunkX - radiusChunks; cx <= playerChunkX + radiusChunks; cx++) {
            for (int cz = playerChunkZ - radiusChunks; cz <= playerChunkZ + radiusChunks; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                if (queuedKeys.contains(key)) continue;
                boolean needsSurface = !chunkColorCache.containsKey(key);
                boolean needsUnderground = underground && !undergroundCache.containsKey(key);
                boolean needsOre = oreOverlay && !oreCache.containsKey(key);
                if (needsSurface || needsUnderground || needsOre) {
                    int flags = (needsSurface ? 1 : 0) | (needsUnderground ? 2 : 0) | (needsOre ? 4 : 0);
                    // Closer chunks go to the front of the queue
                    int dist = Math.abs(cx - playerChunkX) + Math.abs(cz - playerChunkZ);
                    if (dist <= 2) {
                        scanQueue.addFirst(new long[]{cx, cz, playerY, flags});
                    } else {
                        scanQueue.addLast(new long[]{cx, cz, playerY, flags});
                    }
                    queuedKeys.add(key);
                }
            }
        }

        // Process a limited batch
        int scanned = 0;
        while (!scanQueue.isEmpty() && scanned < MAX_SCANS_PER_TICK) {
            long[] entry = scanQueue.pollFirst();
            int cx = (int) entry[0];
            int cz = (int) entry[1];
            int pY = (int) entry[2];
            int flags = (int) entry[3];
            if ((flags & 1) != 0 && !hasChunkData(cx, cz)) {
                scanChunk(world, cx, cz);
            }
            if ((flags & 2) != 0) {
                scanUnderground(world, cx, cz, pY);
            }
            if ((flags & 4) != 0) {
                scanOreOverlay(world, cx, cz);
            }
            queuedKeys.remove(ChunkPos.asLong(cx, cz));
            scanned++;
        }
    }

    /**
     * Drain up to {@code maxScans} entries from the scan queue.
     * Call this from GUI updateScreen so the queue keeps processing while a screen is open.
     */
    public static void processScanQueue(World world, int maxScans) {
        int scanned = 0;
        while (!scanQueue.isEmpty() && scanned < maxScans) {
            long[] entry = scanQueue.pollFirst();
            int cx = (int) entry[0];
            int cz = (int) entry[1];
            int pY = (int) entry[2];
            int flags = (int) entry[3];
            if ((flags & 1) != 0 && !hasChunkData(cx, cz)) {
                scanChunk(world, cx, cz);
            }
            if ((flags & 2) != 0) {
                scanUnderground(world, cx, cz, pY);
            }
            if ((flags & 4) != 0) {
                scanOreOverlay(world, cx, cz);
            }
            queuedKeys.remove(ChunkPos.asLong(cx, cz));
            scanned++;
        }
    }

    /** Returns true if there are queued chunks still waiting to be scanned. */
    public static boolean hasPendingScans() {
        return !scanQueue.isEmpty();
    }
}

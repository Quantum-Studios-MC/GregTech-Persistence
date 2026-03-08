package gregtech.integration.groovy;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * GroovyScript registry for adding custom stone types for ore generation.
 * Allows scripts to register any blockstate as a stone type that GregTech ore veins can generate in.
 * <p>
 * <h3>Usage in GroovyScript:</h3>
 * <pre>{@code
 * // Add a new stone type from a blockstate, using an existing ore prefix and stone material
 * // The block will be recognized as replaceable stone during ore generation
 * mods.gregtech.stoneType.add(
 *     "custom_stone",                     // unique name
 *     blockstate('modid:block'),           // the blockstate to match
 *     material('stone'),                   // material with DUST property (for processing byproducts)
 *     oreprefix('ore')                     // which ore prefix to use for ores in this stone
 * )
 *
 * // With explicit drop behavior (true = drops unique ore item; false = drops like normal stone ore)
 * mods.gregtech.stoneType.add(
 *     "custom_granite",
 *     blockstate('modid:custom_granite'),
 *     material('granite'),
 *     oreprefix('oreGranite'),
 *     true
 * )
 *
 * // With a custom sound type
 * mods.gregtech.stoneType.add(
 *     "custom_gravel",
 *     blockstate('minecraft:gravel'),
 *     material('flint'),
 *     oreprefix('ore'),
 *     false,
 *     "ground"                             // sound type: stone, ground, sand, wood, etc.
 * )
 *
 * // With a custom ore drop multiplier (e.g. 2x for a nether-like dimension stone)
 * mods.gregtech.stoneType.add(
 *     "nether_basalt",
 *     blockstate('modid:nether_basalt'),
 *     material('basalt'),
 *     oreprefix('ore'),
 *     true,
 *     "stone",
 *     2                                    // raw ore drop multiplier
 * )
 * }</pre>
 */
public class GroovyStoneTypeRegistry extends VirtualizedRegistry<StoneType> {

    // Start custom GrS stone type IDs at 64 to leave room for future GT stone types (currently up to 20)
    private static int nextStoneTypeId = 64;

    public GroovyStoneTypeRegistry() {
        super();
    }

    @Override
    public void onReload() {
        removeScripted().forEach(stoneType -> {
            // StoneType constructor auto-registers in STONE_TYPE_REGISTRY; we can't easily undo that,
            // but we log it for the user's awareness
            GroovyLog.get().warn("StoneType '{}' was added via script and cannot be fully removed on reload. " +
                    "A game restart is required to remove stone types.", stoneType.name);
        });
    }

    /**
     * Register a blockstate as a new stone type for ore generation.
     *
     * @param name           unique name for this stone type (e.g. "custom_stone")
     * @param blockState     the blockstate to match and replace during ore generation
     * @param stoneMaterial  the material for processing byproducts (must have DUST property)
     * @param orePrefix      the ore prefix to use (e.g. ore, oreGranite, oreNetherrack)
     */
    public void add(@NotNull String name, @NotNull IBlockState blockState,
                    @NotNull Material stoneMaterial, @NotNull OrePrefix orePrefix) {
        add(name, blockState, stoneMaterial, orePrefix, false, null);
    }

    /**
     * Register a blockstate as a new stone type with explicit drop behavior.
     *
     * @param name              unique name for this stone type
     * @param blockState        the blockstate to match
     * @param stoneMaterial     the material for processing byproducts (must have DUST property)
     * @param orePrefix         the ore prefix to use
     * @param shouldDropAsItem  true if this ore should drop a unique ore item block
     */
    public void add(@NotNull String name, @NotNull IBlockState blockState,
                    @NotNull Material stoneMaterial, @NotNull OrePrefix orePrefix,
                    boolean shouldDropAsItem) {
        add(name, blockState, stoneMaterial, orePrefix, shouldDropAsItem, null);
    }

    /**
     * Register a blockstate as a new stone type with full control.
     *
     * @param name              unique name for this stone type
     * @param blockState        the blockstate to match
     * @param stoneMaterial     the material for processing byproducts (must have DUST property)
     * @param orePrefix         the ore prefix to use
     * @param shouldDropAsItem  true if this ore should drop a unique ore item block
     * @param soundTypeName     name of the sound type ("stone", "ground", "sand", "wood", etc.), or null for stone
     */
    public void add(@NotNull String name, @NotNull IBlockState blockState,
                    @NotNull Material stoneMaterial, @NotNull OrePrefix orePrefix,
                    boolean shouldDropAsItem, @Nullable String soundTypeName) {
        add(name, blockState, stoneMaterial, orePrefix, shouldDropAsItem, soundTypeName, 1);
    }

    /**
     * Register a blockstate as a new stone type with full control including raw ore drop multiplier.
     *
     * @param name              unique name for this stone type
     * @param blockState        the blockstate to match
     * @param stoneMaterial     the material for processing byproducts (must have DUST property)
     * @param orePrefix         the ore prefix to use
     * @param shouldDropAsItem  true if this ore should drop a unique ore item block
     * @param soundTypeName     name of the sound type ("stone", "ground", "sand", "wood", etc.), or null for stone
     * @param oreDropMultiplier base multiplier for raw ore drops (e.g. 2 for nether-like, 3 for end-like)
     */
    public void add(@NotNull String name, @NotNull IBlockState blockState,
                    @NotNull Material stoneMaterial, @NotNull OrePrefix orePrefix,
                    boolean shouldDropAsItem, @Nullable String soundTypeName, int oreDropMultiplier) {
        if (name == null || name.isEmpty()) {
            GroovyLog.get().error("Stone type name cannot be null or empty");
            return;
        }
        if (blockState == null) {
            GroovyLog.get().error("Block state cannot be null for stone type '{}'", name);
            return;
        }
        if (stoneMaterial == null) {
            GroovyLog.get().error("Stone material cannot be null for stone type '{}'", name);
            return;
        }
        if (!stoneMaterial.hasProperty(PropertyKey.DUST)) {
            GroovyLog.get().error("Stone material '{}' must have a DUST property for stone type '{}'",
                    stoneMaterial, name);
            return;
        }
        if (orePrefix == null) {
            GroovyLog.get().error("Ore prefix cannot be null for stone type '{}'", name);
            return;
        }

        // Check for duplicate name
        for (StoneType existing : StoneType.STONE_TYPE_REGISTRY) {
            if (existing.name.equals(name)) {
                GroovyLog.get().error("Stone type with name '{}' already exists", name);
                return;
            }
        }

        SoundType soundType = resolveSoundType(soundTypeName);

        // Create the predicate that matches this specific blockstate
        final IBlockState targetState = blockState;
        Predicate<IBlockState> predicate = state -> state == targetState;

        // Create the stone supplier
        Supplier<IBlockState> stoneSupplier = () -> targetState;

        int id = nextStoneTypeId++;
        if (id >= 128) {
            GroovyLog.get().error("Maximum number of stone types (128) exceeded when adding '{}'", name);
            return;
        }

        try {
            StoneType stoneType = new StoneType(id, name, soundType, orePrefix, stoneMaterial,
                    stoneSupplier, predicate, shouldDropAsItem, oreDropMultiplier);
            addScripted(stoneType);
            GroovyLog.get().info("Registered stone type '{}' (id={}) with ore prefix '{}' for block {}",
                    name, id, orePrefix.name, blockState);
        } catch (Exception e) {
            GroovyLog.get().error("Failed to register stone type '{}': {}", name, e.getMessage());
        }
    }

    private static SoundType resolveSoundType(@Nullable String name) {
        if (name == null || name.isEmpty()) return SoundType.STONE;
        switch (name.toLowerCase()) {
            case "stone":
                return SoundType.STONE;
            case "ground":
            case "dirt":
                return SoundType.GROUND;
            case "sand":
                return SoundType.SAND;
            case "wood":
                return SoundType.WOOD;
            case "metal":
                return SoundType.METAL;
            case "glass":
                return SoundType.GLASS;
            case "plant":
            case "grass":
                return SoundType.PLANT;
            case "cloth":
            case "wool":
                return SoundType.CLOTH;
            case "snow":
                return SoundType.SNOW;
            default:
                GroovyLog.get().warn("Unknown sound type '{}', defaulting to stone", name);
                return SoundType.STONE;
        }
    }
}

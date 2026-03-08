package gregtech.integration.jei.basic;

import gregtech.api.unification.ore.OrePrefix;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Registry for dynamically managing OrePrefix entries displayed in the JEI Material Tree.
 * <p>
 * The original Material Tree had a hardcoded list of 29 prefixes with fixed pixel coordinates
 * and arrow connections. This registry preserves all original positions and connections exactly,
 * while allowing new prefixes and arrows to be added dynamically.
 * <p>
 * After the default prefixes are registered, the registry scans all OrePrefix entries for ones
 * that are familiar (via GT6-style {@link OrePrefix#getFamiliarPrefixes()}) with existing tree
 * prefixes. These auto-discovered prefixes are placed in overflow rows below the main tree.
 * <p>
 * To add a new prefix to the material tree manually:
 * 
 * <pre>
 * MaterialTreeRegistry.registerPrefix(OrePrefix.myNewPrefix, 104, 55);
 * MaterialTreeRegistry.registerArrow("r7", 72, 123, OrePrefix.myNewPrefix, OrePrefix.plate);
 * </pre>
 */
public class MaterialTreeRegistry {

    private static final List<OrePrefix> ORDERED_PREFIXES = new ArrayList<>();
    private static final Map<OrePrefix, int[]> PREFIX_POSITIONS = new LinkedHashMap<>();
    private static final List<ArrowConnection> ARROW_CONNECTIONS = new ArrayList<>();

    /** Set of OrePrefix names to exclude from auto-discovery (ores, processing temps). */
    private static final Set<String> AUTO_DISCOVER_BLACKLIST = new HashSet<>();

    private static int fluidX = 154;
    private static int fluidY = 101;

    /** The computed height needed to fit all prefixes (including overflow rows). */
    private static int requiredHeight = 166;

    static {
        initBlacklist();
        registerDefaults();
        autoDiscoverFamiliarPrefixes();
    }

    // --- Public API ---

    /**
     * Register a new OrePrefix to be displayed in the material tree at the given pixel coordinates.
     * The prefix will be appended to the end of the tree's prefix list.
     *
     * @param prefix the OrePrefix to add
     * @param x      pixel X coordinate for the item slot
     * @param y      pixel Y coordinate for the item slot
     */
    public static void registerPrefix(OrePrefix prefix, int x, int y) {
        if (!PREFIX_POSITIONS.containsKey(prefix)) {
            ORDERED_PREFIXES.add(prefix);
        }
        PREFIX_POSITIONS.put(prefix, new int[] { x, y });
    }

    /**
     * Register an arrow connection between two prefixes.
     * The arrow is drawn only when both {@code from} and {@code to} prefixes have items.
     *
     * @param arrowName the name of the registered arrow graphic (e.g. "r7", "2d16")
     * @param drawX     pixel X coordinate to draw the arrow at
     * @param drawY     pixel Y coordinate to draw the arrow at
     * @param from      the source OrePrefix
     * @param to        the destination OrePrefix
     */
    public static void registerArrow(String arrowName, int drawX, int drawY,
                                     OrePrefix from, OrePrefix to) {
        ARROW_CONNECTIONS.add(new ArrowConnection(arrowName, drawX, drawY,
                exists -> exists.getOrDefault(from, false) && exists.getOrDefault(to, false)));
    }

    /**
     * Register an arrow connection with a custom visibility condition.
     * Use this for arrows with complex display logic (e.g. "show only if no intermediate exists").
     *
     * @param arrowName the name of the registered arrow graphic
     * @param drawX     pixel X coordinate to draw the arrow at
     * @param drawY     pixel Y coordinate to draw the arrow at
     * @param condition predicate receiving a map of OrePrefix to exists boolean. Return true to draw.
     */
    public static void registerArrow(String arrowName, int drawX, int drawY,
                                     Predicate<Map<OrePrefix, Boolean>> condition) {
        ARROW_CONNECTIONS.add(new ArrowConnection(arrowName, drawX, drawY, condition));
    }

    /**
     * Set the fluid display position on the material tree.
     */
    public static void setFluidPosition(int x, int y) {
        fluidX = x;
        fluidY = y;
    }

    /**
     * Exclude a prefix from auto-discovery by name.
     * Call this before the MaterialTreeRegistry class is loaded if you want to prevent
     * certain prefixes from being auto-added to the tree.
     *
     * @param prefixName the name of the OrePrefix to exclude
     */
    public static void blacklistFromAutoDiscovery(@NotNull String prefixName) {
        AUTO_DISCOVER_BLACKLIST.add(prefixName);
    }

    // --- Getters ---

    /** Get the ordered list of all registered prefixes. */
    public static List<OrePrefix> getPrefixes() {
        return Collections.unmodifiableList(ORDERED_PREFIXES);
    }

    /** Get the pixel position {x, y} for a registered prefix, or null if not registered. */
    public static int[] getPosition(OrePrefix prefix) {
        return PREFIX_POSITIONS.get(prefix);
    }

    /** Get all registered arrow connections. */
    public static List<ArrowConnection> getArrows() {
        return Collections.unmodifiableList(ARROW_CONNECTIONS);
    }

    /** Get the fluid slot X position. */
    public static int getFluidX() {
        return fluidX;
    }

    /** Get the fluid slot Y position. */
    public static int getFluidY() {
        return fluidY;
    }

    /**
     * Get the required height for the material tree display.
     * This accounts for overflow rows added by auto-discovery.
     *
     * @return the total pixel height needed
     */
    public static int getRequiredHeight() {
        return requiredHeight;
    }

    // --- Arrow Connection ---

    public static class ArrowConnection {

        public final String arrowName;
        public final int drawX;
        public final int drawY;
        public final Predicate<Map<OrePrefix, Boolean>> condition;

        public ArrowConnection(String arrowName, int drawX, int drawY,
                               Predicate<Map<OrePrefix, Boolean>> condition) {
            this.arrowName = arrowName;
            this.drawX = drawX;
            this.drawY = drawY;
            this.condition = condition;
        }
    }

    // --- Default Registration ---

    private static void registerDefaults() {
        // Register all original 29 prefixes with their exact pixel coordinates.
        // These coordinates are hand-tuned to the 176x166 JEI material tree background.
        registerPrefix(OrePrefix.dustTiny, 4, 67);           // 0
        registerPrefix(OrePrefix.dust, 4, 101);              // 1
        registerPrefix(OrePrefix.dustSmall, 4, 135);         // 2
        registerPrefix(OrePrefix.cableGtSingle, 29, 55);     // 3
        registerPrefix(OrePrefix.ingotHot, 29, 85);          // 4
        registerPrefix(OrePrefix.ingot, 29, 117);            // 5
        registerPrefix(OrePrefix.gem, 29, 117);              // 6
        registerPrefix(OrePrefix.block, 29, 147);            // 7
        registerPrefix(OrePrefix.wireGtSingle, 54, 55);      // 8
        registerPrefix(OrePrefix.stick, 54, 85);             // 9
        registerPrefix(OrePrefix.nugget, 54, 117);           // 10
        registerPrefix(OrePrefix.plate, 54, 147);            // 11
        registerPrefix(OrePrefix.wireFine, 79, 55);          // 12
        registerPrefix(OrePrefix.frameGt, 79, 85);           // 13
        registerPrefix(OrePrefix.round, 79, 117);            // 14
        registerPrefix(OrePrefix.pipeNormalFluid, 79, 147);  // 15
        registerPrefix(OrePrefix.pipeNormalItem, 79, 147);   // 16
        registerPrefix(OrePrefix.screw, 104, 55);            // 17
        registerPrefix(OrePrefix.bolt, 104, 85);             // 18
        registerPrefix(OrePrefix.gear, 104, 117);            // 19
        registerPrefix(OrePrefix.plateDouble, 104, 147);     // 20
        registerPrefix(OrePrefix.spring, 129, 55);           // 21
        registerPrefix(OrePrefix.stickLong, 129, 85);        // 22
        registerPrefix(OrePrefix.gearSmall, 129, 117);       // 23
        registerPrefix(OrePrefix.plateDense, 129, 147);      // 24
        registerPrefix(OrePrefix.springSmall, 154, 55);      // 25
        registerPrefix(OrePrefix.ring, 154, 78);             // 26
        // fluid at (154, 101)
        registerPrefix(OrePrefix.lens, 154, 124);            // 27
        registerPrefix(OrePrefix.foil, 154, 147);            // 28

        // Register all original arrow connections with their exact conditions
        registerDefaultArrows();
    }

    @SuppressWarnings("OverlyComplexMethod")
    private static void registerDefaultArrows() {
        // Short aliases for readability
        final OrePrefix dustTiny = OrePrefix.dustTiny;
        final OrePrefix dust = OrePrefix.dust;
        final OrePrefix dustSmall = OrePrefix.dustSmall;
        final OrePrefix cableGtSingle = OrePrefix.cableGtSingle;
        final OrePrefix ingotHot = OrePrefix.ingotHot;
        final OrePrefix ingot = OrePrefix.ingot;
        final OrePrefix gem = OrePrefix.gem;
        final OrePrefix block = OrePrefix.block;
        final OrePrefix wireGtSingle = OrePrefix.wireGtSingle;
        final OrePrefix stick = OrePrefix.stick;
        final OrePrefix nugget = OrePrefix.nugget;
        final OrePrefix plate = OrePrefix.plate;
        final OrePrefix wireFine = OrePrefix.wireFine;
        final OrePrefix frameGt = OrePrefix.frameGt;
        final OrePrefix round = OrePrefix.round;
        final OrePrefix pipeNormalFluid = OrePrefix.pipeNormalFluid;
        final OrePrefix pipeNormalItem = OrePrefix.pipeNormalItem;
        final OrePrefix screw = OrePrefix.screw;
        final OrePrefix bolt = OrePrefix.bolt;
        final OrePrefix gear = OrePrefix.gear;
        final OrePrefix plateDouble = OrePrefix.plateDouble;
        final OrePrefix spring = OrePrefix.spring;
        final OrePrefix stickLong = OrePrefix.stickLong;
        final OrePrefix gearSmall = OrePrefix.gearSmall;
        final OrePrefix plateDense = OrePrefix.plateDense;
        final OrePrefix springSmall = OrePrefix.springSmall;
        final OrePrefix ring = OrePrefix.ring;
        final OrePrefix lens = OrePrefix.lens;
        final OrePrefix foil = OrePrefix.foil;

        // dustTiny <-> dust
        registerArrow("2d16", 10, 85, dustTiny, dust);
        // dust <-> dustSmall
        registerArrow("2d16", 10, 119, dust, dustSmall);
        // dust <-> block (if no ingot or gem)
        registerArrow("2r16d37", 22, 107,
                exists -> !exists.getOrDefault(ingot, false) && !exists.getOrDefault(gem, false) &&
                        exists.getOrDefault(dust, false) && exists.getOrDefault(block, false));
        // dust -> ingotHot
        registerArrow("r3u15r4", 22, 92, dust, ingotHot);
        // dust -> ingot/gem (if no ingotHot)
        registerArrow("r3d16r4", 22, 109,
                exists -> !exists.getOrDefault(ingotHot, false) && exists.getOrDefault(dust, false) &&
                        (exists.getOrDefault(ingot, false) || exists.getOrDefault(gem, false)));
        // ingotHot -> ingot
        registerArrow("d14", 35, 103, ingotHot, ingot);
        // ingot/gem <-> block
        registerArrow("2d12", 35, 135, exists -> exists.getOrDefault(block, false) &&
                (exists.getOrDefault(ingot, false) || exists.getOrDefault(gem, false)));
        // ingot -> wireGtSingle
        registerArrow("r3u57r4", 47, 66, ingot, wireGtSingle);
        // ingot/gem -> stick
        registerArrow("r3u32r4", 47, 91, exists -> exists.getOrDefault(stick, false) &&
                (exists.getOrDefault(ingot, false) || exists.getOrDefault(gem, false)));
        // ingot -> nugget
        registerArrow("r7", 47, 123, ingot, nugget);
        // ingot -> plate
        registerArrow("r3d26r4", 47, 125, ingot, plate);
        // ingot -> wireFine (if no wireGtSingle)
        registerArrow("r3u62r29", 47, 61, exists -> !exists.getOrDefault(wireGtSingle, false) &&
                exists.getOrDefault(ingot, false) && exists.getOrDefault(wireFine, false));
        // block -> plate
        registerArrow("r7", 47, 158, block, plate);
        // wireGtSingle -> cableGtSingle
        registerArrow("l7", 47, 57, wireGtSingle, cableGtSingle);
        // wireGtSingle -> wireFine
        registerArrow("r7", 72, 61, wireGtSingle, wireFine);
        // stick -> frameGt
        registerArrow("d7r25u6", 62, 103, stick, frameGt);
        // stick -> bolt
        registerArrow("d7r50u6", 62, 103, stick, bolt);
        // stick -> gear
        registerArrow("d7r50d7", 62, 103, stick, gear);
        // stick -> stickLong
        registerArrow("d7r75u6", 62, 103, stick, stickLong);
        // stick -> gearSmall
        registerArrow("d7r75d7", 62, 103, stick, gearSmall);
        // stick -> springSmall
        registerArrow("d7r87u46r4", 62, 61, stick, springSmall);
        // stick -> ring
        registerArrow("d7r87u22r4", 62, 85, stick, ring);
        // nugget -> round
        registerArrow("r7", 72, 123, nugget, round);
        // plate -> pipeNormalFluid/pipeNormalItem
        registerArrow("u7r25d6", 62, 140, exists -> exists.getOrDefault(plate, false) &&
                (exists.getOrDefault(pipeNormalFluid, false) ||
                        exists.getOrDefault(pipeNormalItem, false)));
        // plate -> gear
        registerArrow("u7r50u5", 62, 135, plate, gear);
        // plate -> plateDouble
        registerArrow("u7r50d6", 62, 140, plate, plateDouble);
        // plate -> gearSmall
        registerArrow("u7r75u5", 62, 135, plate, gearSmall);
        // plate -> plateDense
        registerArrow("u7r75d6", 62, 140, plate, plateDense);
        // plate -> lens
        registerArrow("u7r87u8r4", 62, 130, plate, lens);
        // plate -> foil
        registerArrow("u7r87d15r4", 62, 140, plate, foil);
        // bolt -> screw
        registerArrow("u12", 110, 73, bolt, screw);
        // stickLong -> spring
        registerArrow("u12", 135, 73, stickLong, spring);
    }

    // --- Blacklist for auto-discovery ---

    private static void initBlacklist() {
        // Ore variants are too numerous and not useful on the material tree
        for (String name : new String[] {
                "ore", "oreGranite", "oreDiorite", "oreAndesite", "oreBlackgranite", "oreRedgranite",
                "oreMarble", "oreBasalt", "oreSand", "oreRedSand", "oreNetherrack", "oreEndstone",
                "oreKomatiite", "oreKimberlite", "oreLimestone", "oreQuartzite",
                "oreGreenSchist", "oreBlueSchist", "oreShale", "oreSlate", "oreGneiss",
                "orePoor",
                // Processing intermediates
                "dustImpure", "dustPure", "crushed", "crushedPurified", "crushedCentrifuged",
                "crushedTiny", "crushedPurifiedTiny", "crushedCentrifugedTiny",
                // Tool heads - too many, not a material form
                "toolHeadBuzzSaw", "toolHeadScrewdriver", "toolHeadDrill", "toolHeadChainsaw",
                "toolHeadWrench", "turbineBlade",
                // Nuclear fuel forms
                "fuelRod", "fuelRodDepleted", "fuelRodHotDepleted", "fuelPellet", "fuelPelletDepleted",
                "dustSpentFuel", "dustBredFuel", "dustFissionByproduct",
                // Non-material prefixes
                "block", "stone", "log", "plank", "slab", "stair", "fence", "fenceGate", "door",
                "lampGt", "paneGlass", "blockGlass", "craftingLens", "dye", "battery", "circuit", "component",
                // Surface indicator rocks
                "rockGt"
        }) {
            AUTO_DISCOVER_BLACKLIST.add(name);
        }
    }

    // --- Auto-Discovery ---

    /**
     * Scans all registered OrePrefix entries and auto-adds ones that are familiar with
     * existing tree prefixes but not yet registered. Auto-discovered prefixes are placed
     * in overflow rows below the main tree area.
     */
    private static void autoDiscoverFamiliarPrefixes() {
        // Collect prefixes already in the tree
        Set<OrePrefix> existingPrefixes = new HashSet<>(ORDERED_PREFIXES);

        // Find prefixes that should be auto-added
        // Group them by their "anchor" - the tree prefix they are most familiar with
        Map<OrePrefix, List<OrePrefix>> anchorToNewPrefixes = new LinkedHashMap<>();

        for (OrePrefix candidate : OrePrefix.values()) {
            // Skip if already in the tree
            if (existingPrefixes.contains(candidate)) continue;
            // Skip blacklisted prefixes
            if (AUTO_DISCOVER_BLACKLIST.contains(candidate.name)) continue;
            // Skip prefixes with no generation condition (non-material prefixes)
            if (!candidate.isUnificationEnabled) continue;
            // Skip self-referencing prefixes (glass, stone, etc.)
            if (candidate.isSelfReferencing) continue;

            // Find the best anchor: the existing tree prefix that this candidate is most directly familiar with
            OrePrefix bestAnchor = findBestAnchor(candidate, existingPrefixes);
            if (bestAnchor != null) {
                anchorToNewPrefixes.computeIfAbsent(bestAnchor, k -> new ArrayList<>()).add(candidate);
            }
        }

        // Place auto-discovered prefixes in overflow rows
        if (!anchorToNewPrefixes.isEmpty()) {
            placeOverflowPrefixes(anchorToNewPrefixes);
        }
    }

    /**
     * Find the best "anchor" prefix for an auto-discovered prefix.
     * Prefers a direct familiar relationship; falls back to indirect (familiar-of-familiar).
     */
    private static OrePrefix findBestAnchor(OrePrefix candidate, Set<OrePrefix> existingPrefixes) {
        Set<OrePrefix> candidateFamiliar = candidate.getFamiliarPrefixes();
        if (candidateFamiliar.isEmpty()) return null;

        // Direct match: candidate is directly familiar with a tree prefix
        for (OrePrefix familiar : candidateFamiliar) {
            if (existingPrefixes.contains(familiar)) {
                return familiar;
            }
        }

        // Indirect: candidate is familiar with something that is familiar with a tree prefix
        for (OrePrefix familiar : candidateFamiliar) {
            for (OrePrefix familiarOfFamiliar : familiar.getFamiliarPrefixes()) {
                if (existingPrefixes.contains(familiarOfFamiliar)) {
                    return familiarOfFamiliar;
                }
            }
        }

        return null;
    }

    /**
     * Place overflow prefixes in rows below the main tree area.
     * Uses 7 columns (x: 4, 29, 54, 79, 104, 129, 154) with 30px row spacing.
     */
    private static void placeOverflowPrefixes(Map<OrePrefix, List<OrePrefix>> anchorToNewPrefixes) {
        final int[] COLUMNS = { 4, 29, 54, 79, 104, 129, 154 };
        final int ROW_SPACING = 30;
        final int BASE_ROW_Y = 177; // First overflow row, just below the 166px main area

        int currentRow = 0;
        int currentCol = 0;

        for (Map.Entry<OrePrefix, List<OrePrefix>> entry : anchorToNewPrefixes.entrySet()) {
            List<OrePrefix> newPrefixes = entry.getValue();

            for (OrePrefix prefix : newPrefixes) {
                int x = COLUMNS[currentCol];
                int y = BASE_ROW_Y + (currentRow * ROW_SPACING);
                registerPrefix(prefix, x, y);

                currentCol++;
                if (currentCol >= COLUMNS.length) {
                    currentCol = 0;
                    currentRow++;
                }
            }
        }

        // Update required height
        int lastRowY = BASE_ROW_Y + (currentRow * ROW_SPACING);
        requiredHeight = Math.max(166, lastRowY + 20); // 20px padding for the slot
    }
}

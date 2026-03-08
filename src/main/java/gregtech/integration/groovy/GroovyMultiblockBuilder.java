package gregtech.integration.groovy;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.BlockInfo;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import com.cleanroommc.groovyscript.api.GroovyLog;
import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <h2>GroovyScript Multiblock Builder</h2>
 * Dead-simple, non-coder-friendly API for creating custom GregTech multiblocks.
 * <p>
 * <h3>Basic Usage (in GroovyScript):</h3>
 * <pre>{@code
 * // Create a simple 3x3x3 multiblock
 * mods.gregtech.multiblock("my_custom_machine")
 *     .recipeMap("blast_recipes")
 *     .pattern(
 *         "XXX", "XCX", "XXX",   // layer 1 (bottom)
 *         "XXX", "C#C", "XXX",   // layer 2 (middle)
 *         "XSX", "XCX", "XXX"    // layer 3 (top, S = controller)
 *     )
 *     .casing("solid_steel")
 *     .register()
 *
 * // With custom tiered blocks (e.g., motor blocks that speed up machines):
 * mods.gregtech.multiblock("motor_boosted_mixer")
 *     .recipeMap("mixer")
 *     .pattern(
 *         "XXX", "XTX", "XXX",
 *         "XXX", "T#T", "XXX",
 *         "XXX", "XSX", "XXX"
 *     )
 *     .casing("solid_steel")
 *     .tieredBlock('T' as char)
 *         .tier(1, blockstate('minecraft:iron_block'))
 *         .tier(2, blockstate('minecraft:gold_block'))
 *         .tier(3, blockstate('minecraft:diamond_block'))
 *         .speedBonus(0.25)          // +25% speed per tier
 *         .euDiscount(0.10)          // -10% EU per tier
 *         .parallelBonus(2)          // +2 parallel per tier
 *     .done()
 *     .register()
 * }</pre>
 * <p>
 * <h3>Pattern Characters:</h3>
 * <ul>
 *     <li>{@code S} - Controller (self)</li>
 *     <li>{@code X} - Casing blocks (auto-replaced with buses/hatches)</li>
 *     <li>{@code #} - Air</li>
 *     <li>{@code C} - Coils (when {@code withCoils()} is used)</li>
 *     <li>{@code M} - Muffler hatch (when {@code withMuffler()} is used)</li>
 *     <li>Custom chars - Via {@code .where()} or {@code .tieredBlock()}</li>
 * </ul>
 */
public class GroovyMultiblockBuilder {

    // Required
    private final String name;
    private RecipeMap<?> recipeMap;

    // Pattern
    private final List<String> patternRows = new ArrayList<>();
    private int rowsPerLayer = -1;
    private char casingChar = 'X';
    private char coilChar = 'C';
    private char airChar = '#';
    private char controllerChar = 'S';
    private char mufflerChar = 'M';

    // Casing
    private IBlockState casingState;
    private int minCasings = -1; // auto-calculate if not set

    // Textures
    private ICubeRenderer baseTexture;
    private ICubeRenderer frontOverlay;

    // Features
    private boolean useCoils = false;
    private boolean hasMuffler = false;
    private boolean canBeDistinct = false;
    private SoundEvent breakdownSound;

    // Tooltip
    private final List<String> tooltipLines = new ArrayList<>();

    // Extra char mappings
    private final TreeMap<Character, TraceabilityPredicate> extraMappings = new TreeMap<>();

    // Tiered block entries (key = pattern char)
    private final LinkedHashMap<Character, TieredBlockEntry> tieredBlocks = new LinkedHashMap<>();

    // Closure hooks (for advanced users)
    private Closure<?> onFormStructureClosure;
    private Closure<?> onCheckRecipeClosure;

    // Registration
    private int mteId = -1;

    /**
     * Create a new multiblock builder.
     *
     * @param name unique identifier for this multiblock (e.g., "my_custom_furnace")
     */
    public GroovyMultiblockBuilder(@NotNull String name) {
        this.name = name;
    }

    // ==================== RECIPE MAP ====================

    /**
     * Set the recipe map by name. Examples: "blast_recipes", "assembler", "vacuum_recipes"
     */
    public GroovyMultiblockBuilder recipeMap(String recipeMapName) {
        RecipeMap<?> map = RecipeMap.getByName(recipeMapName);
        if (map == null) {
            GroovyLog.get().error("Unknown recipe map: '{}'. Use RecipeMap names like 'blast_recipes', 'assembler', etc.", recipeMapName);
            return this;
        }
        this.recipeMap = map;
        return this;
    }

    /**
     * Set the recipe map directly.
     */
    public GroovyMultiblockBuilder recipeMap(RecipeMap<?> recipeMap) {
        this.recipeMap = recipeMap;
        return this;
    }

    // ==================== PATTERN ====================

    /**
     * Define the multiblock's 3D pattern. Pass ALL rows for ALL layers in order.
     * <p>
     * Each string is one row. The number of rows per layer is auto-detected from
     * the first layer. Layers are separated automatically.
     * <p>
     * Example for a 3x3x3 multiblock (3 rows per layer, 3 layers):
     * <pre>{@code
     * .pattern(
     *     "XXX", "XXX", "XXX",   // layer 1
     *     "XXX", "X#X", "XXX",   // layer 2
     *     "XXX", "XSX", "XXX"    // layer 3
     * )
     * }</pre>
     */
    public GroovyMultiblockBuilder pattern(String... rows) {
        patternRows.clear();
        for (String row : rows) {
            patternRows.add(row);
        }
        return this;
    }

    /**
     * Set the number of rows per aisle layer. If not called, it's auto-detected
     * from the first string's length (assumes square cross-section).
     */
    public GroovyMultiblockBuilder rowsPerLayer(int rows) {
        this.rowsPerLayer = rows;
        return this;
    }

    // ==================== CASING ====================

    /**
     * Set casing by a simple name. Supported names:
     * <ul>
     *     <li>"bronze_bricks", "primitive_bricks", "coke_bricks"</li>
     *     <li>"invar_heatproof", "aluminium_frostproof"</li>
     *     <li>"solid_steel" / "steel_solid", "clean_stainless_steel" / "stainless_clean"</li>
     *     <li>"stable_titanium" / "titanium_stable", "robust_tungstensteel" / "tungstensteel_robust"</li>
     *     <li>"hsse_sturdy" / "sturdy_hsse", "ptfe_inert" / "ptfe_inert_casing"</li>
     *     <li>"palladium_substation"</li>
     * </ul>
     * You can also pass the exact enum name from MetalCasingType.
     */
    public GroovyMultiblockBuilder casing(String casingName) {
        CasingLookup result = lookupCasing(casingName);
        if (result != null) {
            this.casingState = result.state;
            if (this.baseTexture == null) {
                this.baseTexture = result.texture;
            }
        } else {
            GroovyLog.get().error("Unknown casing name: '{}'. See GroovyMultiblockBuilder javadoc for valid names.", casingName);
        }
        return this;
    }

    /**
     * Set casing by block state directly.
     */
    public GroovyMultiblockBuilder casing(IBlockState state) {
        this.casingState = state;
        return this;
    }

    /**
     * Set minimum number of casing blocks required. If not set, auto-calculated.
     */
    public GroovyMultiblockBuilder minCasings(int count) {
        this.minCasings = count;
        return this;
    }

    // ==================== TEXTURES ====================

    /**
     * Set the base texture (casing appearance) by path.
     * Example: "casings/solid/machine_casing_solid_steel"
     * <p>
     * For textures from other mods: "modid:casings/my_casing"
     */
    public GroovyMultiblockBuilder texture(String texturePath) {
        ICubeRenderer renderer = Textures.CUBE_RENDERER_REGISTRY.get(texturePath);
        if (renderer != null) {
            this.baseTexture = renderer;
        } else {
            GroovyLog.get().error("Unknown texture: '{}'. Check Textures.CUBE_RENDERER_REGISTRY for valid paths.", texturePath);
        }
        return this;
    }

    /**
     * Set the base texture directly.
     */
    public GroovyMultiblockBuilder texture(ICubeRenderer texture) {
        this.baseTexture = texture;
        return this;
    }

    /**
     * Set the front overlay texture by path.
     * Example: "multiblock/blast_furnace", "multiblock/vacuum_freezer"
     */
    public GroovyMultiblockBuilder overlay(String overlayPath) {
        ICubeRenderer renderer = Textures.CUBE_RENDERER_REGISTRY.get(overlayPath);
        if (renderer != null) {
            this.frontOverlay = renderer;
        } else {
            GroovyLog.get().error("Unknown overlay: '{}'. Check Textures.CUBE_RENDERER_REGISTRY for valid paths.", overlayPath);
        }
        return this;
    }

    /**
     * Set the front overlay texture directly.
     */
    public GroovyMultiblockBuilder overlay(ICubeRenderer overlay) {
        this.frontOverlay = overlay;
        return this;
    }

    // ==================== COILS ====================

    /**
     * Enable heating coil support. Coil blocks in the pattern (character 'C' by default)
     * will be recognized, and the multiblock will:
     * <ul>
     *     <li>Track coil temperature</li>
     *     <li>Get energy tier heat bonuses (like the EBF)</li>
     *     <li>Use HeatingCoilRecipeLogic for temperature-aware overclocking</li>
     *     <li>Check recipe temperature requirements</li>
     * </ul>
     */
    public GroovyMultiblockBuilder withCoils() {
        this.useCoils = true;
        return this;
    }

    /**
     * Set the character used for coils in the pattern. Default is 'C'.
     */
    public GroovyMultiblockBuilder coilChar(char c) {
        this.coilChar = c;
        return this;
    }

    // ==================== MUFFLER ====================

    /**
     * Enable muffler hatch requirement. The character 'M' in the pattern
     * will be mapped to a muffler hatch slot.
     */
    public GroovyMultiblockBuilder withMuffler() {
        this.hasMuffler = true;
        return this;
    }

    /**
     * Set the character used for muffler in the pattern. Default is 'M'.
     */
    public GroovyMultiblockBuilder mufflerChar(char c) {
        this.mufflerChar = c;
        return this;
    }

    // ==================== PATTERN CHARACTERS ====================

    /**
     * Set the character used for casing blocks. Default is 'X'.
     */
    public GroovyMultiblockBuilder casingChar(char c) {
        this.casingChar = c;
        return this;
    }

    /**
     * Set the character used for the controller. Default is 'S'.
     */
    public GroovyMultiblockBuilder controllerChar(char c) {
        this.controllerChar = c;
        return this;
    }

    /**
     * Set the character used for air blocks. Default is '#'.
     */
    public GroovyMultiblockBuilder airChar(char c) {
        this.airChar = c;
        return this;
    }

    /**
     * Map a custom character in the pattern to specific block states.
     * <pre>{@code
     * .where('G', blockState(MetaBlocks.TRANSPARENT_CASING.getState(CasingType.TEMPERED_GLASS)))
     * }</pre>
     */
    public GroovyMultiblockBuilder where(char c, TraceabilityPredicate predicate) {
        extraMappings.put(c, predicate);
        return this;
    }

    /**
     * Map a custom character to specific block states (convenience).
     */
    public GroovyMultiblockBuilder where(char c, IBlockState... states) {
        extraMappings.put(c, MultiblockControllerBase.states(states));
        return this;
    }

    /**
     * Map a custom character to specific blocks (convenience).
     */
    public GroovyMultiblockBuilder where(char c, Block... blocks) {
        extraMappings.put(c, MultiblockControllerBase.blocks(blocks));
        return this;
    }

    // ==================== TIERED BLOCKS (Custom Logic Blocks) ====================

    /**
     * Create a tiered block entry for a pattern character. Tiered blocks are custom blocks
     * that provide bonuses based on their tier - like motor blocks that speed up machines,
     * or custom coils with unique stats.
     * <p>
     * All tiered blocks at positions using this character must be the same tier.
     * <p>
     * Returns a {@link TieredBlockEntry} builder. Call {@code .done()} to return to
     * the multiblock builder.
     * <p>
     * <h3>Example: Motor blocks that speed up a mixer</h3>
     * <pre>{@code
     * mods.gregtech.multiblock("motor_mixer")
     *     .recipeMap("mixer")
     *     .pattern(
     *         "XXX", "XTX", "XXX",
     *         "XXX", "T#T", "XXX",
     *         "XXX", "XSX", "XXX"
     *     )
     *     .casing("solid_steel")
     *     .tieredBlock('T' as char)
     *         .tier(1, blockstate('minecraft:iron_block'))       // LV Motor = tier 1
     *         .tier(2, blockstate('minecraft:gold_block'))       // MV Motor = tier 2
     *         .tier(3, blockstate('minecraft:diamond_block'))    // HV Motor = tier 3
     *         .speedBonus(0.25)       // Each tier reduces duration by 25% (tier 3 = 75% faster)
     *         .euDiscount(0.10)       // Each tier reduces EU/t by 10%
     *         .parallelBonus(2)       // Each tier adds 2 parallel recipes
     *     .done()
     *     .register()
     * }</pre>
     *
     * @param patternChar the character used in the pattern for these blocks
     * @return a TieredBlockEntry builder (call .done() to finish)
     */
    public TieredBlockEntry tieredBlock(char patternChar) {
        TieredBlockEntry entry = new TieredBlockEntry(this, patternChar);
        tieredBlocks.put(patternChar, entry);
        return entry;
    }

    // ==================== CLOSURE HOOKS (Advanced Users) ====================

    /**
     * Set a closure that is called when the multiblock's structure forms.
     * The closure receives a Map containing matched tier data.
     * <p>
     * Tier data keys are "tier_X" where X is the pattern character.
     * E.g., if you used {@code .tieredBlock('T' as char)}, the key is "tier_T".
     * Coil temperature is at key "coil_temp".
     * <p>
     * <pre>{@code
     * .onFormStructure { context ->
     *     println "Motor tier: ${context.tier_T}"
     *     println "Coil temp: ${context.coil_temp}"
     * }
     * }</pre>
     */
    public GroovyMultiblockBuilder onFormStructure(Closure<?> closure) {
        this.onFormStructureClosure = closure;
        return this;
    }

    /**
     * Set a closure called to check if a recipe should run.
     * The closure receives a recipe object and a context Map, and should return true/false.
     * <p>
     * <pre>{@code
     * .onCheckRecipe { recipe, context ->
     *     // Only allow recipes if motor tier >= 2
     *     return context.tier_T >= 2
     * }
     * }</pre>
     */
    public GroovyMultiblockBuilder onCheckRecipe(Closure<?> closure) {
        this.onCheckRecipeClosure = closure;
        return this;
    }

    // ==================== MISCELLANEOUS ====================

    /**
     * Allow this multiblock to use distinct bus mode.
     */
    public GroovyMultiblockBuilder distinct() {
        this.canBeDistinct = true;
        return this;
    }

    /**
     * Set the breakdown sound effect.
     */
    public GroovyMultiblockBuilder breakdownSound(SoundEvent sound) {
        this.breakdownSound = sound;
        return this;
    }

    /**
     * Add a tooltip line to the multiblock's item.
     */
    public GroovyMultiblockBuilder tooltip(String line) {
        this.tooltipLines.add(line);
        return this;
    }

    /**
     * Set the MTE numeric ID. If not set, auto-assigns from the GroovyScript ID range (32000+).
     * Only use this if you need a specific ID for compatibility.
     */
    public GroovyMultiblockBuilder id(int id) {
        this.mteId = id;
        return this;
    }

    // ==================== REGISTRATION ====================

    /**
     * Validate and register the multiblock. Returns the created controller MTE.
     *
     * @return the registered GroovyMultiblockController, or null if validation failed
     */
    public GroovyMultiblockController register() {
        // Validation
        if (name == null || name.isEmpty()) {
            GroovyLog.get().error("Multiblock name cannot be empty!");
            return null;
        }
        if (recipeMap == null) {
            GroovyLog.get().error("Multiblock '{}' has no recipe map! Call .recipeMap(\"name\") first.", name);
            return null;
        }
        if (patternRows.isEmpty()) {
            GroovyLog.get().error("Multiblock '{}' has no pattern! Call .pattern(...) first.", name);
            return null;
        }
        if (casingState == null) {
            GroovyLog.get().error("Multiblock '{}' has no casing! Call .casing(\"name\") first.", name);
            return null;
        }

        // Default textures
        if (baseTexture == null) {
            baseTexture = Textures.SOLID_STEEL_CASING;
            GroovyLog.get().warn("Multiblock '{}' has no base texture, defaulting to solid steel casing.", name);
        }
        if (frontOverlay == null) {
            frontOverlay = Textures.BLAST_FURNACE_OVERLAY;
            GroovyLog.get().warn("Multiblock '{}' has no front overlay, defaulting to blast furnace overlay.", name);
        }

        // Parse pattern into aisle layers
        String[] aisleArray = parsePattern();
        if (aisleArray == null) return null;

        // Auto-calculate minimum casings if not set
        int calcMinCasings = this.minCasings;
        if (calcMinCasings < 0) {
            calcMinCasings = countChar(casingChar) / 2; // roughly half the casing positions
            if (calcMinCasings < 1) calcMinCasings = 1;
        }

        // Build extra char arrays (skip chars used by tiered blocks)
        char[] extraChars = null;
        TraceabilityPredicate[] extraPreds = null;
        if (!extraMappings.isEmpty()) {
            extraChars = new char[extraMappings.size()];
            extraPreds = new TraceabilityPredicate[extraMappings.size()];
            int i = 0;
            for (Map.Entry<Character, TraceabilityPredicate> entry : extraMappings.entrySet()) {
                extraChars[i] = entry.getKey();
                extraPreds[i] = entry.getValue();
                i++;
            }
        }

        // Build tiered block data
        char[] tieredChars = null;
        TieredBlockData[] tieredData = null;
        if (!tieredBlocks.isEmpty()) {
            tieredChars = new char[tieredBlocks.size()];
            tieredData = new TieredBlockData[tieredBlocks.size()];
            int i = 0;
            for (Map.Entry<Character, TieredBlockEntry> entry : tieredBlocks.entrySet()) {
                TieredBlockEntry tbe = entry.getValue();
                if (tbe.tiers.isEmpty()) {
                    GroovyLog.get().error("Multiblock '{}': tiered block '{}' has no tiers defined!",
                            name, entry.getKey());
                    return null;
                }
                tieredChars[i] = entry.getKey();
                tieredData[i] = tbe.buildData();
                i++;
            }
        }

        // Determine pack ID for the resource location
        String packId = GroovyScriptModule.getPackId();
        if (packId.isEmpty()) {
            packId = "groovyscript";
        }
        ResourceLocation mteRL = new ResourceLocation(packId, name);

        // Create the controller
        GroovyMultiblockController controller = new GroovyMultiblockController(
                mteRL, recipeMap, aisleArray,
                casingChar, useCoils ? coilChar : (char) 0,
                airChar, controllerChar,
                hasMuffler ? mufflerChar : (char) 0,
                calcMinCasings, casingState,
                baseTexture, frontOverlay,
                useCoils, hasMuffler, canBeDistinct,
                breakdownSound,
                tooltipLines.isEmpty() ? null : tooltipLines.toArray(new String[0]),
                extraChars, extraPreds,
                tieredChars, tieredData,
                onFormStructureClosure, onCheckRecipeClosure);

        // Register
        int id = this.mteId;
        if (id < 0) {
            id = GroovyMultiblockRegistry.getNextId();
        }

        try {
            MetaTileEntities.registerMetaTileEntity(id, controller);
            GroovyLog.get().info("Registered custom multiblock '{}' with ID {}", name, id);
        } catch (Exception e) {
            GroovyLog.get().error("Failed to register multiblock '{}': {}", name, e.getMessage());
            return null;
        }

        return controller;
    }

    // ==================== INTERNAL HELPERS ====================

    /**
     * Parse the flat list of pattern rows into aisle layer strings (newline-separated rows).
     */
    private String[] parsePattern() {
        int rpl = this.rowsPerLayer;
        if (rpl <= 0) {
            // Auto-detect from first row length, assuming square cross-section
            if (!patternRows.isEmpty()) {
                rpl = patternRows.get(0).length();
                // If all rows have the same length, that's the rows per layer
                // But we need the HEIGHT of the pattern, not width
                // The pattern is: each aisle = rpl rows, each row = width chars
                // For a cube, rpl = number of rows in a layer = height of the cross section
                // We need to figure this out from the total rows
                // Standard: if we have N rows total and each row has W chars,
                // then we have N/rpl layers where rpl needs to be determined
                // Best heuristic: try to find the smallest rpl > 1 that divides total evenly
                int total = patternRows.size();
                rpl = findRowsPerLayer(total, patternRows.get(0).length());
            }
        }

        if (rpl <= 0 || patternRows.size() % rpl != 0) {
            GroovyLog.get().error(
                    "Multiblock '{}': pattern has {} rows which is not evenly divisible. Set .rowsPerLayer(n) explicitly.",
                    name, patternRows.size());
            return null;
        }

        int layers = patternRows.size() / rpl;
        String[] aisles = new String[layers];
        for (int layer = 0; layer < layers; layer++) {
            StringBuilder sb = new StringBuilder();
            for (int row = 0; row < rpl; row++) {
                if (row > 0) sb.append('\n');
                sb.append(patternRows.get(layer * rpl + row));
            }
            aisles[layer] = sb.toString();
        }
        return aisles;
    }

    /**
     * Heuristic to find rows-per-layer. Prefers the string length (width) as rows-per-layer
     * for square patterns, otherwise finds smallest valid divisor > 1.
     */
    private int findRowsPerLayer(int totalRows, int firstRowLength) {
        // For standard GT multiblocks, rows per layer = height of the cross-section
        // which is typically the same as the width (square cross section)
        // Try common values: firstRowLength, then 2, 3, 4...
        if (totalRows % firstRowLength == 0 && firstRowLength > 1) {
            return firstRowLength;
        }
        for (int i = 2; i <= totalRows; i++) {
            if (totalRows % i == 0) return i;
        }
        return totalRows;
    }

    /**
     * Count occurrences of a character in the pattern.
     */
    private int countChar(char c) {
        int count = 0;
        for (String row : patternRows) {
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) == c) count++;
            }
        }
        return count;
    }

    // ==================== CASING LOOKUP ====================

    private static class CasingLookup {

        final IBlockState state;
        final ICubeRenderer texture;

        CasingLookup(IBlockState state, ICubeRenderer texture) {
            this.state = state;
            this.texture = texture;
        }
    }

    /**
     * Look up a casing by friendly name. Supports many aliases for convenience.
     */
    private static CasingLookup lookupCasing(String name) {
        if (name == null) return null;
        String key = name.toLowerCase().replace('-', '_').replace(' ', '_');

        // Metal casings
        switch (key) {
            case "bronze_bricks":
            case "bronze_plated_bricks":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.BRONZE_BRICKS),
                        Textures.BRONZE_PLATED_BRICKS);
            case "primitive_bricks":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.PRIMITIVE_BRICKS),
                        Textures.PRIMITIVE_BRICKS);
            case "coke_bricks":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.COKE_BRICKS),
                        Textures.COKE_BRICKS);
            case "invar_heatproof":
            case "heatproof":
            case "heat_proof":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.INVAR_HEATPROOF),
                        Textures.HEAT_PROOF_CASING);
            case "aluminium_frostproof":
            case "frostproof":
            case "frost_proof":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.ALUMINIUM_FROSTPROOF),
                        Textures.FROST_PROOF_CASING);
            case "solid_steel":
            case "steel_solid":
            case "steel":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID),
                        Textures.SOLID_STEEL_CASING);
            case "clean_stainless_steel":
            case "stainless_clean":
            case "stainless_steel":
            case "stainless":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN),
                        Textures.CLEAN_STAINLESS_STEEL_CASING);
            case "stable_titanium":
            case "titanium_stable":
            case "titanium":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.TITANIUM_STABLE),
                        Textures.STABLE_TITANIUM_CASING);
            case "robust_tungstensteel":
            case "tungstensteel_robust":
            case "tungstensteel":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.TUNGSTENSTEEL_ROBUST),
                        Textures.ROBUST_TUNGSTENSTEEL_CASING);
            case "hsse_sturdy":
            case "sturdy_hsse":
            case "hsse":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.HSSE_STURDY),
                        Textures.STURDY_HSSE_CASING);
            case "ptfe_inert":
            case "ptfe_inert_casing":
            case "ptfe":
            case "inert_ptfe":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.PTFE_INERT_CASING),
                        Textures.INERT_PTFE_CASING);
            case "palladium_substation":
            case "palladium":
                return new CasingLookup(
                        MetaBlocks.METAL_CASING.getState(MetalCasingType.PALLADIUM_SUBSTATION),
                        Textures.PALLADIUM_SUBSTATION_CASING);
            default:
                // Try matching MetalCasingType enum directly
                try {
                    MetalCasingType type = MetalCasingType.valueOf(key.toUpperCase());
                    return new CasingLookup(
                            MetaBlocks.METAL_CASING.getState(type),
                            lookupMetalCasingTexture(type));
                } catch (IllegalArgumentException ignored) {}
                return null;
        }
    }

    private static ICubeRenderer lookupMetalCasingTexture(MetalCasingType type) {
        switch (type) {
            case BRONZE_BRICKS:
                return Textures.BRONZE_PLATED_BRICKS;
            case PRIMITIVE_BRICKS:
                return Textures.PRIMITIVE_BRICKS;
            case COKE_BRICKS:
                return Textures.COKE_BRICKS;
            case INVAR_HEATPROOF:
                return Textures.HEAT_PROOF_CASING;
            case ALUMINIUM_FROSTPROOF:
                return Textures.FROST_PROOF_CASING;
            case STEEL_SOLID:
                return Textures.SOLID_STEEL_CASING;
            case STAINLESS_CLEAN:
                return Textures.CLEAN_STAINLESS_STEEL_CASING;
            case TITANIUM_STABLE:
                return Textures.STABLE_TITANIUM_CASING;
            case TUNGSTENSTEEL_ROBUST:
                return Textures.ROBUST_TUNGSTENSTEEL_CASING;
            case HSSE_STURDY:
                return Textures.STURDY_HSSE_CASING;
            case PTFE_INERT_CASING:
                return Textures.INERT_PTFE_CASING;
            case PALLADIUM_SUBSTATION:
                return Textures.PALLADIUM_SUBSTATION_CASING;
            default:
                return Textures.SOLID_STEEL_CASING;
        }
    }

    // ==================== TIERED BLOCK ENTRY (Sub-builder) ====================

    /**
     * Sub-builder for defining tiered blocks. Call {@code .done()} to return to
     * the main multiblock builder.
     * <p>
     * Tiered blocks are custom blocks placed in the multiblock pattern that provide
     * bonuses based on their tier. All blocks at positions using this character must
     * be the same tier (like how heating coils must all match).
     * <p>
     * <h3>Bonuses available:</h3>
     * <ul>
     *     <li>{@code speedBonus(n)} - Each tier reduces recipe duration by {@code n * tier}.
     *         E.g., 0.25 means tier 1 = 25% faster, tier 2 = 50% faster, tier 3 = 75% faster</li>
     *     <li>{@code euDiscount(n)} - Each tier reduces EU/t by {@code n * tier}.
     *         E.g., 0.10 means tier 1 = 10% less EU, tier 2 = 20% less EU</li>
     *     <li>{@code parallelBonus(n)} - Each tier adds {@code n} parallel recipes.
     *         E.g., 2 means tier 1 = 2 parallels, tier 2 = 4, tier 3 = 6</li>
     * </ul>
     */
    public static class TieredBlockEntry {

        private final GroovyMultiblockBuilder parent;
        private final char patternChar;
        final LinkedHashMap<IBlockState, TierDef> tiers = new LinkedHashMap<>();
        private double speedBonusPerTier = 0.0;
        private double euDiscountPerTier = 0.0;
        private int parallelPerTier = 0;

        TieredBlockEntry(GroovyMultiblockBuilder parent, char patternChar) {
            this.parent = parent;
            this.patternChar = patternChar;
        }

        /**
         * Register a block state as a specific tier.
         *
         * @param tierLevel the tier number (1, 2, 3, etc. - higher is better)
         * @param state     the block state for this tier
         * @return this entry for chaining
         */
        public TieredBlockEntry tier(int tierLevel, IBlockState state) {
            return tier(tierLevel, state, null);
        }

        /**
         * Register a block state as a specific tier with a display name.
         *
         * @param tierLevel the tier number (1, 2, 3, etc.)
         * @param state     the block state for this tier
         * @param name      human-readable name (e.g., "LV Motor Block")
         * @return this entry for chaining
         */
        public TieredBlockEntry tier(int tierLevel, IBlockState state, String name) {
            if (state == null) {
                GroovyLog.get().error("Cannot register null block state as tiered block");
                return this;
            }
            if (tierLevel < 0) {
                GroovyLog.get().error("Tiered block tier must be >= 0, got: {}", tierLevel);
                return this;
            }
            tiers.put(state, new TierDef(tierLevel, name != null ? name : ("Tier " + tierLevel)));
            return this;
        }

        /**
         * Set the speed bonus per tier. Each tier reduces recipe duration.
         * <p>
         * Formula: {@code duration = baseDuration * (1 - speedBonus * tier)}
         * <p>
         * Example: {@code .speedBonus(0.25)} means tier 1 = 25% faster, tier 3 = 75% faster.
         * Clamped so minimum duration multiplier is 0.05 (5% of original).
         *
         * @param bonusPerTier fraction of speed gained per tier (0.0 to 1.0)
         */
        public TieredBlockEntry speedBonus(double bonusPerTier) {
            this.speedBonusPerTier = bonusPerTier;
            return this;
        }

        /**
         * Set the EU/t discount per tier. Each tier reduces energy consumption.
         * <p>
         * Formula: {@code EU/t = baseEUt * (1 - euDiscount * tier)}
         * <p>
         * Example: {@code .euDiscount(0.10)} means tier 2 = 20% less EU/t.
         * Clamped so minimum EU multiplier is 0.05.
         *
         * @param discountPerTier fraction of EU saved per tier (0.0 to 1.0)
         */
        public TieredBlockEntry euDiscount(double discountPerTier) {
            this.euDiscountPerTier = discountPerTier;
            return this;
        }

        /**
         * Set the number of parallel recipes added per tier.
         * <p>
         * Formula: {@code parallels = parallelPerTier * tier}
         * <p>
         * Example: {@code .parallelBonus(2)} means tier 1 = 2 parallels, tier 3 = 6 parallels.
         *
         * @param parallelsPerTier additional parallel recipes per tier
         */
        public TieredBlockEntry parallelBonus(int parallelsPerTier) {
            this.parallelPerTier = parallelsPerTier;
            return this;
        }

        /**
         * Finish configuring this tiered block and return to the multiblock builder.
         */
        public GroovyMultiblockBuilder done() {
            return parent;
        }

        /**
         * Build the internal data representation used by the controller.
         */
        TieredBlockData buildData() {
            IBlockState[] states = new IBlockState[tiers.size()];
            int[] tierLevels = new int[tiers.size()];
            String[] names = new String[tiers.size()];
            int i = 0;
            for (Map.Entry<IBlockState, TierDef> entry : tiers.entrySet()) {
                states[i] = entry.getKey();
                tierLevels[i] = entry.getValue().tier;
                names[i] = entry.getValue().name;
                i++;
            }
            return new TieredBlockData(patternChar, states, tierLevels, names,
                    speedBonusPerTier, euDiscountPerTier, parallelPerTier);
        }

        private static class TierDef {

            final int tier;
            final String name;

            TierDef(int tier, String name) {
                this.tier = tier;
                this.name = name;
            }
        }
    }

    // ==================== TIERED BLOCK DATA (Internal) ====================

    /**
     * Immutable data holder for a tiered block configuration.
     * Passed to the controller at registration time.
     */
    static class TieredBlockData {

        final char patternChar;
        final IBlockState[] states;
        final int[] tierLevels;
        final String[] names;
        final double speedBonusPerTier;
        final double euDiscountPerTier;
        final int parallelPerTier;

        TieredBlockData(char patternChar, IBlockState[] states, int[] tierLevels, String[] names,
                        double speedBonusPerTier, double euDiscountPerTier, int parallelPerTier) {
            this.patternChar = patternChar;
            this.states = states;
            this.tierLevels = tierLevels;
            this.names = names;
            this.speedBonusPerTier = speedBonusPerTier;
            this.euDiscountPerTier = euDiscountPerTier;
            this.parallelPerTier = parallelPerTier;
        }

        /**
         * Get the tier level for a block state, or -1 if not a valid tiered block.
         */
        int getTierForState(IBlockState state) {
            for (int i = 0; i < states.length; i++) {
                if (states[i].equals(state)) {
                    return tierLevels[i];
                }
            }
            return -1;
        }

        /**
         * Create a TraceabilityPredicate that matches these tiered blocks
         * and enforces that they must all be the same tier.
         */
        TraceabilityPredicate createPredicate() {
            final TieredBlockData data = this;
            String contextKey = "TieredBlock_" + patternChar;
            return new TraceabilityPredicate(
                    (BlockWorldState blockWorldState) -> {
                        IBlockState blockState = blockWorldState.getBlockState();
                        int tier = data.getTierForState(blockState);
                        if (tier < 0) {
                            blockWorldState.setError(new PatternStringError(
                                    "gregtech.multiblock.pattern.error.tiered_block"));
                            return false;
                        }
                        Object currentTier = blockWorldState.getMatchContext().getOrPut(contextKey, tier);
                        if (!currentTier.equals(tier)) {
                            blockWorldState.setError(new PatternStringError(
                                    "gregtech.multiblock.pattern.error.tiered_block_mismatch"));
                            return false;
                        }
                        return true;
                    },
                    () -> {
                        // Sort by tier for JEI preview
                        List<Map.Entry<IBlockState, Integer>> sorted = new ArrayList<>();
                        for (int i = 0; i < data.states.length; i++) {
                            final int idx = i;
                            sorted.add(new AbstractMap.SimpleEntry<>(data.states[idx], data.tierLevels[idx]));
                        }
                        sorted.sort(Comparator.comparingInt(Map.Entry::getValue));
                        return sorted.stream()
                                .map(e -> new BlockInfo(e.getKey(), null))
                                .toArray(BlockInfo[]::new);
                    })
                    .addTooltips("gregtech.multiblock.pattern.error.tiered_block");
        }
    }
}

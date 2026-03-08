package gregtech.integration.groovy;

import gregtech.api.GTValues;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.IHeatingCoil;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.core.sound.GTSoundEvents;
import gregtech.integration.groovy.GroovyMultiblockBuilder.TieredBlockData;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.groovyscript.api.GroovyLog;
import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generic multiblock controller created by GroovyScript's multiblock builder.
 * Supports simple multiblocks, coil-based multiblocks, custom tiered logic blocks,
 * and closure-based custom logic hooks.
 * <p>
 * Non-coders can create custom multiblocks without writing any Java by using
 * the {@link GroovyMultiblockBuilder} API in GroovyScript.
 *
 * <h3>Tiered Block Support:</h3>
 * Tiered blocks (like custom motor blocks) provide bonuses based on their tier:
 * <ul>
 *     <li>Speed bonus - reduces recipe duration per tier</li>
 *     <li>EU discount - reduces EU/t per tier</li>
 *     <li>Parallel bonus - adds parallel recipes per tier</li>
 * </ul>
 */
public class GroovyMultiblockController extends RecipeMapMultiblockController implements IHeatingCoil {

    // Pattern definition
    private final String[] aisles;
    private final char casingChar;
    private final char coilChar;
    private final char airChar;
    private final char controllerChar;
    private final char mufflerChar;
    private final int minCasings;
    private final IBlockState casingState;

    // Textures
    private final ICubeRenderer baseTexture;
    private final ICubeRenderer frontOverlay;

    // Coil support
    private final boolean useCoils;
    private int currentTemperature;

    // Config
    private final boolean hasMuffler;
    private final boolean canBeDistinct;
    private final SoundEvent breakdownSound;
    private final String[] tooltipLines;

    // Extra char->predicate mappings
    private final char[] extraChars;
    private final TraceabilityPredicate[] extraPredicates;

    // Tiered block support
    private final char[] tieredChars;
    private final TieredBlockData[] tieredBlockData;
    private final Map<Character, Integer> currentTiers = new HashMap<>();

    // Closure hooks
    private final Closure<?> onFormStructureClosure;
    private final Closure<?> onCheckRecipeClosure;

    /**
     * Full constructor used by the builder. Do not call directly - use {@link GroovyMultiblockBuilder}.
     */
    GroovyMultiblockController(ResourceLocation metaTileEntityId,
                               RecipeMap<?> recipeMap,
                               String[] aisles,
                               char casingChar,
                               char coilChar,
                               char airChar,
                               char controllerChar,
                               char mufflerChar,
                               int minCasings,
                               IBlockState casingState,
                               ICubeRenderer baseTexture,
                               ICubeRenderer frontOverlay,
                               boolean useCoils,
                               boolean hasMuffler,
                               boolean canBeDistinct,
                               SoundEvent breakdownSound,
                               String[] tooltipLines,
                               char[] extraChars,
                               TraceabilityPredicate[] extraPredicates,
                               char[] tieredChars,
                               TieredBlockData[] tieredBlockData,
                               Closure<?> onFormStructureClosure,
                               Closure<?> onCheckRecipeClosure) {
        super(metaTileEntityId, recipeMap);
        this.aisles = aisles;
        this.casingChar = casingChar;
        this.coilChar = coilChar;
        this.airChar = airChar;
        this.controllerChar = controllerChar;
        this.mufflerChar = mufflerChar;
        this.minCasings = minCasings;
        this.casingState = casingState;
        this.baseTexture = baseTexture;
        this.frontOverlay = frontOverlay;
        this.useCoils = useCoils;
        this.hasMuffler = hasMuffler;
        this.canBeDistinct = canBeDistinct;
        this.breakdownSound = breakdownSound;
        this.tooltipLines = tooltipLines;
        this.extraChars = extraChars;
        this.extraPredicates = extraPredicates;
        this.tieredChars = tieredChars;
        this.tieredBlockData = tieredBlockData;
        this.onFormStructureClosure = onFormStructureClosure;
        this.onCheckRecipeClosure = onCheckRecipeClosure;

        // Always use the custom recipe logic (handles coils + tiered blocks)
        this.recipeMapWorkable = new GroovyMultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new GroovyMultiblockController(
                metaTileEntityId, recipeMap, aisles,
                casingChar, coilChar, airChar, controllerChar, mufflerChar,
                minCasings, casingState, baseTexture, frontOverlay,
                useCoils, hasMuffler, canBeDistinct, breakdownSound, tooltipLines,
                extraChars, extraPredicates, tieredChars, tieredBlockData,
                onFormStructureClosure, onCheckRecipeClosure);
    }

    // ==================== STRUCTURE PATTERN ====================

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern pattern = FactoryBlockPattern.start();

        // Add all aisles
        for (String aisle : aisles) {
            String[] rows = aisle.split("\n");
            pattern.aisle(rows);
        }

        // Controller (self)
        pattern.where(controllerChar, selfPredicate());

        // Casing blocks with auto abilities
        TraceabilityPredicate casingPredicate = states(casingState).setMinGlobalLimited(minCasings);
        if (hasMuffler) {
            casingPredicate = casingPredicate.or(autoAbilities(true, true, true, true, true, true, false));
        } else {
            casingPredicate = casingPredicate.or(autoAbilities());
        }
        pattern.where(casingChar, casingPredicate);

        // Coils
        if (useCoils && coilChar != 0) {
            pattern.where(coilChar, heatingCoils());
        }

        // Air
        if (airChar != 0) {
            pattern.where(airChar, air());
        }

        // Muffler
        if (hasMuffler && mufflerChar != 0) {
            pattern.where(mufflerChar, abilities(MultiblockAbility.MUFFLER_HATCH));
        }

        // Extra character predicates
        if (extraChars != null) {
            for (int i = 0; i < extraChars.length; i++) {
                pattern.where(extraChars[i], extraPredicates[i]);
            }
        }

        // Tiered block predicates
        if (tieredChars != null && tieredBlockData != null) {
            for (int i = 0; i < tieredChars.length; i++) {
                pattern.where(tieredChars[i], tieredBlockData[i].createPredicate());
            }
        }

        return pattern.build();
    }

    // ==================== STRUCTURE FORMATION ====================

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);

        // Handle coils
        if (useCoils) {
            IHeatingCoilBlockStats coilType = context.getOrDefault("CoilType", CoilType.CUPRONICKEL);
            this.currentTemperature = coilType.getCoilTemperature();
            this.currentTemperature += 100 *
                    Math.max(0, GTUtility.getFloorTierByVoltage(getEnergyContainer().getInputVoltage()) - GTValues.MV);
        }

        // Handle tiered blocks
        currentTiers.clear();
        if (tieredChars != null && tieredBlockData != null) {
            for (int i = 0; i < tieredChars.length; i++) {
                String contextKey = "TieredBlock_" + tieredChars[i];
                Integer tier = context.get(contextKey);
                if (tier != null) {
                    currentTiers.put(tieredChars[i], tier);
                }
            }
        }

        // Call user's onFormStructure closure if provided
        if (onFormStructureClosure != null) {
            try {
                Map<String, Object> closureContext = buildClosureContext();
                onFormStructureClosure.call(closureContext);
            } catch (Exception e) {
                GroovyLog.get().error("Error in onFormStructure closure for '{}': {}",
                        metaTileEntityId, e.getMessage());
            }
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.currentTemperature = 0;
        this.currentTiers.clear();
    }

    // ==================== RECIPE CHECKING ====================

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        // Check coil temperature
        if (useCoils) {
            if (this.currentTemperature < recipe.getProperty(TemperatureProperty.getInstance(), 0)) {
                return false;
            }
        }

        // Call user's onCheckRecipe closure if provided
        if (onCheckRecipeClosure != null) {
            try {
                Map<String, Object> closureContext = buildClosureContext();
                Object result = onCheckRecipeClosure.call(recipe, closureContext);
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
            } catch (Exception e) {
                GroovyLog.get().error("Error in onCheckRecipe closure for '{}': {}",
                        metaTileEntityId, e.getMessage());
            }
        }

        return super.checkRecipe(recipe, consumeIfSuccess);
    }

    // ==================== TIERED BLOCK API (used by GroovyMultiblockRecipeLogic) ====================

    /**
     * Whether this controller uses heating coils.
     */
    boolean usesCoils() {
        return useCoils;
    }

    /**
     * Get the current tier for a tiered block character, or 0 if not matched.
     */
    public int getTier(char patternChar) {
        return currentTiers.getOrDefault(patternChar, 0);
    }

    /**
     * Get total duration multiplier from all tiered blocks.
     * Returns a value like 0.5 meaning "50% of original duration" (= 2x speed).
     */
    double getTotalDurationMultiplier() {
        if (tieredChars == null || tieredBlockData == null) return 1.0;
        double multiplier = 1.0;
        for (int i = 0; i < tieredChars.length; i++) {
            int tier = currentTiers.getOrDefault(tieredChars[i], 0);
            if (tier > 0 && tieredBlockData[i].speedBonusPerTier > 0.0) {
                double reduction = tieredBlockData[i].speedBonusPerTier * tier;
                multiplier *= Math.max(0.05, 1.0 - reduction); // Floor at 5% of original
            }
        }
        return multiplier;
    }

    /**
     * Get total EU multiplier from all tiered blocks.
     * Returns a value like 0.8 meaning "80% of original EU/t".
     */
    double getTotalEuMultiplier() {
        if (tieredChars == null || tieredBlockData == null) return 1.0;
        double multiplier = 1.0;
        for (int i = 0; i < tieredChars.length; i++) {
            int tier = currentTiers.getOrDefault(tieredChars[i], 0);
            if (tier > 0 && tieredBlockData[i].euDiscountPerTier > 0.0) {
                double reduction = tieredBlockData[i].euDiscountPerTier * tier;
                multiplier *= Math.max(0.05, 1.0 - reduction);
            }
        }
        return multiplier;
    }

    /**
     * Get total parallel bonus from all tiered blocks.
     * Returns the sum of (parallelPerTier * tier) for all tiered block entries.
     */
    int getTotalParallelBonus() {
        if (tieredChars == null || tieredBlockData == null) return 0;
        int parallels = 0;
        for (int i = 0; i < tieredChars.length; i++) {
            int tier = currentTiers.getOrDefault(tieredChars[i], 0);
            if (tier > 0 && tieredBlockData[i].parallelPerTier > 0) {
                parallels += tieredBlockData[i].parallelPerTier * tier;
            }
        }
        return parallels;
    }

    /**
     * Build a context map for closure calls, containing all tier data and coil info.
     */
    private Map<String, Object> buildClosureContext() {
        Map<String, Object> ctx = new HashMap<>();
        // Add tiered block data
        for (Map.Entry<Character, Integer> entry : currentTiers.entrySet()) {
            ctx.put("tier_" + entry.getKey(), entry.getValue());
        }
        // Add coil data
        if (useCoils) {
            ctx.put("coil_temp", currentTemperature);
        }
        return ctx;
    }

    // ==================== IHeatingCoil ====================

    @Override
    public int getCurrentTemperature() {
        return this.currentTemperature;
    }

    // ==================== RENDERING ====================

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return baseTexture;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return frontOverlay;
    }

    // ==================== CONFIG ====================

    @Override
    public boolean canBeDistinct() {
        return canBeDistinct;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return hasMuffler;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return breakdownSound != null ? breakdownSound : GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        if (tooltipLines != null) {
            for (String line : tooltipLines) {
                tooltip.add(line);
            }
        }
        // Show tiered block info
        if (tieredBlockData != null) {
            for (int i = 0; i < tieredBlockData.length; i++) {
                TieredBlockData data = tieredBlockData[i];
                if (data.speedBonusPerTier > 0) {
                    tooltip.add(String.format("\u00a77Tiered '%c' blocks: \u00a7a+%.0f%% speed per tier",
                            tieredChars[i], data.speedBonusPerTier * 100));
                }
                if (data.euDiscountPerTier > 0) {
                    tooltip.add(String.format("\u00a77Tiered '%c' blocks: \u00a7a-%.0f%% EU/t per tier",
                            tieredChars[i], data.euDiscountPerTier * 100));
                }
                if (data.parallelPerTier > 0) {
                    tooltip.add(String.format("\u00a77Tiered '%c' blocks: \u00a7a+%d parallels per tier",
                            tieredChars[i], data.parallelPerTier));
                }
            }
        }
    }
}

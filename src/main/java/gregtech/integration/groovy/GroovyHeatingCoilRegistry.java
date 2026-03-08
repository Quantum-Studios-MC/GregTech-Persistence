package gregtech.integration.groovy;

import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.unification.material.Material;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * GroovyScript registry for custom heating coils.
 * Allows scripts to register any block as a heating coil with custom stats.
 * <p>
 * <h3>Usage in GroovyScript:</h3>
 * <pre>{@code
 * // Register a block as a heating coil
 * mods.gregtech.coils.add(blockstate('minecraft:diamond_block'), "diamond_coil", 12000, 16, 8, 8)
 *
 * // Register with a material reference
 * mods.gregtech.coils.add(blockstate('minecraft:emerald_block'), "emerald_coil", 15000, 32, 16, 9, material('tritanium'))
 *
 * // Remove an existing coil
 * mods.gregtech.coils.remove(blockstate('gregtech:wire_coil', 0))
 *
 * // List all registered coils
 * mods.gregtech.coils.streamRecipes().each { println it }
 * }</pre>
 */
public class GroovyHeatingCoilRegistry extends VirtualizedRegistry<IBlockState> {

    public GroovyHeatingCoilRegistry() {
        super();
    }

    @Override
    public void onReload() {
        removeScripted().forEach(GregTechAPI.HEATING_COILS::remove);
        restoreFromBackup().forEach(state -> {
            // We can't easily restore the original stats, so just log it
            GroovyLog.get().warn("Cannot fully restore removed heating coil for state: {}", state);
        });
    }

    /**
     * Register a block state as a heating coil.
     *
     * @param state           the block state to register
     * @param name            unique name for this coil type
     * @param coilTemperature temperature in Kelvin (e.g., 1800 for Cupronickel)
     * @param level           multi-smelter parallel level (multiplied by 32 for parallel count)
     * @param energyDiscount  multi-smelter energy discount factor
     * @param tier            coil tier (0=Cupronickel level, higher = better cracking/pyrolyse)
     */
    public void add(@NotNull IBlockState state, @NotNull String name, int coilTemperature,
                    int level, int energyDiscount, int tier) {
        add(state, name, coilTemperature, level, energyDiscount, tier, null);
    }

    /**
     * Register a block state as a heating coil with a material reference.
     *
     * @param state           the block state to register
     * @param name            unique name for this coil type
     * @param coilTemperature temperature in Kelvin
     * @param level           multi-smelter parallel level
     * @param energyDiscount  multi-smelter energy discount factor
     * @param tier            coil tier
     * @param material        the material of the coil (for color/tooltip), or null
     */
    public void add(@NotNull IBlockState state, @NotNull String name, int coilTemperature,
                    int level, int energyDiscount, int tier, @Nullable Material material) {
        if (state == null) {
            GroovyLog.get().error("Cannot register null block state as heating coil");
            return;
        }
        if (name == null || name.isEmpty()) {
            GroovyLog.get().error("Heating coil name cannot be empty");
            return;
        }
        if (coilTemperature <= 0) {
            GroovyLog.get().error("Heating coil temperature must be positive, got: {}", coilTemperature);
            return;
        }

        GroovyHeatingCoilStats stats = new GroovyHeatingCoilStats(name, coilTemperature, level, energyDiscount, tier, material);
        GregTechAPI.HEATING_COILS.put(state, stats);
        addScripted(state);
        GroovyLog.get().info("Registered heating coil '{}' with temperature {}K", name, coilTemperature);
    }

    /**
     * Remove a block state from the heating coil registry.
     */
    public void remove(@NotNull IBlockState state) {
        if (state == null) {
            GroovyLog.get().error("Cannot remove null block state from heating coils");
            return;
        }
        if (GregTechAPI.HEATING_COILS.containsKey(state)) {
            addBackup(state);
            GregTechAPI.HEATING_COILS.remove(state);
        } else {
            GroovyLog.get().warn("Block state is not registered as a heating coil: {}", state);
        }
    }

    /**
     * Stream all registered heating coils for inspection.
     */
    public SimpleObjectStream<Map.Entry<IBlockState, IHeatingCoilBlockStats>> streamCoils() {
        return new SimpleObjectStream<>(GregTechAPI.HEATING_COILS.entrySet())
                .setRemover(entry -> {
                    GregTechAPI.HEATING_COILS.remove(entry.getKey());
                    return true;
                });
    }

    /**
     * Get the heating coil stats for a block state, or null if not a coil.
     */
    @Nullable
    public IHeatingCoilBlockStats getStats(@NotNull IBlockState state) {
        return GregTechAPI.HEATING_COILS.get(state);
    }

    /**
     * Get the temperature of a coil block state, or 0 if not a coil.
     */
    public int getTemperature(@NotNull IBlockState state) {
        IHeatingCoilBlockStats stats = GregTechAPI.HEATING_COILS.get(state);
        return stats != null ? stats.getCoilTemperature() : 0;
    }

    /**
     * Check if a block state is registered as a heating coil.
     */
    public boolean isCoil(@NotNull IBlockState state) {
        return GregTechAPI.HEATING_COILS.containsKey(state);
    }

    // ==================== INTERNAL ====================

    /**
     * Simple IHeatingCoilBlockStats implementation for GroovyScript-registered coils.
     */
    public static class GroovyHeatingCoilStats implements IHeatingCoilBlockStats {

        private final String name;
        private final int coilTemperature;
        private final int level;
        private final int energyDiscount;
        private final int tier;
        private final Material material;

        public GroovyHeatingCoilStats(String name, int coilTemperature, int level, int energyDiscount,
                                      int tier, @Nullable Material material) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.tier = tier;
            this.material = material;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getCoilTemperature() {
            return coilTemperature;
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public int getEnergyDiscount() {
            return energyDiscount;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Nullable
        @Override
        public Material getMaterial() {
            return material;
        }

        @Override
        public String toString() {
            return String.format("HeatingCoil{name='%s', temp=%dK, level=%d, discount=%d, tier=%d}",
                    name, coilTemperature, level, energyDiscount, tier);
        }
    }
}

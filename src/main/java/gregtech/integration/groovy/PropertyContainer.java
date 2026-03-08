package gregtech.integration.groovy;

import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;

import net.minecraftforge.fml.common.eventhandler.EventPriority;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.event.EventBusType;
import com.cleanroommc.groovyscript.event.GroovyEventManager;
import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import com.cleanroommc.groovyscript.sandbox.LoadStage;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

public class PropertyContainer extends GroovyPropertyContainer {

    private final GroovyHeatingCoilRegistry coilRegistry = new GroovyHeatingCoilRegistry();
    private final GroovyStoneTypeRegistry stoneTypeRegistry = new GroovyStoneTypeRegistry();

    public PropertyContainer() {
        // Register the coils registry as a GrS property for reload support
    }

    /**
     * Access the heating coils registry for adding/removing custom coils.
     * <pre>{@code
     * mods.gregtech.coils.add(blockstate('minecraft:diamond_block'), "diamond_coil", 12000, 16, 8, 8)
     * }</pre>
     */
    public GroovyHeatingCoilRegistry getCoils() {
        return coilRegistry;
    }

    /**
     * Access the stone type registry for adding custom stone types for ore generation.
     * <pre>{@code
     * mods.gregtech.stoneType.add("custom_stone", blockstate('modid:block'), material('stone'), oreprefix('ore'))
     * }</pre>
     */
    public GroovyStoneTypeRegistry getStoneType() {
        return stoneTypeRegistry;
    }

    public void materialEvent(EventPriority priority, @DelegatesTo(MaterialEvent.class) Closure<?> eventListener) {
        if (GroovyScriptModule.isCurrentlyRunning() &&
                GroovyScript.getSandbox().getCurrentLoader() != LoadStage.PRE_INIT) {
            GroovyLog.get().error("GregTech's material event can only be used in pre init!");
            return;
        }
        ClosureHelper.withEnvironment(eventListener, new MaterialEvent(), true);
        GroovyEventManager.INSTANCE.listen(priority, EventBusType.MAIN, MaterialEvent.class, eventListener);
    }

    public void materialEvent(Closure<?> eventListener) {
        materialEvent(EventPriority.NORMAL, eventListener);
    }

    public void lateMaterialEvent(EventPriority priority, Closure<?> eventListener) {
        if (GroovyScriptModule.isCurrentlyRunning() &&
                GroovyScript.getSandbox().getCurrentLoader() != LoadStage.PRE_INIT) {
            GroovyLog.get().error("GregTech's material event can only be used in pre init!");
            return;
        }
        GroovyEventManager.INSTANCE.listen(priority, EventBusType.MAIN, PostMaterialEvent.class,
                eventListener);
    }

    public void lateMaterialEvent(Closure<?> eventListener) {
        lateMaterialEvent(EventPriority.NORMAL, eventListener);
    }

    /**
     * Create a new custom multiblock builder. Usage in GroovyScript:
     * <pre>{@code
     * mods.gregtech.multiblock("my_machine")
     *     .recipeMap("assembler")
     *     .pattern("XXX", "XXX", "XXX",
     *              "XXX", "X#X", "XXX",
     *              "XXX", "XSX", "XXX")
     *     .casing("solid_steel")
     *     .register()
     * }</pre>
     *
     * @param name unique identifier for the multiblock
     * @return a new GroovyMultiblockBuilder
     */
    public GroovyMultiblockBuilder multiblock(String name) {
        return new GroovyMultiblockBuilder(name);
    }
}

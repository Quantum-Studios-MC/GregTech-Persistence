package gregtech.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyLog;

/**
 * Simple ID tracker for GroovyScript-registered multiblocks.
 * IDs are allocated from the GroovyScript MTE range (32000+).
 */
public class GroovyMultiblockRegistry {

    private static int nextId = 32000;

    /**
     * Get the next available MTE ID and increment the counter.
     */
    public static int getNextId() {
        if (nextId > Short.MAX_VALUE) {
            GroovyLog.get().error("Ran out of MTE IDs for GroovyScript multiblocks! Max is {}", Short.MAX_VALUE);
            return -1;
        }
        return nextId++;
    }

    /**
     * Reset the ID counter. Called on script reload.
     */
    public static void reset() {
        nextId = 32000;
    }

    /**
     * Set the starting ID for multiblock registration.
     * Useful if you have other GroovyScript MTEs and need to avoid conflicts.
     */
    public static void setStartId(int startId) {
        nextId = startId;
    }
}

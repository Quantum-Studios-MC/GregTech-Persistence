package gregtech.api.worldgen.shape;

import gregtech.api.worldgen.config.OreConfigUtils;

import net.minecraft.util.math.Vec3i;

import com.google.gson.JsonObject;

import java.util.Random;

/**
 * Generates a vertical columnar "dike" or "pipe" shape, simulating geological intrusions
 * like kimberlite pipes or volcanic dikes.
 * <p>
 * The shape is a vertical cylinder with a configurable radius and height.
 * An optional taper factor causes the radius to shrink toward the top,
 * creating a conical pipe shape.
 * <p>
 * JSON configuration:
 * <pre>
 * "generator": {
 *   "type": "dike",
 *   "radius": [2, 5],
 *   "height": [20, 40],
 *   "taper": 0.5
 * }
 * </pre>
 * Where taper 0.0 = perfect cylinder, 1.0 = full cone tapering to a point at the top.
 */
public class DikeGenerator extends ShapeGenerator {

    private int radiusMin;
    private int radiusMax;
    private int heightMin;
    private int heightMax;
    private float taper;

    public DikeGenerator() {}

    @Override
    public void loadFromConfig(JsonObject object) {
        int[] radiusData = OreConfigUtils.getIntRange(object.get("radius"));
        this.radiusMin = radiusData[0];
        this.radiusMax = radiusData[1];
        int[] heightData = OreConfigUtils.getIntRange(object.get("height"));
        this.heightMin = heightData[0];
        this.heightMax = heightData[1];
        if (object.has("taper")) {
            this.taper = object.get("taper").getAsFloat();
        } else {
            this.taper = 0.0f;
        }
    }

    @Override
    public Vec3i getMaxSize() {
        return new Vec3i(radiusMax * 2, heightMax, radiusMax * 2);
    }

    @Override
    public void generate(Random gridRandom, IBlockGeneratorAccess relativeBlockAccess) {
        int radius = radiusMin == radiusMax ? radiusMax :
                (gridRandom.nextInt(radiusMax - radiusMin) + radiusMin);
        int height = heightMin == heightMax ? heightMax :
                (gridRandom.nextInt(heightMax - heightMin) + heightMin);

        int halfHeight = height / 2;

        for (int y = -halfHeight; y <= halfHeight; y++) {
            // Calculate the effective radius at this Y level based on taper
            // At bottom (-halfHeight), full radius; at top (+halfHeight), radius * (1 - taper)
            float progress = (float) (y + halfHeight) / height;
            float effectiveRadius = radius * (1.0f - taper * progress);
            if (effectiveRadius < 0.5f) break;

            int er = (int) Math.ceil(effectiveRadius);
            float er2 = effectiveRadius * effectiveRadius;

            for (int x = -er; x <= er; x++) {
                for (int z = -er; z <= er; z++) {
                    if (x * x + z * z <= er2) {
                        relativeBlockAccess.generateBlock(x, y, z);
                    }
                }
            }
        }
    }
}

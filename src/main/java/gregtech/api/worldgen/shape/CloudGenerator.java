package gregtech.api.worldgen.shape;

import gregtech.api.worldgen.config.OreConfigUtils;

import net.minecraft.util.math.Vec3i;

import com.google.gson.JsonObject;

import java.util.Random;

/**
 * Generates a diffuse "cloud" of scattered ore blocks within an ellipsoidal region.
 * Unlike the solid EllipsoidGenerator, this generator uses a density falloff from the center,
 * creating a natural-looking dispersed ore deposit.
 * <p>
 * The density of ore placement decreases with distance from the center based on the
 * falloff exponent. Higher falloff values create tighter, more concentrated clouds;
 * lower values create more uniform distributions.
 * <p>
 * JSON configuration:
 * <pre>
 * "generator": {
 *   "type": "cloud",
 *   "radius": [8, 16],
 *   "density_falloff": 1.5
 * }
 * </pre>
 * Where density_falloff controls how quickly ore density drops toward the edges.
 * A value of 1.0 is linear falloff; 2.0 is quadratic (more concentrated at center).
 * Default is 1.5.
 */
public class CloudGenerator extends ShapeGenerator {

    private int radiusMin;
    private int radiusMax;
    private float densityFalloff;

    public CloudGenerator() {}

    @Override
    public void loadFromConfig(JsonObject object) {
        int[] radiusData = OreConfigUtils.getIntRange(object.get("radius"));
        this.radiusMin = radiusData[0];
        this.radiusMax = radiusData[1];
        if (object.has("density_falloff")) {
            this.densityFalloff = object.get("density_falloff").getAsFloat();
        } else {
            this.densityFalloff = 1.5f;
        }
    }

    @Override
    public Vec3i getMaxSize() {
        return new Vec3i(radiusMax * 2, radiusMax * 2, radiusMax * 2);
    }

    @Override
    public void generate(Random gridRandom, IBlockGeneratorAccess relativeBlockAccess) {
        int radiusX = radiusMin == radiusMax ? radiusMax :
                (gridRandom.nextInt(radiusMax - radiusMin) + radiusMin);
        int radiusY = radiusMin == radiusMax ? radiusMax / 2 :
                (gridRandom.nextInt(radiusMax - radiusMin) + radiusMin) / 2;
        int radiusZ = radiusMin == radiusMax ? radiusMax :
                (gridRandom.nextInt(radiusMax - radiusMin) + radiusMin);

        if (radiusY < 1) radiusY = 1;

        float rx2 = radiusX * radiusX;
        float ry2 = radiusY * radiusY;
        float rz2 = radiusZ * radiusZ;

        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    // Normalized distance from center (0.0 at center, 1.0 at edge)
                    float dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                    if (dist > 1.0f) continue;

                    // Density decreases from center to edge based on falloff
                    float placementChance = (float) Math.pow(1.0f - dist, densityFalloff);
                    if (gridRandom.nextFloat() < placementChance) {
                        relativeBlockAccess.generateBlock(x, y, z, false);
                    }
                }
            }
        }
    }
}

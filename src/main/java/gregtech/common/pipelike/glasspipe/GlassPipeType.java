package gregtech.common.pipelike.glasspipe;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.client.renderer.pipe.PipeModelRedirector;
import gregtech.client.renderer.pipe.PipeModelRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public enum GlassPipeType implements IPipeType<GlassPipeProperties> {

    // Basic tier - temp 1200K, base throughput 10 L/t
    BASIC_TINY("basic_tiny", 0.25f, 0, "basic", 1200, 10, false, false, false, false),
    BASIC_SMALL("basic_small", 0.375f, 0, "basic", 1200, 20, false, false, false, false),
    BASIC_NORMAL("basic_normal", 0.5f, 0, "basic", 1200, 60, false, false, false, false),
    BASIC_LARGE("basic_large", 0.75f, 0, "basic", 1200, 120, false, false, false, false),
    BASIC_HUGE("basic_huge", 0.875f, 0, "basic", 1200, 240, false, false, false, false),

    // Tempered tier - temp 1800K, base throughput 30 L/t
    TEMPERED_TINY("tempered_tiny", 0.25f, 1, "tempered", 1800, 30, true, false, false, false),
    TEMPERED_SMALL("tempered_small", 0.375f, 1, "tempered", 1800, 60, true, false, false, false),
    TEMPERED_NORMAL("tempered_normal", 0.5f, 1, "tempered", 1800, 180, true, false, false, false),
    TEMPERED_LARGE("tempered_large", 0.75f, 1, "tempered", 1800, 360, true, false, false, false),
    TEMPERED_HUGE("tempered_huge", 0.875f, 1, "tempered", 1800, 720, true, false, false, false),

    // Borosilicate tier - temp 2100K, base throughput 60 L/t
    BOROSILICATE_TINY("borosilicate_tiny", 0.25f, 2, "borosilicate", 2100, 60, true, true, false, false),
    BOROSILICATE_SMALL("borosilicate_small", 0.375f, 2, "borosilicate", 2100, 120, true, true, false, false),
    BOROSILICATE_NORMAL("borosilicate_normal", 0.5f, 2, "borosilicate", 2100, 360, true, true, false, false),
    BOROSILICATE_LARGE("borosilicate_large", 0.75f, 2, "borosilicate", 2100, 720, true, true, false, false),
    BOROSILICATE_HUGE("borosilicate_huge", 0.875f, 2, "borosilicate", 2100, 1440, true, true, false, false),

    // Laminated tier - temp 2400K, base throughput 120 L/t
    LAMINATED_TINY("laminated_tiny", 0.25f, 3, "laminated", 2400, 120, true, true, true, false),
    LAMINATED_SMALL("laminated_small", 0.375f, 3, "laminated", 2400, 240, true, true, true, false),
    LAMINATED_NORMAL("laminated_normal", 0.5f, 3, "laminated", 2400, 720, true, true, true, false),
    LAMINATED_LARGE("laminated_large", 0.75f, 3, "laminated", 2400, 1440, true, true, true, false),
    LAMINATED_HUGE("laminated_huge", 0.875f, 3, "laminated", 2400, 2880, true, true, true, false),

    // Fusion tier - temp 10000K, base throughput 240 L/t
    FUSION_TINY("fusion_tiny", 0.25f, 4, "fusion", 10000, 240, true, true, true, true),
    FUSION_SMALL("fusion_small", 0.375f, 4, "fusion", 10000, 480, true, true, true, true),
    FUSION_NORMAL("fusion_normal", 0.5f, 4, "fusion", 10000, 1440, true, true, true, true),
    FUSION_LARGE("fusion_large", 0.75f, 4, "fusion", 10000, 2880, true, true, true, true),
    FUSION_HUGE("fusion_huge", 0.875f, 4, "fusion", 10000, 5760, true, true, true, true);

    /** Number of tiers (basic, tempered, borosilicate, laminated, fusion) */
    public static final int TIER_COUNT = 5;
    /** Number of sizes per tier (tiny, small, normal, large, huge) */
    public static final int SIZE_COUNT = 5;
    public static final String[] TIER_NAMES = { "basic", "tempered", "borosilicate", "laminated", "fusion" };
    public static final String[] SIZE_NAMES = { "tiny", "small", "normal", "large", "huge" };

    public static final GlassPipeType[] VALUES = values();

    private final String name;
    private final float thickness;
    private final int tierIndex;
    private final GlassPipeProperties properties;

    GlassPipeType(String name, float thickness, int tierIndex, String tierName, int maxTemp, int throughput,
                  boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        this.name = name;
        this.thickness = thickness;
        this.tierIndex = tierIndex;
        this.properties = new GlassPipeProperties(tierName, maxTemp, throughput,
                gasProof, acidProof, cryoProof, plasmaProof);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public GlassPipeProperties modifyProperties(GlassPipeProperties baseProperties) {
        return properties;
    }

    @Override
    public boolean isPaintable() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PipeModelRedirector getModel() {
        return PipeModelRegistry.getGlassPipeModel(tierIndex);
    }

    public GlassPipeProperties getProperties() {
        return properties;
    }

    /** Returns the tier index (0=basic, 1=tempered, 2=borosilicate, 3=laminated, 4=fusion) */
    public int getTierIndex() {
        return tierIndex;
    }

    /** Returns the size index (0=tiny, 1=small, 2=normal, 3=large, 4=huge) */
    public int getSizeIndex() {
        return ordinal() % SIZE_COUNT;
    }

    /** Returns the tier name (basic, tempered, etc.) */
    public String getTierName() {
        return TIER_NAMES[tierIndex];
    }

    /** Returns the size name (tiny, small, normal, large, huge) */
    public String getSizeName() {
        return SIZE_NAMES[getSizeIndex()];
    }

    /** Get a specific variant by tier and size index */
    public static GlassPipeType getByTierAndSize(int tierIndex, int sizeIndex) {
        return VALUES[tierIndex * SIZE_COUNT + sizeIndex];
    }
}

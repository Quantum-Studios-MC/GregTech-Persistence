package gregtech.client.model;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.GTUtility;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public class PoorOreBakedModel {

    private static final Map<Entry, ModelResourceLocation> ENTRIES = new Object2ObjectOpenHashMap<>();

    public static ModelResourceLocation registerPoorOreEntry(StoneType stoneType, Material material) {
        return ENTRIES.computeIfAbsent(
                new Entry(stoneType, material.getMaterialIconSet(), material.getProperty(PropertyKey.ORE).isEmissive()),
                Entry::getModelId);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (Map.Entry<Entry, ModelResourceLocation> e : ENTRIES.entrySet()) {
            event.getMap().registerSprite(MaterialIconType.orePoor.getBlockTexturePath(e.getKey().iconSet));
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> overlayCache = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<Entry, ModelResourceLocation> e : ENTRIES.entrySet()) {
            IBakedModel overlay = overlayCache.computeIfAbsent(
                    MaterialIconType.orePoor.getBlockTexturePath(e.getKey().iconSet),
                    tex -> new ModelFactory(ModelFactory.ModelTemplate.ORE_OVERLAY)
                            .addSprite("texture", tex)
                            .bake());
            event.getModelRegistry().putObject(e.getValue(), e.getKey().emissive ?
                    new EmissiveOreBakedModel(e.getKey().stoneType, overlay) :
                    new OreBakedModel(e.getKey().stoneType, overlay));
        }
    }

    private static final class Entry {

        private final StoneType stoneType;
        private final MaterialIconSet iconSet;
        private final boolean emissive;

        private final int hash;

        private Entry(StoneType stoneType, MaterialIconSet iconSet, boolean emissive) {
            this.stoneType = stoneType;
            this.iconSet = iconSet;
            this.emissive = emissive;

            this.hash = Objects.hash("poor", stoneType.name, iconSet.name, emissive);
        }

        public ModelResourceLocation getModelId() {
            return new ModelResourceLocation(GTUtility.gregtechId(
                    "ore_poor_" + this.stoneType.name + "_" + this.iconSet.name + (this.emissive ? "_emissive" : "")),
                    "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return this.stoneType.name.equals(entry.stoneType.name) &&
                    this.iconSet.name.equals(entry.iconSet.name) &&
                    this.emissive == entry.emissive;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "poor_stoneType=" + stoneType.name + ", iconSet=" + iconSet.name + ", emissive=" + emissive;
        }
    }
}

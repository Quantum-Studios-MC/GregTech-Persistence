package gregtech.client.renderer.pipe;

import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.unification.material.Material;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.cover.CoverRendererPackage;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.CacheKey;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class GlassPipeModel extends AbstractPipeModel<CacheKey> {

    private static final int END_CAP_COLOR = 0xFFCCDDE8;
    private static final int GLASS_TUBE_COLOR = 0x60D8E8F0;
    private static final float FLUID_INNER_SCALE = 0.6f;

    private final @NotNull Supplier<SpriteInformation> inTex;
    private final @NotNull Supplier<SpriteInformation> sideTex;
    private final Object2ObjectOpenHashMap<CacheKey, StructureQuadCache> fluidInnerCache = new Object2ObjectOpenHashMap<>();

    public GlassPipeModel(@NotNull Supplier<SpriteInformation> inTex, @NotNull Supplier<SpriteInformation> sideTex) {
        this.inTex = inTex;
        this.sideTex = sideTex;
        PIPE_CACHES.add(fluidInnerCache);
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideTex.get();
    }

    @Override
    protected @NotNull CacheKey toKey(@NotNull IExtendedBlockState state) {
        return defaultKey(state);
    }

    @Override
    protected StructureQuadCache constructForKey(CacheKey key) {
        return StructureQuadCache.create(PipeQuadHelper.create(key.getThickness()), inTex.get(), sideTex.get());
    }

    private StructureQuadCache constructFluidInnerForKey(CacheKey key) {
        float innerThickness = key.getThickness() * FLUID_INNER_SCALE;
        return StructureQuadCache.create(PipeQuadHelper.create(innerThickness), inTex.get(), sideTex.get());
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IExtendedBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        List<BakedQuad> quads = new ObjectArrayList<>();
        BlockRenderLayer currentLayer = getCurrentRenderLayer();

        CacheKey key = toKey(state);
        byte connectionMask = BlockPipe.readConnectionMask(state);
        byte closedMask = safeByte(state.getValue(PipeRenderProperties.CLOSED_MASK_PROPERTY));
        byte blockedMask = safeByte(state.getValue(PipeRenderProperties.BLOCKED_MASK_PROPERTY));
        CoverRendererPackage rendererPackage = state.getValue(CoverRendererPackage.CRP_PROPERTY);
        byte coverMask = rendererPackage == null ? 0 : rendererPackage.getMask();

        StructureQuadCache cache = pipeCache.computeIfAbsent(key, this::constructForKey);

        if (currentLayer == BlockRenderLayer.CUTOUT_MIPPED) {
            ColorData endCapData = new ColorData(END_CAP_COLOR);
            cache.addCoresToList(quads, connectionMask, endCapData);
            cache.addCappersToList(quads, connectionMask, closedMask, coverMask, endCapData);

            Integer fluidColor = state.getValue(PipeRenderProperties.FLUID_COLOR_PROPERTY);
            if (fluidColor != null && fluidColor != 0) {
                int opaqueFluid = 0xFF000000 | (fluidColor & 0x00FFFFFF);
                ColorData fluidData = new ColorData(opaqueFluid);
                StructureQuadCache innerCache = fluidInnerCache.computeIfAbsent(key,
                        this::constructFluidInnerForKey);
                innerCache.addTubesToList(quads, connectionMask, fluidData);
                innerCache.addCoresToList(quads, connectionMask, fluidData);
            }
        }

        if (currentLayer == BlockRenderLayer.TRANSLUCENT || currentLayer == null) {
            ColorData glassData = new ColorData(GLASS_TUBE_COLOR);
            cache.addTubesToList(quads, connectionMask, glassData);

            if (currentLayer == null) {
                cache.addCoresToList(quads, connectionMask, glassData);
                cache.addCappersToList(quads, connectionMask, closedMask, coverMask, glassData);
            }
        }

        if (rendererPackage != null) {
            renderCovers(quads, rendererPackage, state);
        }

        return quads;
    }

    @Override
    protected boolean shouldRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @Nullable
    protected PipeItemModel<CacheKey> getItemModel(PipeModelRedirector redirector, @NotNull ItemStack stack,
                                                   World world, EntityLivingBase entity) {
        if (stack.getItem() instanceof ItemBlockPipe<?, ?>i) {
            return new PipeItemModel<>(redirector, this, new CacheKey(i.getBlock().getPipeType().getThickness()),
                    new ColorData(GLASS_TUBE_COLOR));
        }
        return null;
    }
}

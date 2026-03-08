package gregtech.common.worldgen.stoneLayer;

import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.blocks.StoneVariantBlock.StoneType;
import gregtech.common.blocks.StoneVariantBlock.StoneVariant;

import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

/**
 * Ported from GT6's Loader_Worldgen.java stone layer definitions.
 * Defines all stone layers, their embedded ores, boundary ores, and small gem ores.
 *
 * Chance values are per-block out of 10000, converted from GT6's fraction system:
 * U4=2500, U6=1667, U8=1250, U12=833, U16=625, U24=417, U32=313,
 * U48=208, U64=156, U96=104, U128=78
 */
public class StoneLayerDefinitions {

    // GT6 chance constants converted to per-10000
    private static final int U4 = 2500;
    private static final int U6 = 1667;
    private static final int U8 = 1250;
    private static final int U12 = 833;
    private static final int U16 = 625;
    private static final int U24 = 417;
    private static final int U32 = 313;
    private static final int U48 = 208;
    private static final int U64 = 156;
    private static final int U96 = 104;
    private static final int U128 = 78;

    public static void init() {
        registerLayers();
        registerBoundaryOres();
        registerSmallGemOres();
    }

    // ---- Helper methods for getting block states ----

    private static IBlockState gtStone(StoneType type) {
        return MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH).getState(type);
    }

    private static IBlockState gtCobble(StoneType type) {
        return MetaBlocks.STONE_BLOCKS.get(StoneVariant.COBBLE).getState(type);
    }

    private static IBlockState gtMossy(StoneType type) {
        return MetaBlocks.STONE_BLOCKS.get(StoneVariant.COBBLE_MOSSY).getState(type);
    }

    private static IBlockState vanillaStone() {
        return Blocks.STONE.getDefaultState();
    }

    private static IBlockState vanillaCobble() {
        return Blocks.COBBLESTONE.getDefaultState();
    }

    private static IBlockState vanillaMossyCobble() {
        return Blocks.MOSSY_COBBLESTONE.getDefaultState();
    }

    private static IBlockState vanillaGranite() {
        return Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE);
    }

    private static IBlockState vanillaDiorite() {
        return Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE);
    }

    private static IBlockState vanillaAndesite() {
        return Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE);
    }

    // ---- Layer Registration ----

    private static void registerLayers() {
        // ==========================================
        // Vanilla Stone Layers
        // ==========================================

        // Primary vanilla stone layer - common ores
        new StoneLayer(vanillaStone(), vanillaCobble(), vanillaMossyCobble(), Materials.Stone,
                new StoneLayerOres(Materials.Emerald, U48, 16, 60),
                new StoneLayerOres(Materials.Diamond, U128, 8, 24),
                new StoneLayerOres(Materials.Lapis, U48, 16, 48),
                new StoneLayerOres(Materials.Redstone, U32, 8, 24),
                new StoneLayerOres(Materials.Gold, U64, 8, 32),
                new StoneLayerOres(Materials.Iron, U16, 40, 80),
                new StoneLayerOres(Materials.Coal, U8, 60, 100));

        // Stone layer with iron ores
        new StoneLayer(vanillaStone(), vanillaCobble(), vanillaMossyCobble(), Materials.Stone,
                new StoneLayerOres(Materials.BandedIron, U6, 30, 70));

        // Stone layer with copper/tin
        new StoneLayer(vanillaStone(), vanillaCobble(), vanillaMossyCobble(), Materials.Stone,
                new StoneLayerOres(Materials.Copper, U16, 20, 50),
                new StoneLayerOres(Materials.Cassiterite, U16, 0, 32),
                new StoneLayerOres(Materials.Sphalerite, U16, 0, 32));

        // Coal-bearing stone layer
        new StoneLayer(vanillaStone(), vanillaCobble(), vanillaMossyCobble(), Materials.Coal,
                new StoneLayerOres(Materials.Coal, U8, 60, 100),
                new StoneLayerOres(Materials.BrownLimonite, U16, 0, 32),
                new StoneLayerOres(Materials.YellowLimonite, U16, 0, 32));

        // ==========================================
        // Basaltic Stuff
        // ==========================================

        // Komatiite layers
        new StoneLayer(gtStone(StoneType.KOMATIITE), gtCobble(StoneType.KOMATIITE),
                gtMossy(StoneType.KOMATIITE), Materials.Komatiite);

        new StoneLayer(gtStone(StoneType.KOMATIITE), gtCobble(StoneType.KOMATIITE),
                gtMossy(StoneType.KOMATIITE), Materials.Komatiite,
                new StoneLayerOres(Materials.Magnesite, U16, 20, 50),
                new StoneLayerOres(Materials.Cinnabar, U12, 0, 32),
                new StoneLayerOres(Materials.Redstone, U8, 0, 30),
                new StoneLayerOres(Materials.Pyrite, U12, 0, 30));

        // Kimberlite layers
        new StoneLayer(gtStone(StoneType.KIMBERLITE), gtCobble(StoneType.KIMBERLITE),
                gtMossy(StoneType.KIMBERLITE), Materials.Kimberlite);

        new StoneLayer(gtStone(StoneType.KIMBERLITE), gtCobble(StoneType.KIMBERLITE),
                gtMossy(StoneType.KIMBERLITE), Materials.Kimberlite,
                new StoneLayerOres(Materials.Diamond, U48, 0, 12));

        // Basalt layers
        new StoneLayer(gtStone(StoneType.BASALT), gtCobble(StoneType.BASALT),
                gtMossy(StoneType.BASALT), Materials.Basalt);

        new StoneLayer(gtStone(StoneType.BASALT), gtCobble(StoneType.BASALT),
                gtMossy(StoneType.BASALT), Materials.Basalt,
                new StoneLayerOres(Materials.Bastnasite, U24, 24, 32),
                new StoneLayerOres(Materials.Monazite, U32, 24, 32),
                new StoneLayerOres(Materials.Pyrolusite, U8, 16, 48));

        new StoneLayer(gtStone(StoneType.BASALT), gtCobble(StoneType.BASALT),
                gtMossy(StoneType.BASALT), Materials.Basalt,
                new StoneLayerOres(Materials.Olivine, U32, 0, 32),
                new StoneLayerOres(Materials.Uvarovite, U32, 8, 40),
                new StoneLayerOres(Materials.Grossular, U32, 16, 48),
                new StoneLayerOres(Materials.Chromite, U8, 32, 64));

        // Andesite layers
        new StoneLayer(vanillaAndesite(), null, null, Materials.Andesite);

        new StoneLayer(vanillaAndesite(), null, null, Materials.Andesite,
                new StoneLayerOres(Materials.Gold, U12, 0, 32));

        // ==========================================
        // Chalky Stuff
        // ==========================================

        // Marble layers
        new StoneLayer(gtStone(StoneType.MARBLE), gtCobble(StoneType.MARBLE),
                gtMossy(StoneType.MARBLE), Materials.Marble);

        new StoneLayer(gtStone(StoneType.MARBLE), gtCobble(StoneType.MARBLE),
                gtMossy(StoneType.MARBLE), Materials.Marble,
                new StoneLayerOres(Materials.Cassiterite, U16, 20, 80));

        new StoneLayer(gtStone(StoneType.MARBLE), gtCobble(StoneType.MARBLE),
                gtMossy(StoneType.MARBLE), Materials.Marble,
                new StoneLayerOres(Materials.Cassiterite, U16, 20, 80),
                new StoneLayerOres(Materials.Sphalerite, U8, 10, 30),
                new StoneLayerOres(Materials.Chalcopyrite, U8, 0, 20),
                new StoneLayerOres(Materials.Pyrite, U12, 0, 30));

        // Limestone layers
        new StoneLayer(gtStone(StoneType.LIMESTONE), gtCobble(StoneType.LIMESTONE),
                gtMossy(StoneType.LIMESTONE), Materials.Limestone);

        new StoneLayer(gtStone(StoneType.LIMESTONE), gtCobble(StoneType.LIMESTONE),
                gtMossy(StoneType.LIMESTONE), Materials.Limestone,
                new StoneLayerOres(Materials.Stibnite, U24, 10, 30),
                new StoneLayerOres(Materials.Galena, U8, 30, 120),
                new StoneLayerOres(Materials.Lead, U16, 50, 70));

        new StoneLayer(gtStone(StoneType.LIMESTONE), gtCobble(StoneType.LIMESTONE),
                gtMossy(StoneType.LIMESTONE), Materials.Limestone,
                new StoneLayerOres(Materials.Pyrite, U16, 0, 30),
                new StoneLayerOres(Materials.Galena, U8, 5, 25),
                new StoneLayerOres(Materials.Galena, U8, 80, 120),
                new StoneLayerOres(Materials.Wulfenite, U32, 30, 45),
                new StoneLayerOres(Materials.Powellite, U32, 35, 50),
                new StoneLayerOres(Materials.Molybdenite, U128, 30, 50),
                new StoneLayerOres(Materials.Tetrahedrite, U8, 40, 80),
                new StoneLayerOres(Materials.Copper, U16, 40, 80));

        new StoneLayer(gtStone(StoneType.LIMESTONE), gtCobble(StoneType.LIMESTONE),
                gtMossy(StoneType.LIMESTONE), Materials.Limestone,
                new StoneLayerOres(Materials.Scheelite, U64, 0, 16),
                new StoneLayerOres(Materials.Tungstate, U64, 0, 16),
                new StoneLayerOres(Materials.YellowLimonite, U8, 16, 48),
                new StoneLayerOres(Materials.BrownLimonite, U8, 32, 64),
                new StoneLayerOres(Materials.Malachite, U12, 16, 64));

        // ==========================================
        // Granites
        // ==========================================

        // Black Granite layers
        new StoneLayer(gtStone(StoneType.BLACK_GRANITE), gtCobble(StoneType.BLACK_GRANITE),
                gtMossy(StoneType.BLACK_GRANITE), Materials.GraniteBlack);

        new StoneLayer(gtStone(StoneType.BLACK_GRANITE), gtCobble(StoneType.BLACK_GRANITE),
                gtMossy(StoneType.BLACK_GRANITE), Materials.GraniteBlack,
                new StoneLayerOres(Materials.Cooperite, U32, 0, 16),
                new StoneLayerOres(Materials.Iridium, U64, 0, 8),
                new StoneLayerOres(Materials.Emerald, U64, 24, 48));

        // Red Granite layers
        new StoneLayer(gtStone(StoneType.RED_GRANITE), gtCobble(StoneType.RED_GRANITE),
                gtMossy(StoneType.RED_GRANITE), Materials.GraniteRed);

        new StoneLayer(gtStone(StoneType.RED_GRANITE), gtCobble(StoneType.RED_GRANITE),
                gtMossy(StoneType.RED_GRANITE), Materials.GraniteRed,
                new StoneLayerOres(Materials.Pitchblende, U32, 0, 18),
                new StoneLayerOres(Materials.Uraninite, U32, 0, 16),
                new StoneLayerOres(Materials.Tantalite, U64, 30, 40));

        // Vanilla Granite layers
        new StoneLayer(vanillaGranite(), null, null, Materials.Granite);

        new StoneLayer(vanillaGranite(), null, null, Materials.Granite,
                new StoneLayerOres(Materials.BlueTopaz, U64, 8, 32),
                new StoneLayerOres(Materials.Topaz, U64, 24, 48));

        new StoneLayer(vanillaGranite(), null, null, Materials.Granite,
                new StoneLayerOres(Materials.Apatite, U8, 32, 64),
                new StoneLayerOres(Materials.Phosphate, U24, 36, 60),
                new StoneLayerOres(Materials.TricalciumPhosphate, U24, 40, 56));

        // Diorite layers
        new StoneLayer(vanillaDiorite(), null, null, Materials.Diorite);

        new StoneLayer(vanillaDiorite(), null, null, Materials.Diorite,
                new StoneLayerOres(Materials.Sapphire, U64, 24, 48),
                new StoneLayerOres(Materials.GreenSapphire, U64, 24, 48),
                new StoneLayerOres(Materials.Ruby, U64, 24, 48));

        new StoneLayer(vanillaDiorite(), null, null, Materials.Diorite,
                new StoneLayerOres(Materials.Garnierite, U8, 16, 48),
                new StoneLayerOres(Materials.Pentlandite, U8, 24, 56),
                new StoneLayerOres(Materials.Cobaltite, U8, 32, 64));

        // ==========================================
        // Schists
        // ==========================================

        // Green Schist layers
        new StoneLayer(gtStone(StoneType.GREEN_SCHIST), gtCobble(StoneType.GREEN_SCHIST),
                gtMossy(StoneType.GREEN_SCHIST), Materials.GreenSchist);

        new StoneLayer(gtStone(StoneType.GREEN_SCHIST), gtCobble(StoneType.GREEN_SCHIST),
                gtMossy(StoneType.GREEN_SCHIST), Materials.GreenSchist,
                new StoneLayerOres(Materials.Andradite, U32, 8, 40),
                new StoneLayerOres(Materials.Almandine, U32, 16, 48));

        // Blue Schist layers
        new StoneLayer(gtStone(StoneType.BLUE_SCHIST), gtCobble(StoneType.BLUE_SCHIST),
                gtMossy(StoneType.BLUE_SCHIST), Materials.BlueSchist);

        new StoneLayer(gtStone(StoneType.BLUE_SCHIST), gtCobble(StoneType.BLUE_SCHIST),
                gtMossy(StoneType.BLUE_SCHIST), Materials.BlueSchist,
                new StoneLayerOres(Materials.Spessartine, U32, 8, 40),
                new StoneLayerOres(Materials.Pyrope, U32, 16, 48));

        // ==========================================
        // Other Stuff
        // ==========================================

        // Gneiss layers
        new StoneLayer(gtStone(StoneType.GNEISS), gtCobble(StoneType.GNEISS),
                gtMossy(StoneType.GNEISS), Materials.Gneiss);

        new StoneLayer(gtStone(StoneType.GNEISS), gtCobble(StoneType.GNEISS),
                gtMossy(StoneType.GNEISS), Materials.Gneiss,
                new StoneLayerOres(Materials.Graphite, U8, 0, 64));

        // Shale layers
        new StoneLayer(gtStone(StoneType.SHALE), gtCobble(StoneType.SHALE),
                gtMossy(StoneType.SHALE), Materials.Shale);

        // Slate layers
        new StoneLayer(gtStone(StoneType.SLATE), gtCobble(StoneType.SLATE),
                gtMossy(StoneType.SLATE), Materials.Slate);

        // Quartzite layers
        new StoneLayer(gtStone(StoneType.QUARTZITE), gtCobble(StoneType.QUARTZITE),
                gtMossy(StoneType.QUARTZITE), Materials.Quartzite);

        new StoneLayer(gtStone(StoneType.QUARTZITE), gtCobble(StoneType.QUARTZITE),
                gtMossy(StoneType.QUARTZITE), Materials.Quartzite,
                new StoneLayerOres(Materials.CertusQuartz, U16, 16, 48),
                new StoneLayerOres(Materials.Barite, U32, 0, 32));
    }

    // ---- Boundary Ore Registration ----

    private static void registerBoundaryOres() {
        // Coal/Stone boundary -> Amber-like ores (using Coal as substitute since Amber doesn't exist)
        // In GT6: StoneLayer.bothsides(MT.Coal, MT.Stone, new StoneLayerOres(MT.Amber, U4, 30, 70, ...))

        // Komatiite/Basalt boundary
        // In GT6 spawns Perlite - not available in CEu, skip

        // Limestone/Basalt boundary -> Ilmenite and Rutile (TiO2)
        StoneLayer.bothSides(Materials.Limestone, Materials.Basalt,
                new StoneLayerOres(Materials.Ilmenite, U8, 0, 32),
                new StoneLayerOres(Materials.Rutile, U12, 0, 32));

        // Dolomite/Diorite -> Opal and Diatomite (topbottom in GT6)
        // Using Calcite as substitute for Dolomite (Dolomite doesn't exist in CEu)
        StoneLayer.topBottom(Materials.Calcite, Materials.Diorite,
                new StoneLayerOres(Materials.Opal, U64, 48, 64),
                new StoneLayerOres(Materials.Diatomite, U16, 16, 64));

        // Dolomite/Quartzite -> Kyanite, Lepidolite, Spodumene
        StoneLayer.bothSides(Materials.Calcite, Materials.Quartzite,
                new StoneLayerOres(Materials.Kyanite, U16, 32, 72),
                new StoneLayerOres(Materials.Lepidolite, U32, 16, 48),
                new StoneLayerOres(Materials.Spodumene, U32, 32, 64));

        // Chalk(Marble)/Quartzite -> Asbestos, Talc, Glauconite
        StoneLayer.bothSides(Materials.Marble, Materials.Quartzite,
                new StoneLayerOres(Materials.Asbestos, U4, 0, 48),
                new StoneLayerOres(Materials.Talc, U4, 0, 80),
                new StoneLayerOres(Materials.GlauconiteSand, U4, 32, 80));

        // GraniteBlack/Gneiss -> Biotite
        StoneLayer.bothSides(Materials.GraniteBlack, Materials.Gneiss,
                new StoneLayerOres(Materials.Biotite, U16, 16, 48));

        // GraniteBlack/Marble -> Lapis, Sodalite, Lazurite, Pyrite
        StoneLayer.bothSides(Materials.GraniteBlack, Materials.Marble,
                new StoneLayerOres(Materials.Lapis, U8, 0, 48),
                new StoneLayerOres(Materials.Sodalite, U16, 0, 48),
                new StoneLayerOres(Materials.Lazurite, U16, 0, 48),
                new StoneLayerOres(Materials.Pyrite, U16, 0, 48));

        // GraniteBlack/Basalt -> Diamond, Graphite (topbottom)
        StoneLayer.topBottom(Materials.GraniteBlack, Materials.Basalt,
                new StoneLayerOres(Materials.Diamond, U64, 0, 32),
                new StoneLayerOres(Materials.Graphite, U8, 0, 32));

        // GraniteRed/Gneiss -> Asbestos
        StoneLayer.bothSides(Materials.GraniteRed, Materials.Gneiss,
                new StoneLayerOres(Materials.Asbestos, U64, 16, 48));

        // Granite/Salt -> use Borax
        StoneLayer.bothSides(Materials.Granite, Materials.Salt,
                new StoneLayerOres(Materials.Borax, U8, 16, 48));

        // Talc/Salt -> Borax
        StoneLayer.bothSides(Materials.Talc, Materials.Salt,
                new StoneLayerOres(Materials.Borax, U8, 16, 48));
    }

    // ---- Small Gem Ores ----

    private static void registerSmallGemOres() {
        // In GT6, any material with RANDOM_SMALL_GEM_ORE property gets added.
        // Port the most common gem ores:
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Almandine);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Pyrope);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Sapphire);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.GreenSapphire);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Ruby);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Emerald);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Olivine);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Topaz);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.BlueTopaz);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Opal);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Grossular);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Spessartine);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Andradite);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Uvarovite);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Diamond);
        StoneLayer.RANDOM_SMALL_GEM_ORES.add(Materials.Lapis);
    }
}

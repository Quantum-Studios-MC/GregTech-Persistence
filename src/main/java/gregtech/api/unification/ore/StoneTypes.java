package gregtech.api.unification.ore;

import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.blocks.StoneVariantBlock.StoneVariant;

import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class StoneTypes {

    // Real Types that drop custom Ores

    public static final StoneType STONE = new StoneType(0, "stone", SoundType.STONE, OrePrefix.ore, Materials.Stone,
            () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.STONE),
            state -> state.getBlock() instanceof BlockStone &&
                    state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE,
            true);

    public static StoneType NETHERRACK = new StoneType(1, "netherrack", SoundType.STONE, OrePrefix.oreNetherrack,
            Materials.Netherrack,
            Blocks.NETHERRACK::getDefaultState,
            state -> state.getBlock() == Blocks.NETHERRACK, true, 2);

    public static StoneType ENDSTONE = new StoneType(2, "endstone", SoundType.STONE, OrePrefix.oreEndstone,
            Materials.Endstone,
            Blocks.END_STONE::getDefaultState,
            state -> state.getBlock() == Blocks.END_STONE, true, 3);

    // Dummy Types used for better world generation

    public static StoneType SANDSTONE = new StoneType(3, "sandstone", SoundType.STONE, OrePrefix.oreSand,
            Materials.SiliconDioxide,
            () -> Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT),
            state -> state.getBlock() instanceof BlockSandStone &&
                    state.getValue(BlockSandStone.TYPE) == BlockSandStone.EnumType.DEFAULT,
            false);

    public static StoneType RED_SANDSTONE = new StoneType(4, "red_sandstone", SoundType.STONE, OrePrefix.oreRedSand,
            Materials.SiliconDioxide,
            () -> Blocks.RED_SANDSTONE.getDefaultState().withProperty(BlockRedSandstone.TYPE,
                    BlockRedSandstone.EnumType.DEFAULT),
            state -> state.getBlock() instanceof BlockRedSandstone &&
                    state.getValue(BlockRedSandstone.TYPE) == BlockRedSandstone.EnumType.DEFAULT,
            false);

    public static StoneType GRANITE = new StoneType(5, "granite", SoundType.STONE, OrePrefix.oreGranite,
            Materials.Granite,
            () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.GRANITE),
            state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.GRANITE,
            false);

    public static StoneType DIORITE = new StoneType(6, "diorite", SoundType.STONE, OrePrefix.oreDiorite,
            Materials.Diorite,
            () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.DIORITE),
            state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.DIORITE,
            false);

    public static StoneType ANDESITE = new StoneType(7, "andesite", SoundType.STONE, OrePrefix.oreAndesite,
            Materials.Andesite,
            () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE),
            state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.ANDESITE,
            false);

    public static StoneType BLACK_GRANITE = new StoneType(8, "black_granite", SoundType.STONE,
            OrePrefix.oreBlackgranite, Materials.GraniteBlack,
            () -> gtStoneState(StoneVariantBlock.StoneType.BLACK_GRANITE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.BLACK_GRANITE), false);

    public static StoneType RED_GRANITE = new StoneType(9, "red_granite", SoundType.STONE, OrePrefix.oreRedgranite,
            Materials.GraniteRed,
            () -> gtStoneState(StoneVariantBlock.StoneType.RED_GRANITE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.RED_GRANITE), false);

    public static StoneType MARBLE = new StoneType(10, "marble", SoundType.STONE, OrePrefix.oreMarble, Materials.Marble,
            () -> gtStoneState(StoneVariantBlock.StoneType.MARBLE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.MARBLE), false);

    public static StoneType BASALT = new StoneType(11, "basalt", SoundType.STONE, OrePrefix.oreBasalt, Materials.Basalt,
            () -> gtStoneState(StoneVariantBlock.StoneType.BASALT),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.BASALT), false);

    public static StoneType KOMATIITE = new StoneType(12, "komatiite", SoundType.STONE, OrePrefix.oreKomatiite,
            Materials.Komatiite,
            () -> gtStoneState(StoneVariantBlock.StoneType.KOMATIITE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.KOMATIITE), false);

    public static StoneType KIMBERLITE = new StoneType(13, "kimberlite", SoundType.STONE, OrePrefix.oreKimberlite,
            Materials.Kimberlite,
            () -> gtStoneState(StoneVariantBlock.StoneType.KIMBERLITE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.KIMBERLITE), false);

    public static StoneType LIMESTONE = new StoneType(14, "limestone", SoundType.STONE, OrePrefix.oreLimestone,
            Materials.Limestone,
            () -> gtStoneState(StoneVariantBlock.StoneType.LIMESTONE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.LIMESTONE), false);

    public static StoneType QUARTZITE_STONE = new StoneType(15, "quartzite", SoundType.STONE, OrePrefix.oreQuartzite,
            Materials.Quartzite,
            () -> gtStoneState(StoneVariantBlock.StoneType.QUARTZITE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.QUARTZITE), false);

    public static StoneType GREEN_SCHIST = new StoneType(16, "green_schist", SoundType.STONE, OrePrefix.oreGreenSchist,
            Materials.GreenSchist,
            () -> gtStoneState(StoneVariantBlock.StoneType.GREEN_SCHIST),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.GREEN_SCHIST), false);

    public static StoneType BLUE_SCHIST = new StoneType(17, "blue_schist", SoundType.STONE, OrePrefix.oreBlueSchist,
            Materials.BlueSchist,
            () -> gtStoneState(StoneVariantBlock.StoneType.BLUE_SCHIST),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.BLUE_SCHIST), false);

    public static StoneType SHALE = new StoneType(18, "shale", SoundType.STONE, OrePrefix.oreShale,
            Materials.Shale,
            () -> gtStoneState(StoneVariantBlock.StoneType.SHALE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.SHALE), false);

    public static StoneType SLATE = new StoneType(19, "slate", SoundType.STONE, OrePrefix.oreSlate,
            Materials.Slate,
            () -> gtStoneState(StoneVariantBlock.StoneType.SLATE),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.SLATE), false);

    public static StoneType GNEISS = new StoneType(20, "gneiss", SoundType.STONE, OrePrefix.oreGneiss,
            Materials.Gneiss,
            () -> gtStoneState(StoneVariantBlock.StoneType.GNEISS),
            state -> gtStonePredicate(state, StoneVariantBlock.StoneType.GNEISS), false);

    private static IBlockState gtStoneState(StoneVariantBlock.StoneType stoneType) {
        return MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH).getState(stoneType);
    }

    private static boolean gtStonePredicate(IBlockState state, StoneVariantBlock.StoneType stoneType) {
        StoneVariantBlock block = MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH);
        return state.getBlock() == block && block.getState(state) == stoneType;
    }
}

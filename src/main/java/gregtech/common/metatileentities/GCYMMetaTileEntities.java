package gregtech.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.common.metatileentities.multi.electric.*;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityParallelHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityTieredHatch;

import static gregtech.api.util.GTUtility.gregtechId;

/**
 * Registration class for GCYM (Gregicality Multiblocks) MetaTileEntities.
 * Uses IDs 10100-10199 for multiblocks and 10200-10219 for hatches.
 */
public final class GCYMMetaTileEntities {

    // GCYM Multiblocks
    public static MetaTileEntityLargeMacerator LARGE_MACERATOR;
    public static MetaTileEntityAlloyBlastSmelter ALLOY_BLAST_SMELTER;
    public static MetaTileEntityLargeArcFurnace LARGE_ARC_FURNACE;
    public static MetaTileEntityLargeAssembler LARGE_ASSEMBLER;
    public static MetaTileEntityLargeAutoclave LARGE_AUTOCLAVE;
    public static MetaTileEntityLargeBender LARGE_BENDER;
    public static MetaTileEntityLargeBrewery LARGE_BREWERY;
    public static MetaTileEntityLargeCentrifuge LARGE_CENTRIFUGE;
    public static MetaTileEntityLargeChemicalBath LARGE_CHEMICAL_BATH;
    public static MetaTileEntityLargeExtractor LARGE_EXTRACTOR;
    public static MetaTileEntityLargeCutter LARGE_CUTTER;
    public static MetaTileEntityLargeDistillery LARGE_DISTILLERY;
    public static MetaTileEntityLargeElectrolyzer LARGE_ELECTROLYZER;
    public static MetaTileEntityLargePolarizer LARGE_POLARIZER;
    public static MetaTileEntityLargeExtruder LARGE_EXTRUDER;
    public static MetaTileEntityLargeSolidifier LARGE_SOLIDIFIER;
    public static MetaTileEntityLargeMixer LARGE_MIXER;
    public static MetaTileEntityLargePackager LARGE_PACKAGER;
    public static MetaTileEntityLargeEngraver LARGE_ENGRAVER;
    public static MetaTileEntityLargeSifter LARGE_SIFTER;
    public static MetaTileEntityLargeWiremill LARGE_WIREMILL;
    public static MetaTileEntityElectricImplosionCompressor ELECTRIC_IMPLOSION_COMPRESSOR;
    public static MetaTileEntityMegaBlastFurnace MEGA_BLAST_FURNACE;
    public static MetaTileEntityMegaVacuumFreezer MEGA_VACUUM_FREEZER;
    public static MetaTileEntityLargeCircuitAssembler LARGE_CIRCUIT_ASSEMBLER;

    // GCYM Hatches
    public static final MetaTileEntityParallelHatch[] PARALLEL_HATCH = new MetaTileEntityParallelHatch[4];
    public static final MetaTileEntityTieredHatch[] TIERED_HATCH = new MetaTileEntityTieredHatch[GTValues.V.length];

    private GCYMMetaTileEntities() {}

    public static void init() {
        // GCYM Multiblocks: IDs 10100-10124
        LARGE_MACERATOR = MetaTileEntities.registerMetaTileEntity(10100,
                new MetaTileEntityLargeMacerator(gregtechId("large_macerator")));
        ALLOY_BLAST_SMELTER = MetaTileEntities.registerMetaTileEntity(10101,
                new MetaTileEntityAlloyBlastSmelter(gregtechId("alloy_blast_smelter")));
        LARGE_ARC_FURNACE = MetaTileEntities.registerMetaTileEntity(10102,
                new MetaTileEntityLargeArcFurnace(gregtechId("large_arc_furnace")));
        LARGE_ASSEMBLER = MetaTileEntities.registerMetaTileEntity(10103,
                new MetaTileEntityLargeAssembler(gregtechId("large_assembler")));
        LARGE_AUTOCLAVE = MetaTileEntities.registerMetaTileEntity(10104,
                new MetaTileEntityLargeAutoclave(gregtechId("large_autoclave")));
        LARGE_BENDER = MetaTileEntities.registerMetaTileEntity(10105,
                new MetaTileEntityLargeBender(gregtechId("large_bender")));
        LARGE_BREWERY = MetaTileEntities.registerMetaTileEntity(10106,
                new MetaTileEntityLargeBrewery(gregtechId("large_brewer")));
        LARGE_CENTRIFUGE = MetaTileEntities.registerMetaTileEntity(10107,
                new MetaTileEntityLargeCentrifuge(gregtechId("large_centrifuge")));
        LARGE_CHEMICAL_BATH = MetaTileEntities.registerMetaTileEntity(10108,
                new MetaTileEntityLargeChemicalBath(gregtechId("large_chemical_bath")));
        // 10109 free
        LARGE_EXTRACTOR = MetaTileEntities.registerMetaTileEntity(10110,
                new MetaTileEntityLargeExtractor(gregtechId("large_extractor")));
        LARGE_CUTTER = MetaTileEntities.registerMetaTileEntity(10111,
                new MetaTileEntityLargeCutter(gregtechId("large_cutter")));
        LARGE_DISTILLERY = MetaTileEntities.registerMetaTileEntity(10112,
                new MetaTileEntityLargeDistillery(gregtechId("large_distillery")));
        LARGE_ELECTROLYZER = MetaTileEntities.registerMetaTileEntity(10113,
                new MetaTileEntityLargeElectrolyzer(gregtechId("large_electrolyzer")));
        LARGE_POLARIZER = MetaTileEntities.registerMetaTileEntity(10114,
                new MetaTileEntityLargePolarizer(gregtechId("large_polarizer")));
        LARGE_EXTRUDER = MetaTileEntities.registerMetaTileEntity(10115,
                new MetaTileEntityLargeExtruder(gregtechId("large_extruder")));
        LARGE_SOLIDIFIER = MetaTileEntities.registerMetaTileEntity(10116,
                new MetaTileEntityLargeSolidifier(gregtechId("large_solidifier")));
        LARGE_MIXER = MetaTileEntities.registerMetaTileEntity(10117,
                new MetaTileEntityLargeMixer(gregtechId("large_mixer")));
        LARGE_PACKAGER = MetaTileEntities.registerMetaTileEntity(10118,
                new MetaTileEntityLargePackager(gregtechId("large_packager")));
        LARGE_ENGRAVER = MetaTileEntities.registerMetaTileEntity(10119,
                new MetaTileEntityLargeEngraver(gregtechId("large_engraver")));
        LARGE_SIFTER = MetaTileEntities.registerMetaTileEntity(10120,
                new MetaTileEntityLargeSifter(gregtechId("large_sifter")));
        LARGE_WIREMILL = MetaTileEntities.registerMetaTileEntity(10121,
                new MetaTileEntityLargeWiremill(gregtechId("large_wiremill")));
        ELECTRIC_IMPLOSION_COMPRESSOR = MetaTileEntities.registerMetaTileEntity(10122,
                new MetaTileEntityElectricImplosionCompressor(gregtechId("electric_implosion_compressor")));
        MEGA_BLAST_FURNACE = MetaTileEntities.registerMetaTileEntity(10125,
                new MetaTileEntityMegaBlastFurnace(gregtechId("mega_blast_furnace")));
        MEGA_VACUUM_FREEZER = MetaTileEntities.registerMetaTileEntity(10126,
                new MetaTileEntityMegaVacuumFreezer(gregtechId("mega_vacuum_freezer")));
        LARGE_CIRCUIT_ASSEMBLER = MetaTileEntities.registerMetaTileEntity(10128,
                new MetaTileEntityLargeCircuitAssembler(gregtechId("large_circuit_assembler")));

        // GCYM Hatches: IDs 10200-10219
        // Parallel Hatches: IV, LuV, ZPM, UV (4 tiers)
        for (int i = 0; i < PARALLEL_HATCH.length; i++) {
            int tier = GTValues.IV + i;
            PARALLEL_HATCH[i] = MetaTileEntities.registerMetaTileEntity(10200 + i,
                    new MetaTileEntityParallelHatch(gregtechId(String.format("parallel_hatch.%s",
                            GTValues.VN[tier].toLowerCase())), tier));
        }

        // Tiered Hatches: one per voltage tier
        for (int i = 0; i < TIERED_HATCH.length; i++) {
            if (!GregTechAPI.isHighTier() && i > GTValues.UHV)
                break;

            String voltageName = GTValues.VN[i].toLowerCase();
            TIERED_HATCH[i] = MetaTileEntities.registerMetaTileEntity(10210 + i,
                    new MetaTileEntityTieredHatch(gregtechId("tiered_hatch." + voltageName), i));
        }
    }
}

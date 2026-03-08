package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty.GasTier;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.util.GTUtility.gregtechId;

public class GCYMSecondDegreeMaterials {

    public static void register() {
        HSLASteel = new Material.Builder(3020, gregtechId("hsla_steel"))
                .ingot().fluid()
                .color(0x808080).iconSet(MaterialIconSet.METALLIC)
                .flags(EXT2_METAL, GENERATE_DOUBLE_PLATE, GENERATE_SPRING, GENERATE_FRAME,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_ROTOR)
                .components(Invar, 2, Vanadium, 1, Titanium, 1, Molybdenum, 1)
                .blast(b -> b.temp(1711, GasTier.LOW).blastStats(VA[HV], 1000))
                .build();

        TitaniumTungstenCarbide = new Material.Builder(3021, gregtechId("titanium_tungsten_carbide"))
                .ingot().fluid()
                .color(0x800D0D).iconSet(MaterialIconSet.METALLIC)
                .flags(GENERATE_PLATE)
                .components(TungstenCarbide, 1, TitaniumCarbide, 2)
                .blast(b -> b.temp(3800, GasTier.HIGH).blastStats(VA[EV], 1000))
                .build();

        IncoloyMA956 = new Material.Builder(3022, gregtechId("incoloy_ma_956"))
                .ingot().fluid()
                .color(0x37BF7E).iconSet(MaterialIconSet.METALLIC)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_FRAME)
                .components(VanadiumSteel, 4, Manganese, 2, Aluminium, 5, Yttrium, 2)
                .blast(b -> b.temp(3625, GasTier.MID).blastStats(VA[EV], 800))
                .build();
    }
}

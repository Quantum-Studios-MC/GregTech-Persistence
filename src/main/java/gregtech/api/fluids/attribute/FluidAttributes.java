package gregtech.api.fluids.attribute;

import net.minecraft.client.resources.I18n;

import static gregtech.api.util.GTUtility.gregtechId;

public final class FluidAttributes {

    public static final FluidAttribute ACID = new FluidAttribute(gregtechId("acid"),
            list -> list.add(I18n.format("gregtech.fluid.type_acid.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.acid_proof")));

    public static final FluidAttribute RADIOACTIVE = new FluidAttribute(gregtechId("radioactive"),
            list -> list.add(I18n.format("gregtech.fluid.type_radioactive.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.radioactive_proof")));

    public static final FluidAttribute CORROSIVE = new FluidAttribute(gregtechId("corrosive"),
            list -> list.add(I18n.format("gregtech.fluid.type_corrosive.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.corrosive_proof")));

    public static final FluidAttribute TOXIC = new FluidAttribute(gregtechId("toxic"),
            list -> list.add(I18n.format("gregtech.fluid.type_toxic.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.toxic_proof")));

    public static final FluidAttribute FLAMMABLE = new FluidAttribute(gregtechId("flammable"),
            list -> list.add(I18n.format("gregtech.fluid.type_flammable.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.flammable_proof")));

    public static final FluidAttribute SLUDGE = new FluidAttribute(gregtechId("sludge"),
            list -> list.add(I18n.format("gregtech.fluid.type_sludge.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.sludge_proof")));

    private FluidAttributes() {}
}

package gregtech.common.pipelike.fluidpipe;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperties;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shared tooltip renderer for fluid pipe constraints.
 */
public final class FluidPipeTooltipUtil {

    private FluidPipeTooltipUtil() {}

    public static void appendVanillaTooltip(@NotNull List<String> tooltip,
                                            @NotNull FluidPipeProperties properties,
                                            @NotNull Material material,
                                            @NotNull FluidPipeType pipeType) {
        tooltip.add(TextFormatting.GOLD + "" + TextFormatting.BOLD +
                I18n.format("gregtech.fluid_pipe.constraints.header"));
        tooltip.add(TextFormatting.GRAY + " " + I18n.format("gregtech.fluid_pipe.constraints.material",
                TextFormatting.WHITE + material.getLocalizedName() + TextFormatting.GRAY));
        tooltip.add(TextFormatting.GRAY + " " + I18n.format("gregtech.fluid_pipe.constraints.size",
                TextFormatting.WHITE + pipeType.getName() + TextFormatting.GRAY));

        tooltip.add(TextFormatting.GRAY + " " + I18n.format("gregtech.fluid_pipe.constraints.ph_range",
                TextFormatting.AQUA + String.format("%.1f", properties.getMinPH()) + TextFormatting.GRAY,
                TextFormatting.AQUA + String.format("%.1f", properties.getMaxPH()) + TextFormatting.GRAY,
                TextFormatting.YELLOW + I18n.format(properties.getPHResistanceTierKey()) + TextFormatting.GRAY));

        // Compact proof flags - state containment + chemical
        StringBuilder stateFlags = new StringBuilder();
        appendProofFlag(stateFlags, "Gas", properties.isGasProof());
        appendProofFlag(stateFlags, "Plasma", properties.isPlasmaProof());
        appendProofFlag(stateFlags, "Cryo", properties.isCryoProof());
        tooltip.add(TextFormatting.GRAY + " " + stateFlags.toString());

        StringBuilder chemFlags = new StringBuilder();
        appendProofFlag(chemFlags, "Acid", properties.isAcidProof());
        appendProofFlag(chemFlags, "Toxic", properties.isToxicProof());
        appendProofFlag(chemFlags, "Radioactive", properties.isRadioactiveProof());
        appendProofFlag(chemFlags, "Corrosive", properties.isCorrosiveProof());
        appendProofFlag(chemFlags, "Flammable", properties.isFlammableProof());
        appendProofFlag(chemFlags, "Sludge", properties.isSludgeProof());
        tooltip.add(TextFormatting.GRAY + " " + chemFlags.toString());
    }

    public static void appendMuiTooltip(@NotNull RichTooltip tooltip,
                                        @NotNull FluidPipeProperties properties,
                                        @NotNull Material material,
                                        @NotNull FluidPipeType pipeType) {
        tooltip.addLine(IKey.str(TextFormatting.DARK_AQUA + I18n.format("gregtech.fluid_pipe.constraints.header")));
        tooltip.addLine(IKey.lang("gregtech.fluid_pipe.constraints.material", material.getLocalizedName()));
        tooltip.addLine(IKey.lang("gregtech.fluid_pipe.constraints.size",
                pipeType.getName()));
        tooltip.addLine(IKey.lang("gregtech.fluid_pipe.constraints.ph_range",
                String.format("%.1f", properties.getMinPH()),
                String.format("%.1f", properties.getMaxPH()),
                I18n.format(properties.getPHResistanceTierKey())));
        tooltip.addLine(IKey.lang("gregtech.fluid_pipe.constraints.toxic", proofText(properties.isToxicProof())));
        tooltip.addLine(
                IKey.lang("gregtech.fluid_pipe.constraints.radioactive", proofText(properties.isRadioactiveProof())));
        tooltip.addLine(IKey.lang("gregtech.fluid_pipe.constraints.corrosive", proofText(properties.isCorrosiveProof())));
        tooltip.addLine(IKey.lang("gregtech.fluid_pipe.constraints.flammable", proofText(properties.isFlammableProof())));
        tooltip.addLine(IKey.lang("gregtech.fluid_pipe.constraints.sludge", proofText(properties.isSludgeProof())));
    }

    private static void appendProofLine(@NotNull List<String> tooltip, @NotNull String key, boolean proofed) {
        String value = proofed ?
                TextFormatting.GREEN + I18n.format("gregtech.fluid_pipe.constraints.allowed") :
                TextFormatting.RED + I18n.format("gregtech.fluid_pipe.constraints.restricted");
        tooltip.add(TextFormatting.GRAY + I18n.format(key, value));
    }

    private static void appendProofFlag(@NotNull StringBuilder sb, @NotNull String label, boolean proofed) {
        if (sb.length() > 0) sb.append(TextFormatting.DARK_GRAY).append(" | ");
        sb.append(proofed ? TextFormatting.GREEN : TextFormatting.RED).append(label);
    }

    private static String proofText(boolean proofed) {
        return proofed ?
                TextFormatting.GREEN + I18n.format("gregtech.fluid_pipe.constraints.allowed") :
                TextFormatting.RED + I18n.format("gregtech.fluid_pipe.constraints.restricted");
    }
}

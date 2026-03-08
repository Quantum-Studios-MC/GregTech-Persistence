package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.GTFluid;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidDataProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.fluids.FluidConstants.CRYOGENIC_FLUID_THRESHOLD;

public class FluidTooltipUtil {

    /**
     * Registry Mapping of <Fluid, Tooltip>
     */
    private static final Map<Fluid, List<Supplier<List<String>>>> tooltips = new HashMap<>();

    /**
     * Used to register a tooltip to a Fluid.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     */
    public static void registerTooltip(@NotNull Fluid fluid, @NotNull Supplier<List<String>> tooltip) {
        List<Supplier<List<String>>> list = tooltips.computeIfAbsent(fluid, $ -> new ArrayList<>(1));
        list.add(tooltip);
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param fluid The Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static @NotNull List<String> getFluidTooltip(@Nullable Fluid fluid) {
        if (fluid == null) {
            return Collections.emptyList();
        }

        var list = tooltips.get(fluid);
        if (list == null) return Collections.emptyList();
        List<String> tooltip = new ArrayList<>();
        for (var supplier : list) {
            tooltip.addAll(supplier.get());
        }
        return tooltip;
    }

    public static void handleFluidTooltip(@NotNull RichTooltip tooltip, @Nullable Fluid fluid) {
        if (fluid == null) return;

        if (fluid instanceof GTFluid gtFluid) {
            handleGTFluidTooltip(tooltip, gtFluid);
            return;
        }

        appendRegisteredTooltips(tooltip, fluid);
    }

    private static void appendRegisteredTooltips(@NotNull RichTooltip tooltip, @NotNull Fluid fluid) {

        var tooltipList = tooltips.get(fluid);
        if (tooltipList == null) return;

        for (var subList : tooltipList) {
            for (String tooltipStr : subList.get()) {
                tooltip.addLine(IKey.str(tooltipStr));
            }
        }
    }

    private static void handleGTFluidTooltip(@NotNull RichTooltip tooltip, @NotNull GTFluid fluid) {
        Material material = fluid instanceof GTFluid.GTMaterialFluid matFluid ? matFluid.getMaterial() : null;

        if (material != null && !material.getChemicalFormula().isEmpty()) {
            tooltip.addLine(KeyUtil.string(TextFormatting.YELLOW, material.getChemicalFormula()));
        }

        tooltip.addLine(IKey.lang("gregtech.fluid.temperature", fluid.getTemperature()));
        tooltip.addLine(IKey.lang(fluid.getState().getTranslationKey()));

        List<String> attributeLines = new ArrayList<>();
        fluid.getAttributes().forEach(attribute -> attribute.appendFluidTooltips(attributeLines));
        for (String attributeLine : attributeLines) {
            if (!attributeLine.isEmpty()) {
                tooltip.addLine(IKey.str(TextFormatting.GRAY + "- " + TextFormatting.RESET + attributeLine));
            }
        }

        if (fluid.getTemperature() < CRYOGENIC_FLUID_THRESHOLD) {
            tooltip.addLine(IKey.lang("gregtech.fluid.temperature.cryogenic"));
        }

        if (material != null && material.hasProperty(PropertyKey.FLUID_DATA)) {
            FluidDataProperty data = material.getProperty(PropertyKey.FLUID_DATA);

            tooltip.addLine(IKey.str(TextFormatting.DARK_GRAY + "----------------"));
            tooltip.addLine(IKey.lang("gregtech.fluid_data.viscosity", String.format("%.1f", data.getViscosity()),
                    I18n.format(data.getViscosityCategory())));
            tooltip.addLine(IKey.lang("gregtech.fluid_data.ph", String.format("%.1f", data.getPH()),
                    I18n.format(data.getPHCategory())));
            tooltip.addLine(IKey.lang("gregtech.fluid_data.density", String.format("%.3f", data.getDensity())));
            tooltip.addLine(IKey.lang("gregtech.fluid_data.heat_capacity",
                    String.format("%.3f", data.getSpecificHeatCapacity())));
            tooltip.addLine(IKey.lang("gregtech.fluid_data.surface_tension",
                    String.format("%.1f", data.getSurfaceTension())));

            if (data.getElectricalConductivity() > 0) {
                tooltip.addLine(IKey.lang("gregtech.fluid_data.conductivity",
                        String.format("%.2f", data.getElectricalConductivity())));
            }
        }
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param stack A FluidStack, containing the Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static @NotNull List<String> getFluidTooltip(@Nullable FluidStack stack) {
        if (stack == null) {
            return Collections.emptyList();
        }

        return getFluidTooltip(stack.getFluid());
    }

    public static void handleFluidTooltip(@NotNull RichTooltip tooltip, @Nullable FluidStack stack) {
        if (stack == null) return;
        handleFluidTooltip(tooltip, stack.getFluid());
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param fluidName A String representing a Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static @NotNull List<String> getFluidTooltip(@Nullable String fluidName) {
        if (fluidName == null || fluidName.isEmpty()) {
            return Collections.emptyList();
        }

        return getFluidTooltip(FluidRegistry.getFluid(fluidName));
    }

    public static Supplier<List<String>> createGTFluidTooltip(@NotNull GTFluid fluid) {
        Material material = fluid instanceof GTFluid.GTMaterialFluid matFluid ? matFluid.getMaterial() : null;
        return createFluidTooltip(material, fluid, fluid.getState());
    }

    public static Supplier<List<String>> createFluidTooltip(@Nullable Material material, @NotNull Fluid fluid,
                                                            @NotNull FluidState fluidState) {
        return () -> {
            List<String> tooltip = new ArrayList<>();
            if (material != null && !material.getChemicalFormula().isEmpty()) {
                tooltip.add(TextFormatting.YELLOW + material.getChemicalFormula());
            }

            tooltip.add(I18n.format("gregtech.fluid.temperature", fluid.getTemperature()));
            tooltip.add(I18n.format(fluidState.getTranslationKey()));
            if (fluid instanceof GTFluid gtFluid) {
                gtFluid.getAttributes().forEach(a -> a.appendFluidTooltips(tooltip));
            }

            if (fluid.getTemperature() < CRYOGENIC_FLUID_THRESHOLD) {
                tooltip.add(I18n.format("gregtech.fluid.temperature.cryogenic"));
            }

            if (material != null && material.hasProperty(PropertyKey.FLUID_DATA)) {
                FluidDataProperty data = material.getProperty(PropertyKey.FLUID_DATA);
                tooltip.add(I18n.format("gregtech.fluid_data.viscosity", String.format("%.1f", data.getViscosity()),
                        I18n.format(data.getViscosityCategory())));
                tooltip.add(I18n.format("gregtech.fluid_data.ph", String.format("%.1f", data.getPH()),
                        I18n.format(data.getPHCategory())));
                tooltip.add(I18n.format("gregtech.fluid_data.density", String.format("%.3f", data.getDensity())));
                if (TooltipHelper.isShiftDown()) {
                    tooltip.add(I18n.format("gregtech.fluid_data.heat_capacity",
                            String.format("%.3f", data.getSpecificHeatCapacity())));
                    tooltip.add(I18n.format("gregtech.fluid_data.surface_tension",
                            String.format("%.1f", data.getSurfaceTension())));
                    if (data.getElectricalConductivity() > 0) {
                        tooltip.add(I18n.format("gregtech.fluid_data.conductivity",
                                String.format("%.2f", data.getElectricalConductivity())));
                    }
                }
            }

            return tooltip;
        };
    }

    public static void fluidInfo(@Nullable FluidStack stack, @NotNull RichTooltip tooltip, boolean showAmount,
                                 boolean showTooltip, boolean showMolAmount) {
        if (stack == null) return;

        if (showAmount) {
            tooltip.addLine(IKey.str("%,d L", stack.amount));
        }

        if (showTooltip) {
            handleFluidTooltip(tooltip, stack);
        }

        if (showMolAmount) {
            addIngotMolFluidTooltip(tooltip, stack);
        }
    }

    public static void fluidInfo(@Nullable FluidStack stack, @NotNull RichTooltip tooltip) {
        fluidInfo(stack, tooltip, true, true, true);
    }

    public static void addIngotMolFluidTooltip(@NotNull RichTooltip tooltip, @NotNull FluidStack fluidStack) {
        // Add tooltip showing how many "ingot moles" (increments of 144) this fluid is if shift is held
        if (TooltipHelper.isShiftDown() && fluidStack.amount > GTValues.L) {
            int numIngots = fluidStack.amount / GTValues.L;
            int extra = fluidStack.amount % GTValues.L;
            String fluidAmount = String.format(" %,d L = %,d * %d L", fluidStack.amount, numIngots, GTValues.L);
            if (extra != 0) {
                fluidAmount += String.format(" + %d L", extra);
            }
            tooltip.addLine(KeyUtil.lang(TextFormatting.GRAY, "gregtech.gui.amount_raw", fluidAmount));
        }
    }

    public static @NotNull IKey getFluidModNameKey(@NotNull FluidStack fluidStack) {
        return IKey.str(getFluidModName(fluidStack.getFluid()));
    }

    public static @NotNull String getFluidModName(@NotNull FluidStack fluidStack) {
        return getFluidModName(fluidStack.getFluid());
    }

    public static @NotNull String getFluidModName(@NotNull Fluid fluid) {
        String fluidModId = getFluidModID(fluid);
        ModContainer modContainer = Loader.instance().getIndexedModList().get(fluidModId);
        String modName = modContainer != null ? modContainer.getName() : fluidModId;
        return "§9§o" + modName + "§r";
    }

    public static @NotNull String getFluidModID(@NotNull Fluid fluid) {
        String fluidModName = FluidRegistry.getDefaultFluidName(fluid);
        if (fluidModName == null || fluidModName.isEmpty()) {
            return "unknown";
        }
        int separatorIndex = fluidModName.indexOf(":");
        return separatorIndex > 0 ? fluidModName.substring(0, separatorIndex) : fluidModName;
    }
}

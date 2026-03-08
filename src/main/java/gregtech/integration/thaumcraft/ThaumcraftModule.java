package gregtech.integration.thaumcraft;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import gregtech.modules.GregTechModules;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@GregTechModule(
                moduleID = GregTechModules.MODULE_TC,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.THAUMCRAFT,
                name = "GregTech Thaumcraft Integration",
                description = "Thaumcraft Integration Module")
public class ThaumcraftModule extends IntegrationSubmodule {

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ThaumcraftModule.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        TCRecipes.init();
    }
}

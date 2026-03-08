package gregtech.integration.twilightforest;

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
                moduleID = GregTechModules.MODULE_TF,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.TWILIGHT_FOREST,
                name = "GregTech Twilight Forest Integration",
                description = "Twilight Forest Integration Module")
public class TwilightForestModule extends IntegrationSubmodule {

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(TwilightForestModule.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        TFRecipes.init();
    }
}

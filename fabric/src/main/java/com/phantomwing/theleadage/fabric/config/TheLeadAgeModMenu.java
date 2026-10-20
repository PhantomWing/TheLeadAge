package com.phantomwing.theleadage.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

/**
 * The config screen Mod Menu opens from its mod list, as NeoForge's mod list opens its own: Cloth Config's, made from
 * {@link TheLeadAgeFabricConfig}. Loaded by Mod Menu alone, through the {@code modmenu} entrypoint.
 */
public final class TheLeadAgeModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(TheLeadAgeFabricConfig.class, parent).get();
    }
}

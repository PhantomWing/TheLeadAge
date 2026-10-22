package com.phantomwing.theleadage.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * The config screen Mod Menu opens from its mod list, as NeoForge's mod list opens its own: Cloth Config's, made from
 * {@link TheLeadAgeFabricConfig}. Loaded by Mod Menu alone, through the {@code modmenu} entrypoint.
 */
public final class TheLeadAgeModMenu implements ModMenuApi {
    static {
        // AutoConfig lists every declared field, the option ids' constants among them as text boxes.
        AutoConfig.getGuiRegistry(TheLeadAgeFabricConfig.class).registerPredicateProvider(
                (optionKey, field, config, defaults, registry) -> List.of(),
                field -> Modifier.isStatic(field.getModifiers()));
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(TheLeadAgeFabricConfig.class, parent).get();
    }
}

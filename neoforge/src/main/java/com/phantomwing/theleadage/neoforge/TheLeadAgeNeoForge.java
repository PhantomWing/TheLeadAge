package com.phantomwing.theleadage.neoforge;

import com.phantomwing.theleadage.TheLeadAgeCommon;
import com.phantomwing.theleadage.neoforge.client.TheLeadAgeNeoForgeClient;
import com.phantomwing.theleadage.neoforge.condition.ModConditions;
import com.phantomwing.theleadage.neoforge.loot.ModLootModifiers;
import com.phantomwing.theleadage.neoforge.world.ModBiomeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * NeoForge entrypoint for The Lead Age. Loader-agnostic bootstrap via
 * {@link TheLeadAgeCommon#init()}, then the NeoForge config + (client-only)
 * config screen.
 */
@Mod(TheLeadAgeCommon.MOD_ID)
public final class TheLeadAgeNeoForge {
    public TheLeadAgeNeoForge(IEventBus modEventBus, ModContainer container) {
        TheLeadAgeCommon.init();

        // Config-gated lead-ore biome modifier serializer.
        ModBiomeModifiers.register(modEventBus);

        // Recipe-override condition codec ({@code theleadage:config_boolean}).
        ModConditions.register(modEventBus);

        // Global Loot Modifiers (lead horse armor replacing iron horse armor in loot).
        ModLootModifiers.register(modEventBus);

        container.registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);

        // Client-only: the in-game config screen (isolated so the server never
        // loads the referenced Screen classes).
        if (FMLEnvironment.getDist().isClient()) {
            TheLeadAgeNeoForgeClient.init(container);
        }
    }
}

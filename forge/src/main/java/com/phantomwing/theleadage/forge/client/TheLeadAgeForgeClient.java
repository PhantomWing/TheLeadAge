package com.phantomwing.theleadage.forge.client;

import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.forge.client.compat.LeadSpearModels;
import dev.architectury.platform.Platform;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

/**
 * Client-only bootstrap for The Lead Age on Forge.
 *
 * <p>A class of its own, reached only behind the {@code isClient()} guard in the {@code @Mod} class: the client types
 * it names would otherwise be resolved when that class is verified, on a dedicated server too. Forge 1.20.1 has no
 * config screen of its own to offer (NeoForge's {@code ConfigurationScreen}), so the config is edited in its file.</p>
 */
public final class TheLeadAgeForgeClient {
    private TheLeadAgeForgeClient() {
    }

    public static void init(IEventBus modEventBus) {
        // Panel tint providers are registered by ModColorHandlersForge (a client
        // RegisterColorHandlersEvent subscriber), not here.
        // Vanilla's sprite source types are a plain map, and mods are constructed in parallel: on the main thread, once
        // construction is done and before the first resource load.
        modEventBus.addListener((FMLConstructModEvent event) -> event.enqueueWork(MurkyWaterForge::register));
        modEventBus.addListener(TheLeadAgeForgeClient::clientSetup);
        if (Platform.isModLoaded(ModIds.VANILLA_BACKPORT)) {
            modEventBus.addListener(LeadSpearModels::registerAdditional);
        }
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        // The Lead Spear held as a spear where Vanilla Backport makes it one.
        if (Platform.isModLoaded(ModIds.VANILLA_BACKPORT)) {
            LeadSpearModels.registerHeldModel();
        }
    }
}

package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.neoforge.compat.vanillabackport.LeadSpear;
import dev.architectury.platform.Platform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only bootstrap for The Lead Age on NeoForge.
 *
 * <p>Isolated from the {@code @Mod} class because it references client-only types
 * ({@link ConfigurationScreen} -> {@code Screen}). If those lived in the entrypoint
 * the dedicated server would try to load/verify them and crash; calling {@link #init}
 * via {@code invokestatic} from behind an {@code isClient()} guard keeps them off
 * the server.</p>
 */
public final class TheLeadAgeNeoForgeClient {
    private TheLeadAgeNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus, ModContainer container) {
        // Registers the in-game config screen (Mods -> The Lead Age -> Config).
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // Panel tint providers are registered by ModColorHandlersNeoForge (a client
        // RegisterColorHandlersEvent subscriber), not here.
        modEventBus.addListener(TheLeadAgeNeoForgeClient::clientSetup);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        // The Lead Spear held as a spear where Vanilla Backport makes it one. Backported Spears picks the held
        // model through the item model's own override.
        if (Platform.isModLoaded(ModIds.VANILLA_BACKPORT) && !Platform.isModLoaded(ModIds.BACKPORTED_SPEARS)) {
            LeadSpear.registerHeldModel();
        }
    }
}

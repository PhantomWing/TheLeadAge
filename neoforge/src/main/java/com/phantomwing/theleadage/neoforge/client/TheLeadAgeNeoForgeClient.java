package com.phantomwing.theleadage.neoforge.client;

import net.neoforged.fml.ModContainer;
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

    public static void init(ModContainer container) {
        // Registers the in-game config screen (Mods -> The Lead Age -> Config).
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}

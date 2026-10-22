package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.network.LeadSettingsPayload;
import com.phantomwing.theleadage.platform.LeadSettings;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

/**
 * The client's side of the raw lead settings the server sends ({@code LeadSettingsSync}): kept while it plays there,
 * and the water it tinted drawn again by them; forgotten once it leaves.
 */
public final class LeadSettingsClient {
    private LeadSettingsClient() {
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, LeadSettingsPayload.TYPE, LeadSettingsPayload.CODEC,
                (payload, context) -> context.queue(() -> received(payload)));
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> LeadSettings.forget());
    }

    private static void received(LeadSettingsPayload settings) {
        if (LeadSettings.received(settings) && Minecraft.getInstance().level != null) {
            Minecraft.getInstance().levelExtractor.allChanged();
        }
    }
}

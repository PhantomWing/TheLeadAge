package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.effect.RecedingLeads;
import com.phantomwing.theleadage.network.RecedingLeadPayload;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.networking.NetworkManager;

/**
 * The client's side of the contamination the server says is receding ({@link RecedingLeads}): kept while it plays there,
 * and the water around each step drawn again.
 */
public final class RecedingLeadsClient {
    private RecedingLeadsClient() {
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, RecedingLeadPayload.TYPE, RecedingLeadPayload.CODEC,
                (payload, context) -> context.queue(() -> {
                    RecedingLeads.received(payload.pos(),
                            payload.cut() > 0 ? new RecedingLeads.State(payload.step(), payload.cut()) : null);
                    LeadWaterTint.leadChanged(payload.pos());
                }));
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> RecedingLeads.forgetClient());
    }
}

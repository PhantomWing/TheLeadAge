package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.effect.RecedingLeads;
import com.phantomwing.theleadage.network.RecedingLeadPayload;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * The client's side of the contamination the server says is receding ({@link RecedingLeads}): kept while it plays in that
 * level, and the water around each step drawn again.
 */
public final class RecedingLeadsClient {
    private RecedingLeadsClient() {
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, RecedingLeadPayload.TYPE, RecedingLeadPayload.CODEC,
                (payload, context) -> context.queue(() -> {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) {
                        return;
                    }
                    RecedingLeads.received(payload.pos(),
                            payload.cut() > 0 ? new RecedingLeads.State(payload.step(), payload.cut()) : null, level.getGameTime());
                    LeadWaterTint.leadChanged(payload.pos());
                }));
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> RecedingLeads.forgetClient());
        // A respawn or another dimension: what was told of the level left behind doesn't recede in the next.
        ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register((before, after) -> RecedingLeads.forgetClient());
    }
}

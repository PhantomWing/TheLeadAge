package com.phantomwing.theleadage.network;

import com.phantomwing.theleadage.client.LeadSettingsClient;
import com.phantomwing.theleadage.platform.LeadSettings;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sends the server's raw lead settings ({@link LeadSettings}) to each player as they join, and to everyone again once
 * the server's config changes, which it looks at every second. Registered from {@code TheLeadAgeCommon.init()}.
 */
public final class LeadSettingsSync {
    private static final int LOOK_EVERY = 20;
    private static LeadSettingsPayload lastSent;

    private LeadSettingsSync() {
    }

    public static void register() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            LeadSettingsClient.register();
        }
        PlayerEvent.PLAYER_JOIN.register(LeadSettingsSync::send);
        TickEvent.SERVER_POST.register(LeadSettingsSync::tick);
    }

    private static void send(ServerPlayer player) {
        if (Packets.canReceive(player, LeadSettingsPayload.ID)) {
            Packets.send(player, LeadSettingsPayload.ID, LeadSettings.ofConfig()::write);
        }
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % LOOK_EVERY != 0) {
            return;
        }
        LeadSettingsPayload now = LeadSettings.ofConfig();
        if (now.equals(lastSent)) {
            return;
        }
        boolean first = lastSent == null;
        lastSent = now;
        if (!first) {
            server.getPlayerList().getPlayers().forEach(LeadSettingsSync::send);
        }
    }
}

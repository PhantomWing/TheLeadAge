package com.phantomwing.theleadage.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/** Sending the mod's packets through Architectury 9's {@link NetworkManager}, whose packets are a buffer under an id. */
public final class Packets {
    private Packets() {
    }

    /**
     * Whether {@code player}'s client can read a packet of {@code id}: on Fabric, whether it has the mod (a vanilla or
     * older client is skipped rather than sent what it would drop). On Forge always: Forge refuses a client without
     * the mod at login, and Architectury 9's Forge {@code canPlayerReceive} holds the ids the client sends back for
     * {@code architectury:sync_ids} - its C2S ones, and only after the login event the join sync is sent from - so it
     * never holds for these S2C packets (The Silver Age's 1.20.1 line found it).
     */
    public static boolean canReceive(ServerPlayer player, ResourceLocation id) {
        return Platform.isForge() || NetworkManager.canPlayerReceive(player, id);
    }

    public static void send(ServerPlayer player, ResourceLocation id, Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        writer.accept(buf);
        NetworkManager.sendToPlayer(player, id, buf);
    }
}

package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.client.RecedingLeadsClient;
import com.phantomwing.theleadage.network.RecedingLeadPayload;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Raw lead that has stopped leaking - mined, blown up, pushed away or dried out - whose contamination recedes from its
 * far edge inwards, a block every {@value LeadWater#RECEDE_TICKS} ticks as water flows, rather than clearing at once.
 * The server keeps it, and still doses whoever is in what is left; it sends each block of it to the players near it,
 * whose water is drawn by it. Held in memory only, so what was receding when the server stops is clean water when it
 * starts again. Registered from {@code TheLeadAgeCommon.init()}.
 */
public final class RecedingLeads {
    /** How far from receding lead a player is told of it: farther than any water it leaves is drawn. */
    private static final double SEND_DISTANCE = 96.0;
    private static final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<State>> SERVER = new HashMap<>();
    /** What the server last told this client, replaced whole on each change: chunk-drawing threads read it. */
    private static volatile Long2ObjectMap<State> client = Long2ObjectMaps.emptyMap();

    private RecedingLeads() {
    }

    /**
     * What receding lead left: the step its spreading had got to when it stopped, and the longest route through the water
     * that is still fouled ({@link LeadWater.Leak#strength(int, int, BlockPos)}).
     */
    public record State(int step, int cut) {
    }

    public static void register() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            RecedingLeadsClient.register();
        } else {
            NetworkManager.registerS2CPayloadType(RecedingLeadPayload.TYPE, RecedingLeadPayload.CODEC);
        }
        TickEvent.SERVER_LEVEL_POST.register(RecedingLeads::tick);
        LifecycleEvent.SERVER_STOPPED.register(server -> SERVER.clear());
    }

    /** Raw lead at {@code pos}, {@code step} steps into its spreading, has stopped leaking: what it left starts to recede. */
    public static void start(ServerLevel level, BlockPos pos, int step) {
        State from = LeadWater.recedeFrom(level, pos, step, LeadWater.range(false));
        if (from == null) {
            return;
        }
        Long2ObjectOpenHashMap<State> receding = SERVER.computeIfAbsent(level.dimension(), key -> new Long2ObjectOpenHashMap<>());
        State before = receding.get(pos.asLong());
        if (before == null || before.cut() < from.cut()) {
            receding.put(pos.asLong(), from);
            send(level, pos, from);
        }
    }

    /**
     * The receding lead in {@code level} by position, and what each has left: the server's own, or what it told this
     * client. Read only.
     */
    public static Long2ObjectMap<State> of(LevelReader level) {
        if (level instanceof ServerLevel server) {
            Long2ObjectOpenHashMap<State> receding = SERVER.get(server.dimension());
            return receding == null ? Long2ObjectMaps.emptyMap() : receding;
        }
        return level.isClientSide() ? client : Long2ObjectMaps.emptyMap();
    }

    private static void tick(ServerLevel level) {
        Long2ObjectOpenHashMap<State> receding = SERVER.get(level.dimension());
        long now = level.getGameTime();
        if (receding == null || receding.isEmpty() || now % LeadWater.RECEDE_TICKS != 0) {
            return;
        }
        ObjectIterator<Long2ObjectMap.Entry<State>> entries = receding.long2ObjectEntrySet().fastIterator();
        while (entries.hasNext()) {
            Long2ObjectMap.Entry<State> entry = entries.next();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            State state = new State(entry.getValue().step(), entry.getValue().cut() - 1);
            if (state.cut() <= 0 || !level.isLoaded(pos)) {
                entries.remove();
                send(level, pos, null);
                continue;
            }
            entry.setValue(state);
            if (now % LeadWater.INTERVAL == 0) {
                LeadWater.contaminate(level, pos, state.step(), state.cut());
            }
            send(level, pos, state);
        }
    }

    private static void send(ServerLevel level, BlockPos pos, @Nullable State state) {
        RecedingLeadPayload payload = new RecedingLeadPayload(pos, state == null ? 0 : state.step(), state == null ? 0 : state.cut());
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= SEND_DISTANCE * SEND_DISTANCE
                    && NetworkManager.canPlayerReceive(player, RecedingLeadPayload.TYPE)) {
                NetworkManager.sendToPlayer(player, payload);
            }
        }
    }

    /** The server told this client what receding lead at {@code pos} has left, nothing once it has gone. */
    public static void received(BlockPos pos, @Nullable State state) {
        Long2ObjectOpenHashMap<State> next = new Long2ObjectOpenHashMap<>(client);
        if (state != null) {
            next.put(pos.asLong(), state);
        } else {
            next.remove(pos.asLong());
        }
        client = next;
    }

    /**
     * This client changed raw lead at {@code pos} so it stopped leaking before the server said so - breaking a block shows
     * at once - and what it left recedes from {@code from} until the server's word on it comes.
     */
    public static void predict(BlockPos pos, @Nullable State from) {
        if (from != null && !client.containsKey(pos.asLong())) {
            received(pos, from);
        }
    }

    /** The client left the server: nothing it was told recedes there any more. */
    public static void forgetClient() {
        client = Long2ObjectMaps.emptyMap();
    }
}

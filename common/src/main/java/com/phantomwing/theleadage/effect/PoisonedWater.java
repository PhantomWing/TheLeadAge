package com.phantomwing.theleadage.effect;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Which water raw lead poisons, as the server asks it many times a tick - mobs choosing where to go and planning paths
 * ({@link PoisonedWaterAvoidance}), coral looking for water ({@link LeadWater#poisonsCoral}). The lead around each chunk
 * section asked about is kept for the tick, and each lead's routes through the water for {@link #ROUTES_KEPT} ticks,
 * by each thread for each level: the server ticks every dimension on one thread, and a cache of one level would be
 * thrown away by the next.
 */
public final class PoisonedWater {
    /** How long a lead's routes through the water are kept, in ticks: water seldom changes, and a route a second old only steers. */
    private static final int ROUTES_KEPT = 20;

    private static final ThreadLocal<Map<ResourceKey<Level>, Kept>> KEPT = ThreadLocal.withInitial(HashMap::new);
    /** Counts the changes to raw lead, so no thread's lead around a section outlives one within its tick. */
    private static final AtomicInteger CHANGES = new AtomicInteger();

    private PoisonedWater() {
    }

    /** Raw lead changed - placed, stepped on, gone, receding: the lead around it is found again. */
    public static void leadChanged() {
        CHANGES.incrementAndGet();
    }

    /** The raw lead poisoning the water at {@code pos}, or null where it is clean, dry or not loaded. */
    @Nullable
    public static BlockPos poisoner(ServerLevel level, BlockPos pos) {
        // Most water asked about is nowhere near raw lead: that is told by the section alone, before the block is read.
        LeadWater.Nearby nearby = KEPT.get().computeIfAbsent(level.dimension(), key -> new Kept()).nearby(level, SectionPos.asLong(pos));
        if (nearby.isEmpty() || !LeadWater.isLoaded(level, pos) || !level.getFluidState(pos).is(FluidTags.WATER)) {
            return null;
        }
        return nearby.poisoner(pos);
    }

    /** One thread's lead around the sections it asked about in one level, and each lead's routes. */
    private static final class Kept {
        private final Long2ObjectMap<LeadWater.Nearby> sections = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<Routes> routes = new Long2ObjectOpenHashMap<>();
        @Nullable
        private ServerLevel level;
        private long time = Long.MIN_VALUE;
        private int changes;

        LeadWater.Nearby nearby(ServerLevel level, long section) {
            long now = level.getGameTime();
            int changed = CHANGES.get();
            if (level != this.level) {
                // The same dimension's level made anew: a world left and another opened.
                routes.clear();
                this.level = level;
                time = Long.MIN_VALUE;
            }
            if (now != time || changed != changes) {
                sections.clear();
                routes.values().removeIf(kept -> now < kept.at() || now - kept.at() > ROUTES_KEPT);
                time = now;
                changes = changed;
            }
            LeadWater.Nearby nearby = sections.get(section);
            if (nearby == null) {
                nearby = LeadWater.nearby(level, SectionPos.of(section), lead -> routesOf(level, lead, now));
                sections.put(section, nearby);
            }
            return nearby;
        }

        private LeadWater.Leak routesOf(ServerLevel level, BlockPos lead, long now) {
            Routes kept = routes.get(lead.asLong());
            if (kept != null && kept.leak().range() == LeadWater.range(false)) {
                return kept.leak();
            }
            LeadWater.Leak leak = LeadWater.poisonLeak(level, lead);
            routes.put(lead.asLong(), new Routes(leak, now));
            return leak;
        }

        /** A lead's routes, and the tick they were found. */
        private record Routes(LeadWater.Leak leak, long at) {
        }
    }
}

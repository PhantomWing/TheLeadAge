package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.platform.LeadSettings;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A campfire burning over raw lead - a block in {@link ModTags.Blocks#CAMPFIRE_FUME_SOURCES} - sends a stream of lead
 * fumes up with its smoke, which they taint the lead's dark purple ({@code LeadCampfireSmoke}). A hay bale between the
 * two makes it a signal fire, as it would anywhere: its smoke, and the fumes with it, rise much higher. A player touching the
 * stream breathes it - only where the fumes have got to: the stream climbs from the fire at the smoke's speed once it starts,
 * and clears from the bottom up as the last of it rises once it stops - worse the longer they stay ({@link LeadFumes#touch}); creepers and pillagers
 * keep away from it ({@code AvoidRepellentBlockGoal}). Soul campfires too. Ticked by each loader's
 * {@code CampfireBlockEntityMixin}.
 */
public final class CampfireFumes {
    /** How far the stream rises over the fire: where the campfire's own smoke fades out, and a signal fire's. */
    private static final int STREAM_HEIGHT = 8;
    private static final int SIGNAL_STREAM_HEIGHT = 20;
    /** How often the stream is breathed. */
    private static final int INTERVAL = 10;
    /** How fast the fumes rise, in blocks a tick: the campfire smoke's own speed. */
    private static final double RISE = 0.07;
    /**
     * How long the last of the fumes take to clear once the fire stops giving them off: the longest a puff of its smoke
     * lives, and of a signal fire's.
     */
    private static final int CLEAR_TICKS = 130;
    private static final int SIGNAL_CLEAR_TICKS = 330;
    /**
     * Each campfire's streams, the newest last: when each started, when it stopped or -1, and whether it was a signal
     * fire's. A fire lit again while the last of its fumes still rise starts a stream under them, rather than losing them.
     */
    private static final Map<CampfireBlockEntity, List<long[]>> STREAMS = new WeakHashMap<>();

    private CampfireFumes() {
    }

    /** Whether the campfire at {@code pos} is lit over raw lead, or over a hay bale on raw lead, so it gives off fumes. */
    public static boolean isFuming(Level level, BlockPos pos, BlockState state) {
        if (!state.is(BlockTags.CAMPFIRES) || !state.hasProperty(CampfireBlock.LIT) || !state.getValue(CampfireBlock.LIT)
                || !LeadSettings.campfireFumes(level.isClientSide())) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.is(ModTags.Blocks.CAMPFIRE_FUME_SOURCES)
                || below.is(Blocks.HAY_BLOCK) && level.getBlockState(pos.below(2)).is(ModTags.Blocks.CAMPFIRE_FUME_SOURCES);
    }

    /** Whether the campfire is a signal fire, its smoke rising much higher: a hay bale under it. */
    private static boolean isSignal(BlockState state) {
        return state.hasProperty(CampfireBlock.SIGNAL_FIRE) && state.getValue(CampfireBlock.SIGNAL_FIRE);
    }

    /** A campfire's server tick, lit or not: its stream starts, rises, stops and clears, and is breathed. */
    public static void tick(ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        long now = level.getGameTime();
        List<long[]> streams = STREAMS.get(campfire);
        long[] last = streams == null || streams.isEmpty() ? null : streams.get(streams.size() - 1);
        if (isFuming(level, pos, state)) {
            if (last == null || last[1] >= 0) {
                if (streams == null) {
                    streams = new ArrayList<>();
                    STREAMS.put(campfire, streams);
                }
                streams.add(new long[] {now, -1, isSignal(state) ? 1 : 0});
            }
        } else if (last != null && last[1] < 0) {
            last[1] = now;
        }
        if (streams == null) {
            return;
        }
        streams.removeIf(stream -> stream[1] >= 0 && now - stream[1] > (stream[2] == 1 ? SIGNAL_CLEAR_TICKS : CLEAR_TICKS));
        if (streams.isEmpty()) {
            STREAMS.remove(campfire);
            return;
        }
        if (now % INTERVAL == 0) {
            for (long[] stream : streams) {
                double top = pos.getY() + 0.5 + (now - stream[0]) * RISE;
                double bottom = pos.getY() + 0.5 + (stream[1] < 0 ? 0.0 : (now - stream[1]) * RISE);
                AABB column = column(level, pos, stream[2] == 1);
                if (bottom < Math.min(top, column.maxY)) {
                    dose(level, column.setMinY(bottom).setMaxY(Math.min(top, column.maxY)));
                }
            }
        }
    }

    /** The players touching the stream off the campfire at {@code pos}, risen all the way, breathe it. */
    public static void doseStream(ServerLevel level, BlockPos pos) {
        dose(level, stream(level, pos));
    }

    private static void dose(ServerLevel level, AABB stream) {
        for (Player player : level.getEntitiesOfClass(Player.class, stream,
                player -> !player.isSpectator() && player.isAlive())) {
            LeadFumes.touch(player, LeadFumes.FUMES_STAGE_TICKS);
        }
    }

    /** The stream risen all the way: the column over the fire, up to where its smoke fades or the first block in the way. */
    public static AABB stream(BlockGetter level, BlockPos pos) {
        return column(level, pos, isSignal(level.getBlockState(pos)));
    }

    /** The column a stream rises in, as high as a signal fire's smoke where it is one. */
    private static AABB column(BlockGetter level, BlockPos pos, boolean signal) {
        int height = signal ? SIGNAL_STREAM_HEIGHT : STREAM_HEIGHT;
        BlockPos.MutableBlockPos at = pos.above().mutable();
        while (at.getY() <= pos.getY() + height && level.getBlockState(at).getCollisionShape(level, at).isEmpty()) {
            at.move(Direction.UP);
        }
        return new AABB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1, at.getY(), pos.getZ() + 1);
    }
}

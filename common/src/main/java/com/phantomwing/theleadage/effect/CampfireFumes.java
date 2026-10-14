package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.platform.CommonConfig;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A campfire burning over raw lead - a block in {@link ModTags.Blocks#CAMPFIRE_FUME_SOURCES} - sends a stream of lead
 * fumes up with its smoke, which they taint the lead's dark purple ({@code LeadCampfireSmoke}). A player touching the
 * stream breathes it - only where the fumes have got to: the stream climbs from the fire at the smoke's speed once it starts,
 * and clears from the bottom up as the last of it rises once it stops - worse the longer they stay ({@link LeadFumes#touch}); creepers and pillagers
 * keep away from it ({@code AvoidRepellentBlockGoal}). Soul campfires too. Ticked by each loader's
 * {@code CampfireBlockEntityMixin}.
 */
public final class CampfireFumes {
    /** How far the stream rises over the fire: where the campfire's own smoke fades out. */
    private static final int STREAM_HEIGHT = 8;
    /** How often the stream is breathed. */
    private static final int INTERVAL = 10;
    /** How fast the fumes rise, in blocks a tick: the campfire smoke's own speed. */
    private static final double RISE = 0.07;
    /** How long the last of the fumes take to clear once the fire stops giving them off: the longest a puff lives. */
    private static final int CLEAR_TICKS = 130;
    /** Each campfire's stream: when it started, and when it stopped or -1. */
    private static final Map<CampfireBlockEntity, long[]> STREAMS = new WeakHashMap<>();

    private CampfireFumes() {
    }

    /** Whether the campfire at {@code pos} is lit over raw lead, so it gives off fumes. */
    public static boolean isFuming(BlockGetter level, BlockPos pos, BlockState state) {
        return state.is(BlockTags.CAMPFIRES)
                && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)
                && level.getBlockState(pos.below()).is(ModTags.Blocks.CAMPFIRE_FUME_SOURCES)
                && CommonConfig.campfireLeadFumes();
    }

    /** A campfire's server tick, lit or not: its stream starts, rises, stops and clears, and is breathed. */
    public static void tick(ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        long now = level.getGameTime();
        long[] stream = STREAMS.get(campfire);
        if (isFuming(level, pos, state)) {
            if (stream == null || stream[1] >= 0) {
                stream = new long[] {now, -1};
                STREAMS.put(campfire, stream);
            }
        } else if (stream != null && stream[1] < 0) {
            stream[1] = now;
        }
        if (stream == null) {
            return;
        }
        if (stream[1] >= 0 && now - stream[1] > CLEAR_TICKS) {
            STREAMS.remove(campfire);
            return;
        }
        if (now % INTERVAL == 0) {
            double top = pos.getY() + 0.5 + (now - stream[0]) * RISE;
            double bottom = pos.getY() + 0.5 + (stream[1] < 0 ? 0.0 : (now - stream[1]) * RISE);
            AABB column = stream(level, pos);
            if (bottom < Math.min(top, column.maxY)) {
                dose(level, column.setMinY(bottom).setMaxY(Math.min(top, column.maxY)));
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
        BlockPos.MutableBlockPos at = pos.above().mutable();
        while (at.getY() <= pos.getY() + STREAM_HEIGHT && level.getBlockState(at).getCollisionShape(level, at).isEmpty()) {
            at.move(Direction.UP);
        }
        return new AABB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1, at.getY(), pos.getZ() + 1);
    }
}

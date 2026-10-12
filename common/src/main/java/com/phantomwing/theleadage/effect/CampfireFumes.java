package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.platform.CommonConfig;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * A campfire burning over raw lead - a block in {@link ModTags.Blocks#CAMPFIRE_FUME_SOURCES} - sends a stream of lead
 * fumes up with its smoke, as high and as thick. A player touching the stream breathes it, worse the longer they
 * stay ({@link LeadFumes#touch}); creepers and pillagers keep away from it ({@code AvoidRepellentBlockGoal}). Soul
 * campfires too. Ticked by each loader's {@code CampfireBlockEntityMixin}.
 */
public final class CampfireFumes {
    /** How far the stream rises over the fire: where the campfire's own smoke fades out. */
    private static final int STREAM_HEIGHT = 8;
    /** How often the stream is breathed. */
    private static final int INTERVAL = 10;

    private CampfireFumes() {
    }

    /** Whether the campfire at {@code pos} is lit over raw lead, so it gives off fumes. */
    public static boolean isFuming(BlockGetter level, BlockPos pos, BlockState state) {
        return state.is(BlockTags.CAMPFIRES)
                && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)
                && level.getBlockState(pos.below()).is(ModTags.Blocks.CAMPFIRE_FUME_SOURCES)
                && CommonConfig.campfireLeadFumes();
    }

    /** A lit campfire's server tick. */
    public static void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % INTERVAL == 0 && isFuming(level, pos, state)) {
            doseStream(level, pos);
        }
    }

    /** The players touching the stream off the campfire at {@code pos} breathe it. */
    public static void doseStream(ServerLevel level, BlockPos pos) {
        for (Player player : level.getEntitiesOfClass(Player.class, stream(level, pos),
                player -> !player.isSpectator() && player.isAlive())) {
            LeadFumes.touch(player);
        }
    }

    /** The stream: the column over the fire, up to where its smoke fades or the first block in the way. */
    public static AABB stream(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos at = pos.above().mutable();
        while (at.getY() <= pos.getY() + STREAM_HEIGHT && level.getBlockState(at).getCollisionShape(level, at).isEmpty()) {
            at.move(Direction.UP);
        }
        return new AABB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1, at.getY(), pos.getZ() + 1);
    }

    /** A lit campfire's client tick: the fumes rise with its smoke, at the rate and from the spots vanilla's smoke does. */
    public static void particleTick(Level level, BlockPos pos, BlockState state) {
        RandomSource random = level.getRandom();
        if (random.nextFloat() >= 0.11F || !isFuming(level, pos, state)) {
            return;
        }
        for (int i = random.nextInt(2) + 2; i > 0; i--) {
            level.addAlwaysVisibleParticle(ModParticles.LEAD_FUMES.get(), true,
                    pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                    pos.getY() + random.nextDouble() + random.nextDouble(),
                    pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                    0.0, 0.07, 0.0);
        }
    }
}

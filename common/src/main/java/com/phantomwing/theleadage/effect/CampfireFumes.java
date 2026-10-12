package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.platform.CommonConfig;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A campfire burning over lead - a block in {@link ModTags.Blocks#CAMPFIRE_FUME_SOURCES}, as hay under one sends
 * its smoke higher - gives off lead fumes: bursts at the lead torch's pace that dose the players beside it, and
 * that creepers and pillagers keep away from ({@code AvoidRepellentBlockGoal}). Soul campfires too. Ticked by
 * each loader's {@code CampfireBlockEntityMixin}.
 */
public final class CampfireFumes {
    /** A burst every 130 ticks on average, the lead torch's mean gap. */
    private static final int MEAN_INTERVAL = 130;
    private static final double DOSE_CHANCE = 1.0; // as the torch: a sure dose within the fumes' full range

    private CampfireFumes() {
    }

    /** Whether the campfire at {@code pos} is lit over lead, so it gives off fumes. */
    public static boolean isFuming(BlockGetter level, BlockPos pos, BlockState state) {
        return state.is(BlockTags.CAMPFIRES)
                && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)
                && level.getBlockState(pos.below()).is(ModTags.Blocks.CAMPFIRE_FUME_SOURCES)
                && CommonConfig.campfireLeadFumes();
    }

    /** A lit campfire's server tick. */
    public static void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getRandom().nextInt(MEAN_INTERVAL) == 0 && isFuming(level, pos, state)) {
            burst(level, pos);
        }
    }

    /** One burst: the fumes rise off the fire and dose the players close enough to breathe them. */
    public static void burst(ServerLevel level, BlockPos pos) {
        LeadFumes.plume(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5);
        LeadFumes.expose(level, pos, DOSE_CHANCE);
    }
}

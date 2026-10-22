package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The calls a test makes that each modloader answers its own way, each behind a signature both share. NeoForge's
 * and Fabric's test source sets each have this class, so a test body compiles on both unchanged.
 */
public final class TestLoader {
    private TestLoader() {
    }

    /**
     * The colour Fabric's beacon takes from the block at {@code pane}, or null: vanilla's beacon has no colour of the
     * block's own to ask for, so the mod's beacon mixin asks the pane, as this does.
     */
    public static Integer beaconColor(GameTestHelper helper, BlockPos pane, BlockPos beacon) {
        BlockState state = helper.getBlockState(pane);
        return state.getBlock() instanceof LeadedGlassPaneBlock block
                ? block.beaconColor(state, helper.getLevel(), helper.absolutePos(pane)) : null;
    }
}

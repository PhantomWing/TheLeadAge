package com.phantomwing.theleadage.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * The calls a test makes that each modloader answers its own way, each behind a signature both share. NeoForge's
 * and Fabric's test source sets each have this class, so a test body compiles on both unchanged.
 */
public final class TestLoader {
    private TestLoader() {
    }

    /** The colour NeoForge's beacon takes from the block at {@code pane} for a beam from {@code beacon}, or null. */
    public static Integer beaconColor(GameTestHelper helper, BlockPos pane, BlockPos beacon) {
        return helper.getBlockState(pane).getBeaconColorMultiplier(helper.getLevel(), helper.absolutePos(pane),
                helper.absolutePos(beacon));
    }
}

package com.phantomwing.theleadage.block.custom;

import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Surface transformations applied when a {@link HeavyOrbBlock} lands on a block hard
 * enough — e.g. brick variants crack and grass is pounded into dirt. Extend the map
 * to add more impact transforms.
 */
public final class HeavyOrbTransforms {
    private static final Map<Block, Block> TRANSFORMS = Util.make(new HashMap<>(), map -> {
        // Brick/tile variants crack under the impact.
        map.put(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS);
        map.put(Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
        map.put(Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS);
        map.put(Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES);
        map.put(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS);
        map.put(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        // Grass-topped dirt blocks get pounded down to bare dirt.
        map.put(Blocks.GRASS_BLOCK, Blocks.DIRT);
        map.put(Blocks.PODZOL, Blocks.DIRT);
        map.put(Blocks.MYCELIUM, Blocks.DIRT);
    });

    private HeavyOrbTransforms() {
    }

    /** The block {@code state} becomes on a hard impact, or {@code null} if it doesn't transform. */
    @Nullable
    public static BlockState transform(BlockState state) {
        Block result = TRANSFORMS.get(state.getBlock());
        return result == null ? null : result.defaultBlockState();
    }
}

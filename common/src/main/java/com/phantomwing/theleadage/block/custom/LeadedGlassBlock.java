package com.phantomwing.theleadage.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Clear leaded glass. Plain vanilla glass in every respect except the break: see
 * {@link LeadedGlassShatter} for why a pickaxe break sounds like lead and any other break sounds like
 * glass.
 */
public class LeadedGlassBlock extends TransparentBlock {
    public LeadedGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void spawnDestroyByEntityParticles(Level level, @Nullable Entity breaker, BlockPos pos, BlockState state) {
        LeadedGlassShatter.spawnDestroyEffect(level, breaker, pos, state,
                LeadedGlassShatter.shatteredGlass(null));
    }
}

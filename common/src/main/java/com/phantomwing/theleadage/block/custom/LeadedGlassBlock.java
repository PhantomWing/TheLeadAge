package com.phantomwing.theleadage.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clear leaded glass. Plain vanilla glass in every respect except the break: see
 * {@link LeadedGlassShatter} for why a pickaxe break sounds like lead and any other break sounds like
 * glass.
 */
public class LeadedGlassBlock extends AbstractGlassBlock {
    public LeadedGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        LeadedGlassShatter.spawnDestroyEffect(level, player, pos, state,
                LeadedGlassShatter.shatteredGlass(null));
    }
}

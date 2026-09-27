package com.phantomwing.theleadage.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Dyed leaded glass. Still a {@link StainedGlassBlock}, so it keeps the beacon-beam tint; it only
 * differs in the break sound (see {@link LeadedGlassShatter}), whose shatter is colour-matched to the
 * equivalent vanilla stained glass.
 */
public class StainedLeadedGlassBlock extends StainedGlassBlock {
    public StainedLeadedGlassBlock(DyeColor color, Properties properties) {
        super(color, properties);
    }

    @Override
    public void spawnDestroyByEntityParticles(Level level, @Nullable Entity breaker, BlockPos pos, BlockState state) {
        LeadedGlassShatter.spawnDestroyEffect(level, breaker, pos, state,
                LeadedGlassShatter.shatteredGlass(getColor()));
    }
}

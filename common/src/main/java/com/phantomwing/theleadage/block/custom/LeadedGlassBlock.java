package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clear leaded glass. Plain vanilla glass in every respect except the break: see
 * {@link LeadedGlassShatter} for why a pickaxe break sounds like lead and any other break sounds like
 * glass.
 */
public class LeadedGlassBlock extends TransparentBlock {
    /** Typed as the parent's codec type: {@code TransparentBlock#codec} is declared concretely, so an override must match. */
    public static final MapCodec<TransparentBlock> CODEC = simpleCodec(LeadedGlassBlock::new);

    public LeadedGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        LeadedGlassShatter.spawnDestroyEffect(level, player, pos, state,
                LeadedGlassShatter.shatteredGlass(null));
    }
}

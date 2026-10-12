package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The block of raw lead, which contaminates the water on top of it ({@link LeadWater}). Its wet state is potent
 * sulfur's, and only a wet one ticks, every {@value LeadWater#INTERVAL} ticks rather than with a block entity, so the
 * blocks in a storage room cost nothing.
 */
public class RawLeadBlock extends Block {
    public static final MapCodec<RawLeadBlock> CODEC = simpleCodec(RawLeadBlock::new);
    /** A water source on top. */
    public static final BooleanProperty WET = BooleanProperty.create("wet");

    public RawLeadBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WET, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WET);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WET, LeadWater.isWet(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.UP ? state.setValue(WET, LeadWater.isWet(level, pos)) : state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (state.getValue(WET) && !(oldState.is(this) && oldState.getValue(WET))) {
            level.scheduleTick(pos, this, LeadWater.INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(WET)) {
            LeadWater.contaminate(level, pos);
            level.scheduleTick(pos, this, LeadWater.INTERVAL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(WET) || !CommonConfig.leadWaterContamination()) {
            return;
        }
        BlockPos spot = LeadWater.gasSpot(level, pos);
        if (spot != null) {
            level.addAlwaysVisibleParticle(ModParticles.LEAD_FUMES_CLOUD.get(),
                    spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }
}

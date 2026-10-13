package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.effect.LeadFumes;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The block of raw lead: unrefined lead, poisonous to handle. A player who stands on it a few seconds gets Lead
 * Sickness, worse the longer they stay, sneaking or not, and lead dust lifts off it. In water it leaks
 * ({@link LeadWater}). Only a block in water ticks, every {@value LeadWater#INTERVAL} ticks rather than with a
 * block entity, so the blocks in a storage room cost nothing.
 */
public class RawLeadBlock extends Block {
    public static final MapCodec<RawLeadBlock> CODEC = simpleCodec(RawLeadBlock::new);
    /** How far it has leaked into the water it touches: 0 out of water, else the steps its spreading has taken. */
    public static final IntegerProperty CONTAMINATION = IntegerProperty.create("contamination", 0, LeadWater.MAX_STEP);
    /** The spreading takes a step in this many of every three checks: about 20 seconds to spread as far as it does. */
    private static final int SPREAD_CHANCE = 2;

    public RawLeadBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CONTAMINATION, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTAMINATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(CONTAMINATION,
                LeadWater.range() > 0 && LeadWater.touchesWater(context.getLevel(), context.getClickedPos()) ? 1 : 0);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        if (LeadWater.range() == 0 || !LeadWater.touchesWater(level, pos)) {
            return state.setValue(CONTAMINATION, 0);
        }
        return state.getValue(CONTAMINATION) == 0 ? state.setValue(CONTAMINATION, 1) : state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (state.getValue(CONTAMINATION) > 0 && !(oldState.is(this) && oldState.getValue(CONTAMINATION) > 0)) {
            level.scheduleTick(pos, this, LeadWater.INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int step = state.getValue(CONTAMINATION);
        if (step == 0) {
            return;
        }
        int range = LeadWater.range();
        if (range == 0) {
            level.setBlock(pos, state.setValue(CONTAMINATION, 0), Block.UPDATE_CLIENTS);
            return;
        }
        LeadWater.contaminate(level, pos, step);
        if (step < Math.min(LeadWater.lastStep(range), LeadWater.MAX_STEP) && random.nextInt(3) < SPREAD_CHANCE) {
            level.setBlock(pos, state.setValue(CONTAMINATION, step + 1), Block.UPDATE_CLIENTS);
        }
        level.scheduleTick(pos, this, LeadWater.INTERVAL);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof Player player && !player.isSpectator() && CommonConfig.rawLeadContact()) {
            LeadFumes.stand(player, LeadFumes.CONTACT_STAGE_TICKS);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (CommonConfig.rawLeadContact() && level.getBlockState(pos.above()).isAir()) {
            for (int i = random.nextInt(3); i > 0; i--) {
                level.addParticle(ModParticles.LEAD_DUST.get(), pos.getX() + 0.1 + random.nextDouble() * 0.8,
                        pos.getY() + 1.02 + random.nextDouble() * 0.15, pos.getZ() + 0.1 + random.nextDouble() * 0.8, 0.0, 0.0, 0.0);
            }
        }
    }
}

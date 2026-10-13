package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.effect.LeadFumes;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import org.joml.Vector3f;

/**
 * The block of raw lead: unrefined lead, poisonous to handle. A player who stands on it a while gets Lead Sickness,
 * slowly worse the longer they stay, sneaking or not, and it gives off a faint wisp of the lead's white smoke. In water it
 * leaks ({@link LeadWater}). Only a block in water ticks, every {@value LeadWater#INTERVAL} ticks rather than with a
 * block entity, so the blocks in a storage room cost nothing.
 */
public class RawLeadBlock extends Block {
    public static final MapCodec<RawLeadBlock> CODEC = simpleCodec(RawLeadBlock::new);
    /** How far it has leaked into the water it touches: 0 out of water, else the steps its spreading has taken. */
    public static final IntegerProperty CONTAMINATION = IntegerProperty.create("contamination", 0, LeadWater.MAX_STEP);
    /** The spreading takes a step in this many of every three checks: about 20 seconds to spread as far as it does. */
    private static final int SPREAD_CHANCE = 2;
    private static final DustParticleOptions SPECK = new DustParticleOptions(new Vector3f(0.28F, 0.30F, 0.33F), 1.5F);

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
                LeadWater.touchesWater(context.getLevel(), context.getClickedPos()) ? 1 : 0);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        if (!LeadWater.touchesWater(level, pos)) {
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
        LeadWater.contaminate(level, pos, step);
        if (step < LeadWater.MAX_STEP && random.nextInt(3) < SPREAD_CHANCE) {
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
        if (CommonConfig.rawLeadContact() && level.getBlockState(pos.above()).isAir() && random.nextBoolean()) {
            level.addParticle(ParticleTypes.WHITE_SMOKE, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.05,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.01, 0.0);
        }
        int step = state.getValue(CONTAMINATION);
        if (step == 0 || !CommonConfig.leadWaterContamination()) {
            return;
        }
        // A block's animation ticks every second or two, so each lets loose a good handful, more the further it reaches.
        LeadWater.Leak leak = LeadWater.leak(level, pos);
        for (int i = Math.max(2, leak.size() / 4); i > 0; i--) {
            BlockPos at = leak.pick(step, random);
            if (at != null && random.nextFloat() < leak.strength(step, at)) {
                level.addParticle(SPECK, at.getX() + random.nextDouble(), at.getY() + random.nextDouble(),
                        at.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
            }
        }
    }
}

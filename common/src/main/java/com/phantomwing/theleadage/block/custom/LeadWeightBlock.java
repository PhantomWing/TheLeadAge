package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.entity.custom.LeadWeightEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An 8³ lead ball that falls like an anvil and crushes whatever it lands on (the
 * combat logic lives in {@link LeadWeightEntity}). When placed against the underside
 * of a block it {@link #HANGING hangs} from a chain until that block is broken,
 * then drops.
 *
 * <p>Anvil-style wear: a hard ground landing has a fall-scaled chance to chip the
 * weight down a tier — {@code lead_weight → chipped → damaged → shatters}. The tier
 * is the block's identity (three registered blocks); the chain lives in
 * {@link ModBlocks#nextWeightTier}. Landing on a hopper is exempt (it's collected
 * intact). Stackable items, so no per-item durability and no block entity.</p>
 */
public class LeadWeightBlock extends FallingBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<LeadWeightBlock> CODEC = simpleCodec(LeadWeightBlock::new);
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    private static final int LEAD_DUST_COLOR = 0x595959;
    /** Minimum blocks fallen for the impact to transform the surface (crack bricks, etc.). */
    private static final double TRANSFORM_FALL_DISTANCE = 3.0;
    // Landing thud volume scales with the drop, clamped between a soft tap and a full slam.
    private static final float MIN_THUD_VOLUME = 0.3f;
    private static final float MAX_THUD_VOLUME = 1.0f;
    private static final double FULL_THUD_FALL = 10.0; // drop (blocks) at which the thud maxes out

    // Chip chance ramps with the drop: 0 at/under BREAK_FALL_MIN, then +10%/block to BREAK_CHANCE_MAX.
    private static final double BREAK_FALL_MIN = 2.0;     // at/under this a landing never chips it
    private static final double BREAK_FALL_MAX = 12.0;    // at/over this the chance is maxed (100%)
    private static final double BREAK_CHANCE_MAX = 1.0;   // a 12-block+ drop always chips a tier

    public LeadWeightBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HANGING, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState()
                .setValue(HANGING, canHang(context.getLevel(), context.getClickedPos()))
                .setValue(WATERLOGGED, waterlogged);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /** True when something directly above can anchor the weight: a sturdy ceiling, another weight, or a vertical chain. */
    private static boolean canHang(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        return aboveState.getBlock() instanceof LeadWeightBlock
                || isVerticalChain(aboveState)
                || aboveState.isFaceSturdy(level, above, Direction.DOWN);
    }

    /** A chain (lead or vanilla) standing upright — a valid anchor for the weight's own chain to hang from. */
    private static boolean isVerticalChain(BlockState state) {
        return state.getBlock() instanceof ChainBlock
                && state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HANGING)) {
            // Hangs until the block it is attached to is gone, then the chain snaps.
            if (!canHang(level, pos) && pos.getY() >= level.getMinBuildHeight()) {
                detach(level, pos, state);
            }
        } else if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            LeadWeightEntity.fromBlock(level, pos, state, null);
        }
    }

    /** Right-clicking a hanging weight snaps its chain (then it falls, or sits if a floor holds it up). */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!state.getValue(HANGING)) {
            return InteractionResult.PASS;
        }
        // Only snap the chain when empty-handed — any held item passes through, so you can place a weight
        // under/beside this one (or use other items) without the one you click dropping.
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            detach(level, pos, state);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** A projectile (e.g. an arrow) hitting a hanging weight snaps its chain too, crediting the shooter. */
    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        super.onProjectileHit(level, state, hit, projectile);
        if (!level.isClientSide && state.getValue(HANGING)) {
            Player shooter = projectile.getOwner() instanceof Player player ? player : null;
            detach(level, hit.getBlockPos(), state, shooter);
        }
    }

    private static void detach(Level level, BlockPos pos, BlockState state) {
        detach(level, pos, state, null);
    }

    /** Snap the chain: the weight falls if nothing solid holds it up, otherwise it just settles in place. */
    private static void detach(Level level, BlockPos pos, BlockState state, Player owner) {
        level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f);
        BlockState detached = state.setValue(HANGING, false);
        BlockState below = level.getBlockState(pos.below());
        // Drop if there's nothing under it — or if the thing under it is itself a lead weight, which is no
        // real floor (it may be hanging/falling too), so a detached stack of orbs all drops together.
        boolean falls = isFree(below) || below.getBlock() instanceof LeadWeightBlock;
        if (falls && pos.getY() >= level.getMinBuildHeight()) {
            LeadWeightEntity.fromBlock(level, pos, detached, owner);
        } else {
            level.setBlock(pos, detached, Block.UPDATE_ALL); // a floor holds it — just settle (no fall thud)
        }
    }

    /** Heavy landing thud + a burst of dust from the surface it struck, then a chance to chip down a tier. */
    @Override
    public void onLand(Level level, BlockPos pos, BlockState fallingState, BlockState landOnState, FallingBlockEntity entity) {
        BlockState surface = level.getBlockState(pos.below());
        float thud = thudVolume(entity, pos);

        // Material character comes from the struck surface's place sound (a dull thump on
        // dirt, a clack on stone, ...), pitched down and layered with a deep mace-style
        // smash so it always lands as a heavy "thud" rather than a metallic clank. Both scale
        // with the drop, so a short fall is a soft tap and a long one a full slam.
        if (!surface.isAir()) {
            SoundType surfaceSound = surface.getSoundType();
            level.playSound(null, pos, surfaceSound.getPlaceSound(), SoundSource.BLOCKS,
                    surfaceSound.getVolume() * 1.2f * thud, surfaceSound.getPitch() * 0.75f);
        }
        level.playSound(null, pos, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.BLOCKS, thud, 0.9f);

        if (level instanceof ServerLevel server && !surface.isAir()) {
            double cx = pos.getX() + 0.5, cy = pos.getY() + 0.05, cz = pos.getZ() + 0.5;
            // Dust of the surface the weight struck, not the weight itself.
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, surface),
                    cx, cy, cz, 30, 0.35, 0.05, 0.35, 0.15);
            server.sendParticles(ParticleTypes.POOF, cx, cy + 0.1, cz, 8, 0.3, 0.02, 0.3, 0.02);
        }

        double fall = entity.getStartPos().getY() - pos.getY();

        // A hard enough impact transforms certain surfaces (bricks crack, grass -> dirt).
        if (fall >= TRANSFORM_FALL_DISTANCE) {
            BlockState transformed = LeadWeightTransforms.transform(surface);
            if (transformed != null) {
                level.setBlockAndUpdate(pos.below(), transformed);
            }
        }

        // The landing can chip the weight down a tier (the last tier shatters). A weight a hopper is about
        // to swallow is exempt — it's collected intact (see LeadWeightEntity#convertHopperLanding).
        boolean ontoHopper = surface.getBlock() instanceof HopperBlock;
        if (!ontoHopper && entity.getRandom().nextDouble() < breakChance(fall)) {
            degradeLanded(level, pos);
        }
    }

    /** Chip the just-landed weight at {@code pos} to its next tier, or shatter it if it was the last. */
    private void degradeLanded(Level level, BlockPos pos) {
        Block next = ModBlocks.nextWeightTier(this);
        if (next == null) {
            level.removeBlock(pos, false); // damaged tier -> it shatters and is gone
            shatterFx(level, pos);
            return;
        }
        BlockState placed = level.getBlockState(pos);
        boolean waterlogged = placed.hasProperty(WATERLOGGED) && placed.getValue(WATERLOGGED);
        level.setBlock(pos, next.defaultBlockState().setValue(WATERLOGGED, waterlogged), Block.UPDATE_ALL);
        chipFx(level, pos);
    }

    /**
     * Called when the weight can't place where it lands (e.g. it came down on an entity) and drops as an
     * item instead — {@link #onLand} never fires, so give it the thud here too. No chip: that's a crush,
     * not a ground landing, and FallingBlockEntity drops the same-tier item.
     */
    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity entity) {
        level.playSound(null, pos, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.BLOCKS, thudVolume(entity, pos), 0.9f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, 20, 0.3, 0.1, 0.3, 0.1);
        }
    }

    /** A short metallic crack + a puff of lead dust when the weight chips down a tier. */
    private void chipFx(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5f, 1.5f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 12, 0.25, 0.15, 0.25, 0.08);
        }
    }

    /** Heavy "lead weight cracks apart" sound + a burst of its own dust, when the last tier shatters. */
    private void shatterFx(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 0.8f, 0.8f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 35, 0.3, 0.2, 0.3, 0.12);
        }
    }

    /**
     * Chance a landing chips the weight, ramping from 0 at/under {@value #BREAK_FALL_MIN} blocks fallen to
     * {@value #BREAK_CHANCE_MAX} at {@value #BREAK_FALL_MAX}+ — so short hops are safe and long drops punish.
     */
    public static double breakChance(double fallDistance) {
        if (fallDistance <= BREAK_FALL_MIN) {
            return 0.0;
        }
        double t = Mth.clamp((fallDistance - BREAK_FALL_MIN) / (BREAK_FALL_MAX - BREAK_FALL_MIN), 0.0, 1.0);
        return t * BREAK_CHANCE_MAX;
    }

    /** Landing thud volume, ramped from a soft tap (short drop) to a full slam (long drop). */
    private static float thudVolume(FallingBlockEntity entity, BlockPos pos) {
        double fall = entity.getStartPos().getY() - pos.getY();
        float t = (float) Mth.clamp((fall - 1.0) / (FULL_THUD_FALL - 1.0), 0.0, 1.0);
        return Mth.lerp(t, MIN_THUD_VOLUME, MAX_THUD_VOLUME);
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return LEAD_DUST_COLOR;
    }
}

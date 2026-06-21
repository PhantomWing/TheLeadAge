package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.entity.custom.HeavyOrbEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A 12³ lead ball that falls like an anvil and crushes whatever it lands on (the
 * combat logic lives in {@link HeavyOrbEntity}). When placed against the underside
 * of a block it {@link #HANGING hangs} from a chain until that block is broken,
 * then drops.
 */
public class HeavyOrbBlock extends FallingBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<HeavyOrbBlock> CODEC = simpleCodec(HeavyOrbBlock::new);
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    private static final int LEAD_DUST_COLOR = 0x595959;
    /** Minimum blocks fallen for the impact to transform the surface (crack bricks, etc.). */
    private static final double TRANSFORM_FALL_DISTANCE = 5.0;

    public HeavyOrbBlock(Properties properties) {
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

    /** True when there is a sturdy block face directly above to hang the chain from. */
    private static boolean canHang(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HANGING)) {
            // Hangs until the block it is attached to is gone, then the chain snaps and it falls.
            if (!canHang(level, pos) && pos.getY() >= level.getMinBuildHeight()) {
                level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f);
                HeavyOrbEntity.fromBlock(level, pos, state.setValue(HANGING, false), null);
            }
        } else if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            HeavyOrbEntity.fromBlock(level, pos, state, null);
        }
    }

    /** Heavy landing thud + a burst of dust from the surface it struck. */
    @Override
    public void onLand(Level level, BlockPos pos, BlockState fallingState, BlockState landOnState, FallingBlockEntity entity) {
        BlockState surface = level.getBlockState(pos.below());

        // Material character comes from the struck surface's place sound (a dull thump on
        // dirt, a clack on stone, ...), pitched down and layered with a deep mace-style
        // smash so it always lands as a heavy "thud" rather than a metallic clank.
        if (!surface.isAir()) {
            SoundType surfaceSound = surface.getSoundType();
            level.playSound(null, pos, surfaceSound.getPlaceSound(), SoundSource.BLOCKS,
                    surfaceSound.getVolume() * 1.2f, surfaceSound.getPitch() * 0.75f);
        }
        level.playSound(null, pos, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.BLOCKS, 0.9f, 0.9f);

        if (level instanceof ServerLevel server && !surface.isAir()) {
            double cx = pos.getX() + 0.5, cy = pos.getY() + 0.05, cz = pos.getZ() + 0.5;
            // Dust of the surface the orb struck, not the orb itself.
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, surface),
                    cx, cy, cz, 30, 0.35, 0.05, 0.35, 0.15);
            server.sendParticles(ParticleTypes.POOF, cx, cy + 0.1, cz, 8, 0.3, 0.02, 0.3, 0.02);
        }

        // A hard enough impact transforms certain surfaces (bricks crack, grass -> dirt).
        if (entity.getStartPos().getY() - pos.getY() >= TRANSFORM_FALL_DISTANCE) {
            BlockState transformed = HeavyOrbTransforms.transform(surface);
            if (transformed != null) {
                level.setBlockAndUpdate(pos.below(), transformed);
            }
        }
    }

    /**
     * Called when the orb can't place where it lands (e.g. it came down on an entity) and
     * drops as an item instead — {@link #onLand} never fires, so give it the thud here too.
     */
    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity entity) {
        level.playSound(null, pos, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.BLOCKS, 0.9f, 0.9f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, 20, 0.3, 0.1, 0.3, 0.1);
        }
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return LEAD_DUST_COLOR;
    }
}

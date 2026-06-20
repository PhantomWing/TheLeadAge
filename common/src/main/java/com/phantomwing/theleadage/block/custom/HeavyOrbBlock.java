package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.entity.custom.HeavyOrbEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A 12³ lead ball that falls like an anvil and crushes whatever it lands on (the
 * combat logic lives in {@link HeavyOrbEntity}). When placed against the underside
 * of a block it {@link #HANGING hangs} from a chain until that block is broken,
 * then drops.
 */
public class HeavyOrbBlock extends FallingBlock {
    public static final MapCodec<HeavyOrbBlock> CODEC = simpleCodec(HeavyOrbBlock::new);
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    private static final int LEAD_DUST_COLOR = 0x595959;

    public HeavyOrbBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HANGING, false));
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HANGING, canHang(context.getLevel(), context.getClickedPos()));
    }

    /** True when there is a sturdy block face directly above to hang the chain from. */
    private static boolean canHang(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HANGING)) {
            // Hangs until the block it is attached to is gone, then detaches and falls.
            if (!canHang(level, pos) && pos.getY() >= level.getMinBuildHeight()) {
                HeavyOrbEntity.fromBlock(level, pos, state.setValue(HANGING, false), null);
            }
        } else if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            HeavyOrbEntity.fromBlock(level, pos, state, null);
        }
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return LEAD_DUST_COLOR;
    }
}

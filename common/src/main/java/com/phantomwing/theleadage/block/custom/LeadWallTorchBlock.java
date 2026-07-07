package com.phantomwing.theleadage.block.custom;

import com.phantomwing.theleadage.block.entity.LeadTorchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Wall-mounted twin of {@link LeadTorchBlock} — same gray-white flame and lingering toxicity. */
public class LeadWallTorchBlock extends WallTorchBlock implements EntityBlock {
    public LeadWallTorchBlock(Properties properties) {
        super(ParticleTypes.SMOKE, properties); // the ctor particle is unused — animateTick is overridden
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Vanilla wall-torch flame position: nudged out of the wall toward the facing.
        Direction opposite = state.getValue(FACING).getOpposite();
        LeadTorchBlock.emitFlame(level,
                pos.getX() + 0.5 + 0.27 * opposite.getStepX(),
                pos.getY() + 0.7 + 0.22,
                pos.getZ() + 0.5 + 0.27 * opposite.getStepZ());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LeadTorchBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return LeadTorchBlock.exposureTicker(level, type);
    }
}

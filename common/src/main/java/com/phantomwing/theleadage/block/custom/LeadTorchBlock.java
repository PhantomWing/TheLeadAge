package com.phantomwing.theleadage.block.custom;

import com.phantomwing.theleadage.block.entity.LeadTorchBlockEntity;
import com.phantomwing.theleadage.block.entity.ModBlockEntities;
import com.phantomwing.theleadage.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A torch dipped in lead: the salts burn a light grayish-white instead of orange, and the open
 * flame slowly sickens anyone lingering beside it (see {@link LeadTorchBlockEntity}). Standing
 * variant; {@link LeadWallTorchBlock} is the wall-mounted twin.
 */
public class LeadTorchBlock extends TorchBlock implements EntityBlock {
    public LeadTorchBlock(Properties properties) {
        super(ParticleTypes.SMOKE, properties); // the ctor particle is unused — animateTick is overridden
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        emitFlame(level, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5);
    }

    /**
     * Vanilla torch ambience with the flame swapped for the gray-white lead flame, and the smoke for
     * the pale WHITE_SMOKE — burning lead gives off light-gray oxide fumes, not sooty dark smoke.
     */
    static void emitFlame(Level level, double x, double y, double z) {
        level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(ModParticles.LEAD_FLAME.get(), x, y, z, 0.0, 0.0, 0.0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LeadTorchBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return exposureTicker(level, type);
    }

    /** The server-side exposure ticker (shared with the wall variant). */
    @Nullable
    @SuppressWarnings("unchecked")
    static <T extends BlockEntity> BlockEntityTicker<T> exposureTicker(Level level, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.LEAD_TORCH.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<LeadTorchBlockEntity>) LeadTorchBlockEntity::serverTick;
    }
}

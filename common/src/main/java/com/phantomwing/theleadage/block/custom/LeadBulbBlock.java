package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/** Vanilla's copper bulb, toggled with sounds of its own ("Lead Bulb turns on") where vanilla's name copper. */
public class LeadBulbBlock extends CopperBulbBlock {
    public static final MapCodec<LeadBulbBlock> CODEC = simpleCodec(LeadBulbBlock::new);

    public LeadBulbBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends CopperBulbBlock> codec() {
        return CODEC;
    }

    /** Vanilla's toggle, with the mod's sounds. */
    @Override
    public void checkAndFlip(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) {
            return;
        }
        BlockState flipped = state;
        if (!state.getValue(POWERED)) {
            flipped = state.cycle(LIT);
            level.playSound(null, pos, flipped.getValue(LIT) ? ModSounds.LEAD_BULB_TURN_ON.get() : ModSounds.LEAD_BULB_TURN_OFF.get(),
                    SoundSource.BLOCKS);
        }
        level.setBlock(pos, flipped.setValue(POWERED, powered), Block.UPDATE_ALL);
    }
}

package com.phantomwing.theleadage.block.entity;

import com.phantomwing.theleadage.effect.LeadFumes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The lead torch's open lead-salt flame periodically coughs out a burst of toxic fumes — the same
 * smoke + sickly olive swirl as broken lead ore — at random intervals ({@value #MIN_INTERVAL}–
 * {@value #MAX_INTERVAL} ticks apart). Any player near the block when a burst fires takes a dose of
 * Lead Sickness (or climbs a stage), with the same hiss as the ore; the whole exposure just reuses
 * {@link LeadFumes#expose}. The enclosed lead lantern is the safe alternative.
 *
 * <p>The interval timer is transient (not saved): a torch simply re-arms it on chunk reload.</p>
 */
public class LeadTorchBlockEntity extends BlockEntity {
    private static final int MIN_INTERVAL = 60;    // 3s — shortest gap between fume bursts
    private static final int MAX_INTERVAL = 200;   // 10s — longest gap
    private static final double DOSE_CHANCE = 1.0; // guaranteed dose within LeadFumes' full range

    /** Ticks until the next fume burst; {@code -1} until the timer is first armed (transient). */
    private int ticksUntilBurst = -1;

    public LeadTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEAD_TORCH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LeadTorchBlockEntity torch) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (torch.ticksUntilBurst > 0) {
            torch.ticksUntilBurst--;
            return;
        }
        // Re-arm the timer with a fresh random gap. The first pass after load only arms it; every later
        // firing puffs out a burst of fumes and doses nearby players — like breaking lead ore, on a timer.
        boolean arming = torch.ticksUntilBurst < 0;
        torch.ticksUntilBurst = MIN_INTERVAL + serverLevel.getRandom().nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
        if (arming) {
            return;
        }
        emitFumes(serverLevel, pos);
        LeadFumes.expose(serverLevel, pos, DOSE_CHANCE);
    }

    /** A toxic wisp at the torch — the same pale lead-oxide smoke as lead ore's release, smaller. */
    private static void emitFumes(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.7, cz = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.WHITE_SMOKE, cx, cy, cz, 7, 0.12, 0.08, 0.12, 0.008);
        level.sendParticles(ParticleTypes.WHITE_SMOKE, cx, cy + 0.15, cz, 4, 0.22, 0.12, 0.22, 0.003);
    }
}

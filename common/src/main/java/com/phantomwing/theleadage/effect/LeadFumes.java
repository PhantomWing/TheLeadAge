package com.phantomwing.theleadage.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * "Lead fumes" dosing. When lead ore is mined the fumes settle for ~1s and then dose nearby living
 * entities (the miner included) with {@link ModMobEffects#LEAD_SICKNESS}. That delay is the avoidance
 * window — get at least {@value #RADIUS} blocks from the block (back off, or mine from range) and the
 * fumes miss you. Per entity the chance is the source's base chance scaled by distance: full within
 * {@value #FULL_RANGE} blocks, falling to 0 at {@value #RADIUS}. Undead don't breathe, so they're immune.
 *
 * <p>Uses the server's own {@link TickTask} scheduler, so no per-loader tick hook is needed.</p>
 */
public final class LeadFumes {
    private static final int DELAY_TICKS = 20;     // ~1s window to step back before the dose lands
    private static final double RADIUS = 3.0;      // dose range; the chance reaches 0 at this distance
    private static final double FULL_RANGE = 1.5;  // full chance within this distance (then it falls off)
    private static final int MAX_AMPLIFIER = 2;    // exposure stacks up to Lead Sickness III
    // Per-level durations (ticks), decreasing so each level sits atop the next and peels back to it as
    // it expires (vanilla's nested-effect resume). Even spacing -> ~10s per level; a full III clears in ~30s.
    private static final int[] LEVEL_DURATIONS = {600, 400, 200};

    private LeadFumes() {
    }

    /** Schedule a single dose check ~1s after the break, giving entities a beat to move away. */
    public static void schedule(ServerLevel level, BlockPos pos, double baseChance) {
        MinecraftServer server = level.getServer();
        server.tell(new TickTask(server.getTickCount() + DELAY_TICKS, () -> dose(level, pos, baseChance)));
    }

    private static void dose(ServerLevel level, BlockPos pos, double baseChance) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = AABB.ofSize(center, RADIUS * 2.0, RADIUS * 2.0, RADIUS * 2.0);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.isInvertedHealAndHarm()) {
                continue; // undead don't breathe the fumes
            }
            double chance = baseChance * falloff(entity.position().distanceTo(center));
            if (chance > 0.0 && level.getRandom().nextDouble() < chance) {
                // Lead builds up: a fresh dose stacks the level (capped). Rebuilding the whole nest each
                // time keeps the lower levels intact, so when exposure stops it wears off level by level.
                MobEffectInstance current = entity.getEffect(ModMobEffects.leadSicknessHolder());
                int topLevel = current == null ? 0 : Math.min(current.getAmplifier() + 1, MAX_AMPLIFIER);
                applyNest(entity, topLevel);
                // A toxic hiss the moment the fumes take hold in someone (at their position).
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 0.7f);
            }
        }
    }

    /**
     * (Re)apply the Lead Sickness nest up to {@code topLevel}: each level is added with its own
     * (shorter, higher) duration, so vanilla stores the lower levels beneath the active one and resumes
     * them as it expires — the effect wears off III → II → I → clear instead of vanishing all at once.
     */
    private static void applyNest(LivingEntity entity, int topLevel) {
        entity.removeEffect(ModMobEffects.leadSicknessHolder());
        for (int level = 0; level <= topLevel; level++) {
            entity.addEffect(new MobEffectInstance(ModMobEffects.leadSicknessHolder(),
                    LEVEL_DURATIONS[level], level, false, true, true));
        }
    }

    /** Strong up close: full within {@value #FULL_RANGE} blocks, then a linear drop to 0 at {@value #RADIUS}. */
    private static double falloff(double dist) {
        if (dist <= FULL_RANGE) {
            return 1.0;
        }
        if (dist >= RADIUS) {
            return 0.0;
        }
        return (RADIUS - dist) / (RADIUS - FULL_RANGE);
    }
}

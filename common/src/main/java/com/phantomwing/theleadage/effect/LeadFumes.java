package com.phantomwing.theleadage.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * "Lead fumes" exposure. Breaking lead ore (and the lead torch's periodic bursts) immediately exposes
 * nearby <b>players</b>, with a distance-based chance (full within {@value #FULL_RANGE} blocks, falling
 * to 0 at {@value #RADIUS}) and only if nothing solid stands between them. Only players breathe the
 * fumes; other mobs ignore them.
 *
 * <p>Built entirely from vanilla effects — no custom effect, overlay or mixin, so it reads like a
 * pufferfish. The sickness escalates in stages that <em>stack</em>: <b>Hunger → +Weakness →
 * +Poison +Nausea</b>. Each exposure climbs one stage (capped at the third) and refreshes the whole
 * stack, so at the worst stage you carry all of them at once. Stop being exposed and they expire —
 * the shorter ones first — dropping you back down; milk clears it.</p>
 */
public final class LeadFumes {
    private static final double RADIUS = 3.0;       // exposure range; the chance reaches 0 at this distance
    private static final double FULL_RANGE = 1.5;   // full chance within this distance (then it falls off)
    private static final int HUNGER_TICKS = 280;    // 14s — stage 1
    private static final int WEAKNESS_TICKS = 240;  // 12s — stage 2
    private static final int POISON_TICKS = 160;    // 8s — stage 3 (Poison won't kill on its own)
    private static final int NAUSEA_TICKS = 200;    // 10s — stage 3

    private LeadFumes() {
    }

    /** Immediately expose nearby players to the fumes (distance-based chance per player). */
    public static void expose(ServerLevel level, BlockPos pos, double baseChance) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = AABB.ofSize(center, RADIUS * 2.0, RADIUS * 2.0, RADIUS * 2.0);
        for (Player player : level.getEntitiesOfClass(Player.class, box)) {
            double chance = baseChance * falloff(player.position().distanceTo(center));
            if (chance > 0.0 && level.getRandom().nextDouble() < chance && hasClearPath(level, pos, center, player)) {
                escalate(player);
                // A toxic hiss the moment the fumes take hold (at the affected player).
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 0.7f);
            }
        }
    }

    /**
     * Whether the fumes can actually reach the player — a solid wall between the source and the
     * player's head blocks them. Cast from the player's eyes toward the source (the player end sits
     * in open air, unlike the source, which may be inside a full-cube ore block); a ray that only
     * strikes the source block's own position counts as clear.
     */
    private static boolean hasClearPath(ServerLevel level, BlockPos pos, Vec3 center, Player player) {
        BlockHitResult hit = level.clip(new ClipContext(player.getEyePosition(), center,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }

    /**
     * One dose: climb one stage, then (re)apply every effect up to it, so the sickness STACKS rather
     * than swaps. Shared by the ore fumes ({@link #expose}) and the lead torch, so both walk the same
     * <b>Hunger → +Weakness → +Poison +Nausea</b> ladder on repeated exposure.
     */
    public static void escalate(LivingEntity entity) {
        applyStack(entity, Math.min(currentStage(entity) + 1, 3));
    }

    /** The stage an entity is already in, read from the highest lead effect present (0 = none). */
    private static int currentStage(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.POISON)) {
            return 3;
        }
        if (entity.hasEffect(MobEffects.WEAKNESS)) {
            return 2;
        }
        if (entity.hasEffect(MobEffects.HUNGER)) {
            return 1;
        }
        return 0;
    }

    /** Apply and refresh every effect up to {@code stage}: Hunger, then +Weakness, then +Poison +Nausea. */
    private static void applyStack(LivingEntity entity, int stage) {
        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, HUNGER_TICKS, 0));
        if (stage >= 2) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS, 0));
        }
        if (stage >= 3) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS, 0)); // Nausea
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

package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * "Lead fumes" exposure. Breaking lead ore or a block of raw lead (and the lead torch's periodic bursts) immediately exposes
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

    private static final double GEAR_DOSE_CHANCE = 1.0; // crumbling gear is point-blank: always a dose

    /** Chance mining raw lead is a BIG release (hiss + guaranteed dose) rather than a small wisp. */
    private static final float MINED_PLUME_CHANCE = 0.30f;
    private static final double MINED_BIG_DOSE_CHANCE = 1.0;    // big release: guaranteed dose at point-blank
    private static final double MINED_SMALL_DOSE_CHANCE = 0.25; // small wisp: low chance even up close

    /** How long a player stays in lead fumes - a campfire's stream - or contaminated water per stage. */
    public static final int FUMES_STAGE_TICKS = 60;
    /** How long a player stands on raw lead per stage, the first included: the same pace as the fumes, after a moment. */
    public static final int CONTACT_STAGE_TICKS = 60;
    /** When each player last took a dose from lead they stay in: one timer for every source, so two at once climb no faster. */
    private static final Map<Player, Long> LAST_TOUCH = new WeakHashMap<>();
    /**
     * How long a player's stand on raw lead can go unfelt before it starts over: a player standing still only tells
     * the server where they are once a second.
     */
    private static final int STAND_LAPSE = 40;
    /** Each player's stand on raw lead: when it last dosed them (or began), and when it was last felt. */
    private static final Map<Player, long[]> STANDING = new WeakHashMap<>();

    private LeadFumes() {
    }

    /** Immediately expose nearby players to fumes released by a block (distance-based chance per player). */
    public static void expose(ServerLevel level, BlockPos pos, double baseChance) {
        expose(level, Vec3.atCenterOf(pos), pos, baseChance);
    }

    /** As {@link #expose(ServerLevel, BlockPos, double)}, for fumes released in open air rather than at a block. */
    public static void expose(ServerLevel level, Vec3 center, double baseChance) {
        expose(level, center, null, baseChance);
    }

    private static void expose(ServerLevel level, Vec3 center, @Nullable BlockPos sourceBlock, double baseChance) {
        AABB box = AABB.ofSize(center, RADIUS * 2.0, RADIUS * 2.0, RADIUS * 2.0);
        for (Player player : level.getEntitiesOfClass(Player.class, box)) {
            double chance = baseChance * falloff(player.position().distanceTo(center));
            if (chance > 0.0 && level.getRandom().nextDouble() < chance
                    && hasClearPath(level, sourceBlock, center, player)) {
                escalate(player);
                // A toxic hiss the moment the fumes take hold (at the affected player).
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 0.7f);
            }
        }
    }

    /**
     * A player in lead they can stay in - a campfire's stream, contaminated water: a dose on contact, and another
     * every {@code stageTicks} while they stay, so it worsens the longer they do. Whether it dosed them.
     */
    public static boolean touch(Player player, int stageTicks) {
        if (!isDue(player, stageTicks)) {
            return false;
        }
        LAST_TOUCH.put(player, player.level().getGameTime());
        escalate(player);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 0.7f);
        return true;
    }

    /** Whether {@link #touch} would dose the player now, rather than let them wait out the stage of their last dose. */
    public static boolean isDue(Player player, int stageTicks) {
        long now = player.level().getGameTime();
        Long last = LAST_TOUCH.get(player);
        return last == null || now < last || now - last >= stageTicks;
    }

    /**
     * A player standing on a block of raw lead: nothing at first, a dose once they have stood there
     * {@code stageTicks}, and another every {@code stageTicks} after. Stepping off for a couple of seconds starts it
     * over.
     */
    public static void stand(Player player, int stageTicks) {
        long now = player.level().getGameTime();
        long[] stand = STANDING.get(player);
        if (stand == null || now < stand[1] || now - stand[1] > STAND_LAPSE) {
            STANDING.put(player, new long[] {now, now});
            return;
        }
        stand[1] = now;
        if (now - stand[0] >= stageTicks && touch(player, stageTicks)) {
            stand[0] = now;
        }
    }

    /**
     * Raw lead has been mined at {@code pos}, lead ore or a block of raw lead: every harvest fumes, and a roll
     * decides a big release or a faint wisp. The fumes expose nearby players at once, so keeping clear of the
     * block dodges them.
     */
    public static void mined(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5, y = pos.getY() + 0.6, z = pos.getZ() + 0.5;
        if (level.getRandom().nextFloat() < MINED_PLUME_CHANCE) {
            // A pale lead-oxide plume. The hiss plays when the fumes take hold, not here.
            plume(level, x, y, z);
            expose(level, pos, MINED_BIG_DOSE_CHANCE);
        } else {
            wisp(level, x, y - 0.05, z);
            expose(level, pos, MINED_SMALL_DOSE_CHANCE);
        }
    }

    /**
     * A piece of lead gear has crumbled: lead dust and fumes at the wearer, dosing them point-blank and
     * anyone standing close. Applies to every {@link LivingEntity}, so a mob's lead armour failing gasses
     * the players around it too.
     */
    public static void equipmentBroken(LivingEntity wearer, Item item) {
        if (!(wearer.level() instanceof ServerLevel level)
                || !item.builtInRegistryHolder().is(ModTags.Items.LEAD_EQUIPMENT)) {
            return;
        }
        Vec3 at = wearer.getEyePosition().subtract(0.0, 0.3, 0.0); // chest height — where the gear sat
        plume(level, at.x, at.y, at.z);
        expose(level, at, GEAR_DOSE_CHANCE);
    }

    /**
     * The big release: a dense core, a halo billowing out over the {@value #FULL_RANGE} blocks it doses everyone in,
     * and thin wisps out to where its reach runs out, so the cloud shows how far it is dangerous.
     */
    public static void plume(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ModParticles.LEAD_FUMES.get(), x, y, z, 16, 0.3, 0.12, 0.3, 0.02);
        level.sendParticles(ModParticles.LEAD_FUMES.get(), x, y + 0.15, z, 18, 0.8, 0.25, 0.8, 0.015);
        level.sendParticles(ModParticles.LEAD_FUMES.get(), x, y + 0.25, z, 12, 1.3, 0.35, 1.3, 0.008);
    }

    /** The small release: a faint wisp, spreading a little way. */
    public static void wisp(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ModParticles.LEAD_FUMES.get(), x, y, z, 7, 0.45, 0.12, 0.45, 0.01);
    }

    /**
     * Whether the fumes can actually reach the player — a solid wall between the source and the
     * player's head blocks them. Cast from the player's eyes toward the source (the player end sits
     * in open air, unlike the source, which may be inside a full-cube ore block); a ray that only
     * strikes the source block's own position counts as clear. {@code sourceBlock} is null when the
     * fumes come from open air, where there is no block to exempt.
     */
    private static boolean hasClearPath(ServerLevel level, @Nullable BlockPos sourceBlock, Vec3 center, Player player) {
        BlockHitResult hit = level.clip(new ClipContext(player.getEyePosition(), center,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS
                || (sourceBlock != null && hit.getBlockPos().equals(sourceBlock));
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

package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.sound.ModSounds;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.EntityTypeTags;
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
 * nearby players and mobs, with a distance-based chance (full within {@value #FULL_RANGE} blocks, falling
 * to 0 at {@value #RADIUS}) and only if nothing solid stands between them. The undead, and what takes no effects, are
 * spared ({@link #canSicken}).
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

    /** How long a player or mob stays in lead fumes - a campfire's stream - or contaminated water per stage. */
    public static final int FUMES_STAGE_TICKS = 60;
    /** How long a player or mob stands on raw lead per stage, the first included: the same pace as the fumes, after a moment. */
    public static final int CONTACT_STAGE_TICKS = 60;
    /** When each player or mob last took a dose from lead they stay in: one timer for every source, so two at once climb no faster. */
    private static final Map<LivingEntity, Long> LAST_TOUCH = new WeakHashMap<>();
    /**
     * How long a player's stand on raw lead, or stay in contaminated water, can go unfelt before it starts over: a player
     * standing still only tells the server where they are once a second.
     */
    private static final int STAND_LAPSE = 40;
    /** Each player's or mob's stand on raw lead: when it last dosed them (or began), and when it was last felt. */
    private static final Map<LivingEntity, long[]> STANDING = new WeakHashMap<>();
    /** Each player's or mob's stay in contaminated water, as {@link #STANDING}. */
    private static final Map<LivingEntity, long[]> SOAKING = new WeakHashMap<>();
    /** Each player's or mob's last dose of lead: when, and the stage it brought them to. */
    private static final Map<LivingEntity, long[]> DOSED = new WeakHashMap<>();

    private LeadFumes() {
    }

    /**
     * Whether lead can make {@code entity} sick: players and living mobs - fish in contaminated water, a cow by a lead
     * torch - but not the undead, which no poison touches, nor what takes no effects at all, like an armour stand. Every
     * source of Lead Sickness asks this, so none of them leaves the mobs out.
     */
    public static boolean canSicken(LivingEntity entity) {
        return entity.isAlive() && !entity.isSpectator() && entity.isAffectedByPotions()
                && !entity.getType().is(EntityTypeTags.UNDEAD);
    }

    /** Immediately expose nearby players and mobs to fumes released by a block (distance-based chance for each). */
    public static void expose(ServerLevel level, BlockPos pos, double baseChance) {
        expose(level, Vec3.atCenterOf(pos), pos, baseChance);
    }

    /** As {@link #expose(ServerLevel, BlockPos, double)}, for fumes released in open air rather than at a block. */
    public static void expose(ServerLevel level, Vec3 center, double baseChance) {
        expose(level, center, null, baseChance);
    }

    private static void expose(ServerLevel level, Vec3 center, @Nullable BlockPos sourceBlock, double baseChance) {
        AABB box = AABB.ofSize(center, RADIUS * 2.0, RADIUS * 2.0, RADIUS * 2.0);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LeadFumes::canSicken)) {
            double chance = baseChance * falloff(entity.position().distanceTo(center));
            if (chance > 0.0 && level.getRandom().nextDouble() < chance
                    && hasClearPath(level, sourceBlock, center, entity) && escalate(entity)) {
                hiss(entity);
            }
        }
    }

    /**
     * A player or mob in lead they can stay in - a campfire's stream, contaminated water: a dose on contact, and another
     * every {@code stageTicks} while they stay, so it worsens the longer they do. Whether it dosed them.
     */
    public static boolean touch(LivingEntity entity, int stageTicks) {
        if (!isDue(entity, stageTicks)) {
            return false;
        }
        LAST_TOUCH.put(entity, entity.level().getGameTime());
        if (escalate(entity)) {
            hiss(entity);
        }
        return true;
    }

    /** Whether {@link #touch} would dose the player or mob now, rather than let them wait out the stage of their last dose. */
    public static boolean isDue(LivingEntity entity, int stageTicks) {
        long now = entity.level().getGameTime();
        Long last = LAST_TOUCH.get(entity);
        return last == null || now < last || now - last >= stageTicks;
    }

    /**
     * A player or mob standing on a block of raw lead: nothing at first, a dose once they have stood there
     * {@code stageTicks}, and another every {@code stageTicks} after. Stepping off for a couple of seconds starts it
     * over.
     */
    public static void stand(LivingEntity entity, int stageTicks) {
        stay(STANDING, entity, stageTicks);
    }

    /**
     * A player or mob in contaminated water, as {@link #stand} on raw lead: nothing at first, a dose once they have been in
     * it {@code stageTicks}, and another every {@code stageTicks} after. Out of it for a couple of seconds starts it over.
     */
    public static void soak(LivingEntity entity, int stageTicks) {
        stay(SOAKING, entity, stageTicks);
    }

    /**
     * That a player or mob in contaminated water is still in water there, between the doses: the water is only looked
     * through for them when a dose is due, which would otherwise leave their stay unfelt and start it over.
     */
    public static void stillSoaking(LivingEntity entity) {
        long now = entity.level().getGameTime();
        long[] soak = SOAKING.get(entity);
        if (soak != null && now >= soak[1] && now - soak[1] <= STAND_LAPSE) {
            soak[1] = now;
        }
    }

    private static void stay(Map<LivingEntity, long[]> stays, LivingEntity entity, int stageTicks) {
        long now = entity.level().getGameTime();
        long[] stay = stays.get(entity);
        if (stay == null || now < stay[1] || now - stay[1] > STAND_LAPSE) {
            stays.put(entity, new long[] {now, now});
            return;
        }
        stay[1] = now;
        if (now - stay[0] >= stageTicks && touch(entity, stageTicks)) {
            stay[0] = now;
        }
    }

    /**
     * The toxic hiss as lead takes hold, and each time it gets worse, at whoever it took hold of: not for a dose that only
     * keeps the worst stage going, which every few seconds in contaminated water was all one heard. Players only, or a
     * pond of fish dosed each on its own clock would hiss without pause; a mob shows its effects' swirls instead.
     */
    private static void hiss(LivingEntity entity) {
        if (entity instanceof Player) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.LEAD_FUMES_HISS.get(), SoundSource.PLAYERS, 0.6f, 0.7f);
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
     * Whether the fumes can actually reach the player or mob — a solid wall between the source and
     * their head blocks them. Cast from their eyes toward the source (that end sits in open air,
     * unlike the source, which may be inside a full-cube ore block); a ray that only strikes the
     * source block's own position counts as clear. {@code sourceBlock} is null when the fumes come
     * from open air, where there is no block to exempt.
     */
    private static boolean hasClearPath(ServerLevel level, @Nullable BlockPos sourceBlock, Vec3 center, LivingEntity entity) {
        BlockHitResult hit = level.clip(new ClipContext(entity.getEyePosition(), center,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        return hit.getType() == HitResult.Type.MISS
                || (sourceBlock != null && hit.getBlockPos().equals(sourceBlock));
    }

    /**
     * One dose: climb one stage, then (re)apply every effect up to it, so the sickness STACKS rather
     * than swaps. Shared by the ore fumes ({@link #expose}) and the lead torch, so both walk the same
     * <b>Hunger → +Weakness → +Poison +Nausea</b> ladder on repeated exposure. Whether it made them worse: false at the
     * last stage, which a dose only keeps going.
     */
    public static boolean escalate(LivingEntity entity) {
        int current = currentStage(entity);
        int stage = Math.min(current + 1, 3);
        applyStack(entity, stage);
        DOSED.put(entity, new long[] {entity.level().getGameTime(), stage});
        return stage > current;
    }

    /**
     * The stage an entity is already in (0 = none): the highest the lead brought it to whose effect it still has from
     * the lead. Weakness from a potion, Poison from a cave spider or Hunger from rotten flesh are no stage of it, and
     * milk, which clears the effects, clears the stage with them.
     */
    private static int currentStage(LivingEntity entity) {
        long[] dose = DOSED.get(entity);
        if (dose == null) {
            return 0;
        }
        long since = entity.level().getGameTime() - dose[0];
        int stage = (int) dose[1];
        if (since < 0) {
            return 0;
        }
        if (stage >= 3 && since < POISON_TICKS && entity.hasEffect(MobEffects.POISON)) {
            return 3;
        }
        if (stage >= 2 && since < WEAKNESS_TICKS && entity.hasEffect(MobEffects.WEAKNESS)) {
            return 2;
        }
        return since < HUNGER_TICKS && entity.hasEffect(MobEffects.HUNGER) ? 1 : 0;
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

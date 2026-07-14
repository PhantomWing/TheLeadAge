package com.phantomwing.theleadage.entity;

import com.phantomwing.theleadage.entity.ai.AvoidRepellentBlockGoal;
import com.phantomwing.theleadage.platform.CommonConfig;
import com.phantomwing.theleadage.tags.ModTags;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Pillager;

/**
 * Burning lead drives mobs off — the mod's answer to soul torches repelling piglins.
 *
 * <p>Vanilla's version is tag-driven ({@code #minecraft:piglin_repellents}), but only because piglins
 * are <em>brain</em>-based: the tag feeds {@code MemoryModuleType.NEAREST_REPELLENT}, which the piglin
 * brain turns into a {@code SetWalkTargetAwayFrom} behaviour. Creepers and pillagers are <em>goal</em>-based
 * and have no equivalent, so the avoidance is injected as a goal on each one that joins a level.
 * (Villagers are brain-based too, which is why they are <em>not</em> here — they would need the memory
 * added to their private {@code MEMORY_TYPES}, a sensor to populate it, and a brain behaviour.)</p>
 *
 * <p>Each mob gets its own tag and its own config toggle, so they can be tuned independently.</p>
 */
public final class LeadFumeRepellent {
    /**
     * Creepers: the same slot as their cat/ocelot avoidance (3). Their goals are 1 {@code FloatGoal},
     * 2 {@code SwellGoal}, 3 the cat/ocelot avoidance, 4 {@code MeleeAttackGoal}. At 3 a torch makes a
     * creeper back off instead of closing in, but it does <em>not</em> outrank {@code SwellGoal} — walk
     * up to a fleeing creeper and it can still take you with it. A torch is a deterrent, not a shield.
     */
    private static final int CREEPER_PRIORITY = 3;

    /**
     * Pillagers: slot 7. Their goals run 0 {@code FloatGoal}, 1 obtain-banner, 2 hold-ground,
     * 3 {@code RangedCrossbowAttackGoal} + {@code PathfindToRaidGoal}, 4 move-through-village +
     * long-distance patrol, 5 raid celebration, 8 {@code RandomStrollGoal}.
     *
     * <p>7 is the <b>least invasive free slot</b>: it beats only the idle stroll, so torches turn away
     * <em>wandering</em> pillagers while leaving raids, patrols and combat exactly as vanilla — a
     * pillager already shooting at you (goal 3) ignores the torch entirely. Slot 6 is deliberately left
     * empty for other mods to use without colliding with us.</p>
     */
    private static final int PILLAGER_PRIORITY = 7;

    /** Matches the speeds vanilla mobs use to flee cats and ocelots. */
    private static final double WALK_SPEED = 1.0;
    private static final double SPRINT_SPEED = 1.2;

    private LeadFumeRepellent() {
    }

    public static void register() {
        EntityEvent.ADD.register((entity, level) -> {
            if (level.isClientSide()) {
                return EventResult.pass();
            }
            if (entity instanceof Creeper creeper) {
                creeper.goalSelector.addGoal(CREEPER_PRIORITY, new AvoidRepellentBlockGoal(
                        creeper, ModTags.Blocks.CREEPER_REPELLENTS,
                        CommonConfig::creepersAvoidLeadFumes, WALK_SPEED, SPRINT_SPEED));
            } else if (entity instanceof Pillager pillager) {
                pillager.goalSelector.addGoal(PILLAGER_PRIORITY, new AvoidRepellentBlockGoal(
                        pillager, ModTags.Blocks.PILLAGER_REPELLENTS,
                        CommonConfig::pillagersAvoidLeadFumes, WALK_SPEED, SPRINT_SPEED));
            }
            return EventResult.pass();
        });
    }
}

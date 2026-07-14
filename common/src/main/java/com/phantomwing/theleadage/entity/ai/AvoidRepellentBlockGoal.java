package com.phantomwing.theleadage.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;

/**
 * Flee from any nearby block in a tag — a block-based counterpart to vanilla's entity-based
 * {@code AvoidEntityGoal}, used to make creepers keep away from lead torches.
 *
 * <p><b>Why a goal and not a tag.</b> Vanilla's soul-torch-repels-piglins works via
 * {@code #minecraft:piglin_repellents}, but that tag is only ever read by the <em>piglin brain</em>
 * ({@code MemoryModuleType.NEAREST_REPELLENT} → {@code SetWalkTargetAwayFrom}). Creepers are
 * goal-based and have no such machinery, so the behaviour has to be written out rather than tagged in.</p>
 *
 * <p><b>Two things are throttled separately, and mixing them up makes the mob dither.</b>
 * {@link DefaultRandomPos#getPosAway} is <em>stochastic</em>: it samples a random spot in a cone away
 * from the threat and returns null whenever the sample overshoots the radius or isn't pathable, so it
 * fails a good share of the time. Vanilla's {@code AvoidEntityGoal} hides that by retrying it on every
 * goal tick — and this goal does the same. Only the <em>block search</em> is throttled
 * ({@link #SCAN_INTERVAL}), because that is the part that actually costs anything. Throttling the flee
 * attempt as well made creepers linger next to a torch for many seconds, occasionally getting a lucky
 * roll — which is precisely the bug this structure avoids.</p>
 *
 * <p>Note that {@code canUse()} is only polled every <em>other</em> tick: {@code Mob.serverAiStep}
 * alternates {@code goalSelector.tick()} with {@code tickRunningGoals()}. So intervals here are in
 * goal ticks, and are worth roughly double in game ticks.</p>
 */
public class AvoidRepellentBlockGoal extends Goal {
    /** Search box around the mob, matching vanilla's piglin repellent search (8 wide, 4 tall). */
    private static final int SEARCH_HORIZONTAL = 8;
    private static final int SEARCH_VERTICAL = 4;
    /**
     * Goal ticks between full block searches, when no repellent is already known. Cheap: vanilla's
     * piglins run this very same search <em>every</em> tick, so this is an order of magnitude lighter.
     */
    private static final int SCAN_INTERVAL = 5;
    /** How far away to look for somewhere to run to (vanilla AvoidEntityGoal uses the same 16/7). */
    private static final int FLEE_HORIZONTAL = 16;
    private static final int FLEE_VERTICAL = 7;
    /** Within this distance (squared) of the repellent, break into a run — as AvoidEntityGoal does. */
    private static final double SPRINT_WITHIN_SQR = 49.0;

    private final PathfinderMob mob;
    private final TagKey<Block> repellents;
    /** The config gate for this mob's avoidance, read live so toggling needs no restart. */
    private final BooleanSupplier enabled;
    private final double walkSpeed;
    private final double sprintSpeed;

    /** The repellent we are running from. Cached between attempts so a re-try costs no search. */
    @Nullable
    private BlockPos repellent;
    @Nullable
    private Path fleePath;
    private int scanCooldown;

    public AvoidRepellentBlockGoal(PathfinderMob mob, TagKey<Block> repellents, BooleanSupplier enabled,
                                   double walkSpeed, double sprintSpeed) {
        this.mob = mob;
        this.repellents = repellents;
        this.enabled = enabled;
        this.walkSpeed = walkSpeed;
        this.sprintSpeed = sprintSpeed;
        // Stagger the first search so a crowd of mobs doesn't all scan on the same tick.
        this.scanCooldown = mob.getRandom().nextInt(SCAN_INTERVAL);
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!enabled.getAsBoolean()) {
            return false; // checked before the search, so the feature costs nothing when off
        }
        if (!repellentNearby()) {
            return false;
        }

        // Retried on EVERY goal tick (see the class doc): getPosAway fails often by design, so a miss
        // must not cost a whole cooldown — otherwise the mob just stands there.
        Vec3 threat = Vec3.atBottomCenterOf(repellent);
        Vec3 escape = DefaultRandomPos.getPosAway(mob, FLEE_HORIZONTAL, FLEE_VERTICAL, threat);
        if (escape == null) {
            return false;
        }
        // Don't "flee" to somewhere no further from the torch than we already are (vanilla does this too).
        if (threat.distanceToSqr(escape) < threat.distanceToSqr(mob.position())) {
            return false;
        }
        fleePath = mob.getNavigation().createPath(escape.x, escape.y, escape.z, 0);
        return fleePath != null;
    }

    /** Is a repellent in range? Re-validates the cached one cheaply; the full search behind it is throttled. */
    private boolean repellentNearby() {
        if (repellent != null && stillRepels(repellent)) {
            return true;
        }
        repellent = null;
        if (scanCooldown-- > 0) {
            return false;
        }
        scanCooldown = SCAN_INTERVAL;
        repellent = BlockPos.findClosestMatch(mob.blockPosition(), SEARCH_HORIZONTAL, SEARCH_VERTICAL,
                pos -> mob.level().getBlockState(pos).is(repellents)).orElse(null);
        return repellent != null;
    }

    /** Cheap: the cached block is still in range and still tagged (it may have been broken or replaced). */
    private boolean stillRepels(BlockPos pos) {
        return pos.closerToCenterThan(mob.position(), SEARCH_HORIZONTAL)
                && mob.level().getBlockState(pos).is(repellents);
    }

    @Override
    public boolean canContinueToUse() {
        // Also re-checked here, so switching the config off stops a mob that is already fleeing.
        return enabled.getAsBoolean() && !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(fleePath, walkSpeed);
    }

    @Override
    public void stop() {
        // Deliberately keep `repellent`: if the flee was short or blocked and we're still beside the
        // torch, the next tick re-tries immediately instead of waiting on another search.
        fleePath = null;
    }

    @Override
    public void tick() {
        if (repellent == null) {
            return;
        }
        // Panic (run) while still close to it, then settle to a walk once clear.
        boolean close = mob.distanceToSqr(Vec3.atCenterOf(repellent)) < SPRINT_WITHIN_SQR;
        mob.getNavigation().setSpeedModifier(close ? sprintSpeed : walkSpeed);
    }
}

package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToDoubleFunction;

/**
 * Mobs keep out of water raw lead poisons, as they keep out of fire: fish, dolphins, turtles, axolotls and frogs, land
 * animals and villagers, squid. It leans on vanilla's own choices, through each loader's mixins: where a mob picks to go
 * next ({@code RandomPosMixin}: a place in poisoned water scores worst, so a clean one always wins, which is also how a
 * fish the poison spread over gets out), what a path through a block costs ({@code SwimNodeEvaluatorMixin},
 * {@code WalkNodeEvaluatorMixin}: poisoned water costs as much as fire, so a path goes round it where it can and through
 * it where it must), and the way a squid, which plans no paths, swims ({@code SquidRandomMovementGoalMixin}). The undead,
 * which it can't hurt, take no notice. Only on the server's own thread: a mod that pathfinds on another gets clean water
 * there, rather than a race on the level.
 */
public final class PoisonedWaterAvoidance {
    /** What a block of poisoned water costs a path, on top of what the block costs already: as much as one of fire. */
    public static final float PATH_COST = 16.0F;
    /** How much worse a place in poisoned water scores as somewhere to go: more than any mob likes a place. */
    private static final double PLACE_PENALTY = 1000.0;
    /** How fast a squid swims out of poisoned water: as fast as it swims anywhere. */
    private static final float SQUID_SPEED = 0.2F;
    /** How far ahead a squid looks for poisoned water before it sets off: a couple of seconds' swim. */
    private static final double SQUID_LOOKAHEAD = 3.0;
    /** How often a squid in poisoned water turns for the way out, in ticks. */
    private static final int SQUID_CHECK_EVERY = 10;

    private PoisonedWaterAvoidance() {
    }

    /** Whether {@code mob} keeps out of poisoned water: one lead can make sick, on the server's thread. */
    public static boolean avoids(Mob mob) {
        return LeadWater.range(false) > 0 && CommonConfig.mobsAvoidLeadWater()
                && mob.level() instanceof ServerLevel level && level.getServer().isSameThread() && LeadFumes.canSicken(mob);
    }

    /** {@code scorer}, the mob's own liking for a place to go, with poisoned water scoring worst where it {@link #avoids}. */
    public static ToDoubleFunction<BlockPos> scorer(PathfinderMob mob, ToDoubleFunction<BlockPos> scorer) {
        if (!avoids(mob)) {
            return scorer;
        }
        return pos -> scorer.applyAsDouble(pos) - (poisoner((ServerLevel) mob.level(), pos) != null ? PLACE_PENALTY : 0.0);
    }

    /**
     * A path's block, costing {@link #PATH_COST} more than its kind does where it is poisoned water and {@code mob}
     * {@link #avoids} it. Asked again of a block a path finds twice, so it is set rather than added to.
     */
    @Nullable
    public static Node costed(@Nullable Node node, Mob mob) {
        // Water against a wall is WATER_BORDER to an amphibian, as dry land beside water is to a walker: both are asked.
        if (node == null || node.type != PathType.WATER && node.type != PathType.WATER_BORDER && node.type != PathType.BREACH) {
            return node;
        }
        float cost = Math.max(0.0F, mob.getPathfindingMalus(node.type)) + PATH_COST;
        if (node.costMalus < cost && avoids(mob) && poisoner((ServerLevel) mob.level(), new BlockPos(node.x, node.y, node.z)) != null) {
            node.costMalus = cost;
        }
        return node;
    }

    /**
     * A squid setting off at {@code x, y, z} a tick: turned away from the raw lead where that would take it into the water
     * the lead poisons.
     */
    public static Vec3 steer(Squid squid, float x, float y, float z) {
        Vec3 way = new Vec3(x, y, z);
        if (!avoids(squid) || x == 0.0F && z == 0.0F) {
            return way;
        }
        Vec3 flat = new Vec3(x, 0.0, z).normalize().scale(SQUID_LOOKAHEAD);
        BlockPos ahead = BlockPos.containing(squid.position().add(flat));
        BlockPos lead = poisoner((ServerLevel) squid.level(), ahead);
        return lead == null ? way : away(squid, lead, y);
    }

    /** A squid in poisoned water turns for the way out, now and then, as its own swimming would take it back in. */
    public static void swimOut(Squid squid) {
        if (squid.tickCount % SQUID_CHECK_EVERY != 0 || !avoids(squid)) {
            return;
        }
        BlockPos lead = poisoner((ServerLevel) squid.level(), squid.blockPosition());
        if (lead != null) {
            Vec3 out = away(squid, lead, 0.0F);
            squid.setMovementVector((float) out.x, (float) out.y, (float) out.z);
        }
    }

    /** Straight away from the raw lead at {@code lead}, level but for {@code rise}, at a squid's speed. */
    private static Vec3 away(Squid squid, BlockPos lead, float rise) {
        Vec3 from = squid.position().subtract(Vec3.atCenterOf(lead));
        Vec3 flat = new Vec3(from.x, 0.0, from.z);
        if (flat.lengthSqr() < 1.0E-4) {
            flat = new Vec3(1.0, 0.0, 0.0);
        }
        flat = flat.normalize().scale(SQUID_SPEED);
        return new Vec3(flat.x, rise, flat.z);
    }

    @Nullable
    private static BlockPos poisoner(ServerLevel level, BlockPos pos) {
        return PoisonedWater.poisoner(level, pos);
    }
}

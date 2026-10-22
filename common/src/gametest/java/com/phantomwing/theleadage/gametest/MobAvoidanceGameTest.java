package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.effect.PoisonedWaterAvoidance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.phantomwing.theleadage.gametest.TestCompat.AXOLOTL;
import static com.phantomwing.theleadage.gametest.TestCompat.COD;
import static com.phantomwing.theleadage.gametest.TestCompat.DROWNED;
import static com.phantomwing.theleadage.gametest.TestCompat.SQUID;
import static com.phantomwing.theleadage.gametest.TestCompat.fail;

/**
 * Game tests for mobs keeping out of the water raw lead poisons ({@link PoisonedWaterAvoidance}): where a mob picks to go,
 * the routes a fish and an axolotl take, which way a squid sets off, and the undead taking no notice. Each builds a
 * sealed stone tank of still water, one block of it poisoned by raw lead under it just far enough into its spreading,
 * and asks vanilla's own choices at once, so each shows its mixin applies. The tanks keep to x and z 0 to 5: the next
 * test stands six blocks along, and a squid looking ahead into its poisoned tank turned for it.
 * Listed in {@link GameTests}.
 */
public final class MobAvoidanceGameTest {
    private MobAvoidanceGameTest() {
    }

    /** Given a place in poisoned water and one in clean water, a fish picks the clean one; a drowned takes no notice. */
    public static void poisonedWaterScoresWorstAsAPlaceToGo(GameTestHelper helper) {
        BlockPos poisoned = new BlockPos(2, 2, 1);
        BlockPos clean = new BlockPos(4, 2, 1);
        tank(helper, row(1, 4, 1));
        String setup = poisonOnly(helper, poisoned);
        if (setup != null) {
            fail(helper, setup);
            return;
        }
        Cod cod = helper.spawn(COD, new BlockPos(1, 2, 1));
        Drowned drowned = helper.spawn(DROWNED, new BlockPos(1, 4, 1));
        BlockPos[] places = {helper.absolutePos(poisoned), helper.absolutePos(clean)};
        int[] asked = {0};
        Vec3 picked = RandomPos.generateRandomPos(cod, () -> places[asked[0]++ % places.length]);
        String problem = !PoisonedWaterAvoidance.avoids(cod) ? "a cod doesn't keep out of it"
                : PoisonedWaterAvoidance.avoids(drowned) ? "a drowned keeps out of it, though it can't be hurt"
                : !Vec3.atBottomCenterOf(places[1]).equals(picked) ? "a cod picked it over clean water to swim to"
                : null;
        cod.discard();
        drowned.discard();
        succeedUnless(helper, problem);
    }

    /** A fish's route goes round a block of poisoned water rather than through it. */
    public static void fishSwimAroundPoisonedWater(GameTestHelper helper) {
        swimsAround(helper, COD, "a cod");
    }

    /**
     * An axolotl's route - an amphibian's, planned as a walker's is, where water against a wall is a kind of its own -
     * goes round poisoned water rather than through it.
     */
    public static void axolotlsSwimAroundPoisonedWater(GameTestHelper helper) {
        swimsAround(helper, AXOLOTL, "an axolotl");
    }

    /** A squid about to swim into poisoned water turns away from the lead; before the lead leaks, it swims on. */
    public static void squidTurnAwayFromPoisonedWater(GameTestHelper helper) {
        BlockPos poisoned = new BlockPos(4, 2, 1);
        tank(helper, row(1, 4, 1));
        helper.setBlock(poisoned.below(), ModBlocks.RAW_LEAD_BLOCK.get());
        Squid squid = helper.spawn(SQUID, new BlockPos(1, 2, 1));
        Vec3 clean = PoisonedWaterAvoidance.steer(squid, 0.2F, 0.0F, 0.0F);
        String setup = poisonOnly(helper, poisoned);
        if (setup != null) {
            squid.discard();
            fail(helper, setup);
            return;
        }
        Vec3 fouled = PoisonedWaterAvoidance.steer(squid, 0.2F, 0.0F, 0.0F);
        squid.discard();
        succeedUnless(helper, clean.x != 0.2F ? "a squid turned before the lead leaked"
                : fouled.x >= 0.0 ? "a squid set off towards it" : null);
    }

    /**
     * A channel of water two blocks wide: the straight way runs through the poisoned block, the way beside it doesn't.
     * Without the poison the straight way is the cheaper.
     */
    private static void swimsAround(GameTestHelper helper, EntityType<? extends Mob> type, String who) {
        List<BlockPos> water = new ArrayList<>(row(1, 4, 1));
        water.addAll(row(1, 4, 2));
        BlockPos poisoned = new BlockPos(2, 2, 1);
        tank(helper, water);
        String setup = poisonOnly(helper, poisoned);
        if (setup != null) {
            fail(helper, setup);
            return;
        }
        Mob mob = helper.spawn(type, new BlockPos(1, 2, 1));
        // A fish plans a route only once it has felt the water it is in, which its tick does.
        mob.baseTick();
        Path path = mob.getNavigation().createPath(helper.absolutePos(new BlockPos(4, 2, 1)), 0);
        mob.discard();
        if (path == null || !path.canReach()) {
            fail(helper, "The test's setup failed, not the mod: " + who + " found no way through the tank");
            return;
        }
        BlockPos through = helper.absolutePos(poisoned);
        for (int i = 0; i < path.getNodeCount(); i++) {
            if (path.getNode(i).asBlockPos().equals(through)) {
                fail(helper, "In water raw lead fouls, " + who + " swam straight through it when it could go round");
                return;
            }
        }
        helper.succeed();
    }

    /** Raw lead under the water at {@code water}, just far enough into its spreading to poison it and none beside it. */
    private static String poisonOnly(GameTestHelper helper, BlockPos water) {
        BlockPos lead = water.below();
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        LeadWater.Leak leak = LeadWater.leak(helper.getLevel(), helper.absolutePos(lead), LeadWater.range(false));
        int step = 1;
        while (step < LeadWater.MAX_STEP && !leak.poisons(step, helper.absolutePos(water))) {
            step++;
        }
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState().setValue(RawLeadBlock.CONTAMINATION, step));
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (leak.poisons(step, helper.absolutePos(water.relative(side)))) {
                return "The test's setup failed, not the mod: the water beside the poisoned block is poisoned too";
            }
        }
        return null;
    }

    /** The blocks from {@code fromX} to {@code toX} along row {@code z}, at the tank's height. */
    private static List<BlockPos> row(int fromX, int toX, int z) {
        List<BlockPos> row = new ArrayList<>();
        for (int x = fromX; x <= toX; x++) {
            row.add(new BlockPos(x, 2, z));
        }
        return row;
    }

    /** Still water at {@code water}, sealed in stone a block all round, so nothing flows while the test reads it. */
    private static void tank(GameTestHelper helper, List<BlockPos> water) {
        int minX = water.stream().mapToInt(BlockPos::getX).min().orElse(0) - 1;
        int maxX = water.stream().mapToInt(BlockPos::getX).max().orElse(0) + 1;
        int minZ = water.stream().mapToInt(BlockPos::getZ).min().orElse(0) - 1;
        int maxZ = water.stream().mapToInt(BlockPos::getZ).max().orElse(0) + 1;
        for (int x = minX; x <= maxX; x++) {
            for (int y = 1; y <= 3; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
        for (BlockPos at : water) {
            helper.setBlock(at, Blocks.WATER);
        }
    }

    private static void succeedUnless(GameTestHelper helper, String problem) {
        if (problem == null) {
            helper.succeed();
        } else {
            fail(helper, "In water raw lead fouls, " + problem);
        }
    }
}

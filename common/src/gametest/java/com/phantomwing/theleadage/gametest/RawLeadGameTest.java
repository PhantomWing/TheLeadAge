package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.effect.CampfireFumes;
import com.phantomwing.theleadage.effect.LeadFumes;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.entity.LeadFumeRepellent;
import com.phantomwing.theleadage.entity.ai.AvoidRepellentBlockGoal;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.phantomwing.theleadage.gametest.LeadOreGameTest.FIND_TICKS;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.SPAWN_TICKS;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.clearSickness;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.countDoses;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.whenFumesFind;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.whenSpawnLoaded;
import static com.phantomwing.theleadage.gametest.TestCompat.BEE;
import static com.phantomwing.theleadage.gametest.TestCompat.CREEPER;
import static com.phantomwing.theleadage.gametest.TestCompat.PIG;
import static com.phantomwing.theleadage.gametest.TestCompat.fail;
import static com.phantomwing.theleadage.gametest.TestCompat.moveTo;
import static com.phantomwing.theleadage.gametest.TestCompat.reloaded;

/**
 * Game tests for the Block of Raw Lead's toxicity: Silk Touch mining it, a campfire over it with a hay bale between,
 * the campfire's stream rising with its smoke and the bees over it, and the water it leaches into - how its spreading
 * steps on, the routes it takes around what stands in the water, and the edge it tints without poisoning. Listed in
 * {@link GameTests}.
 */
public final class RawLeadGameTest {
    private RawLeadGameTest() {
    }

    /** Mining a block of raw lead with Silk Touch never releases fumes. */
    public static void rawLeadSilkTouchNeverFumes(GameTestHelper helper) {
        countDoses(helper, ModBlocks.RAW_LEAD_BLOCK.get(), true, count -> {
            if (count == 0) {
                helper.succeed();
            } else {
                fail(helper, "Mining raw lead with Silk Touch dosed the player " + count + " times");
            }
        });
    }

    /** A campfire over a hay bale over raw lead gives off fumes; one over hay over anything else doesn't. */
    public static void campfireOverHayOverRawLeadFumes(GameTestHelper helper) {
        record Case(Block bottom, Block middle, boolean fumes) {
        }
        BlockPos fire = new BlockPos(1, 3, 1);
        BlockPos at = helper.absolutePos(fire);
        ServerLevel level = helper.getLevel();
        for (Case c : List.of(
                new Case(ModBlocks.RAW_LEAD_BLOCK.get(), Blocks.HAY_BLOCK, true),
                new Case(ModBlocks.LEAD_BLOCK.get(), Blocks.HAY_BLOCK, false),
                new Case(ModBlocks.RAW_LEAD_BLOCK.get(), Blocks.STONE, false),
                new Case(Blocks.STONE, Blocks.HAY_BLOCK, false))) {
            helper.setBlock(fire.below(2), c.bottom());
            helper.setBlock(fire.below(), c.middle());
            helper.setBlock(fire, Blocks.CAMPFIRE);
            if (CampfireFumes.isFuming(level, at, level.getBlockState(at)) != c.fumes()) {
                fail(helper, "A campfire over " + c.middle() + " over " + c.bottom()
                        + (c.fumes() ? " gives off no fumes" : " gives off fumes"));
                return;
            }
        }
        helper.succeed();
    }

    /**
     * The stream a player breathes climbs from the fire at the smoke's speed: three blocks up, it hasn't reached the
     * player right after the fire is lit, and has a couple of seconds later.
     */
    public static void campfireStreamRisesWithTheSmoke(GameTestHelper helper) {
        whenSpawnLoaded(helper, SPAWN_TICKS, () -> campfireStreamRisesWithTheSmokeNow(helper));
    }

    private static void campfireStreamRisesWithTheSmokeNow(GameTestHelper helper) {
        BlockPos fire = new BlockPos(1, 2, 1);
        for (int y = 3; y <= 7; y++) {
            helper.setBlock(new BlockPos(1, y, 1), Blocks.AIR);
        }
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        moveTo(player, Vec3.atBottomCenterOf(helper.absolutePos(fire.above(3))));
        whenFumesFind(helper, player, fire, FIND_TICKS, () -> {
            clearSickness(player);
            helper.setBlock(fire.below(), ModBlocks.RAW_LEAD_BLOCK.get());
            helper.setBlock(fire, Blocks.CAMPFIRE);
            helper.runAfterDelay(20, () -> {
                if (player.hasEffect(MobEffects.HUNGER)) {
                    fail(helper, "The stream dosed a player three blocks up a second after the fire was lit");
                    return;
                }
                helper.runAfterDelay(60, () -> {
                    if (player.hasEffect(MobEffects.HUNGER)) {
                        helper.succeed();
                    } else {
                        fail(helper, "The stream never rose to a player three blocks over the fire");
                    }
                });
            });
        });
    }

    /** Raw lead in water spreads on: its contamination steps up as it ticks. */
    public static void rawLeadSpreadingStepsOn(GameTestHelper helper) {
        BlockPos lead = new BlockPos(1, 2, 1);
        BlockPos water = lead.above();
        // A sealed pocket of water over the lead, so nothing flows out of the test.
        for (BlockPos wall : List.of(water.north(), water.south(), water.east(), water.west(), water.above())) {
            helper.setBlock(wall, Blocks.STONE);
        }
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        helper.setBlock(water, Blocks.WATER);
        helper.runAfterDelay(120, () -> {
            int step = helper.getBlockState(lead).getValue(RawLeadBlock.CONTAMINATION);
            if (step > 1) {
                helper.succeed();
            } else {
                fail(helper, "Raw lead in water was still at step " + step + " of its spreading after 6 seconds");
            }
        });
    }

    /**
     * Raw lead leaks around what stands in the water: the water behind a wall is reached by the way round, and the
     * wall itself is not.
     */
    public static void rawLeadLeaksAroundAWall(GameTestHelper helper) {
        BlockPos lead = new BlockPos(0, 2, 0);
        BlockPos wall = new BlockPos(1, 2, 0);
        BlockPos behind = new BlockPos(2, 2, 0);
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        helper.setBlock(wall, Blocks.STONE);
        List<BlockPos> water = List.of(behind, new BlockPos(0, 2, 1), new BlockPos(1, 2, 1), new BlockPos(2, 2, 1));
        for (BlockPos at : water) {
            helper.setBlock(at, Blocks.WATER);
        }
        // Read at once, before the water can flow anywhere.
        LeadWater.Leak leak = LeadWater.leak(helper.getLevel(), helper.absolutePos(lead), LeadWater.range(false));
        boolean behindPoisoned = leak.poisons(LeadWater.MAX_STEP, helper.absolutePos(behind));
        boolean wallPoisoned = leak.poisons(LeadWater.MAX_STEP, helper.absolutePos(wall));
        for (BlockPos at : water) {
            helper.setBlock(at, Blocks.AIR);
        }
        if (LeadWater.range(false) < 4) {
            fail(helper, "The lead water range is " + LeadWater.range(false) + ", too short for the way round the wall");
        } else if (!behindPoisoned) {
            fail(helper, "Raw lead didn't leak round a wall to the water behind it");
        } else if (wallPoisoned) {
            fail(helper, "Raw lead leaked into the wall");
        } else {
            helper.succeed();
        }
    }

    /**
     * Raw lead poisons the water as far as its range, tints one block further without poisoning it, and the murk
     * goes further still: a column of water over the lead, read at each height.
     */
    public static void rawLeadEdgeTintsWithoutPoisoning(GameTestHelper helper) {
        int range = LeadWater.range(false);
        if (range == 0) {
            fail(helper, "The lead water range is 0");
            return;
        }
        BlockPos lead = new BlockPos(1, 2, 1);
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        int top = range + LeadWater.MURK_AHEAD;
        for (int d = 1; d <= top; d++) {
            helper.setBlock(lead.above(d), Blocks.WATER);
        }
        LeadWater.Leak leak = LeadWater.leak(helper.getLevel(), helper.absolutePos(lead), range);
        int step = LeadWater.MAX_STEP;
        BlockPos last = helper.absolutePos(lead.above(range));
        BlockPos edge = helper.absolutePos(lead.above(range + 1));
        BlockPos murk = helper.absolutePos(lead.above(range + 2));
        String problem = !leak.poisons(step, last) ? "the water " + range + " blocks up isn't poisoned"
                : leak.poisons(step, edge) ? "the edge, " + (range + 1) + " blocks up, poisons"
                : leak.strength(step, edge) <= 0.0F ? "the edge isn't tinted"
                : leak.strength(step, murk) > 0.0F ? "the water past the edge is tinted"
                : leak.murk(step, murk) <= 0.0F ? "the murk doesn't reach past the edge"
                : null;
        for (int d = 1; d <= top; d++) {
            helper.setBlock(lead.above(d), Blocks.AIR);
        }
        if (problem == null) {
            helper.succeed();
        } else {
            fail(helper, "In a column of water over raw lead, " + problem);
        }
    }

    /**
     * Raw lead the world left beside water without waking it - generated by an aquifer, placed with no neighbour
     * update - wakes on a random tick and starts leaking.
     */
    public static void rawLeadWakesByRandomTick(GameTestHelper helper) {
        BlockPos lead = new BlockPos(1, 2, 1);
        BlockPos water = lead.above();
        for (BlockPos wall : List.of(water.north(), water.south(), water.east(), water.west(), water.above())) {
            helper.setBlock(wall, Blocks.STONE);
        }
        helper.setBlock(water, Blocks.WATER);
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(lead);
        level.setBlock(at, ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (level.getBlockState(at).getValue(RawLeadBlock.CONTAMINATION) != 0) {
            fail(helper, "The raw lead woke as it was set down - the test's setup failed, not the mod");
            return;
        }
        level.getBlockState(at).randomTick(level, at, level.getRandom());
        if (level.getBlockState(at).getValue(RawLeadBlock.CONTAMINATION) == 0) {
            fail(helper, "A random tick didn't wake raw lead lying in water");
        } else if (!level.getBlockTicks().hasScheduledTick(at, ModBlocks.RAW_LEAD_BLOCK.get())) {
            fail(helper, "Raw lead woken by a random tick has no tick to spread by");
        } else {
            helper.succeed();
        }
    }

    /** Raw lead left leaking where its water went without a shape update reaching it dries out at its next tick. */
    public static void rawLeadDriesOutWithoutAnUpdate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(at, ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState().setValue(RawLeadBlock.CONTAMINATION, 2),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (!level.getBlockTicks().hasScheduledTick(at, ModBlocks.RAW_LEAD_BLOCK.get())) {
            fail(helper, "Leaking raw lead set down has no tick - the test's setup failed, not the mod");
            return;
        }
        helper.runAfterDelay(LeadWater.INTERVAL + 2, () -> {
            int step = level.getBlockState(at).getValue(RawLeadBlock.CONTAMINATION);
            if (step != 0) {
                fail(helper, "Raw lead in no water still leaks, at step " + step);
            } else if (level.getBlockTicks().hasScheduledTick(at, ModBlocks.RAW_LEAD_BLOCK.get())) {
                fail(helper, "Dried-out raw lead still ticks");
            } else {
                helper.succeed();
            }
        });
    }

    /** A creeper finds a lead torch and a campfire fuming over raw lead to keep away from. */
    public static void creepersFindWhatRepelsThem(GameTestHelper helper) {
        Creeper creeper = helper.spawnWithNoFreeWill(CREEPER, new BlockPos(0, 2, 0));
        BlockPos torch = new BlockPos(2, 2, 1);
        helper.setBlock(torch.below(), Blocks.STONE);
        helper.setBlock(torch, ModBlocks.LEAD_TORCH.get());
        BlockPos found = repellentFoundBy(creeper);
        if (!helper.absolutePos(torch).equals(found)) {
            fail(helper, "A creeper beside a lead torch found " + found + " to keep away from");
            return;
        }
        helper.setBlock(torch, Blocks.AIR);
        BlockPos fire = new BlockPos(1, 3, 2);
        helper.setBlock(fire.below(), ModBlocks.RAW_LEAD_BLOCK.get());
        helper.setBlock(fire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        found = repellentFoundBy(creeper);
        if (helper.absolutePos(fire).equals(found)) {
            helper.succeed();
        } else {
            fail(helper, "A creeper beside a campfire over raw lead found " + found + " to keep away from");
        }
    }

    /**
     * A creeper notices a lead torch anywhere in its search, 8 blocks along either way and 4 up or down as vanilla's
     * piglins search for soul fire, not only within 8 steps of it: 7 along and 3 aside, then 5 along, 3 up and 5 aside.
     * Far above the test, so no other test's torch or campfire is nearer.
     */
    public static void creepersSearchTheirWholeBox(GameTestHelper helper) {
        BlockPos at = new BlockPos(0, 40, 0);
        Creeper creeper = helper.spawnWithNoFreeWill(CREEPER, at);
        for (BlockPos torch : List.of(at.offset(7, 0, 3), at.offset(5, 3, 5))) {
            helper.setBlock(torch.below(), Blocks.STONE);
            helper.setBlock(torch, ModBlocks.LEAD_TORCH.get());
            BlockPos found = repellentFoundBy(creeper);
            helper.setBlock(torch, Blocks.AIR);
            helper.setBlock(torch.below(), Blocks.AIR);
            if (!helper.absolutePos(torch).equals(found)) {
                creeper.discard();
                fail(helper, "A creeper missed a lead torch " + torch.subtract(at).toShortString() + " from it, finding "
                        + found);
                return;
            }
        }
        creeper.discard();
        helper.succeed();
    }

    /** What a new creeper's goal finds to keep away from, given the goal ticks it takes to search. */
    private static BlockPos repellentFoundBy(Creeper creeper) {
        AvoidRepellentBlockGoal goal = new AvoidRepellentBlockGoal(creeper, ModTags.Blocks.CREEPER_REPELLENTS, () -> true, 1.0, 1.2);
        for (int i = 0; i < 10 && goal.repellent() == null; i++) {
            goal.canUse();
        }
        return goal.repellent();
    }

    /** A campfire's smoke calms the bees of a hive over it, so harvesting doesn't anger them; one giving off lead fumes doesn't. */
    public static void leadFumesCalmNoBees(GameTestHelper helper) {
        BlockPos fire = new BlockPos(1, 3, 1);
        BlockPos hive = fire.above(2);
        helper.setBlock(fire.below(), ModBlocks.RAW_LEAD_BLOCK.get());
        helper.setBlock(fire, Blocks.CAMPFIRE);
        helper.setBlock(hive, Blocks.BEEHIVE);
        ServerLevel level = helper.getLevel();
        boolean fumesCalm = CampfireBlock.isSmokeyPos(level, helper.absolutePos(hive));
        helper.setBlock(fire.below(), Blocks.STONE);
        boolean smokeCalms = CampfireBlock.isSmokeyPos(level, helper.absolutePos(hive));
        helper.setBlock(hive, Blocks.AIR);
        helper.setBlock(fire, Blocks.AIR);
        if (fumesCalm) {
            fail(helper, "A campfire giving off lead fumes calmed the bees over it");
        } else if (!smokeCalms) {
            fail(helper, "A plain campfire's smoke didn't calm the bees over it - the test's setup failed, not the mod");
        } else {
            helper.succeed();
        }
    }

    /** A bee at its hive over a campfire giving off lead fumes breathes them. */
    public static void leadFumesSickenBeesAtTheirHive(GameTestHelper helper) {
        BlockPos fire = new BlockPos(1, 2, 1);
        BlockPos hive = fire.above(2);
        for (int y = 3; y <= 6; y++) {
            helper.setBlock(new BlockPos(1, y, 1), Blocks.AIR);
            helper.setBlock(new BlockPos(2, y, 1), Blocks.AIR);
        }
        helper.setBlock(fire.below(), ModBlocks.RAW_LEAD_BLOCK.get());
        helper.setBlock(fire, Blocks.CAMPFIRE);
        helper.setBlock(hive, Blocks.BEEHIVE);
        // var keeps the bee's class out of the body: it moved package in 1.21.11.
        var bee = helper.spawn(BEE, hive.east());
        bee.setNoAi(true);
        bee.setNoGravity(true);
        helper.runAfterDelay(30, () -> {
            boolean sick = bee.hasEffect(MobEffects.HUNGER);
            helper.setBlock(hive, Blocks.AIR);
            helper.setBlock(fire, Blocks.AIR);
            if (sick) {
                helper.succeed();
            } else {
                fail(helper, "A bee at its hive over a campfire giving off lead fumes didn't breathe them");
            }
        });
    }

    /**
     * A campfire put out and lit again while its fumes still rise keeps the fumes that were rising: a player stepping
     * into the column above the new stream, under the old one's top, breathes them.
     */
    public static void campfireRelitKeepsItsRisingFumes(GameTestHelper helper) {
        whenSpawnLoaded(helper, SPAWN_TICKS, () -> campfireRelitKeepsItsRisingFumesNow(helper));
    }

    private static void campfireRelitKeepsItsRisingFumesNow(GameTestHelper helper) {
        BlockPos fire = new BlockPos(1, 2, 1);
        for (int y = 3; y <= 9; y++) {
            helper.setBlock(new BlockPos(1, y, 1), Blocks.AIR);
        }
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos aside = new BlockPos(3, 2, 1);
        moveTo(player, Vec3.atBottomCenterOf(helper.absolutePos(aside)));
        whenFumesFind(helper, player, fire, FIND_TICKS, () -> {
            clearSickness(player);
            helper.setBlock(fire.below(), ModBlocks.RAW_LEAD_BLOCK.get());
            helper.setBlock(fire, Blocks.CAMPFIRE);
            helper.runAfterDelay(60, () -> {
                helper.setBlock(fire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
                helper.runAfterDelay(10, () -> {
                    helper.setBlock(fire, Blocks.CAMPFIRE);
                    moveTo(player, Vec3.atBottomCenterOf(helper.absolutePos(fire.above(3))));
                    helper.runAfterDelay(15, () -> {
                        if (player.hasEffect(MobEffects.HUNGER)) {
                            helper.succeed();
                        } else {
                            fail(helper, "Lighting the campfire again lost the fumes still rising over it");
                        }
                    });
                });
            });
        });
    }

    /** A creeper loaded with its chunk keeps away from lead torches, as one that spawned does. */
    public static void creepersLoadedFromDiskAvoidLeadTorches(GameTestHelper helper) {
        // Not spawnWithNoFreeWill, which takes every goal away.
        Creeper spawned = helper.spawn(CREEPER, new BlockPos(0, 2, 0));
        Creeper loaded = reloaded(spawned);
        loaded.setUUID(UUID.randomUUID());
        helper.getLevel().addLegacyChunkEntities(Stream.of(loaded));
        if (LeadFumeRepellent.hasAvoidance(spawned) && LeadFumeRepellent.hasAvoidance(loaded)) {
            helper.succeed();
        } else {
            fail(helper, "A creeper " + (LeadFumeRepellent.hasAvoidance(spawned) ? "loaded from disk" : "that spawned")
                    + " doesn't keep away from lead torches");
        }
    }

    /** Weakness from a potion is no stage of Lead Sickness: the first dose of lead brings the first stage only. */
    public static void unrelatedEffectsDontAdvanceLeadSickness(GameTestHelper helper) {
        var pig = helper.spawnWithNoFreeWill(PIG, new BlockPos(0, 2, 0));
        pig.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200));
        LeadFumes.escalate(pig);
        if (!pig.hasEffect(MobEffects.HUNGER) || pig.hasEffect(MobEffects.POISON)) {
            fail(helper, "A first dose of lead on a weakened pig gave " + pig.getActiveEffects() + ", not its first stage");
        } else {
            helper.succeed();
        }
    }
}

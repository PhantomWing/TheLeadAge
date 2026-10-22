package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.effect.LeadFumes;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.effect.RecedingLeads;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.phantomwing.theleadage.gametest.TestCompat.COD;
import static com.phantomwing.theleadage.gametest.TestCompat.DROWNED;
import static com.phantomwing.theleadage.gametest.TestCompat.fail;

/**
 * Game tests for the water raw lead contaminates: it doesn't reach the water under the lead, never goes upstream, a
 * current carries it faster than still water, clean water flowing in lightens it, no block of it is more than a shade
 * darker than the water beside it, what it left recedes from its far edge once the lead is gone, it doses mobs but not
 * the undead, and coral dies in it. The routes are read at once, before the water can flow anywhere. Listed in
 * {@link GameTests}.
 */
public final class LeadWaterGameTest {
    private static final BlockState FALLING_WATER = Fluids.WATER.getFlowing(8, true).createLegacyBlock();

    private LeadWaterGameTest() {
    }

    /** Raw lead set over water doesn't touch it, the water's surface lying short of the block above: it leaks nothing there. */
    public static void rawLeadDoesntLeakIntoWaterUnderIt(GameTestHelper helper) {
        BlockPos lead = new BlockPos(1, 3, 1);
        BlockPos under = lead.below();
        helper.setBlock(under, Blocks.WATER);
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        boolean touches = LeadWater.touchesWater(helper.getLevel(), helper.absolutePos(lead));
        boolean poisons = leakOf(helper, lead).poisons(LeadWater.MAX_STEP, helper.absolutePos(under));
        clear(helper, under);
        if (touches) {
            fail(helper, "Raw lead over water counts as touching it");
        } else if (poisons) {
            fail(helper, "Raw lead leaked into the water under it");
        } else {
            helper.succeed();
        }
    }

    /** The poison never climbs a waterfall: neither one pouring into the pool it fouls, nor one landing on the lead. */
    public static void poisonNeverClimbsAWaterfall(GameTestHelper helper) {
        BlockPos lead = new BlockPos(1, 2, 1);
        BlockPos pool = lead.east();
        BlockPos intoPool = pool.above();
        BlockPos ontoLead = lead.above();
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        helper.setBlock(pool, Blocks.WATER);
        helper.setBlock(intoPool, FALLING_WATER);
        helper.setBlock(ontoLead, FALLING_WATER);
        LeadWater.Leak leak = leakOf(helper, lead);
        String problem = !leak.poisons(LeadWater.MAX_STEP, helper.absolutePos(pool)) ? "the pool beside the lead isn't poisoned"
                : leak.poisons(LeadWater.MAX_STEP, helper.absolutePos(intoPool)) ? "the waterfall pouring into the pool is poisoned"
                : leak.poisons(LeadWater.MAX_STEP, helper.absolutePos(ontoLead)) ? "the waterfall landing on the lead is poisoned"
                : null;
        clear(helper, pool, intoPool, ontoLead);
        succeedUnless(helper, problem);
    }

    /** The poison goes downstream only: into lower water, never into the higher water the fouled water flows from. */
    public static void poisonRunsDownstreamOnly(GameTestHelper helper) {
        BlockPos lead = new BlockPos(0, 2, 0);
        BlockPos fouled = new BlockPos(1, 2, 0);
        BlockPos upstream = new BlockPos(2, 2, 0);
        BlockPos downstream = new BlockPos(1, 2, 1);
        clear(helper, new BlockPos(0, 2, 1), new BlockPos(2, 2, 1));
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        helper.setBlock(fouled, flowing(6));
        helper.setBlock(upstream, flowing(7));
        helper.setBlock(downstream, flowing(5));
        LeadWater.Leak leak = leakOf(helper, lead);
        String problem = !leak.poisons(LeadWater.MAX_STEP, helper.absolutePos(fouled)) ? "the stream beside the lead isn't poisoned"
                : !leak.poisons(LeadWater.MAX_STEP, helper.absolutePos(downstream)) ? "the water downstream isn't poisoned"
                : leak.poisons(LeadWater.MAX_STEP, helper.absolutePos(upstream)) ? "the water upstream is poisoned"
                : null;
        clear(helper, fouled, upstream, downstream);
        succeedUnless(helper, problem);
    }

    /** A current carries the poison faster than still water: three blocks down a stream are reached before three across a pool. */
    public static void currentCarriesPoisonFaster(GameTestHelper helper) {
        BlockPos lead = new BlockPos(0, 2, 0);
        List<BlockPos> pool = List.of(new BlockPos(1, 2, 0), new BlockPos(2, 2, 0), new BlockPos(3, 2, 0));
        List<BlockPos> stream = List.of(new BlockPos(0, 2, 1), new BlockPos(0, 2, 2), new BlockPos(0, 2, 3));
        clear(helper, new BlockPos(1, 2, 1), new BlockPos(1, 2, 2), new BlockPos(1, 2, 3));
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        for (int i = 0; i < 3; i++) {
            helper.setBlock(pool.get(i), Blocks.WATER);
            helper.setBlock(stream.get(i), flowing(7 - i));
        }
        LeadWater.Leak leak = leakOf(helper, lead);
        int step = 4;
        boolean streamReached = leak.since(step, helper.absolutePos(stream.get(2))) >= 0;
        boolean poolReached = leak.since(step, helper.absolutePos(pool.get(2))) >= 0;
        clear(helper, pool.toArray(BlockPos[]::new));
        clear(helper, stream.toArray(BlockPos[]::new));
        succeedUnless(helper, !streamReached ? "the stream three blocks along wasn't reached in " + step + " steps"
                : poolReached ? "the pool three blocks across was reached as soon as the stream" : null);
    }

    /** Clean water flowing into fouled water lightens it: the pool under a clean waterfall is lighter than the pool beside it. */
    public static void cleanInflowLightensPoisonedWater(GameTestHelper helper) {
        BlockPos lead = new BlockPos(1, 2, 1);
        BlockPos plain = lead.west();
        BlockPos underFall = lead.east();
        BlockPos fall = underFall.above();
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        helper.setBlock(plain, Blocks.WATER);
        helper.setBlock(underFall, Blocks.WATER);
        helper.setBlock(fall, FALLING_WATER);
        LeadWater.Leak leak = leakOf(helper, lead);
        float plainStrength = leak.strength(LeadWater.MAX_STEP, helper.absolutePos(plain));
        float underStrength = leak.strength(LeadWater.MAX_STEP, helper.absolutePos(underFall));
        clear(helper, plain, underFall, fall);
        succeedUnless(helper, underStrength <= 0.0F ? "the pool under a clean waterfall isn't tinted at all"
                : underStrength >= plainStrength ? "the pool under a clean waterfall is as dark as the pool beside it ("
                + underStrength + " against " + plainStrength + ")" : null);
    }

    /** No block of fouled water is more than a shade darker than the water beside it, at any step of its spreading. */
    public static void poisonedWaterDarkensFromItsEdge(GameTestHelper helper) {
        int range = LeadWater.range(false);
        BlockPos lead = new BlockPos(0, 2, 0);
        BlockPos[] line = new BlockPos[range + 2];
        for (int i = 0; i < line.length; i++) {
            line[i] = new BlockPos(i + 1, 2, 0);
            helper.setBlock(line[i], Blocks.WATER);
        }
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get());
        LeadWater.Leak leak = leakOf(helper, lead);
        String problem = null;
        for (int step = 1; step <= LeadWater.lastStep(range) && problem == null; step++) {
            for (int i = 0; i + 1 < line.length; i++) {
                float near = leak.strength(step, helper.absolutePos(line[i]));
                float far = leak.strength(step, helper.absolutePos(line[i + 1]));
                if (Math.abs(near - far) > LeadWater.TINT_STEP + 1.0E-4F) {
                    problem = "at step " + step + " the water " + (i + 1) + " blocks out is tinted " + near
                            + " beside water tinted " + far;
                    break;
                }
            }
        }
        clear(helper, line);
        succeedUnless(helper, problem);
    }

    /** Once raw lead is gone, what it left recedes from its far edge at once, a block every few ticks, until it is clean. */
    public static void recedingLeadClearsFromItsEdge(GameTestHelper helper) {
        int range = LeadWater.range(false);
        int depth = 4;
        BlockPos lead = new BlockPos(1, 2, 1);
        // A sealed shaft of still water over the lead, so nothing flows.
        for (int d = 1; d <= depth; d++) {
            BlockPos at = lead.above(d);
            for (BlockPos wall : List.of(at.north(), at.south(), at.east(), at.west())) {
                helper.setBlock(wall, Blocks.STONE);
            }
            helper.setBlock(at, Blocks.WATER);
        }
        helper.setBlock(lead.above(depth + 1), Blocks.STONE);
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState()
                .setValue(RawLeadBlock.CONTAMINATION, LeadWater.lastStep(range)));
        helper.setBlock(lead, Blocks.STONE);
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(lead);
        RecedingLeads.State left = RecedingLeads.of(level).get(at.asLong());
        if (left == null) {
            fail(helper, "Replacing raw lead left nothing in its water to recede");
            return;
        }
        LeadWater.Leak leak = LeadWater.leak(level, at, range);
        String problem = left.cut() != depth - 1 ? "it recedes from " + left.cut() + " blocks out, not the farthest water's " + (depth - 1)
                : leak.strength(left.step(), left.cut(), helper.absolutePos(lead.above(depth))) > 0.0F ? "the farthest water is still tinted"
                : leak.strength(left.step(), left.cut(), helper.absolutePos(lead.above())) <= 0.0F ? "the water beside the lead cleared at once"
                : null;
        if (problem != null) {
            fail(helper, "Once raw lead was gone, " + problem);
            return;
        }
        helper.runAfterDelay(LeadWater.RECEDE_TICKS * depth + 5, () -> {
            if (RecedingLeads.of(level).containsKey(at.asLong())) {
                fail(helper, "What raw lead left was still receding " + (LeadWater.RECEDE_TICKS * depth + 5) + " ticks after it was gone");
            } else {
                helper.succeed();
            }
        });
    }

    /** Fouled water doses the mobs in it as it does players, after a moment in it, but not the undead. */
    public static void contaminatedWaterDosesMobs(GameTestHelper helper) {
        BlockPos lead = new BlockPos(0, 2, 0);
        // A sealed shaft of water over the lead, tall enough for a drowned.
        for (int d = 1; d <= 3; d++) {
            BlockPos at = lead.above(d);
            for (BlockPos wall : List.of(at.north(), at.south(), at.east(), at.west())) {
                helper.setBlock(wall, Blocks.STONE);
            }
            helper.setBlock(at, Blocks.WATER);
        }
        helper.setBlock(lead.above(4), Blocks.STONE);
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState().setValue(RawLeadBlock.CONTAMINATION, LeadWater.MAX_STEP));
        Cod cod = helper.spawn(COD, lead.above());
        Drowned drowned = helper.spawn(DROWNED, lead.above());
        whenFound(helper, lead, List.of(cod, drowned), 40, () -> soak(helper, helper.absolutePos(lead), cod, drowned, 0));
    }

    /** The cod and the drowned stay in the fouled water, looked through as often as the game does, until it should dose them. */
    private static void soak(GameTestHelper helper, BlockPos lead, Cod cod, Drowned drowned, int soaked) {
        LeadWater.contaminate(helper.getLevel(), lead, LeadWater.MAX_STEP);
        if (soaked < LeadFumes.FUMES_STAGE_TICKS) {
            if (cod.hasEffect(MobEffects.HUNGER)) {
                fail(helper, "fouled water dosed a cod after " + soaked + " ticks in it, before " + LeadFumes.FUMES_STAGE_TICKS);
            } else {
                helper.runAfterDelay(LeadWater.INTERVAL, () -> soak(helper, lead, cod, drowned, soaked + LeadWater.INTERVAL));
            }
            return;
        }
        succeedUnless(helper, !cod.hasEffect(MobEffects.HUNGER) ? "fouled water didn't dose a cod swimming in it"
                : drowned.hasEffect(MobEffects.HUNGER) ? "fouled water dosed a drowned" : null);
    }

    /** A player swimming in fouled water, and breathing it, earns the advancement for it. */
    public static void swimmingInPoisonedWaterEarnsItsAdvancement(GameTestHelper helper) {
        LeadOreGameTest.whenSpawnLoaded(helper, LeadOreGameTest.SPAWN_TICKS, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            BlockPos lead = new BlockPos(0, 2, 0);
            helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState().setValue(RawLeadBlock.CONTAMINATION, LeadWater.MAX_STEP));
            helper.setBlock(lead.above(), Blocks.WATER);
            helper.setBlock(lead.above(2), Blocks.WATER);
            TestCompat.moveTo(player, Vec3.atBottomCenterOf(helper.absolutePos(lead.above())));
            AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements()
                    .get(TheLeadAge.resourceLocation("main/poisoned_water"));
            if (advancement == null) {
                fail(helper, "There is no advancement for swimming in fouled water");
                return;
            }
            LeadOreGameTest.whenFumesFind(helper, player, lead.above(), LeadOreGameTest.FIND_TICKS, () -> {
                LeadOreGameTest.clearSickness(player);
                LeadWater.contaminate(helper.getLevel(), helper.absolutePos(lead), LeadWater.MAX_STEP);
                boolean earned = player.getAdvancements().getOrStartProgress(advancement).isDone();
                clear(helper, lead.above(), lead.above(2));
                succeedUnless(helper, earned ? null : "a player swimming in it didn't earn its advancement");
            });
        });
    }

    /** Coral beside fouled water dies as it does out of water; coral beside clean water lives. */
    public static void coralDiesInPoisonedWater(GameTestHelper helper) {
        BlockPos lead = new BlockPos(0, 2, 0);
        BlockPos fouled = lead.above();
        BlockPos coral = fouled.east();
        BlockPos clean = new BlockPos(3, 3, 3);
        BlockPos healthy = clean.east();
        helper.setBlock(lead, ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState().setValue(RawLeadBlock.CONTAMINATION, LeadWater.MAX_STEP));
        helper.setBlock(fouled, Blocks.WATER);
        helper.setBlock(coral, Blocks.BRAIN_CORAL_BLOCK);
        helper.setBlock(clean, Blocks.WATER);
        helper.setBlock(healthy, Blocks.BRAIN_CORAL_BLOCK);
        ServerLevel level = helper.getLevel();
        for (BlockPos at : List.of(coral, healthy)) {
            BlockPos absolute = helper.absolutePos(at);
            level.getBlockState(absolute).tick(level, absolute, level.getRandom());
        }
        String problem = !helper.getBlockState(coral).is(Blocks.DEAD_BRAIN_CORAL_BLOCK) ? "coral beside fouled water didn't die"
                : !helper.getBlockState(healthy).is(Blocks.BRAIN_CORAL_BLOCK) ? "coral beside clean water died"
                : null;
        clear(helper, fouled, clean);
        succeedUnless(helper, problem);
    }

    /** The routes raw lead at {@code lead} takes through the water as it stands, as the client finds them. */
    private static LeadWater.Leak leakOf(GameTestHelper helper, BlockPos lead) {
        return LeadWater.leak(helper.getLevel(), helper.absolutePos(lead), LeadWater.range(false));
    }

    private static BlockState flowing(int amount) {
        return Fluids.WATER.getFlowing(amount, false).createLegacyBlock();
    }

    private static void clear(GameTestHelper helper, BlockPos... at) {
        for (BlockPos pos : at) {
            helper.setBlock(pos, Blocks.AIR);
        }
    }

    private static void succeedUnless(GameTestHelper helper, String problem) {
        if (problem == null) {
            helper.succeed();
        } else {
            fail(helper, "In water raw lead fouls, " + problem);
        }
    }

    /** Runs {@code then} once a search around {@code lead}, as the water's doses make, finds every one of {@code mobs}. */
    private static void whenFound(GameTestHelper helper, BlockPos lead, List<LivingEntity> mobs, int ticksLeft, Runnable then) {
        AABB around = new AABB(helper.absolutePos(lead)).inflate(LeadWater.range(false));
        if (helper.getLevel().getEntitiesOfClass(LivingEntity.class, around).containsAll(mobs)) {
            then.run();
        } else if (ticksLeft == 0) {
            fail(helper, "the search never found the mobs in the water - the test's setup failed, not the mod");
        } else {
            helper.runAfterDelay(1, () -> whenFound(helper, lead, mobs, ticksLeft - 1, then));
        }
    }

    /**
     * What a client was told recedes is let go once the server has said nothing of it for two seconds - the player left
     * for elsewhere, or a break it predicted never came - and kept while the server still speaks of it.
     */
    public static void clientForgetsRecedingItHearsNoMoreOf(GameTestHelper helper) {
        BlockPos quiet = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos told = helper.absolutePos(new BlockPos(3, 2, 3));
        RecedingLeads.State state = new RecedingLeads.State(4, 3);
        try {
            RecedingLeads.forgetClient();
            RecedingLeads.received(quiet, state, 100);
            RecedingLeads.received(told, state, 100);
            RecedingLeads.received(told, state, 130);
            if (!RecedingLeads.forgetStale(135).isEmpty()) {
                fail(helper, "The client let go of receding lead it heard of a second ago");
                return;
            }
            List<BlockPos> forgotten = RecedingLeads.forgetStale(160);
            if (!forgotten.equals(List.of(quiet))) {
                fail(helper, "Two seconds after the last word, the client let go of " + forgotten + " rather than " + quiet);
                return;
            }
            if (!RecedingLeads.forgetStale(200).equals(List.of(told))) {
                fail(helper, "The client kept receding lead it last heard of 70 ticks ago");
                return;
            }
            helper.succeed();
        } finally {
            RecedingLeads.forgetClient();
        }
    }
}

package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Game tests for the "lead fumes" mechanic ({@code LeadOreBlock#playerDestroy}):
 * a non-silk-touch harvest gives Nausea ~30% of the time; silk touch never does.
 * Run headless with {@code ./gradlew :neoforge:runGameTest}.
 *
 * <p>The fumes are random, so the tests mine many times and assert on the count.
 * With {@value #TRIALS} trials the "sometimes" bounds fail with probability ~1e-15,
 * so they're deterministic in practice.</p>
 */
@GameTestHolder("theleadage")
@PrefixGameTestTemplate(false)
public class LeadOreGameTest {
    private static final int TRIALS = 100;

    /** Lead ore gives Nausea sometimes (but not every break). */
    @GameTest(template = "empty")
    public static void leadOreSometimesGivesNausea(GameTestHelper helper) {
        int count = countNausea(helper, ModBlocks.LEAD_ORE.get(), false);
        if (count > 0 && count < TRIALS) {
            helper.succeed();
        } else {
            helper.fail("Expected lead ore to give Nausea sometimes but not always (got " + count + "/" + TRIALS + ")");
        }
    }

    /** Same for deepslate lead ore. */
    @GameTest(template = "empty")
    public static void deepslateLeadOreSometimesGivesNausea(GameTestHelper helper) {
        int count = countNausea(helper, ModBlocks.DEEPSLATE_LEAD_ORE.get(), false);
        if (count > 0 && count < TRIALS) {
            helper.succeed();
        } else {
            helper.fail("Expected deepslate lead ore to give Nausea sometimes but not always (got " + count + "/" + TRIALS + ")");
        }
    }

    /** Silk touch yields the ore block, not raw lead, so it NEVER gives fumes. */
    @GameTest(template = "empty")
    public static void silkTouchNeverGivesNausea(GameTestHelper helper) {
        int count = countNausea(helper, ModBlocks.LEAD_ORE.get(), true);
        if (count == 0) {
            helper.succeed();
        } else {
            helper.fail("Silk touch should never give Nausea (got " + count + "/" + TRIALS + ")");
        }
    }

    /** Mines {@code ore} {@value #TRIALS} times and returns how many breaks applied Nausea. */
    private static int countNausea(GameTestHelper helper, Block ore, boolean silkTouch) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 2, 1);

        int count = 0;
        for (int i = 0; i < TRIALS; i++) {
            // Fresh, full-durability tool + cleared effect so each trial is independent.
            player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe(helper, silkTouch));
            player.removeEffect(MobEffects.CONFUSION);
            helper.setBlock(pos, ore);

            player.gameMode.destroyBlock(helper.absolutePos(pos));

            if (player.hasEffect(MobEffects.CONFUSION)) {
                count++;
            }
        }
        return count;
    }

    private static ItemStack pickaxe(GameTestHelper helper, boolean silkTouch) {
        ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
        if (silkTouch) {
            Holder<Enchantment> silk = helper.getLevel().registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
            pick.enchant(silk, 1);
        }
        return pick;
    }
}

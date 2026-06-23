package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.HeavyOrbTransforms;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.entity.HeavyOrbBlockEntity;
import com.phantomwing.theleadage.entity.custom.HeavyOrbEntity;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.item.custom.HeavyOrbItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

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

    /** Lead Door + a leaded glass pane must resolve to the leaded glass door recipe. */
    @GameTest(template = "empty")
    public static void doorRecipeCombines(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getServer().getRecipeManager();
        CraftingInput input = CraftingInput.of(1, 2, List.of(
                new ItemStack(ModItems.LEAD_DOOR.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANEL.get())));
        Optional<RecipeHolder<CraftingRecipe>> match = recipes.getRecipeFor(RecipeType.CRAFTING, input, level);
        if (match.isEmpty()) {
            helper.fail("No crafting recipe matched lead_door + leaded glass pane");
            return;
        }
        ItemStack result = match.get().value().assemble(input, level.registryAccess());
        if (result.is(ModItems.LEADED_GLASS_DOOR.get())) {
            helper.succeed();
        } else {
            helper.fail("Matched " + match.get().id() + " but result was " + result);
        }
    }

    /** The hit→region mapping must match each came's model layout (u: left→right, v: bottom→top). */
    @GameTest(template = "empty")
    public static void frameRegionMapping(GameTestHelper helper) {
        assertRegion(helper, LeadedGlassFrame.SPLIT_H, 0.25, 0.5, 0);   // left
        assertRegion(helper, LeadedGlassFrame.SPLIT_H, 0.75, 0.5, 1);   // right
        assertRegion(helper, LeadedGlassFrame.SPLIT_V, 0.5, 0.75, 0);   // top
        assertRegion(helper, LeadedGlassFrame.SPLIT_V, 0.5, 0.25, 1);   // bottom
        assertRegion(helper, LeadedGlassFrame.GRID, 0.25, 0.75, 0);     // TL
        assertRegion(helper, LeadedGlassFrame.GRID, 0.75, 0.75, 1);     // TR
        assertRegion(helper, LeadedGlassFrame.GRID, 0.25, 0.25, 2);     // BL
        assertRegion(helper, LeadedGlassFrame.GRID, 0.75, 0.25, 3);     // BR
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_A, 0.2, 0.8, 0); // "/" upper-left
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_A, 0.8, 0.2, 1); // "/" lower-right
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_B, 0.8, 0.8, 0); // "\" upper-right
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_B, 0.2, 0.2, 1); // "\" lower-left
        assertRegion(helper, LeadedGlassFrame.CROSS, 0.5, 0.9, 0);      // top
        assertRegion(helper, LeadedGlassFrame.CROSS, 0.9, 0.5, 1);      // right
        assertRegion(helper, LeadedGlassFrame.CROSS, 0.5, 0.1, 2);      // bottom
        assertRegion(helper, LeadedGlassFrame.CROSS, 0.1, 0.5, 3);      // left
        // 3×3 grid: row-major from the top-left (0..2 top, 3..5 middle, 6..8 bottom)
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.17, 0.83, 0);
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.50, 0.83, 1);
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.83, 0.83, 2);
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.17, 0.50, 3);
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.50, 0.50, 4);
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.83, 0.50, 5);
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.17, 0.17, 6);
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.50, 0.17, 7);
        assertRegion(helper, LeadedGlassFrame.GRID_3, 0.83, 0.17, 8);
        helper.succeed();
    }

    /** Heavy orb transforms are loaded from the mod's datapack and resolve block + (non-)matches. */
    @GameTest(template = "empty")
    public static void heavyOrbTransformsFromData(GameTestHelper helper) {
        var cracked = HeavyOrbTransforms.transform(Blocks.STONE_BRICKS.defaultBlockState());
        var dirt = HeavyOrbTransforms.transform(Blocks.GRASS_BLOCK.defaultBlockState());
        if (cracked == null || !cracked.is(Blocks.CRACKED_STONE_BRICKS)) {
            helper.fail("stone_bricks -> " + cracked + ", expected cracked_stone_bricks");
        } else if (dirt == null || !dirt.is(Blocks.DIRT)) {
            helper.fail("grass_block -> " + dirt + ", expected dirt");
        } else if (HeavyOrbTransforms.transform(Blocks.STONE.defaultBlockState()) != null) {
            helper.fail("plain stone should not transform");
        } else {
            helper.succeed();
        }
    }

    /** A placed orb's wear lives on its block entity and flows back into the picked/dropped item. */
    @GameTest(template = "empty")
    public static void heavyOrbBlockCarriesWear(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        helper.setBlock(rel, ModBlocks.HEAVY_ORB.get());
        BlockState state = helper.getBlockState(rel);
        if (!(helper.getBlockEntity(rel) instanceof HeavyOrbBlockEntity be)) {
            helper.fail("orb block has no HeavyOrbBlockEntity");
            return;
        }
        be.setDamage(55);
        ItemStack clone = ModBlocks.HEAVY_ORB.get().getCloneItemStack(helper.getLevel(), helper.absolutePos(rel), state);
        if (clone.getDamageValue() != 55) {
            helper.fail("picked orb damage = " + clone.getDamageValue() + ", expected 55");
        } else {
            helper.succeed();
        }
    }

    /** A falling orb wears by WEAR_PER_LANDING on impact, stored on the block it becomes. */
    @GameTest(template = "empty")
    public static void heavyOrbWearsOnLanding(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);  // floor
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR);    // carve the drop column out of the barrier
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
        BlockPos spawn = helper.absolutePos(new BlockPos(1, 2, 1));
        HeavyOrbEntity.inAir(helper.getLevel(), spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                ModBlocks.HEAVY_ORB.get().defaultBlockState(), null, 30);
        helper.startSequence()
                .thenIdle(40)
                .thenExecute(() -> {
                    int expected = 30 + HeavyOrbItem.WEAR_PER_LANDING;
                    Integer got = null;
                    // Returned as a worn block...
                    for (int y = 2; y >= 0 && got == null; y--) {
                        if (helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(1, y, 1)))
                                instanceof HeavyOrbBlockEntity be) {
                            got = be.getDamage();
                        }
                    }
                    // ...or, if it couldn't place, as a worn item.
                    if (got == null) {
                        AABB area = AABB.ofSize(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))), 5, 5, 5);
                        for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, area)) {
                            if (item.getItem().is(ModItems.HEAVY_ORB.get())) {
                                got = item.getItem().getDamageValue();
                            }
                        }
                    }
                    if (got == null) {
                        helper.fail("orb did not return as a worn block or item after landing");
                    } else if (got != expected) {
                        helper.fail("returned orb wear = " + got + ", expected " + expected);
                    }
                })
                .thenSucceed();
    }

    /** The heavy orb's air-drop direction snaps the look vector to the right non-upward neighbour. */
    @GameTest(template = "empty")
    public static void heavyOrbLookOffset(GameTestHelper helper) {
        assertOffset(helper, 0, 0, -1, 0, 0, -1);             // north
        assertOffset(helper, 0, 0, 1, 0, 0, 1);               // south
        assertOffset(helper, 1, 0, 0, 1, 0, 0);               // east
        assertOffset(helper, -1, 0, 0, -1, 0, 0);             // west
        assertOffset(helper, 0.707, 0, -0.707, 1, 0, -1);     // northeast (flat diagonal)
        assertOffset(helper, -0.707, 0, 0.707, -1, 0, 1);     // southwest
        assertOffset(helper, 0, -1, 0, 0, -1, 0);             // straight down
        assertOffset(helper, 0, -0.707, -0.707, 0, -1, -1);   // down + north
        assertOffset(helper, 0.577, -0.577, -0.577, 1, -1, -1); // down + northeast corner
        assertOffset(helper, 0, -0.3, -0.954, 0, 0, -1);      // mild down → level forward
        assertOffset(helper, 0, 0.5, -0.866, 0, 0, -1);       // shallow up → level forward (never upward)
        helper.succeed();
    }

    private static void assertOffset(GameTestHelper helper, double lx, double ly, double lz, int ex, int ey, int ez) {
        int[] o = HeavyOrbItem.lookOffset(new Vec3(lx, ly, lz));
        if (o[0] != ex || o[1] != ey || o[2] != ez) {
            helper.fail("look (" + lx + "," + ly + "," + lz + ") -> ["
                    + o[0] + "," + o[1] + "," + o[2] + "], expected [" + ex + "," + ey + "," + ez + "]");
        }
    }

    private static void assertRegion(GameTestHelper helper, LeadedGlassFrame frame, double u, double v, int expected) {
        int got = frame.regionAt(u, v);
        if (got != expected) {
            helper.fail(frame + " at (" + u + "," + v + ") = region " + got + ", expected " + expected);
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

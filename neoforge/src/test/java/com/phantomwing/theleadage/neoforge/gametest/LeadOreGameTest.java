package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadWeightBlock;
import com.phantomwing.theleadage.block.custom.LeadWeightTransforms;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.entity.custom.LeadWeightEntity;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.item.custom.LeadWeightItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

    /** The IronBarsBlock mixin: bars/panes attach to a wall leaded glass pane, but not floor panes, and vanilla still works. */
    @GameTest(template = "empty")
    public static void barsConnectToLeadedGlass(GameTestHelper helper) {
        IronBarsBlock bars = (IronBarsBlock) ModBlocks.LEAD_BARS.get();
        BlockState wallPane = ModBlocks.LEADED_GLASS_PANEL.get().defaultBlockState(); // FACE = WALL by default
        if (!bars.attachsTo(wallPane, false)) {
            helper.fail("bars don't attach to a wall leaded glass pane — the IronBarsBlock mixin didn't apply");
        }
        // Only wall panes anchor; a floor-mounted pane must not connect.
        if (bars.attachsTo(wallPane.setValue(LeadedGlassPaneBlock.FACE, AttachFace.FLOOR), false)) {
            helper.fail("bars wrongly attach to a floor-mounted leaded glass pane");
        }
        // Vanilla behaviour intact: bars still attach to other bars.
        if (!bars.attachsTo(Blocks.IRON_BARS.defaultBlockState(), false)) {
            helper.fail("bars no longer attach to iron bars — the mixin broke vanilla connection");
        }
        helper.succeed();
    }

    /** A lone leaded glass door splits back into a lead door (result) + its pane, design intact (remaining). */
    @GameTest(template = "empty")
    public static void doorRecipeSplits(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getServer().getRecipeManager();
        LeadedGlassConfig config = new LeadedGlassConfig(LeadedGlassFrame.SPLIT_H, List.of(14, 11)); // red | blue
        ItemStack door = new ItemStack(ModItems.LEADED_GLASS_DOOR.get());
        door.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), config);
        CraftingInput input = CraftingInput.of(1, 1, List.of(door));

        Optional<RecipeHolder<CraftingRecipe>> match = recipes.getRecipeFor(RecipeType.CRAFTING, input, level);
        if (match.isEmpty()) {
            helper.fail("No crafting recipe matched a lone leaded glass door");
            return;
        }
        CraftingRecipe recipe = match.get().value();
        ItemStack result = recipe.assemble(input, level.registryAccess());
        if (!result.is(ModItems.LEAD_DOOR.get())) {
            helper.fail("Split result was " + result + ", expected lead_door");
            return;
        }
        ItemStack pane = recipe.getRemainingItems(input).get(0);
        LeadedGlassConfig got = pane.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (!pane.is(ModItems.LEADED_GLASS_PANE_SPLIT.get())
                || got == null || got.frame() != LeadedGlassFrame.SPLIT_H || !got.colors().equals(config.colors())) {
            helper.fail("Returned pane was " + pane + " with config " + got + ", expected a split pane keeping red|blue");
            return;
        }
        helper.succeed();
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

    /** Heavy weight transforms are loaded from the mod's datapack and resolve block + (non-)matches. */
    @GameTest(template = "empty")
    public static void leadWeightTransformsFromData(GameTestHelper helper) {
        var cracked = LeadWeightTransforms.transform(Blocks.STONE_BRICKS.defaultBlockState());
        var dirt = LeadWeightTransforms.transform(Blocks.GRASS_BLOCK.defaultBlockState());
        if (cracked == null || !cracked.is(Blocks.CRACKED_STONE_BRICKS)) {
            helper.fail("stone_bricks -> " + cracked + ", expected cracked_stone_bricks");
        } else if (dirt == null || !dirt.is(Blocks.DIRT)) {
            helper.fail("grass_block -> " + dirt + ", expected dirt");
        } else if (LeadWeightTransforms.transform(Blocks.STONE.defaultBlockState()) != null) {
            helper.fail("plain stone should not transform");
        } else {
            helper.succeed();
        }
    }

    /** A weight degrades tier-by-tier; the last tier has no next, so it shatters. */
    @GameTest(template = "empty")
    public static void leadWeightTierChain(GameTestHelper helper) {
        Block base = ModBlocks.LEAD_WEIGHT.get();
        Block chipped = ModBlocks.CHIPPED_LEAD_WEIGHT.get();
        Block damaged = ModBlocks.DAMAGED_LEAD_WEIGHT.get();
        if (ModBlocks.nextWeightTier(base) != chipped) {
            helper.fail("lead_weight should chip to chipped_lead_weight");
        } else if (ModBlocks.nextWeightTier(chipped) != damaged) {
            helper.fail("chipped should chip to damaged");
        } else if (ModBlocks.nextWeightTier(damaged) != null) {
            helper.fail("damaged should have no next tier (it shatters)");
        } else {
            helper.succeed();
        }
    }

    /** The chip chance is 0 for short drops, rises with fall height, and is capped past the max fall. */
    @GameTest(template = "empty")
    public static void leadWeightBreakChance(GameTestHelper helper) {
        if (LeadWeightBlock.breakChance(1.0) != 0.0 || LeadWeightBlock.breakChance(2.0) != 0.0) {
            helper.fail("short drops (<= 2 blocks) must never chip the weight");
        } else if (!(LeadWeightBlock.breakChance(6.5) > 0.0
                && LeadWeightBlock.breakChance(6.5) < LeadWeightBlock.breakChance(10.0))) {
            helper.fail("chip chance must rise with fall height");
        } else if (LeadWeightBlock.breakChance(12.0) != 1.0 || LeadWeightBlock.breakChance(50.0) != 1.0) {
            helper.fail("chip chance must reach 100% at a high fall and stay capped");
        } else {
            helper.succeed();
        }
    }

    /** An weight that falls onto a hopper is collected by it as an item, not left as a block on top. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void leadWeightDropsIntoHopper(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.HOPPER);  // hopper on the floor
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR);     // carve the drop column out of the barrier
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
        BlockPos spawn = helper.absolutePos(new BlockPos(1, 2, 1));
        LeadWeightEntity.inAir(helper.getLevel(), spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                ModBlocks.LEAD_WEIGHT.get().defaultBlockState(), null);
        helper.startSequence()
                // Poll until the hopper has it, rather than guessing how long the fall + collect takes.
                .thenWaitUntil(() -> {
                    if (!hopperHasOrb(helper)) {
                        helper.fail("hopper has not collected the weight yet");
                    }
                })
                .thenExecute(() -> {
                    if (helper.getBlockState(new BlockPos(1, 1, 1)).is(ModBlocks.LEAD_WEIGHT.get())) {
                        helper.fail("weight placed itself as a block on the hopper instead of being collected");
                    }
                })
                .thenSucceed();
    }

    private static boolean hopperHasOrb(GameTestHelper helper) {
        if (!(helper.getBlockEntity(new BlockPos(1, 0, 1)) instanceof HopperBlockEntity hopper)) {
            return false;
        }
        for (int i = 0; i < hopper.getContainerSize(); i++) {
            if (hopper.getItem(i).is(ModItems.LEAD_WEIGHT.get())) {
                return true;
            }
        }
        return false;
    }

    /** An weight hung directly under a vertical chain stays put — the chain anchors it. */
    @GameTest(template = "empty")
    public static void leadWeightHangsFromVerticalChain(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 3, 1), ModBlocks.LEAD_CHAIN.get());  // vertical (axis Y is the default)
        helper.setBlock(new BlockPos(1, 2, 1),
                ModBlocks.LEAD_WEIGHT.get().defaultBlockState().setValue(BlockStateProperties.HANGING, true));
        helper.startSequence()
                .thenIdle(5)  // let the scheduled FallingBlock tick run its canHang check
                .thenExecute(() -> {
                    BlockState s = helper.getBlockState(new BlockPos(1, 2, 1));
                    if (!s.is(ModBlocks.LEAD_WEIGHT.get()) || !s.getValue(BlockStateProperties.HANGING)) {
                        helper.fail("weight did not stay hanging from the vertical chain above it");
                    }
                })
                .thenSucceed();
    }

    /** A horizontal chain is not an anchor: an weight under one detaches (stops hanging). */
    @GameTest(template = "empty")
    public static void leadWeightDetachesFromHorizontalChain(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 3, 1), ModBlocks.LEAD_CHAIN.get().defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.X));
        helper.setBlock(new BlockPos(1, 2, 1),
                ModBlocks.LEAD_WEIGHT.get().defaultBlockState().setValue(BlockStateProperties.HANGING, true));
        helper.startSequence()
                .thenWaitUntil(() -> {
                    BlockState s = helper.getBlockState(new BlockPos(1, 2, 1));
                    if (s.is(ModBlocks.LEAD_WEIGHT.get()) && s.getValue(BlockStateProperties.HANGING)) {
                        helper.fail("weight is still hanging from a horizontal chain");
                    }
                })
                .thenSucceed();
    }

    /** The lead weight aims at the 8-way adjacent direction; a near-vertical look uses the body facing. */
    @GameTest(template = "empty")
    public static void leadWeightAimDirection(GameTestHelper helper) {
        assertDir(helper, Direction.NORTH, 0, 0, -1, 0, -1);        // look north
        assertDir(helper, Direction.NORTH, 1, 0, 0, 1, 0);         // look east
        assertDir(helper, Direction.NORTH, -1, 0, 0, -1, 0);       // look west
        assertDir(helper, Direction.NORTH, 0, 0, 1, 0, 1);         // look south
        assertDir(helper, Direction.NORTH, 0.707, 0, -0.707, 1, -1); // northeast diagonal
        assertDir(helper, Direction.NORTH, -0.707, 0, 0.707, -1, 1); // southwest diagonal
        assertDir(helper, Direction.NORTH, 0, -0.9, -0.44, 0, -1);  // down-forward → north (horizontal kept)
        assertDir(helper, Direction.NORTH, 0, 0.7, -0.71, 0, -1);   // up-forward → north (up isn't refused)
        assertDir(helper, Direction.EAST, 0.05, -0.998, 0, 1, 0);   // near-straight-down → body facing (east)
        helper.succeed();
    }

    private static void assertDir(GameTestHelper helper, Direction facing, double lx, double ly, double lz, int ex, int ez) {
        int[] d = LeadWeightItem.aimDirection(facing, new Vec3(lx, ly, lz));
        if (d[0] != ex || d[1] != ez) {
            helper.fail("look (" + lx + "," + ly + "," + lz + ") facing " + facing + " -> ["
                    + d[0] + "," + d[1] + "], expected [" + ex + "," + ez + "]");
        }
    }

    /** The drop height follows the look pitch: eye level (1), a block up (2), or a block down (0). */
    @GameTest(template = "empty")
    public static void leadWeightVerticalOffset(GameTestHelper helper) {
        assertVert(helper, 0, 0, -1, 1);          // level → eye level
        assertVert(helper, 0, 0.26, -0.966, 1);   // shallow up → still eye level
        assertVert(helper, 0, -0.26, -0.966, 1);  // shallow down → still eye level
        assertVert(helper, 0, 0.5, -0.866, 2);    // ~30° up → a block higher
        assertVert(helper, 0, -0.5, -0.866, 0);   // ~30° down → a block lower
        assertVert(helper, 0, 0.87, -0.5, 2);     // steep up → capped one above
        assertVert(helper, 0, -0.87, -0.5, 0);    // steep down → capped one below
        assertVert(helper, 0.05, 0.998, 0, 2);    // near-straight-up → above
        assertVert(helper, 0.05, -0.998, 0, 0);   // near-straight-down → below
        helper.succeed();
    }

    private static void assertVert(GameTestHelper helper, double lx, double ly, double lz, int expected) {
        int dy = LeadWeightItem.verticalOffset(new Vec3(lx, ly, lz));
        if (dy != expected) {
            helper.fail("look (" + lx + "," + ly + "," + lz + ") -> dy " + dy + ", expected " + expected);
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

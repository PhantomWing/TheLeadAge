package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.HeavyOrbTransforms;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.block.entity.HeavyOrbBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.entity.custom.HeavyOrbEntity;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.item.custom.HeavyOrbItem;
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
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void heavyOrbWearsOnLanding(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);  // floor
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR);    // carve the drop column out of the barrier
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
        BlockPos spawn = helper.absolutePos(new BlockPos(1, 2, 1));
        HeavyOrbEntity.inAir(helper.getLevel(), spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                ModBlocks.HEAVY_ORB.get().defaultBlockState(), null, 30);
        helper.startSequence()
                // Poll until the orb has actually settled, rather than guessing a fixed idle.
                .thenWaitUntil(() -> {
                    if (landedOrbWear(helper) == null) {
                        helper.fail("orb has not settled yet");
                    }
                })
                .thenExecute(() -> {
                    int expected = 30 + HeavyOrbItem.WEAR_PER_LANDING;
                    int got = landedOrbWear(helper);
                    if (got != expected) {
                        helper.fail("returned orb wear = " + got + ", expected " + expected);
                    }
                })
                .thenSucceed();
    }

    /** A worn orb returned by a landing — as the block it became, or the item if it couldn't place. Null until it settles. */
    private static Integer landedOrbWear(GameTestHelper helper) {
        for (int y = 2; y >= 0; y--) {
            if (helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(1, y, 1)))
                    instanceof HeavyOrbBlockEntity be) {
                return be.getDamage();
            }
        }
        AABB area = AABB.ofSize(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))), 5, 5, 5);
        for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, area)) {
            if (item.getItem().is(ModItems.HEAVY_ORB.get())) {
                return item.getItem().getDamageValue();
            }
        }
        return null;
    }

    /** An orb that falls onto a hopper is collected by it as an item, not left as a block on top. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void heavyOrbDropsIntoHopper(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.HOPPER);  // hopper on the floor
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR);     // carve the drop column out of the barrier
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
        BlockPos spawn = helper.absolutePos(new BlockPos(1, 2, 1));
        HeavyOrbEntity.inAir(helper.getLevel(), spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                ModBlocks.HEAVY_ORB.get().defaultBlockState(), null, 0);
        helper.startSequence()
                // Poll until the hopper has it, rather than guessing how long the fall + collect takes.
                .thenWaitUntil(() -> {
                    if (!hopperHasOrb(helper)) {
                        helper.fail("hopper has not collected the orb yet");
                    }
                })
                .thenExecute(() -> {
                    if (helper.getBlockState(new BlockPos(1, 1, 1)).is(ModBlocks.HEAVY_ORB.get())) {
                        helper.fail("orb placed itself as a block on the hopper instead of being collected");
                    }
                })
                .thenSucceed();
    }

    private static boolean hopperHasOrb(GameTestHelper helper) {
        if (!(helper.getBlockEntity(new BlockPos(1, 0, 1)) instanceof HopperBlockEntity hopper)) {
            return false;
        }
        for (int i = 0; i < hopper.getContainerSize(); i++) {
            if (hopper.getItem(i).is(ModItems.HEAVY_ORB.get())) {
                return true;
            }
        }
        return false;
    }

    /** An orb hung directly under a vertical chain stays put — the chain anchors it. */
    @GameTest(template = "empty")
    public static void heavyOrbHangsFromVerticalChain(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 3, 1), ModBlocks.LEAD_CHAIN.get());  // vertical (axis Y is the default)
        helper.setBlock(new BlockPos(1, 2, 1),
                ModBlocks.HEAVY_ORB.get().defaultBlockState().setValue(BlockStateProperties.HANGING, true));
        helper.startSequence()
                .thenIdle(5)  // let the scheduled FallingBlock tick run its canHang check
                .thenExecute(() -> {
                    BlockState s = helper.getBlockState(new BlockPos(1, 2, 1));
                    if (!s.is(ModBlocks.HEAVY_ORB.get()) || !s.getValue(BlockStateProperties.HANGING)) {
                        helper.fail("orb did not stay hanging from the vertical chain above it");
                    }
                })
                .thenSucceed();
    }

    /** A horizontal chain is not an anchor: an orb under one detaches (stops hanging). */
    @GameTest(template = "empty")
    public static void heavyOrbDetachesFromHorizontalChain(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 3, 1), ModBlocks.LEAD_CHAIN.get().defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.X));
        helper.setBlock(new BlockPos(1, 2, 1),
                ModBlocks.HEAVY_ORB.get().defaultBlockState().setValue(BlockStateProperties.HANGING, true));
        helper.startSequence()
                .thenWaitUntil(() -> {
                    BlockState s = helper.getBlockState(new BlockPos(1, 2, 1));
                    if (s.is(ModBlocks.HEAVY_ORB.get()) && s.getValue(BlockStateProperties.HANGING)) {
                        helper.fail("orb is still hanging from a horizontal chain");
                    }
                })
                .thenSucceed();
    }

    /** The heavy orb's air-drop point: a fixed reach ahead of the eyes, biased to eye level. */
    @GameTest(template = "empty")
    public static void heavyOrbDropPosition(GameTestHelper helper) {
        // Eye at the centre of block (0,1,0) — so eye level = y 1, feet = y 0, below feet = y -1.
        assertDrop(helper, 0, 0, -1, 0, 1, -1);               // north, level → one ahead, eye level
        assertDrop(helper, 1, 0, 0, 1, 1, 0);                 // east, level → eye level
        assertDrop(helper, 0.707, 0, -0.707, 1, 1, -1);       // northeast (flat diagonal) → eye level
        assertDrop(helper, 0, -0.5, -0.866, 0, 1, -1);        // 30° down → still eye level (favours eye level)
        assertDrop(helper, 0, -0.707, -0.707, 0, 0, -1);      // 45° down → drops to feet level, still ahead
        assertDrop(helper, 0, -0.94, -0.34, 0, -1, -1);       // steep down → below feet but still one ahead, in view
        assertDrop(helper, 0.1, -0.995, 0, 0, -1, 0);         // near-vertical → straight down at the feet
        assertDrop(helper, 0, 0.5, -0.866, 0, 1, -1);         // shallow up → eye level (never descends for up looks)
        helper.succeed();
    }

    private static void assertDrop(GameTestHelper helper, double lx, double ly, double lz, int ex, int ey, int ez) {
        Vec3 p = HeavyOrbItem.dropPosition(new Vec3(0.5, 1.62, 0.5), 0.0, new Vec3(lx, ly, lz));
        BlockPos b = BlockPos.containing(p);
        if (b.getX() != ex || b.getY() != ey || b.getZ() != ez) {
            helper.fail("look (" + lx + "," + ly + "," + lz + ") -> " + p + " block ["
                    + b.getX() + "," + b.getY() + "," + b.getZ() + "], expected [" + ex + "," + ey + "," + ez + "]");
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

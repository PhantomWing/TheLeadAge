package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadWeightBlock;
import com.phantomwing.theleadage.block.custom.LeadWeightTransforms;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.effect.LeadFumes;
import com.phantomwing.theleadage.entity.custom.LeadWeightEntity;
import com.phantomwing.theleadage.attribute.ModAttributes;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.item.custom.LeadWeightItem;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.entity.EntityType;
import com.phantomwing.theleadage.block.custom.LeadedGlassPlacement;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Optional;

/**
 * Game tests for the "lead fumes" mechanic ({@code LeadOreBlock#playerDestroy} and {@link LeadFumes}):
 * a non-silk-touch harvest sometimes doses the player with Lead Sickness; silk touch never does.
 * Registered through {@link GameTestRegistration} (1.21.5 registry-based gametests).
 * Run headless with {@code ./gradlew :neoforge:runGameTest}.
 *
 * <p>The fumes are random, so the tests mine many times and assert on the count.
 * With {@value #TRIALS} trials the "sometimes" bounds fail with probability ~1e-15,
 * so they're deterministic in practice.</p>
 *
 * <p><b>The first dose is Hunger, not Nausea.</b> Lead Sickness is a ladder — Hunger, then
 * +Weakness, then +Poison +Nausea — so a single exposure only ever produces Hunger. These tests
 * therefore probe for {@link MobEffects#HUNGER} as the marker that a dose landed, and
 * {@link #leadSicknessLadderEscalates} covers the ladder itself.</p>
 */
public class LeadOreGameTest {
    private static final int TRIALS = 100;

    /** Every effect Lead Sickness can apply — cleared between trials to keep them independent. */
    private static final List<Holder<MobEffect>> SICKNESS_EFFECTS =
            List.of(MobEffects.HUNGER, MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.NAUSEA);

    /** Lead ore doses the player sometimes (but not on every break). */
    public static void leadOreSometimesGivesLeadSickness(GameTestHelper helper) {
        int count = countDoses(helper, ModBlocks.LEAD_ORE.get(), false);
        if (count > 0 && count < TRIALS) {
            helper.succeed();
        } else {
            helper.fail(Component.literal("Expected lead ore to dose sometimes but not always (got " + count + "/" + TRIALS + ")"));
        }
    }

    /** Same for deepslate lead ore. */
    public static void deepslateLeadOreSometimesGivesLeadSickness(GameTestHelper helper) {
        int count = countDoses(helper, ModBlocks.DEEPSLATE_LEAD_ORE.get(), false);
        if (count > 0 && count < TRIALS) {
            helper.succeed();
        } else {
            helper.fail(Component.literal("Expected deepslate lead ore to dose sometimes but not always (got " + count + "/" + TRIALS + ")"));
        }
    }

    /** Silk touch yields the ore block, not raw lead, so it NEVER gives fumes. */
    public static void silkTouchNeverGivesLeadSickness(GameTestHelper helper) {
        int count = countDoses(helper, ModBlocks.LEAD_ORE.get(), true);
        if (count == 0) {
            helper.succeed();
        } else {
            helper.fail(Component.literal("Silk touch should never dose the player (got " + count + "/" + TRIALS + ")"));
        }
    }

    /**
     * The sickness ladder: one dose is Hunger only, a second adds Weakness, a third adds Poison and
     * Nausea — and it stops there rather than climbing further. Exercises {@link LeadFumes#escalate}
     * directly, so it is deterministic (no distance/chance roll involved).
     */
    public static void leadSicknessLadderEscalates(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        clearSickness(player);

        LeadFumes.escalate(player);
        if (!player.hasEffect(MobEffects.HUNGER)) {
            helper.fail(Component.literal("first dose should apply Hunger"));
            return;
        }
        if (player.hasEffect(MobEffects.WEAKNESS) || player.hasEffect(MobEffects.POISON)) {
            helper.fail(Component.literal("first dose should be Hunger ONLY"));
            return;
        }

        LeadFumes.escalate(player);
        if (!player.hasEffect(MobEffects.WEAKNESS)) {
            helper.fail(Component.literal("second dose should add Weakness"));
            return;
        }
        if (player.hasEffect(MobEffects.POISON)) {
            helper.fail(Component.literal("second dose should not reach Poison yet"));
            return;
        }

        LeadFumes.escalate(player);
        if (!player.hasEffect(MobEffects.POISON) || !player.hasEffect(MobEffects.NAUSEA)) {
            helper.fail(Component.literal("third dose should add Poison AND Nausea"));
            return;
        }

        // Capped: a fourth dose refreshes the stack but must not invent a new stage.
        LeadFumes.escalate(player);
        if (!player.hasEffect(MobEffects.HUNGER) || !player.hasEffect(MobEffects.WEAKNESS)
                || !player.hasEffect(MobEffects.POISON) || !player.hasEffect(MobEffects.NAUSEA)) {
            helper.fail(Component.literal("a dose past stage 3 should refresh the whole stack"));
            return;
        }
        helper.succeed();
    }

    /** Lead Door + a leaded glass pane must resolve to the leaded glass door recipe. */
    public static void doorRecipeCombines(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getServer().getRecipeManager();
        // A lead door + two panes (first pane in reading order = the door's top half).
        CraftingInput input = CraftingInput.of(1, 3, List.of(
                new ItemStack(ModItems.LEAD_DOOR.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANEL.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANEL.get())));
        Optional<RecipeHolder<CraftingRecipe>> match = recipes.getRecipeFor(RecipeType.CRAFTING, input, level);
        if (match.isEmpty()) {
            helper.fail(Component.literal("No crafting recipe matched lead_door + two leaded glass panes"));
            return;
        }
        ItemStack result = match.get().value().assemble(input, level.registryAccess());
        if (result.is(ModItems.LEADED_GLASS_DOOR.get())) {
            helper.succeed();
        } else {
            helper.fail(Component.literal("Matched " + match.get().id() + " but result was " + result));
        }
    }

    /**
     * A 2x2 of lead ingots yields ONE Lead Bricks block, the vanilla bricks / nether-bricks ratio for
     * an item-to-block recipe. It read 4 for a long time (copied from the block-to-block cut-lead line
     * above it), which made bricks as cheap as the ingots themselves.
     */
    public static void leadBricksRecipeYieldsOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack ingot = new ItemStack(ModItems.LEAD_INGOT.get());
        CraftingInput input = CraftingInput.of(2, 2, List.of(ingot, ingot, ingot, ingot));
        Optional<RecipeHolder<CraftingRecipe>> match =
                level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (match.isEmpty()) {
            helper.fail(Component.literal("no crafting recipe matched a 2x2 of lead ingots"));
            return;
        }
        ItemStack result = match.get().value().assemble(input, level.registryAccess());
        if (!result.is(ModItems.LEAD_BRICKS.get())) {
            helper.fail(Component.literal("2x2 lead ingots produced " + result + ", expected lead bricks"));
            return;
        }
        if (result.getCount() != 1) {
            helper.fail(Component.literal("expected 1 lead bricks from 4 ingots, got " + result.getCount()));
            return;
        }
        helper.succeed();
    }

    /** The IronBarsBlock mixin: bars/panes attach to a wall leaded glass pane, but not floor panes, and vanilla still works. */
    public static void barsConnectToLeadedGlass(GameTestHelper helper) {
        IronBarsBlock bars = (IronBarsBlock) ModBlocks.LEAD_BARS.get();
        BlockState wallPane = ModBlocks.LEADED_GLASS_PANEL.get().defaultBlockState(); // FACE = WALL by default
        if (!bars.attachsTo(wallPane, false)) {
            helper.fail(Component.literal("bars don't attach to a wall leaded glass pane, so the IronBarsBlock mixin didn't apply"));
        }
        // Only wall panes anchor; a floor-mounted pane must not connect.
        if (bars.attachsTo(wallPane.setValue(LeadedGlassPaneBlock.FACE, AttachFace.FLOOR), false)) {
            helper.fail(Component.literal("bars wrongly attach to a floor-mounted leaded glass pane"));
        }
        // Vanilla behaviour intact: bars still attach to other bars.
        if (!bars.attachsTo(Blocks.IRON_BARS.defaultBlockState(), false)) {
            helper.fail(Component.literal("bars no longer attach to iron bars, so the mixin broke vanilla connection"));
        }
        helper.succeed();
    }

    /** The hit→region mapping must match each came's model layout (u: left→right, v: bottom→top). */
    public static void frameRegionMapping(GameTestHelper helper) {
        assertRegion(helper, LeadedGlassFrame.SPLIT_H, 0.25, 0.5, 0);   // left
        assertRegion(helper, LeadedGlassFrame.SPLIT_H, 0.75, 0.5, 1);   // right
        assertRegion(helper, LeadedGlassFrame.SPLIT_V, 0.5, 0.75, 0);   // top
        assertRegion(helper, LeadedGlassFrame.SPLIT_V, 0.5, 0.25, 1);   // bottom
        assertRegion(helper, LeadedGlassFrame.PLUS, 0.25, 0.75, 0);     // TL
        assertRegion(helper, LeadedGlassFrame.PLUS, 0.75, 0.75, 1);     // TR
        assertRegion(helper, LeadedGlassFrame.PLUS, 0.25, 0.25, 2);     // BL
        assertRegion(helper, LeadedGlassFrame.PLUS, 0.75, 0.25, 3);     // BR
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_A, 0.2, 0.8, 0); // "/" upper-left
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_A, 0.8, 0.2, 1); // "/" lower-right
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_B, 0.8, 0.8, 0); // "\" upper-right
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_B, 0.2, 0.2, 1); // "\" lower-left
        assertRegion(helper, LeadedGlassFrame.CROSS, 0.5, 0.9, 0);      // top
        assertRegion(helper, LeadedGlassFrame.CROSS, 0.9, 0.5, 1);      // right
        assertRegion(helper, LeadedGlassFrame.CROSS, 0.5, 0.1, 2);      // bottom
        assertRegion(helper, LeadedGlassFrame.CROSS, 0.1, 0.5, 3);      // left
        // 3×3 grid: row-major from the top-left (0..2 top, 3..5 middle, 6..8 bottom)
        assertRegion(helper, LeadedGlassFrame.GRID, 0.17, 0.83, 0);
        assertRegion(helper, LeadedGlassFrame.GRID, 0.50, 0.83, 1);
        assertRegion(helper, LeadedGlassFrame.GRID, 0.83, 0.83, 2);
        assertRegion(helper, LeadedGlassFrame.GRID, 0.17, 0.50, 3);
        assertRegion(helper, LeadedGlassFrame.GRID, 0.50, 0.50, 4);
        assertRegion(helper, LeadedGlassFrame.GRID, 0.83, 0.50, 5);
        assertRegion(helper, LeadedGlassFrame.GRID, 0.17, 0.17, 6);
        assertRegion(helper, LeadedGlassFrame.GRID, 0.50, 0.17, 7);
        assertRegion(helper, LeadedGlassFrame.GRID, 0.83, 0.17, 8);
        // Diamond: corners outside the centre rhombus (0 TL, 1 TR, 3 BL, 4 BR), 2 = centre.
        assertRegion(helper, LeadedGlassFrame.DIAMOND, 0.1, 0.9, 0);    // top-left corner
        assertRegion(helper, LeadedGlassFrame.DIAMOND, 0.9, 0.9, 1);    // top-right corner
        assertRegion(helper, LeadedGlassFrame.DIAMOND, 0.5, 0.5, 2);    // centre
        assertRegion(helper, LeadedGlassFrame.DIAMOND, 0.1, 0.1, 3);    // bottom-left corner
        assertRegion(helper, LeadedGlassFrame.DIAMOND, 0.9, 0.1, 4);    // bottom-right corner
        // Diamond lattice: corner triangle pairs around the border, four rhombi in the middle.
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.25, 0.93, 0);  // TL corner, top side
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.75, 0.93, 1);  // TR corner, top side
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.07, 0.75, 2);  // TL corner, left side
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.5, 0.8, 3);    // north rhombus
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.93, 0.75, 4);  // TR corner, right side
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.2, 0.5, 5);    // west rhombus
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.8, 0.5, 6);    // east rhombus
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.07, 0.25, 7);  // BL corner, left side
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.5, 0.2, 8);    // south rhombus
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.93, 0.25, 9);  // BR corner, right side
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.25, 0.07, 10); // BL corner, bottom side
        assertRegion(helper, LeadedGlassFrame.LATTICE, 0.75, 0.07, 11); // BR corner, bottom side
        // Bars: three strips (0..2 left→right for _h, top→bottom for _v).
        assertRegion(helper, LeadedGlassFrame.BARS_H, 0.17, 0.5, 0);    // left
        assertRegion(helper, LeadedGlassFrame.BARS_H, 0.50, 0.5, 1);    // middle
        assertRegion(helper, LeadedGlassFrame.BARS_H, 0.83, 0.5, 2);    // right
        assertRegion(helper, LeadedGlassFrame.BARS_V, 0.5, 0.83, 0);    // top
        assertRegion(helper, LeadedGlassFrame.BARS_V, 0.5, 0.50, 1);    // middle
        assertRegion(helper, LeadedGlassFrame.BARS_V, 0.5, 0.17, 2);    // bottom
        // Diagonal bars: four "/" (or "\") strips, 0 at the top-left (top-right for B) corner.
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_BARS_A, 0.1, 0.9, 0);   // top-left triangle
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_BARS_A, 0.3, 0.6, 1);
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_BARS_A, 0.6, 0.3, 2);
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_BARS_A, 0.9, 0.1, 3);   // bottom-right triangle
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_BARS_B, 0.9, 0.9, 0);   // top-right triangle
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_BARS_B, 0.65, 0.65, 1);
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_BARS_B, 0.35, 0.35, 2);
        assertRegion(helper, LeadedGlassFrame.DIAGONAL_BARS_B, 0.1, 0.1, 3);   // bottom-left triangle
        helper.succeed();
    }

    /**
     * The glass sheet must land inside the door/trapdoor panel in every orientation.
     *
     * <p>{@link LeadedGlassPlacement} maps canonical pane space onto the block; the renderer draws with
     * that matrix and the dye/shear interaction inverts it. Note a forward-then-inverse round trip
     * would prove nothing (it only tests {@code invert()}), so this asserts the geometric invariant
     * instead: the four corners and the centre of the canonical sheet all land within the block's own
     * collision box. That catches a wrong thin axis, a missing translation or a rotation about the
     * wrong point — the ways the sheet ends up somewhere other than in its frame.</p>
     */
    public static void glassPlacementStaysInsidePanel(GameTestHelper helper) {
        BlockState door = ModBlocks.LEADED_GLASS_DOOR.get().defaultBlockState();
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (boolean open : new boolean[]{false, true}) {
                    for (DoorHingeSide hinge : DoorHingeSide.values()) {
                        assertSheetInsidePanel(helper, door
                                .setValue(DoorBlock.HALF, half)
                                .setValue(DoorBlock.FACING, facing)
                                .setValue(DoorBlock.OPEN, open)
                                .setValue(DoorBlock.HINGE, hinge));
                    }
                }
            }
        }
        BlockState trapdoor = ModBlocks.LEADED_GLASS_TRAPDOOR.get().defaultBlockState();
        for (Half half : Half.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (boolean open : new boolean[]{false, true}) {
                    assertSheetInsidePanel(helper, trapdoor
                            .setValue(TrapDoorBlock.HALF, half)
                            .setValue(TrapDoorBlock.FACING, facing)
                            .setValue(TrapDoorBlock.OPEN, open));
                }
            }
        }
        helper.succeed();
    }

    /**
     * Pins the door mirror rule. glassPlacementStaysInsidePanel cannot catch a wrong mirror bit:
     * the half-turn maps the panel box onto itself, so every corner stays inside it either way.
     * orientation() alone is identity or exactly that half-turn, so transforming the design's u axis
     * through it reports the bit directly.
     */
    public static void doorGlassMirrorMatchesFrame(GameTestHelper helper) {
        BlockState door = ModBlocks.LEADED_GLASS_DOOR.get().defaultBlockState();
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (boolean open : new boolean[]{false, true}) {
                    for (DoorHingeSide hinge : DoorHingeSide.values()) {
                        BlockState state = door
                                .setValue(DoorBlock.HALF, half)
                                .setValue(DoorBlock.FACING, facing)
                                .setValue(DoorBlock.OPEN, open)
                                .setValue(DoorBlock.HINGE, hinge);
                        AABB box = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
                        Vector3f u = LeadedGlassPlacement.orientation(state, box)
                                .transformDirection(new Vector3f(1.0f, 0.0f, 0.0f));
                        boolean halfTurn = u.x() < 0.0f;
                        // The frame model is mirror-authored for exactly right-hinge XOR open.
                        boolean expected = (hinge == DoorHingeSide.RIGHT) != open;
                        if (halfTurn != expected) {
                            helper.fail(Component.literal("door " + facing + "/" + hinge + "/open=" + open
                                    + ": half-turn=" + halfTurn + " expected=" + expected));
                            return;
                        }
                    }
                }
            }
        }
        helper.succeed();
    }
    private static void assertSheetInsidePanel(GameTestHelper helper, BlockState state) {
        AABB box = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
        Matrix4f m = LeadedGlassPlacement.orientation(state, box).mul(LeadedGlassPlacement.surface(box));
        // A hair of slack: the sheet is inset inside the frame, so it can only ever sit within the box.
        AABB allowed = box.inflate(0.02);
        double[][] corners = {{0, 0}, {1, 0}, {0, 1}, {1, 1}, {0.5, 0.5}};
        for (double[] c : corners) {
            Vector3f p = m.transformPosition(new Vector3f((float) c[0], (float) c[1], 0.5f));
            if (!allowed.contains(p.x, p.y, p.z)) {
                helper.fail(Component.literal("glass corner (" + c[0] + "," + c[1] + ") landed at " + p
                        + ", outside the panel " + box + " for " + state));
                return;
            }
        }
    }

    /** Heavy weight transforms are loaded from the mod's datapack and resolve block + (non-)matches. */
    public static void leadWeightTransformsFromData(GameTestHelper helper) {
        var cracked = LeadWeightTransforms.transform(Blocks.STONE_BRICKS.defaultBlockState());
        var dirt = LeadWeightTransforms.transform(Blocks.GRASS_BLOCK.defaultBlockState());
        if (cracked == null || !cracked.is(Blocks.CRACKED_STONE_BRICKS)) {
            helper.fail(Component.literal("stone_bricks -> " + cracked + ", expected cracked_stone_bricks"));
        } else if (dirt == null || !dirt.is(Blocks.DIRT)) {
            helper.fail(Component.literal("grass_block -> " + dirt + ", expected dirt"));
        } else if (LeadWeightTransforms.transform(Blocks.STONE.defaultBlockState()) != null) {
            helper.fail(Component.literal("plain stone should not transform"));
        } else {
            helper.succeed();
        }
    }

    /** A weight degrades tier-by-tier; the last tier has no next, so it shatters. */
    public static void leadWeightTierChain(GameTestHelper helper) {
        Block base = ModBlocks.LEAD_WEIGHT.get();
        Block chipped = ModBlocks.CHIPPED_LEAD_WEIGHT.get();
        Block damaged = ModBlocks.DAMAGED_LEAD_WEIGHT.get();
        if (ModBlocks.nextWeightTier(base) != chipped) {
            helper.fail(Component.literal("lead_weight should chip to chipped_lead_weight"));
        } else if (ModBlocks.nextWeightTier(chipped) != damaged) {
            helper.fail(Component.literal("chipped should chip to damaged"));
        } else if (ModBlocks.nextWeightTier(damaged) != null) {
            helper.fail(Component.literal("damaged should have no next tier (it shatters)"));
        } else {
            helper.succeed();
        }
    }

    /** The chip chance is 0 for short drops, rises with fall height, and is capped past the max fall. */
    public static void leadWeightBreakChance(GameTestHelper helper) {
        if (LeadWeightBlock.breakChance(1.0) != 0.0 || LeadWeightBlock.breakChance(2.0) != 0.0) {
            helper.fail(Component.literal("short drops (<= 2 blocks) must never chip the weight"));
        } else if (!(LeadWeightBlock.breakChance(6.5) > 0.0
                && LeadWeightBlock.breakChance(6.5) < LeadWeightBlock.breakChance(10.0))) {
            helper.fail(Component.literal("chip chance must rise with fall height"));
        } else if (LeadWeightBlock.breakChance(12.0) != 1.0 || LeadWeightBlock.breakChance(50.0) != 1.0) {
            helper.fail(Component.literal("chip chance must reach 100% at a high fall and stay capped"));
        } else {
            helper.succeed();
        }
    }

    /** An weight that falls onto a hopper is collected by it as an item, not left as a block on top. */
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
                        helper.fail(Component.literal("hopper has not collected the weight yet"));
                    }
                })
                .thenExecute(() -> {
                    if (helper.getBlockState(new BlockPos(1, 1, 1)).is(ModBlocks.LEAD_WEIGHT.get())) {
                        helper.fail(Component.literal("weight placed itself as a block on the hopper instead of being collected"));
                    }
                })
                .thenSucceed();
    }

    /** A dispenser sets a Lead Weight down in the cell it faces rather than throwing it as an item. */
    public static void dispenserPlacesLeadWeight(GameTestHelper helper) {
        // The template is enclosed in barriers; clear the working layer but leave y=1 as the floor,
        // so the placed weight has something to rest on and never turns into a falling entity.
        for (int x = 0; x <= 2; x++) {
            for (int y = 2; y <= 3; y++) {
                for (int z = 0; z <= 2; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }

        BlockPos dispenser = new BlockPos(1, 2, 1);
        BlockPos target = new BlockPos(2, 2, 1);
        helper.setBlock(dispenser, Blocks.DISPENSER.defaultBlockState()
                .setValue(DispenserBlock.FACING, Direction.EAST));
        DispenserBlockEntity contents = helper.getBlockEntity(dispenser, DispenserBlockEntity.class);
        contents.setItem(0, new ItemStack(ModItems.LEAD_WEIGHT.get()));
        helper.setBlock(new BlockPos(0, 2, 1), Blocks.REDSTONE_BLOCK); // powers the dispenser

        helper.startSequence()
                // Poll rather than guess the dispenser's fire delay.
                .thenWaitUntil(() -> helper.assertBlockPresent(ModBlocks.LEAD_WEIGHT.get(), target))
                .thenExecute(() -> helper.assertEntityNotPresent(EntityType.ITEM))
                .thenSucceed();
    }

    /**
     * The door/trapdoor glass renderer resolves the pane's baked model from a block state. Grid and
     * lattice go through a MULTIPART blockstate whose floor parts carry x=270/y=90 — a 90 degree
     * lay-down — so if that state is not WALL the design renders horizontally instead of upright.
     */
    public static void dynamicPanesDefaultToUpright(GameTestHelper helper) {
        for (RegistrySupplier<Block> pane : List.of(ModBlocks.LEADED_GLASS_PANE_GRID,
                ModBlocks.LEADED_GLASS_PANE_LATTICE, ModBlocks.LEADED_GLASS_PANE_CROSS)) {
            BlockState state = pane.get().defaultBlockState();
            AttachFace face = state.getValue(LeadedGlassPaneBlock.FACE);
            if (face != AttachFace.WALL) {
                helper.fail(Component.literal(pane.getId() + " default face is " + face + ", expected WALL "
                        + "(the door would render its glass lying flat)"));
            }
        }
        helper.succeed();
    }

    private static boolean hopperHasOrb(GameTestHelper helper) {
        HopperBlockEntity hopper = helper.getBlockEntity(new BlockPos(1, 0, 1), HopperBlockEntity.class);
        for (int i = 0; i < hopper.getContainerSize(); i++) {
            if (hopper.getItem(i).is(ModItems.LEAD_WEIGHT.get())) {
                return true;
            }
        }
        return false;
    }

    /** An weight hung directly under a vertical chain stays put — the chain anchors it. */
    public static void leadWeightHangsFromVerticalChain(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 3, 1), ModBlocks.LEAD_CHAIN.get());  // vertical (axis Y is the default)
        helper.setBlock(new BlockPos(1, 2, 1),
                ModBlocks.LEAD_WEIGHT.get().defaultBlockState().setValue(BlockStateProperties.HANGING, true));
        helper.startSequence()
                .thenIdle(5)  // let the scheduled FallingBlock tick run its canHang check
                .thenExecute(() -> {
                    BlockState s = helper.getBlockState(new BlockPos(1, 2, 1));
                    if (!s.is(ModBlocks.LEAD_WEIGHT.get()) || !s.getValue(BlockStateProperties.HANGING)) {
                        helper.fail(Component.literal("weight did not stay hanging from the vertical chain above it"));
                    }
                })
                .thenSucceed();
    }

    /** A horizontal chain is not an anchor: an weight under one detaches (stops hanging). */
    public static void leadWeightDetachesFromHorizontalChain(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 3, 1), ModBlocks.LEAD_CHAIN.get().defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.X));
        helper.setBlock(new BlockPos(1, 2, 1),
                ModBlocks.LEAD_WEIGHT.get().defaultBlockState().setValue(BlockStateProperties.HANGING, true));
        helper.startSequence()
                .thenWaitUntil(() -> {
                    BlockState s = helper.getBlockState(new BlockPos(1, 2, 1));
                    if (s.is(ModBlocks.LEAD_WEIGHT.get()) && s.getValue(BlockStateProperties.HANGING)) {
                        helper.fail(Component.literal("weight is still hanging from a horizontal chain"));
                    }
                })
                .thenSucceed();
    }

    /** The lead weight aims at the 8-way adjacent direction; a near-vertical look uses the body facing. */
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
            helper.fail(Component.literal("look (" + lx + "," + ly + "," + lz + ") facing " + facing + " -> ["
                    + d[0] + "," + d[1] + "], expected [" + ex + "," + ez + "]"));
        }
    }

    /** The drop height follows the look pitch: eye level (1), a block up (2), or a block down (0). */
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
            helper.fail(Component.literal("look (" + lx + "," + ly + "," + lz + ") -> dy " + dy + ", expected " + expected));
        }
    }

    private static void assertRegion(GameTestHelper helper, LeadedGlassFrame frame, double u, double v, int expected) {
        int got = frame.regionAt(u, v);
        if (got != expected) {
            helper.fail(Component.literal(frame + " at (" + u + "," + v + ") = region " + got + ", expected " + expected));
        }
    }

    /**
     * Mines {@code ore} {@value #TRIALS} times and returns how many breaks actually dosed the player.
     *
     * <p>Hunger is the probe because it is stage 1 — every dose applies it, whatever the stage. Note
     * that ALL of the sickness effects are cleared between trials, not just the one being probed: the
     * stage is derived from which effects are still active, so leaving any of them on would ratchet
     * the ladder upward and make the trials depend on each other.</p>
     */
    private static int countDoses(GameTestHelper helper, Block ore, boolean silkTouch) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 2, 1);

        // makeMockServerPlayerInLevel does NOT position the player — it lands at the world origin,
        // far outside the fumes' radius, so every trial would silently score zero. Stand it next to
        // the ore, and clear the surrounding cells: the fumes also require line of sight, and the
        // "empty" template is enclosed in barrier blocks that would otherwise block the ray.
        BlockPos stand = pos.east();
        for (int x = 0; x <= 2; x++) {
            for (int y = 2; y <= 3; y++) {
                for (int z = 0; z <= 2; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
        Vec3 standAt = Vec3.atBottomCenterOf(helper.absolutePos(stand));
        player.snapTo(standAt.x, standAt.y, standAt.z, 0.0f, 0.0f);

        int count = 0;
        for (int i = 0; i < TRIALS; i++) {
            // Fresh, full-durability tool + a clean slate so each trial is independent.
            player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe(helper, silkTouch));
            clearSickness(player);
            helper.setBlock(pos, ore);

            player.gameMode.destroyBlock(helper.absolutePos(pos));

            if (player.hasEffect(MobEffects.HUNGER)) {
                count++;
            }
        }
        return count;
    }

    /** Wipe every Lead Sickness effect, so the next dose starts the ladder from stage 0. */
    private static void clearSickness(ServerPlayer player) {
        for (Holder<MobEffect> effect : SICKNESS_EFFECTS) {
            player.removeEffect(effect);
        }
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

    /**
     * 1.21.2 bakes attribute modifiers onto the item at construction, where a careless Properties
     * handoff silently drops custom lines (ArmorItem/AnimalArmorItem ctors re-apply the material's
     * set over whatever was passed in). Locks both custom sets: armor Heaviness + horse knockback.
     */
    public static void armorKeepsCustomAttributeModifiers(GameTestHelper helper) {
        if (!hasModifier(ModItems.LEAD_HELMET.get().components(), ModAttributes.HEAVINESS.get())) {
            helper.fail(Component.literal("lead helmet lost its Heaviness modifier"));
        }
        if (!hasModifier(ModItems.LEAD_HELMET.get().components(), Attributes.ARMOR.value())) {
            helper.fail(Component.literal("lead helmet lost the material's armor modifier"));
        }
        if (!hasModifier(ModItems.LEAD_HORSE_ARMOR.get().components(), Attributes.KNOCKBACK_RESISTANCE.value())) {
            helper.fail(Component.literal("lead horse armor lost its knockback resistance modifier"));
        }
        if (!hasModifier(ModItems.LEAD_HORSE_ARMOR.get().components(), Attributes.ARMOR.value())) {
            helper.fail(Component.literal("lead horse armor lost the material's armor modifier"));
        }
        helper.succeed();
    }

    /**
     * Guards the one desync that fails silently: a test function registered with no matching
     * {@code test_instance} JSON simply never runs, and the suite still reports all-pass. (The other
     * direction, a JSON naming a function that does not exist, already fails registry load loudly.)
     */
    public static void everyTestFunctionHasAnInstance(GameTestHelper helper) {
        Registry<GameTestInstance> instances = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.TEST_INSTANCE);
        List<String> missing = GameTestRegistration.testNames().stream()
                .filter(name -> !instances.containsKey(
                        Identifier.fromNamespaceAndPath(TheLeadAge.MOD_ID, name)))
                .toList();
        if (!missing.isEmpty()) {
            helper.fail(Component.literal("test functions with no test_instance JSON, so they never run: " + missing));
            return;
        }
        helper.succeed();
    }

    /**
     * Without Farmer's Delight the Lead Knife falls back to vanilla sword properties, and that is what
     * ships on NeoForge (FD publishes no 1.21.5 build) and on Fabric without FDR. 1.21.5 moved these
     * onto the Properties, so the fallback and the FD branch each apply their own set: this pins the
     * fallback, and would fail if the FD-only knife properties ever leaked into the default path.
     */
    public static void leadKnifeFallbackKeepsSwordProperties(GameTestHelper helper) {
        DataComponentMap components = ModItems.LEAD_KNIFE.get().components();
        if (!hasModifier(components, Attributes.ATTACK_DAMAGE.value())
                || !hasModifier(components, Attributes.ATTACK_SPEED.value())) {
            helper.fail(Component.literal("lead knife lost its attack attributes"));
            return;
        }
        Weapon weapon = components.get(DataComponents.WEAPON);
        if (weapon == null || weapon.itemDamagePerAttack() != 1) {
            helper.fail(Component.literal("expected the vanilla sword weapon cost (1 durability per attack), got " + weapon));
            return;
        }
        if (components.get(DataComponents.TOOL) == null) {
            helper.fail(Component.literal("lead knife lost its tool component"));
            return;
        }
        helper.succeed();
    }

    private static boolean hasModifier(DataComponentMap components, Attribute attribute) {
        ItemAttributeModifiers modifiers = components.get(DataComponents.ATTRIBUTE_MODIFIERS);
        return modifiers != null && modifiers.modifiers().stream().anyMatch(e -> e.attribute().value() == attribute);
    }
}

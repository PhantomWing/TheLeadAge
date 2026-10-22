package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;

import java.util.Collections;
import java.util.List;

import static com.phantomwing.theleadage.gametest.TestCompat.IMMOVABLE;
import static com.phantomwing.theleadage.gametest.TestCompat.blockEntity;
import static com.phantomwing.theleadage.gametest.TestCompat.fail;

/**
 * Game tests for lead blocks: the Lead Bulb's redstone toggle, a dyed leaded glass pane tinting a beacon's beam, and
 * solid lead too heavy for a piston. Listed in {@link GameTests}.
 */
public final class LeadBlocksGameTest {
    private LeadBlocksGameTest() {
    }

    /** The Lead Bulb flips on a redstone pulse and keeps its state, as the copper bulb does. */
    public static void leadBulbTogglesOnEachPulse(GameTestHelper helper) {
        BlockPos bulb = new BlockPos(1, 2, 1);
        BlockPos power = bulb.above();
        helper.setBlock(bulb, ModBlocks.LEAD_BULB.get());
        helper.setBlock(power, Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(2, () -> {
            if (!helper.getBlockState(bulb).getValue(CopperBulbBlock.LIT)) {
                fail(helper, "A redstone pulse didn't turn the Lead Bulb on");
                return;
            }
            helper.setBlock(power, Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                if (!helper.getBlockState(bulb).getValue(CopperBulbBlock.LIT)) {
                    fail(helper, "The Lead Bulb turned off when its power went");
                    return;
                }
                helper.setBlock(power, Blocks.REDSTONE_BLOCK);
                helper.runAfterDelay(2, () -> {
                    if (helper.getBlockState(bulb).getValue(CopperBulbBlock.LIT)) {
                        fail(helper, "A second redstone pulse didn't turn the Lead Bulb off");
                    } else {
                        helper.succeed();
                    }
                });
            });
        });
    }

    /**
     * A beacon's beam through a red leaded glass pane turns red, as through a red stained glass pane: the colour
     * the modloader's beacon asks each block above it for. A clear pane leaves the beam as it is.
     */
    public static void beaconBeamTakesThePaneColor(GameTestHelper helper) {
        BlockPos beacon = new BlockPos(1, 2, 1);
        BlockPos pane = beacon.above();
        helper.setBlock(pane, ModBlocks.LEADED_GLASS_PANEL.get());
        Integer clear = TestLoader.beaconColor(helper, pane, beacon);
        blockEntity(helper, pane, LeadedGlassPanelBlockEntity.class)
                .setColors(Collections.nCopies(64, DyeColor.RED.getId()));
        Integer color = TestLoader.beaconColor(helper, pane, beacon);
        int red = 0xFF000000 | DyeColor.RED.getTextureDiffuseColor();
        if (clear != null) {
            fail(helper, "A clear leaded glass pane tints the beam #" + Integer.toHexString(clear));
        } else if (color == null || color != red) {
            fail(helper, "A red leaded glass pane tints the beam " + (color == null ? "nothing" : "#" + Integer.toHexString(color))
                    + ", not red #" + Integer.toHexString(red));
        } else {
            helper.succeed();
        }
    }

    /** Solid lead is too heavy for a piston to push. */
    public static void pistonsCantPushSolidLead(GameTestHelper helper) {
        PushReaction reaction = ModBlocks.LEAD_BLOCK.get().defaultBlockState().getPistonPushReaction();
        if (reaction == IMMOVABLE) {
            helper.succeed();
        } else {
            fail(helper, "A Block of Lead reacts to a piston with " + reaction + ", not " + IMMOVABLE);
        }
    }

    /** Lead stairs stand a blast as the lead blocks they are made of do, not as iron. */
    public static void leadStairsResistBlastsAsLead(GameTestHelper helper) {
        float brickStairs = ModBlocks.LEAD_BRICK_STAIRS.get().getExplosionResistance();
        float cutStairs = ModBlocks.CUT_LEAD_STAIRS.get().getExplosionResistance();
        if (brickStairs != ModBlocks.LEAD_BRICKS.get().getExplosionResistance()
                || cutStairs != ModBlocks.CUT_LEAD.get().getExplosionResistance()) {
            fail(helper, "Lead stairs resist blasts at " + brickStairs + " and " + cutStairs + ", their blocks at "
                    + ModBlocks.LEAD_BRICKS.get().getExplosionResistance());
        } else {
            helper.succeed();
        }
    }

    /** A lead torch on a wall drops a lead torch: the copy of vanilla's wall torch carried a torch's drop on Fabric. */
    public static void leadWallTorchDropsALeadTorch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<ItemStack> drops = Block.getDrops(ModBlocks.LEAD_WALL_TORCH.get().defaultBlockState(), level,
                helper.absolutePos(new BlockPos(1, 2, 1)), null);
        if (drops.size() == 1 && drops.get(0).is(ModItems.LEAD_TORCH.get())) {
            helper.succeed();
        } else {
            fail(helper, "A lead wall torch drops " + drops + ", not a lead torch");
        }
    }

    /** A pane in a wall is in a mob's way, as a glass pane is, and turns with a build; one on the floor is walked over. */
    public static void wallPanesBlockPathsAndTurnWithTheBuild(GameTestHelper helper) {
        BlockState wall = ModBlocks.LEADED_GLASS_PANEL.get().defaultBlockState().setValue(LeadedGlassPaneBlock.FACING, Direction.NORTH);
        if (wall.isPathfindable(PathComputationType.LAND)) {
            fail(helper, "Mobs plan paths through a leaded glass pane in a wall");
            return;
        }
        if (!wall.setValue(LeadedGlassPaneBlock.FACE, AttachFace.FLOOR).isPathfindable(PathComputationType.LAND)) {
            fail(helper, "Mobs won't walk over a leaded glass pane on the floor");
            return;
        }
        Direction turned = wall.rotate(Rotation.CLOCKWISE_90).getValue(LeadedGlassPaneBlock.FACING);
        Direction mirrored = wall.mirror(Mirror.LEFT_RIGHT).getValue(LeadedGlassPaneBlock.FACING);
        if (turned != Direction.EAST || mirrored != Direction.SOUTH) {
            fail(helper, "A north pane turned a quarter faces " + turned + " and mirrored " + mirrored + ", not east and south");
            return;
        }
        helper.succeed();
    }

    /** A pane drops as its loot table says, with its design on it. */
    public static void paneDropsKeepTheirDesign(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, ModBlocks.LEADED_GLASS_PANEL.get());
        LeadedGlassPanelBlockEntity pane = blockEntity(helper, pos, LeadedGlassPanelBlockEntity.class);
        List<Integer> red = List.of(DyeColor.RED.getId());
        pane.setColors(red);
        List<ItemStack> drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), pane);
        LeadedGlassConfig config = drops.size() == 1 ? drops.get(0).get(ModDataComponents.LEADED_GLASS_CONFIG.get()) : null;
        if (config != null && config.colors().equals(red)) {
            helper.succeed();
        } else {
            fail(helper, "A red leaded glass pane drops " + drops + ", not one red pane");
        }
    }
}

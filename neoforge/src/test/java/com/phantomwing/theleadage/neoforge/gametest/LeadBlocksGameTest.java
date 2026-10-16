package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import java.util.Collections;

import static com.phantomwing.theleadage.neoforge.gametest.TestCompat.blockEntity;
import static com.phantomwing.theleadage.neoforge.gametest.TestCompat.fail;

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
     * NeoForge's beacon asks each block above it for. A clear pane leaves the beam as it is.
     */
    public static void beaconBeamTakesThePaneColor(GameTestHelper helper) {
        BlockPos beacon = new BlockPos(1, 2, 1);
        BlockPos pane = beacon.above();
        helper.setBlock(pane, ModBlocks.LEADED_GLASS_PANEL.get());
        BlockState state = helper.getBlockState(pane);
        Integer clear = state.getBeaconColorMultiplier(helper.getLevel(), helper.absolutePos(pane), helper.absolutePos(beacon));
        blockEntity(helper, pane, LeadedGlassPanelBlockEntity.class)
                .setColors(Collections.nCopies(64, DyeColor.RED.getId()));
        Integer color = state.getBeaconColorMultiplier(helper.getLevel(), helper.absolutePos(pane), helper.absolutePos(beacon));
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
        if (reaction == PushReaction.BLOCK) {
            helper.succeed();
        } else {
            fail(helper, "A Block of Lead reacts to a piston with " + reaction + ", not BLOCK");
        }
    }
}

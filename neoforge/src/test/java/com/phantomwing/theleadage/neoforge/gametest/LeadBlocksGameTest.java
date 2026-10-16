package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.material.PushReaction;

import java.util.Collections;
import java.util.List;

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

    /** A beacon's beam through a red leaded glass pane turns red, as through a red stained glass pane. */
    public static void beaconBeamTakesThePaneColor(GameTestHelper helper) {
        BlockPos beacon = new BlockPos(1, 2, 1);
        BlockPos pane = beacon.above();
        for (int y = 4; y <= 8; y++) {
            helper.setBlock(new BlockPos(1, y, 1), Blocks.AIR);
        }
        helper.setBlock(beacon, Blocks.BEACON);
        helper.setBlock(pane, ModBlocks.LEADED_GLASS_PANEL.get());
        blockEntity(helper, pane, LeadedGlassPanelBlockEntity.class)
                .setColors(Collections.nCopies(64, DyeColor.RED.getId()));
        int red = 0xFF000000 | DyeColor.RED.getTextureDiffuseColor();
        helper.succeedWhen(() -> {
            List<BeaconBlockEntity.BeaconBeamSection> sections = blockEntity(helper, beacon, BeaconBlockEntity.class).getBeamSections();
            if (sections.size() < 2) {
                fail(helper, "The beacon's beam has " + sections.size() + " section(s) yet");
            }
            int color = sections.get(1).getColor();
            if (color != red) {
                fail(helper, "The beam through a red leaded glass pane is #" + Integer.toHexString(color)
                        + ", not red #" + Integer.toHexString(red));
            }
        });
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

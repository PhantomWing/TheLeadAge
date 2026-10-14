package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.phantomwing.theleadage.neoforge.gametest.TestCompat.fail;

/** The Lead Pressure Plate: pressed by living things only, one signal level for each. */
public final class LeadPressurePlateGameTest {
    private static final BlockPos PLATE = new BlockPos(1, 2, 1);

    private LeadPressurePlateGameTest() {
    }

    /**
     * Dropped items leave it up; three armor stands - living, and they stand still - give 3, and
     * seventeen the cap of 15. With them gone it springs back up.
     */
    public static void leadPressurePlateCountsMobs(GameTestHelper helper) {
        // The template is enclosed in barriers: open a room over a stone floor for the plate.
        for (int x = 0; x <= 2; x++) {
            for (int y = 2; y <= 3; y++) {
                for (int z = 0; z <= 2; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
        helper.setBlock(PLATE.below(), Blocks.STONE);
        helper.setBlock(PLATE, ModBlocks.LEAD_PRESSURE_PLATE.get());

        ServerLevel level = helper.getLevel();
        Vec3 centre = helper.absoluteVec(Vec3.atBottomCenterOf(PLATE));
        List<Entity> stands = new ArrayList<>();
        helper.startSequence()
                .thenExecute(() -> {
                    for (int i = 0; i < 3; i++) {
                        ItemEntity item = new ItemEntity(level, centre.x, centre.y + 0.2, centre.z, new ItemStack(Items.IRON_INGOT));
                        item.setDeltaMovement(Vec3.ZERO);
                        level.addFreshEntity(item);
                    }
                })
                .thenIdle(20)
                .thenExecute(() -> {
                    if (power(helper) != 0) {
                        fail(helper, "dropped items pressed the plate to " + power(helper));
                    }
                })
                .thenExecute(() -> addStands(level, centre, stands, 3))
                .thenWaitUntil(() -> expectPower(helper, 3))
                .thenExecute(() -> addStands(level, centre, stands, 14))
                .thenWaitUntil(() -> expectPower(helper, 15))
                .thenExecute(() -> stands.forEach(Entity::discard))
                .thenWaitUntil(() -> expectPower(helper, 0))
                .thenSucceed();
    }

    private static void addStands(ServerLevel level, Vec3 at, List<Entity> stands, int count) {
        for (int i = 0; i < count; i++) {
            ArmorStand stand = new ArmorStand(level, at.x, at.y, at.z);
            level.addFreshEntity(stand);
            stands.add(stand);
        }
    }

    private static void expectPower(GameTestHelper helper, int expected) {
        if (power(helper) != expected) {
            fail(helper, "expected the plate at " + expected + ", found " + power(helper));
        }
    }

    private static int power(GameTestHelper helper) {
        return helper.getBlockState(PLATE).getValue(WeightedPressurePlateBlock.POWER);
    }
}

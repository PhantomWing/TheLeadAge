package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static com.phantomwing.theleadage.gametest.TestCompat.fail;

/**
 * Which way the glass in a leaded glass door or trapdoor faces, in every state ({@link LeadedGlassPlacement}): the pane
 * models show the design as authored from their front and mirrored from behind, so a sheet turned the wrong way round
 * reads mirrored. The glass is one rigid sheet: a closed door shows its front to the side it was placed from, whichever
 * its hinge, and opening swings the sheet about the hinge with the panel; a trapdoor's lies face up with the design's top
 * at the hinge, and lifts or swings down about the hinge. Listed in {@link GameTests}.
 */
public final class LeadedGlassPlacementGameTest {
    private static final float TOLERANCE = 1.0e-3f;

    private LeadedGlassPlacementGameTest() {
    }

    /** Every door state's glass faces as one rigid sheet would, never mirrored, and stays inside the door. */
    public static void leadedGlassDoorGlassFacesThePlacer(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (DoorHingeSide hinge : DoorHingeSide.values()) {
                for (boolean open : new boolean[] {false, true}) {
                    for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
                        BlockState state = ModBlocks.LEADED_GLASS_DOOR.get().defaultBlockState()
                                .setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HINGE, hinge)
                                .setValue(DoorBlock.OPEN, open).setValue(DoorBlock.HALF, half);
                        // Closed, the front faces whoever placed it, who looked the way the door faces. Open, it has
                        // swung with the panel: away from a left hinge counter-clockwise seen from above, a right one clockwise.
                        Direction placer = facing.getOpposite();
                        Direction front = !open ? placer
                                : hinge == DoorHingeSide.LEFT ? placer.getCounterClockWise() : placer.getClockWise();
                        check(state, vector(front), new Vector3f(0, 1, 0), problems);
                    }
                }
            }
        }
        report(helper, problems);
    }

    /**
     * Every trapdoor state's glass: closed, face up with the design's top towards the hinge; lifted from the floor, its
     * front turned away from the room and the design upside down; swung down from the ceiling, its front to the room and
     * the design upright.
     */
    public static void leadedGlassTrapdoorGlassSwingsAboutItsHinge(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Half half : Half.values()) {
                for (boolean open : new boolean[] {false, true}) {
                    BlockState state = ModBlocks.LEADED_GLASS_TRAPDOOR.get().defaultBlockState()
                            .setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.HALF, half)
                            .setValue(TrapDoorBlock.OPEN, open);
                    Direction hinge = facing.getOpposite();
                    if (!open) {
                        check(state, new Vector3f(0, 1, 0), vector(hinge), problems);
                    } else if (half == Half.BOTTOM) {
                        check(state, vector(hinge), new Vector3f(0, -1, 0), problems);
                    } else {
                        check(state, vector(facing), new Vector3f(0, 1, 0), problems);
                    }
                }
            }
        }
        report(helper, problems);
    }

    /**
     * Compares where the matrix the renderer draws with sends the design's front and top with where they should be, that
     * its right comes out as top × front (a mirror would reverse it), and that the sheet's middle is inside the block's box.
     */
    private static void check(BlockState state, Vector3f front, Vector3f up, List<String> problems) {
        AABB box = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
        Matrix4f placement = LeadedGlassPlacement.orientation(state, box).mul(LeadedGlassPlacement.surface(box));
        Vector3f gotFront = placement.transformDirection(new Vector3f(0, 0, 1)).normalize();
        Vector3f gotUp = placement.transformDirection(new Vector3f(0, 1, 0)).normalize();
        Vector3f gotRight = placement.transformDirection(new Vector3f(1, 0, 0)).normalize();
        Vector3f middle = placement.transformPosition(new Vector3f(0.5f, 0.5f, 0.5f));
        String where = describe(state);
        if (!near(gotFront, front)) {
            problems.add(where + ": the glass's front faces " + name(gotFront) + ", not " + name(front));
        } else if (!near(gotUp, up)) {
            problems.add(where + ": the design's top points " + name(gotUp) + ", not " + name(up));
        } else if (!near(gotRight, new Vector3f(gotUp).cross(gotFront))) {
            problems.add(where + ": the design is mirrored");
        }
        if (!box.inflate(TOLERANCE).contains(middle.x, middle.y, middle.z)) {
            problems.add(where + ": the glass's middle " + middle + " is outside the block's box " + box);
        }
    }

    private static void report(GameTestHelper helper, List<String> problems) {
        if (problems.isEmpty()) {
            helper.succeed();
            return;
        }
        fail(helper, problems.size() + " states wrong: " + String.join("; ", problems.subList(0, Math.min(6, problems.size())))
                + (problems.size() > 6 ? "; ..." : ""));
    }

    private static Vector3f vector(Direction direction) {
        return new Vector3f(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    private static boolean near(Vector3f a, Vector3f b) {
        return a.distance(b) < TOLERANCE;
    }

    private static String name(Vector3f v) {
        Direction nearest = Direction.getNearest(v.x, v.y, v.z);
        return near(v, vector(nearest)) ? nearest.getSerializedName() : v.toString();
    }

    private static String describe(BlockState state) {
        return state.getValues().entrySet().stream()
                .map(e -> e.getKey().getName() + "=" + e.getValue().toString().toLowerCase(java.util.Locale.ROOT))
                .reduce((a, b) -> a + "," + b).orElse("");
    }
}

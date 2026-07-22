package com.phantomwing.theleadage.block.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Where a leaded glass sheet sits inside a door or trapdoor, as a matrix.
 *
 * <p>"Canonical pane space" is the pane model as authored: a unit cube with the design in the XY
 * plane (x = came-left→right, y = bottom→top) and the sheet thin on Z. These matrices map that space
 * onto the block, and are the <b>single source of truth</b> for the placement — the renderers apply
 * them to their {@code PoseStack}, and the dye/shear interaction inverts them to turn a click back
 * into a {@code (u, v)} on the design. Deriving the inverse by hand instead would mean two
 * transform chains that silently drift apart, and a click landing on the wrong region in exactly the
 * orientations nobody tests.</p>
 *
 * <p>Deliberately free of any client-only type ({@code PoseStack} and friends live in blaze3d), so
 * the server can use it for interaction.</p>
 */
public final class LeadedGlassPlacement {
    /** The pane sheet is 2px thick but the door/trapdoor are 3px; stretch its thin axis to match. */
    private static final float GLASS_DEPTH = 1.5f;
    /** Fraction of that depth the sheet fills, recessing the faces so they don't z-fight the frame. */
    private static final float GLASS_INSET = 0.95f;
    /** A whisker of width inset so the thin side faces sit inside the window frame. */
    private static final float GLASS_EDGE_INSET = 0.99f;
    /** The pane models sit 1px off-centre on their thin axis; the window wants them centred. */
    private static final float PANE_OFFSET = 1.0f / 16.0f;

    private LeadedGlassPlacement() {
    }

    /**
     * Canonical pane space → the slab {@code box}: centre the sheet on the box's thin axis, turn it
     * onto that plane, then fit it to the 3px window. Depends only on the box, so the trapdoor's
     * <em>item</em> renderer (which has no block state) can use it too.
     */
    public static Matrix4f surface(AABB box) {
        Direction.Axis thin = thinAxis(box);
        Matrix4f m = new Matrix4f();
        switch (thin) {
            case X -> m.translate((float) ((box.minX + box.maxX) / 2.0 - 0.5), 0.0f, 0.0f);
            case Y -> m.translate(0.0f, (float) ((box.minY + box.maxY) / 2.0 - 0.5), 0.0f);
            case Z -> m.translate(0.0f, 0.0f, (float) ((box.minZ + box.maxZ) / 2.0 - 0.5));
        }
        // ...then rotate onto the slab's plane (thin-z needs none). A flat surface (thin-y, the
        // trapdoor flap) lays down with XP -90 so its FRONT face points up; +90 would show the
        // mirror-authored back face.
        if (thin != Direction.Axis.Z) {
            m.translate(0.5f, 0.5f, 0.5f);
            if (thin == Direction.Axis.X) {
                m.rotateY((float) Math.toRadians(90.0));
            } else {
                m.rotateX((float) Math.toRadians(-90.0));
            }
            m.translate(-0.5f, -0.5f, -0.5f);
        }
        // Fit to the window: stretch the thin axis, inset the edges, and undo the pane's 1px offset.
        m.translate(0.5f, 0.5f, 0.5f);
        m.scale(GLASS_EDGE_INSET, GLASS_EDGE_INSET, GLASS_INSET * GLASS_DEPTH);
        m.translate(-0.5f, -0.5f, -0.5f);
        m.translate(0.0f, 0.0f, PANE_OFFSET);
        return m;
    }

    /**
     * The state-dependent turn applied <em>outside</em> {@link #surface}: which way the design faces
     * once the door/trapdoor is open, mirrored, or hung from the ceiling. Identity for anything else.
     */
    public static Matrix4f orientation(BlockState state, AABB box) {
        Vec3 c = box.getCenter();
        Matrix4f m = new Matrix4f().translate((float) c.x, (float) c.y, (float) c.z);
        applyOrientation(m, state);
        return m.translate((float) -c.x, (float) -c.y, (float) -c.z);
    }

    private static void applyOrientation(Matrix4f m, BlockState state) {
        if (state.getBlock() instanceof DoorBlock) {
            // A right-hinge open door uses vanilla's MIRRORED models; the collision box only says
            // where the panel is, not that the model there is mirrored. Mirror the glass to match —
            // a half-turn about the panel's vertical centre maps the box onto itself, so it can't drift.
            if (state.getValue(DoorBlock.OPEN) && state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT) {
                m.rotateY((float) Math.PI);
            }
            return;
        }
        if (!state.hasProperty(BlockStateProperties.OPEN) || !state.hasProperty(BlockStateProperties.HALF)) {
            return;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (!state.getValue(BlockStateProperties.OPEN)) {
            // Closed: surface() lays the flat design with its up pointing north; spin it so its up
            // points at the hinge (the side opposite the facing).
            float yaw = switch (facing) {
                case EAST -> 90.0f;
                case SOUTH -> 0.0f;
                case WEST -> 270.0f;
                default -> 180.0f; // NORTH
            };
            if (yaw != 0.0f) {
                m.rotateY((float) Math.toRadians(yaw));
            }
        } else if (state.getValue(BlockStateProperties.HALF) == Half.BOTTOM) {
            // Lifted up from the floor, so the room sees its former underside — vertically mirrored.
            float angle = (float) Math.PI;
            if (facing == Direction.NORTH || facing == Direction.EAST) {
                m.rotateZ(angle);
            } else {
                m.rotateX(angle);
            }
        } else if (facing == Direction.NORTH || facing == Direction.WEST) {
            // Swung down from the ceiling: E/S already read as authored; N/W show the back face.
            m.rotateY((float) Math.PI);
        }
    }

    /**
     * Which region of {@code frame} a click landed on, or {@code -1} when the click missed the design
     * (a thin edge, or outside it). {@code localHit} is the hit position relative to the block corner.
     *
     * <p>Works by inverting the very matrix the renderer draws with, so the mapping cannot disagree
     * with what the player sees.</p>
     */
    public static int regionAt(BlockState state, AABB box, LeadedGlassFrame frame, Vec3 localHit) {
        Matrix4f inverse = orientation(state, box).mul(surface(box)).invert();
        Vector3f p = inverse.transformPosition(
                new Vector3f((float) localHit.x, (float) localHit.y, (float) localHit.z));
        // Back in canonical space the design spans 0..1 on x/y; anything outside is a miss.
        if (p.x < 0.0f || p.x > 1.0f || p.y < 0.0f || p.y > 1.0f) {
            return -1;
        }
        return frame.regionAt(p.x, p.y);
    }

    /** The box's flattest axis — the one the glass sheet is thin on. */
    public static Direction.Axis thinAxis(AABB box) {
        double dx = box.maxX - box.minX, dy = box.maxY - box.minY, dz = box.maxZ - box.minZ;
        if (dy <= dx && dy <= dz) {
            return Direction.Axis.Y;
        }
        return dx <= dz ? Direction.Axis.X : Direction.Axis.Z;
    }
}

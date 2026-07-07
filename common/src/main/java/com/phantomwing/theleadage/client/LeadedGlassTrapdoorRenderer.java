package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.phantomwing.theleadage.block.entity.LeadedGlassTrapdoorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Draws the configurable glass onto a leaded glass trapdoor's flap (see {@link LeadedGlassSurface}).
 *
 * <p>The glass is positioned from the real collision box, which already sits exactly where the flap
 * is for every facing/half/open state. The box can't express which way the design should FACE,
 * though (opposite facings share one box), so the corrections below spin/flip the design in place —
 * each one maps the box onto itself, so the position can't drift.
 *
 * <ul>
 *   <li><b>Closed:</b> the flat design reads upright for a player standing on the trapdoor's open
 *       (facing) side looking toward the hinge.</li>
 *   <li><b>Open:</b> the upright design reads like that flat design lifted about its hinge — from
 *       the room side it shows a half-turn from as-authored.</li>
 * </ul>
 */
public class LeadedGlassTrapdoorRenderer implements BlockEntityRenderer<LeadedGlassTrapdoorBlockEntity> {
    public LeadedGlassTrapdoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LeadedGlassTrapdoorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        if (!state.hasProperty(BlockStateProperties.OPEN)) {
            return;
        }
        VoxelShape shape = state.getShape(be.getLevel(), be.getBlockPos());
        if (shape.isEmpty()) {
            return;
        }
        AABB box = shape.bounds();
        Vec3 centre = box.getCenter();
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        pose.pushPose();
        pose.translate(centre.x, centre.y, centre.z);
        if (!state.getValue(BlockStateProperties.OPEN)) {
            // LeadedGlassSurface lays the flat design with its up pointing north; spin it so its up
            // points at the hinge (the side opposite the facing): N 180, E 90, S 0, W 270 (CCW).
            float yaw = switch (facing) {
                case EAST -> 90f;
                case SOUTH -> 0f;
                case WEST -> 270f;
                default -> 180f; // NORTH
            };
            if (yaw != 0f) {
                pose.mulPose(Axis.YP.rotationDegrees(yaw));
            }
        } else if (state.getValue(BlockStateProperties.HALF) == Half.BOTTOM) {
            // Bottom-anchored: the flap lifted up from the floor, so the room sees its former
            // underside — the design reads vertically mirrored from as-authored. A half-turn about
            // the right horizontal axis through the panel centre lands every facing there.
            pose.mulPose((facing == Direction.NORTH || facing == Direction.EAST ? Axis.ZP : Axis.XP)
                    .rotationDegrees(180));
        } else {
            // Top-anchored: the flap swung down from the ceiling, so the room sees its former top
            // face — the design reads as-authored. E/S already come out of the box that way; N/W
            // show the back face, so turn them around their vertical centre axis.
            if (facing == Direction.NORTH || facing == Direction.WEST) {
                pose.mulPose(Axis.YP.rotationDegrees(180));
            }
        }
        pose.translate(-centre.x, -centre.y, -centre.z);
        LeadedGlassSurface.render(be.getConfig(), box, pose, buffers, light, overlay);
        pose.popPose();
    }
}

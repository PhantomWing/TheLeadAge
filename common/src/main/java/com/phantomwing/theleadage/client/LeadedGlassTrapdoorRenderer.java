package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.phantomwing.theleadage.block.entity.LeadedGlassTrapdoorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Draws the configurable glass onto a leaded glass trapdoor's flap (see LeadedGlassSurface). */
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
        VoxelShape shape = state.getShape(be.getLevel(), be.getBlockPos());
        if (shape.isEmpty()) {
            return;
        }
        pose.pushPose();
        // A closed trapdoor's collision box is identical for all four facings, so the box can't tell the
        // glass which way to face — spin the design to match the trapdoor's facing (matches the blockstate
        // y-rotation: north 0, east 90, south 180, west 270). Open trapdoors get their facing from the box.
        if (state.hasProperty(BlockStateProperties.OPEN) && !state.getValue(BlockStateProperties.OPEN)) {
            float yaw = switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case EAST -> 90f;
                case SOUTH -> 180f;
                case WEST -> 270f;
                default -> 0f; // NORTH
            };
            if (yaw != 0f) {
                pose.translate(0.5, 0.5, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(yaw));
                pose.translate(-0.5, -0.5, -0.5);
            }
        }
        LeadedGlassSurface.render(be.getConfig(), shape.bounds(), pose, buffers, light, overlay);
        pose.popPose();
    }
}

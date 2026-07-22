package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.LeadedGlassPlacement;
import com.mojang.blaze3d.vertex.PoseStack;
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

        pose.pushPose();
        // Which way the design faces (closed yaw / lifted-from-floor / swung-from-ceiling) — shared
        // with the dye/shear interaction, which inverts this same matrix. See LeadedGlassPlacement.
        pose.mulPose(LeadedGlassPlacement.orientation(state, box));
        LeadedGlassSurface.render(be.getConfig(), box, pose, buffers, light, overlay);
        pose.popPose();
    }
}

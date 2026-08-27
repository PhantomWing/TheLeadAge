package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.LeadedGlassPlacement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.phantomwing.theleadage.block.entity.LeadedGlassTrapdoorBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
 *
 * <p>1.21.9: split into extract (reads the block entity and the level) and submit (geometry only).</p>
 */
public class LeadedGlassTrapdoorRenderer implements BlockEntityRenderer<LeadedGlassTrapdoorBlockEntity, LeadedGlassSurfaceRenderState> {
    public LeadedGlassTrapdoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public LeadedGlassSurfaceRenderState createRenderState() {
        return new LeadedGlassSurfaceRenderState();
    }

    @Override
    public void extractRenderState(LeadedGlassTrapdoorBlockEntity be, LeadedGlassSurfaceRenderState state,
                                   float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, breakProgress);
        // Render states are pooled, so clear before every early return — a stale config from the
        // previous occupant of this state would otherwise keep drawing.
        state.config = null;
        state.box = null;

        if (be.getLevel() == null) {
            return;
        }
        BlockState blockState = be.getBlockState();
        if (!blockState.hasProperty(BlockStateProperties.OPEN)) {
            return;
        }
        VoxelShape shape = blockState.getShape(be.getLevel(), be.getBlockPos());
        if (shape.isEmpty()) {
            return;
        }
        state.config = be.getConfig();
        state.box = shape.bounds();
    }

    @Override
    public void submit(LeadedGlassSurfaceRenderState state, PoseStack pose,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        LeadedGlassConfig config = state.config;
        AABB box = state.box;
        if (config == null || box == null) {
            return;
        }
        pose.pushPose();
        // Which way the design faces (closed yaw / lifted-from-floor / swung-from-ceiling) — shared
        // with the dye/shear interaction, which inverts this same matrix. See LeadedGlassPlacement.
        pose.mulPose(LeadedGlassPlacement.orientation(state.blockState, box));
        LeadedGlassSurface.render(config, box, pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}

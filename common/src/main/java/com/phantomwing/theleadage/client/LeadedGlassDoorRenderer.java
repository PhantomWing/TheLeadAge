package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.LeadedGlassPlacement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.phantomwing.theleadage.block.entity.LeadedGlassDoorBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Draws the configurable glass into each half of a leaded glass door (see LeadedGlassSurface).
 *
 * <p>1.21.9: split into extract (reads the block entity and the level) and submit (geometry only).</p>
 */
public class LeadedGlassDoorRenderer implements BlockEntityRenderer<LeadedGlassDoorBlockEntity, LeadedGlassSurfaceRenderState> {
    public LeadedGlassDoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public LeadedGlassSurfaceRenderState createRenderState() {
        return new LeadedGlassSurfaceRenderState();
    }

    @Override
    public void extractRenderState(LeadedGlassDoorBlockEntity be, LeadedGlassSurfaceRenderState state,
                                   float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, breakProgress);
        // Render states are pooled, so clear before every early return — a stale config from the
        // previous occupant of this state would otherwise keep drawing.
        state.config = null;
        state.box = null;

        BlockState blockState = be.getBlockState();
        if (be.getLevel() == null || !(blockState.getBlock() instanceof DoorBlock)) {
            return;
        }
        // The upper half shows the top pane, the lower half the bottom pane.
        LeadedGlassDoorConfig config = be.getConfig();
        LeadedGlassConfig pane = blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER
                ? config.top() : config.bottom();
        VoxelShape shape = blockState.getShape(be.getLevel(), be.getBlockPos());
        if (shape.isEmpty()) {
            return;
        }
        state.config = pane;
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
        // Which way the design faces (the mirrored right-hinge case) — shared with the dye/shear
        // interaction, which inverts this same matrix. See LeadedGlassPlacement.
        pose.mulPose(LeadedGlassPlacement.orientation(state.blockState, box));
        LeadedGlassSurface.render(config, box, pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}

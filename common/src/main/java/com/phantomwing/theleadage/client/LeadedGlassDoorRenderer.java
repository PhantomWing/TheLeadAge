package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.phantomwing.theleadage.block.entity.LeadedGlassDoorBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Draws the configurable glass into each half of a leaded glass door (see LeadedGlassSurface). */
public class LeadedGlassDoorRenderer implements BlockEntityRenderer<LeadedGlassDoorBlockEntity> {
    public LeadedGlassDoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LeadedGlassDoorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (be.getLevel() == null || !(state.getBlock() instanceof DoorBlock)) {
            return;
        }
        // The upper half shows the top pane, the lower half the bottom pane.
        LeadedGlassDoorConfig config = be.getConfig();
        LeadedGlassConfig pane = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER
                ? config.top() : config.bottom();
        VoxelShape shape = state.getShape(be.getLevel(), be.getBlockPos());
        if (shape.isEmpty()) {
            return;
        }
        AABB box = shape.bounds();
        pose.pushPose();
        if (state.getValue(DoorBlock.OPEN) && state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT) {
            // A right-hinge door opens the other way using vanilla's MIRRORED door models; the
            // collision box only says where the open panel is, not that the model there is the
            // mirrored variant. Mirror the glass to match: a half-turn about the panel's vertical
            // centre axis shows the sheet's other face (the design's horizontal mirror) and maps
            // the box onto itself, so the position can't drift.
            Vec3 centre = box.getCenter();
            pose.translate(centre.x, centre.y, centre.z);
            pose.mulPose(Axis.YP.rotationDegrees(180));
            pose.translate(-centre.x, -centre.y, -centre.z);
        }
        LeadedGlassSurface.render(pane, box, pose, buffers, light, overlay);
        pose.popPose();
    }
}

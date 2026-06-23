package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.phantomwing.theleadage.block.entity.LeadedGlassDoorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Draws the configurable glass into the top half of a leaded glass door (see LeadedGlassSurface). */
public class LeadedGlassDoorRenderer implements BlockEntityRenderer<LeadedGlassDoorBlockEntity> {
    public LeadedGlassDoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LeadedGlassDoorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (be.getLevel() == null || !(state.getBlock() instanceof DoorBlock)
                || state.getValue(DoorBlock.HALF) != DoubleBlockHalf.UPPER) {
            return; // glass only on the top half
        }
        VoxelShape shape = state.getShape(be.getLevel(), be.getBlockPos());
        if (!shape.isEmpty()) {
            LeadedGlassSurface.render(be.getConfig(), shape.bounds(), pose, buffers, light, overlay);
        }
    }
}

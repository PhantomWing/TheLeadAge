package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Draws a leaded glass pane's design onto a thin slab (the leaded glass door / trapdoor window).
 * Rather than re-deriving each came layout, it renders the matching pane block's <i>baked model</i>
 * directly — so every came type (and any future one) just works — and supplies the per-region tint
 * itself (the door isn't a pane, so the usual block-colour lookup would find nothing). The slab box
 * (from the block's collision shape) decides which plane to rotate the pane sheet onto.
 */
public final class LeadedGlassSurface {
    /** The pane sheet is 2px thick but the door/trapdoor are 3px; stretch its thin axis to match so it
     *  fills the slab (only the hidden side faces move — the front/back design faces are unaffected). */
    private static final float GLASS_DEPTH = 1.5f;
    /** Fraction of that depth the sheet actually fills, recessing the front/back faces just inside the
     *  overlay so they don't z-fight it — even at distance, where depth precision drops. */
    private static final float GLASS_INSET = 0.95f;
    /** A whisker of width inset (~0.08px per edge) so the pane's thin side faces sit just inside the
     *  window frame instead of z-fighting it — too small to shrink the visible front/back design. */
    private static final float GLASS_EDGE_INSET = 0.99f;

    private LeadedGlassSurface() {
    }

    public static void render(LeadedGlassConfig config, AABB slab, PoseStack pose,
                              MultiBufferSource buffers, int light, int overlay) {
        BlockState paneState = paneStateFor(config);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(paneState);
        Direction.Axis thin = thinAxis(slab);

        pose.pushPose();
        // Centre the (canonically thin-z) pane sheet on its thin axis; the depth stretch below fills the
        // 3px slab, so a centred sheet already reaches both faces (e.g. a flat trapdoor's top).
        switch (thin) {
            case X -> pose.translate((slab.minX + slab.maxX) / 2.0 - 0.5, 0, 0);
            case Y -> pose.translate(0, (slab.minY + slab.maxY) / 2.0 - 0.5, 0);
            case Z -> pose.translate(0, 0, (slab.minZ + slab.maxZ) / 2.0 - 0.5);
        }
        // ...then rotate it onto the slab's plane (thin-z needs no rotation).
        if (thin != Direction.Axis.Z) {
            pose.translate(0.5, 0.5, 0.5);
            pose.mulPose(thin == Direction.Axis.X ? Axis.YP.rotationDegrees(90) : Axis.XP.rotationDegrees(90));
            pose.translate(-0.5, -0.5, -0.5);
        }
        // Stretch the thin axis toward 3px to fill the slab, but stop just short (GLASS_INSET) so the
        // front/back faces recess inside the overlay; a whisker of width inset (GLASS_EDGE_INSET) keeps
        // the thin side faces off the frame too. (Applied in canonical space, where the thin axis is z.)
        pose.translate(0.5, 0.5, 0.5);
        pose.scale(GLASS_EDGE_INSET, GLASS_EDGE_INSET, GLASS_INSET * GLASS_DEPTH);
        pose.translate(-0.5, -0.5, -0.5);

        VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        RandomSource random = RandomSource.create(42L);
        emit(model, paneState, config, null, buffer, pose, light, overlay, random);
        for (Direction dir : Direction.values()) {
            emit(model, paneState, config, dir, buffer, pose, light, overlay, random);
        }
        pose.popPose();
    }

    private static void emit(BakedModel model, BlockState state, LeadedGlassConfig config, Direction dir,
                             VertexConsumer buffer, PoseStack pose, int light, int overlay, RandomSource random) {
        for (BakedQuad quad : model.getQuads(state, dir, random)) {
            int color = colorFor(quad.getTintIndex(), config);
            float r = (color >> 16 & 0xFF) / 255.0f, g = (color >> 8 & 0xFF) / 255.0f, b = (color & 0xFF) / 255.0f;
            buffer.putBulkData(pose.last(), quad, r, g, b, 1.0f, light, overlay);
        }
    }

    /** Per-region tint: a region's dye colour, or white for clear/came (the came has no tintindex). */
    private static int colorFor(int tintIndex, LeadedGlassConfig config) {
        if (tintIndex < 0) {
            return 0xFFFFFF;
        }
        DyeColor dye = config.colorAt(tintIndex);
        return dye == null ? 0xFFFFFF : dye.getTextureDiffuseColor();
    }

    /** The pane block state matching this design: came type → block + orientation, colours → clear bits. */
    private static BlockState paneStateFor(LeadedGlassConfig config) {
        LeadedGlassFrame frame = config.frame();
        Block block = ModBlocks.paneBlockFor(frame);
        BlockState state = block.defaultBlockState();
        if (block instanceof LeadedGlassPaneBlock pane) {
            if (pane.cameType().orientation != null) {
                state = state.setValue(pane.cameType().orientation, pane.cameType().orientationOf(frame));
            }
            for (int i = 0; i < frame.regions(); i++) {
                state = state.setValue(LeadedGlassPaneBlock.CLEAR[i], config.colorAt(i) == null);
            }
        }
        return state;
    }

    private static Direction.Axis thinAxis(AABB box) {
        double dx = box.maxX - box.minX, dy = box.maxY - box.minY, dz = box.maxZ - box.minZ;
        if (dy <= dx && dy <= dz) {
            return Direction.Axis.Y;
        }
        return dx <= dz ? Direction.Axis.X : Direction.Axis.Z;
    }
}

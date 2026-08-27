package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.LeadedGlassPlacement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Draws a leaded glass pane's design onto a thin slab (the leaded glass door / trapdoor window).
 * Rather than re-deriving each came layout, it renders the matching pane block's <i>baked model</i>
 * directly — so every came type (and any future one) just works — and supplies the per-region tint
 * itself (the door isn't a pane, so the usual block-colour lookup would find nothing). The slab box
 * (from the block's collision shape) decides which plane to rotate the pane sheet onto.
 */
public final class LeadedGlassSurface {
    /** Cached: {@code Direction.values()} clones its array per call, and this is a per-frame path. */
    private static final Direction[] DIRECTIONS = Direction.values();

    private LeadedGlassSurface() {
    }

    public static void render(LeadedGlassConfig config, AABB slab, PoseStack pose,
                              SubmitNodeCollector collector, int light, int overlay) {
        pose.pushPose();
        // Canonical pane space -> the slab. The maths lives in LeadedGlassPlacement so the dye/shear
        // interaction can invert this exact matrix to turn a click back into a region — see its docs.
        pose.mulPose(LeadedGlassPlacement.surface(slab));
        renderUpright(config, pose, collector, light, overlay);
        pose.popPose();
    }

    /**
     * The design in canonical (upright wall, facing north) pane space, no surface transform.
     * Used directly by the pane item renderer: an item stack has no block entity, so the dynamic
     * (grid/lattice) clear-cell swap must happen here rather than in the chunk-mesh wrapper.
     */
    public static void renderUpright(LeadedGlassConfig config, PoseStack pose,
                                     SubmitNodeCollector collector, int light, int overlay) {
        BlockState paneState = paneStateFor(config);
        // 1.21.5: block models are BlockStateModels, so quads come per-part via collectParts, which
        // also replaces the old per-direction getQuads(state, dir, random) calls.
        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(paneState);
        List<BlockModelPart> parts = model.collectParts(RandomSource.create(42L));
        // 1.21.9: geometry is submitted rather than written to a buffer here, and the draw happens
        // later. The pose is snapshotted (PoseStack.Pose#copy) at submit time, so every transform
        // applied above is already baked into the Pose the callback receives.
        collector.submitCustomGeometry(pose, Sheets.translucentItemSheet(), (snapshot, buffer) -> {
            for (BlockModelPart part : parts) {
                emit(part, config, null, buffer, snapshot, light, overlay);
                for (Direction dir : DIRECTIONS) {
                    emit(part, config, dir, buffer, snapshot, light, overlay);
                }
            }
        });
    }

    private static void emit(BlockModelPart part, LeadedGlassConfig config, Direction dir,
                             VertexConsumer buffer, PoseStack.Pose pose, int light, int overlay) {
        for (BakedQuad quad : part.getQuads(dir)) {
            int tint = quad.tintIndex();
            // Clear regions must be drawn with the clear sprite, not the tintable white one. For most
            // came types the block state already picked a clear-textured model and this is a no-op,
            // but grid and lattice carry no clear_N state, and their chunk-mesh wrapper can only do the
            // swap from a block entity, which a door has none of. Without this they render solid white.
            BakedQuad out = tint >= 0 && config.colorAt(tint) == null
                    ? LeadedGlassClearSprite.retexture(quad)
                    : quad;
            int color = colorFor(tint, config);
            float r = (color >> 16 & 0xFF) / 255.0f, g = (color >> 8 & 0xFF) / 255.0f, b = (color & 0xFF) / 255.0f;
            buffer.putBulkData(pose, out, r, g, b, 1.0f, light, overlay);
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
        // Ask for the upright wall-mounted pane explicitly rather than trusting the block's default
        // state. Grid and lattice resolve through a MULTIPART blockstate (came + one model per cell),
        // where the face/facing pair decides which parts apply and how they are rotated; every other
        // came type is a single variants entry. Leaving it implicit means the door's canonical space
        // silently depends on the default, which is exactly where the two paths can diverge.
        BlockState state = block.defaultBlockState()
                .setValue(LeadedGlassPaneBlock.FACE, AttachFace.WALL)
                .setValue(LeadedGlassPaneBlock.FACING, Direction.NORTH);
        if (block instanceof LeadedGlassPaneBlock pane) {
            if (pane.cameType().orientation != null) {
                state = state.setValue(pane.cameType().orientation, pane.cameType().orientationOf(frame));
            }
            // Dynamic (cell-based) panes carry no clear_N state; their clear regions come from render
            // data, not the block state, so there is nothing to set here.
            if (!pane.cameType().isDynamic()) {
                for (int i = 0; i < frame.regions(); i++) {
                    state = state.setValue(LeadedGlassPaneBlock.CLEAR[i], config.colorAt(i) == null);
                }
            }
        }
        return state;
    }
}

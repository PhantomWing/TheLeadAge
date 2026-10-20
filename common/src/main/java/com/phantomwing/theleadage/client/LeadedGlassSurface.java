package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.LeadedGlassPlacement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draws a leaded glass pane's design onto a thin slab (the leaded glass door / trapdoor window).
 * Rather than re-deriving each came layout, it renders the matching pane block's <i>baked model</i>
 * directly — so every came type (and any future one) just works — and supplies the per-region tint
 * itself (the door isn't a pane, so the usual block-colour lookup would find nothing). The slab box
 * (from the block's collision shape) decides which plane to rotate the pane sheet onto.
 */
public final class LeadedGlassSurface {
    /** How many designs' quads are kept: as many as a build shows at once, well past it. */
    private static final int DRAWN_KEPT = 256;
    /**
     * Each design's quads, tinted and with its clear regions' sprites swapped, as the model they came from drew them:
     * the design doesn't change from frame to frame, and working them out again each frame for each door made
     * thousands of objects a frame. A model made anew by a resource reload has them worked out again.
     */
    private static final Map<LeadedGlassConfig, Drawn> DRAWN = new ConcurrentHashMap<>();

    private LeadedGlassSurface() {
    }

    private record Drawn(BakedModel model, List<BakedQuad> quads, int[] colors) {
    }

    public static void render(LeadedGlassConfig config, AABB slab, PoseStack pose,
                              MultiBufferSource buffers, int light, int overlay) {
        BlockState paneState = paneStateFor(config);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(paneState);
        Drawn drawn = DRAWN.get(config);
        if (drawn == null || drawn.model() != model) {
            if (DRAWN.size() >= DRAWN_KEPT) {
                DRAWN.clear();
            }
            drawn = draw(model, paneState, config);
            DRAWN.put(config, drawn);
        }

        pose.pushPose();
        // Canonical pane space -> the slab. The maths lives in LeadedGlassPlacement so the dye/shear
        // interaction can invert this exact matrix to turn a click back into a region — see its docs.
        pose.mulPose(LeadedGlassPlacement.surface(slab));

        VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        for (int i = 0; i < drawn.quads().size(); i++) {
            int color = drawn.colors()[i];
            float r = (color >> 16 & 0xFF) / 255.0f, g = (color >> 8 & 0xFF) / 255.0f, b = (color & 0xFF) / 255.0f;
            buffer.putBulkData(pose.last(), drawn.quads().get(i), r, g, b, 1.0f, light, overlay);
        }
        pose.popPose();
    }

    private static Drawn draw(BakedModel model, BlockState state, LeadedGlassConfig config) {
        RandomSource random = RandomSource.create(42L);
        List<BakedQuad> quads = new ArrayList<>();
        IntArrayList colors = new IntArrayList();
        collect(model, state, config, null, random, quads, colors);
        for (Direction dir : Direction.values()) {
            collect(model, state, config, dir, random, quads, colors);
        }
        return new Drawn(model, List.copyOf(quads), colors.toIntArray());
    }

    private static void collect(BakedModel model, BlockState state, LeadedGlassConfig config, Direction dir,
                                RandomSource random, List<BakedQuad> quads, IntArrayList colors) {
        for (BakedQuad quad : model.getQuads(state, dir, random)) {
            int tint = quad.getTintIndex();
            // Clear regions must be drawn with the clear sprite, not the tintable white one. For most
            // came types the block state already picked a clear-textured model and this is a no-op —
            // but grid and lattice carry no clear_N state, and their chunk-mesh wrapper can only do the
            // swap from a block entity, which a door has none of. Without this they render solid white.
            quads.add(tint >= 0 && config.colorAt(tint) == null ? LeadedGlassClearSprite.retexture(quad) : quad);
            colors.add(colorFor(tint, config));
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

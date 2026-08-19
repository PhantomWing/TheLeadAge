package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.client.LeadedGlassClearSprite;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a dynamic (grid / lattice) pane's block-state model and, at chunk-mesh time, retextures
 * whichever glass cells are CLEAR (the tinted white sprite is swapped for its untinted clear
 * counterpart) from the block entity's per-region colours. This keeps the design out of block-state
 * properties, avoiding a 2^regions explosion, while preserving the exact clear-vs-coloured look.
 * It only runs when a section is (re)built, so it adds no per-frame cost.
 *
 * <p>1.21.5: the BakedModel/ModelData pipeline is gone. This extends NeoForge's
 * {@link DelegateBlockStateModel} and does the swap in the level-aware {@code collectParts}, which
 * reads the block entity directly with no ModelData round-trip.</p>
 */
public class LeadedGlassPaneModel extends DelegateBlockStateModel {
    public LeadedGlassPaneModel(BlockStateModel base) {
        super(base);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                             RandomSource random, List<BlockModelPart> parts) {
        int start = parts.size();
        super.collectParts(level, pos, state, random, parts);
        boolean[] clear = LeadedGlassClearSprite.clearFlags(level, pos);
        // No block entity, or no clear cell: leave the authored parts alone. 1.21.5 splits the model
        // into one part per cell, so wrapping them all for a swap that can never fire would cost the
        // common (fully dyed) pane a wrapper per cell on every rebuild.
        if (clear == null || !LeadedGlassClearSprite.hasClear(clear)) {
            return;
        }
        for (int i = start; i < parts.size(); i++) {
            parts.set(i, new RetexturedPart(parts.get(i), clear));
        }
    }

    /** A part whose clear cells are retextured to the untinted clear sprite; everything else forwards. */
    private record RetexturedPart(BlockModelPart part, boolean[] clear) implements BlockModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction side) {
            List<BakedQuad> quads = part.getQuads(side);
            // The mesher asks each part for all six faces plus the unculled list, and most of those
            // lists contain no clear cell, so copy lazily: only once a quad actually changes.
            List<BakedQuad> out = null;
            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);
                BakedQuad swapped = LeadedGlassClearSprite.retexture(quad, clear);
                if (swapped == quad) {
                    continue;
                }
                if (out == null) {
                    out = new ArrayList<>(quads);
                }
                out.set(i, swapped);
            }
            return out == null ? quads : out;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return part.useAmbientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon() {
            return part.particleIcon();
        }

        // NeoForge adds these two to BlockModelPart. Their interface defaults answer from the global
        // ItemBlockRenderTypes map and from useAmbientOcclusion, so without forwarding a wrapped
        // part's own render layer or forced AO would be dropped for exactly the wrapped positions.
        @Override
        public RenderType getRenderType(BlockState state) {
            return part.getRenderType(state);
        }

        @Override
        public TriState ambientOcclusion() {
            return part.ambientOcclusion();
        }
    }
}

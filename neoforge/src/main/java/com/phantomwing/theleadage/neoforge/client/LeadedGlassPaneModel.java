package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.client.LeadedGlassClearSprite;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a dynamic (grid / lattice) pane's block-state model and, at chunk-mesh time, retextures
 * whichever glass cells are CLEAR — swapping the tinted white sprite for its untinted clear
 * counterpart — from the block entity's per-region colours. This keeps the design out of
 * block-state properties (no 2^regions explosion) while preserving the exact clear-vs-coloured
 * look. It only runs when a section is (re)built, so it adds no per-frame cost.
 *
 * <p>1.21.5: the BakedModel/ModelData pipeline is gone — this extends NeoForge's
 * {@link DelegateBlockStateModel} and does the swap in the level-aware {@code collectParts},
 * which can read the block entity directly (no ModelData round-trip).</p>
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
        boolean[] clear = clearFlags(level, pos);
        if (clear == null) {
            return; // no block entity -> render as authored (all coloured)
        }
        for (int i = start; i < parts.size(); i++) {
            parts.set(i, new RetexturedPart(parts.get(i), clear));
        }
    }

    @Nullable
    private static boolean[] clearFlags(BlockAndTintGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane)) {
            return null;
        }
        int n = pane.getColors().size();
        boolean[] clear = new boolean[n];
        for (int i = 0; i < n; i++) {
            clear[i] = pane.colorAt(i) == null;
        }
        return clear;
    }

    /** A part whose clear cells are retextured to the untinted clear sprite; everything else forwards. */
    private record RetexturedPart(BlockModelPart part, boolean[] clear) implements BlockModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction side) {
            List<BakedQuad> quads = part.getQuads(side);
            List<BakedQuad> out = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                int tint = quad.tintIndex();
                out.add(tint >= 0 && tint < clear.length && clear[tint]
                        ? LeadedGlassClearSprite.retexture(quad)
                        : quad);
            }
            return out;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return part.useAmbientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon() {
            return part.particleIcon();
        }
    }
}

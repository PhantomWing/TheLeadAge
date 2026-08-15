package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.client.LeadedGlassClearSprite;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a dynamic (grid / lattice) pane's baked model and, at chunk-mesh time, retextures whichever
 * glass cells are CLEAR — swapping the tinted white sprite for its untinted clear counterpart —
 * from the block entity's per-region colours. This keeps the design out of block-state properties
 * (no 2^regions explosion) while preserving the exact clear-vs-coloured look. It only runs when a
 * section is (re)built, so it adds no per-frame cost; the quads bake into the static chunk mesh.
 */
public class LeadedGlassPaneModel extends BakedModelWrapper<BakedModel> {
    /** Per-region clear flags, read from the block entity in {@link #getModelData} and used in {@link #getQuads}. */
    public static final ModelProperty<boolean[]> CLEAR = new ModelProperty<>();

    public LeadedGlassPaneModel(BakedModel base) {
        super(base);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        if (level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
            int n = pane.getColors().size();
            boolean[] clear = new boolean[n];
            for (int i = 0; i < n; i++) {
                clear[i] = pane.colorAt(i) == null;
            }
            return data.derive().with(CLEAR, clear).build();
        }
        return data;
    }

    /**
     * The wrapper's 3-arg getQuads passes straight through, and on NeoForge a multipart model answers
     * that overload with its FIRST selector (for these panes, the floor-mounted came) rather than the
     * parts matching the state. Route it to the overload that actually selects.
     */
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData data, @Nullable RenderType renderType) {
        List<BakedQuad> quads = originalModel.getQuads(state, side, rand, data, renderType);
        boolean[] clear = data.get(CLEAR);
        if (clear == null) {
            return quads; // no block entity (e.g. inventory) -> render as authored (all coloured)
        }
        List<BakedQuad> out = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            int tint = quad.getTintIndex();
            out.add(tint >= 0 && tint < clear.length && clear[tint]
                    ? LeadedGlassClearSprite.retexture(quad)
                    : quad);
        }
        return out;
    }
}

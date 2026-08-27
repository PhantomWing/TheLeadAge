package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.client.LeadedGlassClearSprite;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Fabric twin of the NeoForge {@code LeadedGlassPaneModel}. Wraps a dynamic (grid / lattice) pane's
 * block-state model and, at chunk-mesh time, retextures whichever glass cells are CLEAR (the tinted
 * white sprite is swapped for its untinted clear counterpart) from the block entity's per-region
 * colours. Runs only when a section is (re)built, so there is no per-frame cost.
 *
 * <p>The swap is installed as a {@code QuadTransform} and the wrapped model is then asked to emit
 * itself, rather than this class walking its parts and re-emitting them. That matters: the wrapped
 * model applies its own render materials (vanilla shade mode, per-part ambient occlusion) and, if it
 * is itself a custom or mesh-based model, only its own {@code emitQuads} produces accurate geometry.
 * Quads whose region is not clear pass through the transform untouched.</p>
 */
public class LeadedGlassPaneModelFabric extends WrapperBlockStateModel {
    public LeadedGlassPaneModelFabric(BlockStateModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        boolean[] clear = LeadedGlassClearSprite.clearFlags(blockView, pos);
        if (clear == null || !LeadedGlassClearSprite.hasClear(clear)) {
            super.emitQuads(emitter, blockView, pos, state, random, cullTest); // nothing to swap
            return;
        }
        emitter.pushTransform(quad -> retextureClear(quad, clear));
        try {
            super.emitQuads(emitter, blockView, pos, state, random, cullTest);
        } finally {
            emitter.popTransform();
        }
    }

    /**
     * This model's geometry depends on the block entity, so it must never be cached and reused across
     * positions. The base class forwards the key to the wrapped model, whose key knows nothing about
     * the pane's colours, hence the explicit {@code null} (which means "do not cache").
     */
    @Override
    @Nullable
    public Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state,
                                    RandomSource random) {
        return null;
    }

    /** Retextures a clear cell's quad in place, dropping its tint. Always keeps the quad. */
    private static boolean retextureClear(MutableQuadView quad, boolean[] clear) {
        if (!LeadedGlassClearSprite.isClear(clear, quad.tintIndex())) {
            return true;
        }
        TextureAtlasSprite from = spriteFinder().find(quad);
        TextureAtlasSprite to = LeadedGlassClearSprite.clearSprite(from);
        if (to != null) {
            // Atlas UVs back to sprite-local, then re-baked onto the clear sprite's bounds.
            for (int i = 0; i < 4; i++) {
                float lu = (quad.u(i) - from.getU0()) / (from.getU1() - from.getU0());
                float lv = (quad.v(i) - from.getV0()) / (from.getV1() - from.getV0());
                quad.uv(i, lu, lv);
            }
            quad.spriteBake(to, MutableQuadView.BAKE_NORMALIZED);
            quad.tintIndex(-1);
        }
        return true;
    }

    /**
     * Deliberately NOT cached in a field. {@code SpriteFinder.get} is already a field read on the
     * atlas (Fabric stores the finder there) and Fabric rebuilds it when the atlas re-stitches.
     * Holding our own copy would survive that invalidation, leaving us matching quads against the
     * previous atlas layout after any resource reload.
     */
    private static SpriteFinder spriteFinder() {
        return SpriteFinder.get(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS));
    }
}

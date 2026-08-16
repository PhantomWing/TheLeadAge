package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.client.LeadedGlassClearSprite;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Fabric twin of the NeoForge {@code LeadedGlassPaneModel}. Wraps a dynamic (grid / lattice) pane's
 * baked model and, at chunk-mesh time, retextures whichever glass cells are CLEAR — swapping the
 * tinted white sprite for its untinted clear counterpart — from the block entity's per-region
 * colours. Runs only when a section is (re)built; no per-frame cost.
 *
 * <p>1.21.4 / FRAPI 5 removed {@code ForwardingBakedModel} and the {@code RenderContext}
 * push/popTransform pipeline: the wrapper now extends vanilla {@link DelegateBakedModel} and feeds
 * the parent's vanilla quads through the {@link QuadEmitter} itself, editing clear cells in place.</p>
 */
public class LeadedGlassPaneModelFabric extends DelegateBakedModel implements FabricBakedModel {
    public LeadedGlassPaneModelFabric(BakedModel wrapped) {
        super(wrapped);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                               Supplier<RandomSource> randomSupplier, Predicate<Direction> cullTest) {
        boolean[] clear = null;
        if (blockView.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
            int n = pane.getColors().size();
            clear = new boolean[n];
            for (int i = 0; i < n; i++) {
                clear[i] = pane.colorAt(i) == null;
            }
        }
        RenderMaterial material = Renderer.get().materialFinder().find();
        for (int i = 0; i <= 6; i++) {
            Direction cull = i == 6 ? null : Direction.values()[i];
            if (cull != null && cullTest.test(cull)) {
                continue;
            }
            for (BakedQuad quad : parent.getQuads(state, cull, randomSupplier.get())) {
                emitter.fromVanilla(quad, material, cull);
                retextureClear(emitter, clear);
                emitter.emit();
            }
        }
    }

    /** Retexture a clear cell's quad to its clear sprite (drop the tint); pass everything else through. */
    private static void retextureClear(MutableQuadView quad, boolean @Nullable [] clear) {
        int tint = quad.tintIndex();
        if (clear == null || tint < 0 || tint >= clear.length || !clear[tint]) {
            return;
        }
        TextureAtlasSprite from = spriteFinder().find(quad);
        TextureAtlasSprite to = LeadedGlassClearSprite.clearSprite(from);
        if (to != null) {
            for (int i = 0; i < 4; i++) {
                float lu = (quad.u(i) - from.getU0()) / (from.getU1() - from.getU0());
                float lv = (quad.v(i) - from.getV0()) / (from.getV1() - from.getV0());
                quad.uv(i, lu, lv);
            }
            quad.spriteBake(to, MutableQuadView.BAKE_NORMALIZED);
            quad.tintIndex(-1);
        }
    }

    /**
     * Deliberately NOT cached in a field. {@code SpriteFinder.get} is already a field read on the
     * atlas (Fabric stores the finder there) and Fabric rebuilds it when the atlas re-stitches.
     * Holding our own copy would survive that invalidation, leaving us matching quads against the
     * previous atlas layout after any resource reload.
     */
    private static SpriteFinder spriteFinder() {
        return SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
    }
}

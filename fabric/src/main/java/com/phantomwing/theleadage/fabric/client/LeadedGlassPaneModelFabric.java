package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Fabric twin of the NeoForge {@code LeadedGlassPaneModel}. Wraps a dynamic (grid / lattice) pane's
 * baked model and, at chunk-mesh time, retextures whichever glass cells are CLEAR — swapping the
 * tinted white sprite for its untinted clear counterpart — from the block entity's per-region
 * colours. Reads the block entity directly in {@link #emitBlockQuads}; no block-state properties, no
 * per-frame cost (runs only when a section is (re)built).
 */
public class LeadedGlassPaneModelFabric extends ForwardingBakedModel {
    private static final Map<TextureAtlasSprite, TextureAtlasSprite> CLEAR_SPRITE = new ConcurrentHashMap<>();
    private static SpriteFinder spriteFinder;

    public LeadedGlassPaneModelFabric(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                               Supplier<RandomSource> randomSupplier, RenderContext context) {
        boolean[] clear = null;
        if (blockView.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
            int n = pane.getColors().size();
            clear = new boolean[n];
            for (int i = 0; i < n; i++) {
                clear[i] = pane.colorAt(i) == null;
            }
        }
        if (clear == null) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }
        boolean[] flags = clear;
        context.pushTransform(quad -> retextureClear(quad, flags));
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }

    /** Retexture a clear cell's quad to its clear sprite (drop the tint); pass everything else through. */
    private static boolean retextureClear(MutableQuadView quad, boolean[] clear) {
        int tint = quad.colorIndex();
        if (tint < 0 || tint >= clear.length || !clear[tint]) {
            return true;
        }
        TextureAtlasSprite from = spriteFinder().find(quad);
        TextureAtlasSprite to = clearSprite(from);
        if (to != null) {
            for (int i = 0; i < 4; i++) {
                float lu = (quad.u(i) - from.getU0()) / (from.getU1() - from.getU0());
                float lv = (quad.v(i) - from.getV0()) / (from.getV1() - from.getV0());
                quad.uv(i, lu, lv);
            }
            quad.spriteBake(to, MutableQuadView.BAKE_NORMALIZED);
            quad.colorIndex(-1);
        }
        return true;
    }

    private static TextureAtlasSprite clearSprite(TextureAtlasSprite white) {
        return CLEAR_SPRITE.computeIfAbsent(white, w -> {
            ResourceLocation name = w.contents().name();
            if (!name.getPath().contains("white_")) {
                return null;
            }
            ResourceLocation clearLoc = ResourceLocation.fromNamespaceAndPath(
                    name.getNamespace(), name.getPath().replace("white_", ""));
            return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(clearLoc);
        });
    }

    private static SpriteFinder spriteFinder() {
        if (spriteFinder == null) {
            spriteFinder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
        }
        return spriteFinder;
    }
}

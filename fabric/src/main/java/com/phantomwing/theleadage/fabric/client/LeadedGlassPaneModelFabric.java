package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.client.LeadedGlassClearSprite;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Fabric twin of the NeoForge {@code LeadedGlassPaneModel}. Wraps a dynamic (grid / lattice) pane's
 * block-state model and, at chunk-mesh time, retextures whichever glass cells are CLEAR — swapping
 * the tinted white sprite for its untinted clear counterpart — from the block entity's per-region
 * colours. Runs only when a section is (re)built; no per-frame cost.
 *
 * <p>1.21.5 / FRAPI 8: models are {@link BlockStateModel}s and the level-aware hook is
 * {@link FabricBlockStateModel#emitQuads}, so the block entity is read directly there — no
 * push/pop transform pipeline, no render-data round-trip. {@code createGeometryKey} keeps its
 * {@code null} default: this model's output depends on the block entity, so it must not be
 * geometry-cached across positions.</p>
 */
public class LeadedGlassPaneModelFabric implements BlockStateModel, FabricBlockStateModel {
    private final BlockStateModel wrapped;

    public LeadedGlassPaneModelFabric(BlockStateModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        wrapped.collectParts(random, parts);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return wrapped.particleIcon();
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<@Nullable Direction> cullTest) {
        boolean[] clear = null;
        if (blockView.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
            int n = pane.getColors().size();
            clear = new boolean[n];
            for (int i = 0; i < n; i++) {
                clear[i] = pane.colorAt(i) == null;
            }
        }
        RenderMaterial material = Renderer.get().materialFinder().find();
        for (BlockModelPart part : wrapped.collectParts(random)) {
            for (int i = 0; i <= 6; i++) {
                Direction cull = i == 6 ? null : Direction.values()[i];
                if (cull != null && cullTest.test(cull)) {
                    continue;
                }
                for (BakedQuad quad : part.getQuads(cull)) {
                    int tint = quad.tintIndex();
                    BakedQuad out = clear != null && tint >= 0 && tint < clear.length && clear[tint]
                            ? LeadedGlassClearSprite.retexture(quad)
                            : quad;
                    emitter.fromVanilla(out, material, cull);
                    emitter.emit();
                }
            }
        }
    }
}

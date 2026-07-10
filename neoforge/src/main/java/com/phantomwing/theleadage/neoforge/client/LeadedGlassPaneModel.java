package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    /** Cache: a tinted white sprite -> its clear counterpart (same name, "white_" prefix dropped). */
    private static final Map<TextureAtlasSprite, TextureAtlasSprite> CLEAR_SPRITE = new ConcurrentHashMap<>();

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
            out.add(tint >= 0 && tint < clear.length && clear[tint] ? retexture(quad) : quad);
        }
        return out;
    }

    /** Swap a coloured cell quad's sprite for its clear counterpart and drop the tint. */
    private static BakedQuad retexture(BakedQuad quad) {
        TextureAtlasSprite from = quad.getSprite();
        TextureAtlasSprite to = clearSprite(from);
        if (to == null) {
            return quad;
        }
        int[] v = quad.getVertices().clone();
        for (int i = 0; i < 4; i++) {
            int o = i * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
            float lu = (Float.intBitsToFloat(v[o]) - from.getU0()) / (from.getU1() - from.getU0());
            float lw = (Float.intBitsToFloat(v[o + 1]) - from.getV0()) / (from.getV1() - from.getV0());
            v[o] = Float.floatToRawIntBits(to.getU0() + lu * (to.getU1() - to.getU0()));
            v[o + 1] = Float.floatToRawIntBits(to.getV0() + lw * (to.getV1() - to.getV0()));
        }
        return new BakedQuad(v, -1, quad.getDirection(), to, quad.isShade());
    }

    @Nullable
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
}

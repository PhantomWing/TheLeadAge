package com.phantomwing.theleadage.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Swaps a leaded glass quad's tinted {@code white_*} sprite for its untinted clear counterpart.
 *
 * <p>The pane models are authored against the tintable white textures, and a clear region is drawn by
 * pointing the same geometry at the {@code white_}-less texture instead. For most came types the
 * block state does that (a {@code clear_N} property picks a pre-textured model variant), but the
 * cell-based types — grid and lattice — deliberately carry no such properties (2^12 states), so the
 * swap has to happen on the baked quads. Both the per-loader chunk-mesh wrappers and
 * {@link LeadedGlassSurface} (doors / trapdoors) need it, hence this shared helper.</p>
 */
public final class LeadedGlassClearSprite {
    // BakedQuad vertex layout (DefaultVertexFormat.BLOCK): 8 ints per vertex, with the u/v pair at
    // offset 4. Spelled out here rather than borrowed from a loader API so this stays loader-agnostic.
    private static final int STRIDE = 8;
    private static final int UV0 = 4;

    /** Cache: a tinted white sprite -> its clear counterpart (same name, "white_" prefix dropped). */
    private static final Map<TextureAtlasSprite, TextureAtlasSprite> CLEAR_SPRITE = new ConcurrentHashMap<>();

    private LeadedGlassClearSprite() {
    }

    /**
     * The quad redrawn with its clear sprite and no tint, or the quad unchanged when it has no clear
     * counterpart — which is the normal case for a came-type whose model already used the clear
     * texture, so callers can apply this to every clear region without checking first.
     */
    public static BakedQuad retexture(BakedQuad quad) {
        TextureAtlasSprite from = quad.sprite();
        TextureAtlasSprite to = clearSprite(from);
        if (to == null) {
            return quad;
        }
        // Remap each vertex's atlas UV from the old sprite's bounds onto the new sprite's.
        int[] v = quad.vertices().clone();
        for (int i = 0; i < 4; i++) {
            int o = i * STRIDE + UV0;
            float lu = (Float.intBitsToFloat(v[o]) - from.getU0()) / (from.getU1() - from.getU0());
            float lw = (Float.intBitsToFloat(v[o + 1]) - from.getV0()) / (from.getV1() - from.getV0());
            v[o] = Float.floatToRawIntBits(to.getU0() + lu * (to.getU1() - to.getU0()));
            v[o + 1] = Float.floatToRawIntBits(to.getV0() + lw * (to.getV1() - to.getV0()));
        }
        return new BakedQuad(v, -1, quad.direction(), to, quad.shade(), quad.lightEmission());
    }

    /**
     * The untinted clear counterpart of a tinted {@code white_*} sprite, or {@code null} if it has
     * none. Public so the Fabric chunk-mesh wrapper can share the lookup — it does the UV swap through
     * the Renderer API rather than on a {@link BakedQuad}, so it can't reuse {@link #retexture}.
     */
    @Nullable
    public static TextureAtlasSprite clearSprite(TextureAtlasSprite white) {
        return CLEAR_SPRITE.computeIfAbsent(white, w -> {
            ResourceLocation name = w.contents().name();
            if (!name.getPath().contains("white_")) {
                return null; // already the clear texture (or not one of ours)
            }
            ResourceLocation clearLoc = ResourceLocation.fromNamespaceAndPath(
                    name.getNamespace(), name.getPath().replace("white_", ""));
            return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(clearLoc);
        });
    }
}

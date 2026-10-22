package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
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

    /**
     * A tinted white texture's name -> its clear counterpart's (same name, "white_" dropped), or itself where it has none.
     * By name, not sprite: a resource reload makes every sprite anew, which a map of sprites would keep and miss.
     */
    private static final Map<ResourceLocation, ResourceLocation> CLEAR_NAME = new ConcurrentHashMap<>();

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
        // This is vanilla BakedQuad's full constructor (6 components). NeoForge patches on a 7th,
        // hasAmbientOcclusion, which this defaults to true because common cannot see the accessor
        // that would read it back. Harmless as long as the pane models keep their AO decision at
        // model level, which the mesher consults first; revisit if a per-quad AO=false ever ships.
        return new BakedQuad(v, -1, quad.direction(), to, quad.shade(), quad.lightEmission());
    }

    /** {@link #retexture(BakedQuad)} when the quad's region is clear; every other quad passes through. */
    public static BakedQuad retexture(BakedQuad quad, boolean @Nullable [] clear) {
        return isClear(clear, quad.tintIndex()) ? retexture(quad) : quad;
    }

    /**
     * Per-region clear flags for the pane at {@code pos}, or {@code null} when no pane block entity
     * lives there (the model then renders exactly as authored). Shared by both loaders' chunk-mesh
     * wrappers so "this region is clear" has one definition rather than one per loader.
     */
    @Nullable
    public static boolean[] clearFlags(BlockAndTintGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane)) {
            return null;
        }
        // A region past the stored colours is clear, as everything else reads it: a pane set down without its design.
        // Sized by the block's regions too, so hasClear sees those.
        int regions = pane.getColors().size();
        if (pane.getBlockState().getBlock() instanceof LeadedGlassPaneBlock block) {
            regions = Math.max(regions, block.cameType().regions);
        }
        boolean[] clear = new boolean[regions];
        for (int i = 0; i < regions; i++) {
            clear[i] = pane.colorAt(i) == null;
        }
        return clear;
    }

    /** Whether any region is clear. A fully coloured pane needs no retexturing at all. */
    public static boolean hasClear(boolean[] clear) {
        for (boolean region : clear) {
            if (region) {
                return true;
            }
        }
        return false;
    }

    /** Whether a quad's tint index addresses a clear region. */
    public static boolean isClear(boolean @Nullable [] clear, int tintIndex) {
        return clear != null && tintIndex >= 0 && (tintIndex >= clear.length || clear[tintIndex]);
    }

    /**
     * The untinted clear counterpart of a tinted {@code white_*} sprite, or {@code null} if it has
     * none. Public so the Fabric chunk-mesh wrapper can share the lookup: it swaps the UVs in place
     * on the emitter's mutable quad through the Renderer API rather than rebuilding a
     * {@link BakedQuad}, so it cannot reuse {@link #retexture}.
     */
    @Nullable
    public static TextureAtlasSprite clearSprite(TextureAtlasSprite white) {
        ResourceLocation name = white.contents().name();
        ResourceLocation clear = CLEAR_NAME.computeIfAbsent(name, n -> n.getPath().contains("white_")
                ? ResourceLocation.fromNamespaceAndPath(n.getNamespace(), n.getPath().replace("white_", ""))
                : n); // already the clear texture (or not one of ours)
        return clear.equals(name) ? null
                : Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(clear);
    }
}

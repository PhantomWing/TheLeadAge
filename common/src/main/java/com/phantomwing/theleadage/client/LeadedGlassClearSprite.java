package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
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
    /**
     * Cache: a tinted white sprite -> its clear counterpart (same name, "white_" prefix dropped).
     * A sprite mapped to ITSELF means "no counterpart", so the negative answer is memoised too:
     * without that, every non-{@code white_} quad re-runs the name check on every chunk rebuild,
     * which is the common fully-clear pane path.
     */
    private static final Map<TextureAtlasSprite, TextureAtlasSprite> CLEAR_SPRITE = new ConcurrentHashMap<>();

    /**
     * The atlas {@link #CLEAR_SPRITE} was built against. A resource reload re-stitches the atlas and
     * hands out fresh sprite instances, so every cached key belongs to a dead generation: nothing can
     * ever hit again and the old sprites would be pinned forever. Volatile because chunk meshing
     * reaches this from several threads.
     */
    private static volatile TextureAtlas cachedAtlas;

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
        // 1.21.11: BakedQuad is a record of four positions and four PACKED uv longs, not the old
        // interleaved int[] vertex array. UVPair does the packing, and the values it holds are
        // absolute ATLAS coordinates, so the sprite-local round trip is unchanged.
        long[] uv = new long[4];
        for (int i = 0; i < 4; i++) {
            long packed = quad.packedUV(i);
            float lu = (UVPair.unpackU(packed) - from.getU0()) / (from.getU1() - from.getU0());
            float lw = (UVPair.unpackV(packed) - from.getV0()) / (from.getV1() - from.getV0());
            uv[i] = UVPair.pack(to.getU0() + lu * (to.getU1() - to.getU0()),
                    to.getV0() + lw * (to.getV1() - to.getV0()));
        }
        // Vanilla's 13-arg constructor. NeoForge patches THREE more components onto this record
        // (bakedNormals, bakedColors, hasAmbientOcclusion) and this overload hard-codes them to
        // UNSPECIFIED / DEFAULT / true, so a quad arriving with custom baked normals or colors from
        // an extended model loses them here. Safe while the pane models keep their AO and shading
        // decisions at model level; revisit if a per-quad override ever ships.
        return new BakedQuad(
                quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                uv[0], uv[1], uv[2], uv[3],
                -1, quad.direction(), to, quad.shade(), quad.lightEmission());
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
        int regions = pane.getColors().size();
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
        return clear != null && tintIndex >= 0 && tintIndex < clear.length && clear[tintIndex];
    }

    /**
     * The untinted clear counterpart of a tinted {@code white_*} sprite, or {@code null} if it has
     * none. Public so the Fabric chunk-mesh wrapper can share the lookup: it swaps the UVs in place
     * on the emitter's mutable quad through the Renderer API rather than rebuilding a
     * {@link BakedQuad}, so it cannot reuse {@link #retexture}.
     */
    @Nullable
    public static TextureAtlasSprite clearSprite(TextureAtlasSprite white) {
        TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        if (atlas != cachedAtlas) {
            CLEAR_SPRITE.clear();
            cachedAtlas = atlas;
        }
        TextureAtlasSprite found = CLEAR_SPRITE.computeIfAbsent(white, w -> {
            Identifier name = w.contents().name();
            if (!name.getPath().contains("white_")) {
                return w; // already the clear texture (or not one of ours)
            }
            Identifier clearLoc = Identifier.fromNamespaceAndPath(
                    name.getNamespace(), name.getPath().replace("white_", ""));
            TextureAtlasSprite clear = atlas.getSprite(clearLoc);
            // getSprite NEVER returns null: it is getOrDefault(id, missingSprite), so an id that was
            // not stitched (a resource pack dropping one clear texture) comes back as the missing
            // texture. Retexturing onto that would paint the clear cells magenta; treat it as "no
            // counterpart" instead, which is what the javadoc promises.
            return clear == atlas.missingSprite() ? w : clear;
        });
        return found == white ? null : found;
    }
}

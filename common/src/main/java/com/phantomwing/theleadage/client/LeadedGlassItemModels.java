package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;

/**
 * Loader-agnostic client registration for the leaded glass item models.
 *
 * <p>1.21.4 removed item-model {@code overrides}, item color handlers and BEWLR: the pane, door
 * and trapdoor icons are data-driven {@code items/*.json} definitions backed by two custom special
 * renderers ({@code theleadage:leaded_glass_pane}, {@code theleadage:leaded_glass_trapdoor}) that
 * draw the design from the stack's config. Their types live in vanilla's private late-bound
 * {@code SpecialModelRenderers.ID_MAPPER}, widened via the access widener (+ NeoForge AT mirror)
 * so registration is byte-identical on both loaders.</p>
 *
 * <p><b>Client-only by call-site isolation.</b> Referenced only from the loaders' client entrypoints,
 * a Dist.CLIENT subscriber and datagen, never from server-reachable code, which is what keeps it off
 * a dedicated server. Deliberately not marked {@code @Environment(CLIENT)}: Architectury rewrites
 * that to NeoForge {@code @OnlyIn}, and TheSilverAge dropped it for the same reason. Keep new call
 * sites client-side.</p>
 */
public final class LeadedGlassItemModels {
    public static final Identifier TRAPDOOR_SPECIAL_ID = TheLeadAge.resourceLocation("leaded_glass_trapdoor");
    public static final Identifier PANE_SPECIAL_ID = TheLeadAge.resourceLocation("leaded_glass_pane");

    private LeadedGlassItemModels() {
    }

    /** Client-setup registrations shared by both loaders; also usable from datagen (serialization side). */
    public static void registerTypes() {
        SpecialModelRenderers.ID_MAPPER.put(TRAPDOOR_SPECIAL_ID, LeadedGlassTrapdoorSpecialRenderer.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(PANE_SPECIAL_ID, LeadedGlassPaneItemSpecialRenderer.Unbaked.MAP_CODEC);
    }

    // 26.1 removed Architectury's RenderTypeRegistry along with programmatic block render layers.
    // Cutout is now inferred from a texture's binary alpha, and true translucency is opted into per
    // texture with force_translucent in the block model (see ModModelProvider), so there is nothing
    // left to register here.
}

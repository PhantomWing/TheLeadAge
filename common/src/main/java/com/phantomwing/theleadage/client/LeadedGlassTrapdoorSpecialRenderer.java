package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.List;

/**
 * The leaded glass trapdoor's item icon: the trapdoor frame model + the configured glass design.
 * 1.21.4 replaced BEWLR ({@code builtin/entity} models) with data-driven {@code special} item
 * models; this is the renderer behind {@code theleadage:leaded_glass_trapdoor}, one instance per
 * bake, carrying the stack's design via {@link #extractArgument}.
 *
 * <p><b>Client-only by call-site isolation.</b> Referenced only from the loaders' client entrypoints,
 * a Dist.CLIENT subscriber and datagen, never from server-reachable code, which is what keeps it off
 * a dedicated server. Deliberately not marked {@code @Environment(CLIENT)}: Architectury rewrites
 * that to NeoForge {@code @OnlyIn}, and TheSilverAge dropped it for the same reason. Keep new call
 * sites client-side.</p>
 */
public class LeadedGlassTrapdoorSpecialRenderer implements SpecialModelRenderer<LeadedGlassConfig> {
    /** A closed, bottom-half trapdoor's flap (3px slab) — the design's canonical north-facing pose. */
    private static final AABB FLAP = new AABB(0.0, 0.0, 0.0, 1.0, 3.0 / 16.0, 1.0);
    private static final LeadedGlassConfig DEFAULT = new LeadedGlassConfig(LeadedGlassFrame.PLAIN,
            List.of(LeadedGlassConfig.CLEAR));

    /**
     * 1.21.6: special renderers report what they draw, which vanilla turns into the item's cached
     * bounding box. Both corners of the unit cube bound a block model in standard block space, and
     * the AABB builder on the other end only needs the extremes. 1.21.11 turned this from a Set to
     * a Consumer, so the points are pushed rather than added.
     */
    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
        extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    @Override
    public LeadedGlassConfig extractArgument(ItemStack stack) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        return config != null ? config : DEFAULT;
    }

    /**
     * 1.21.9: {@code render} became {@code submit} - geometry is queued and drawn later. Only the
     * glass is drawn here: 26.1 puts a submitted BLOCK model through the chunk-layer pipeline,
     * which has no item lighting, so the frame is a plain model layer of the item definition
     * instead (see ModModelProvider) and vanilla lights it like any other block item.
     */
    @Override
    public void submit(@Nullable LeadedGlassConfig config, PoseStack pose,
                       SubmitNodeCollector collector, int light, int overlay, boolean hasFoil,
                       int outlineColor) {
        LeadedGlassSurface.render(config != null ? config : DEFAULT, FLAP, pose, collector, light, overlay);
    }

    /** The data-driven side: {@code {"type": "theleadage:leaded_glass_trapdoor"}} in an items/ definition. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<LeadedGlassConfig> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<LeadedGlassConfig> bake(SpecialModelRenderer.BakingContext context) {
            return new LeadedGlassTrapdoorSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

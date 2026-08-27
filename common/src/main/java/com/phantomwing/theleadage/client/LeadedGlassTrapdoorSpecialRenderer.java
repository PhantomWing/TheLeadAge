package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import org.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
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
     * the AABB builder on the other end only needs the extremes.
     */
    @Override
    public void getExtents(Set<Vector3f> extents) {
        extents.add(new Vector3f(0.0f, 0.0f, 0.0f));
        extents.add(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    @Override
    public LeadedGlassConfig extractArgument(ItemStack stack) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        return config != null ? config : DEFAULT;
    }

    /**
     * 1.21.9: {@code render} became {@code submit} — geometry is queued and drawn later. The frame
     * goes through {@code submitBlockModel}, which carries exactly the arguments the old
     * {@code ModelBlockRenderer.renderModel} call did plus the outline colour.
     */
    @Override
    public void submit(@Nullable LeadedGlassConfig config, ItemDisplayContext context, PoseStack pose,
                       SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
        BlockState frame = ModBlocks.LEADED_GLASS_TRAPDOOR.get().defaultBlockState();
        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(frame);

        // The trapdoor frame (the cut-window overlay texture, cutout). Untinted, so vanilla's own
        // whole-model emission does exactly what is needed here.
        collector.submitBlockModel(pose, RenderType.cutout(), model, 1.0f, 1.0f, 1.0f,
                light, overlay, outlineColor);

        LeadedGlassSurface.render(config != null ? config : DEFAULT, FLAP, pose, collector, light, overlay);
    }

    /** The data-driven side: {@code {"type": "theleadage:leaded_glass_trapdoor"}} in an items/ definition. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new LeadedGlassTrapdoorSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

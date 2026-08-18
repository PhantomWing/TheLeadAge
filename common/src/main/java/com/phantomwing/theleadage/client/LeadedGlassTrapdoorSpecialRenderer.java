package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The leaded glass trapdoor's item icon: the trapdoor frame model + the configured glass design.
 * 1.21.4 replaced BEWLR ({@code builtin/entity} models) with data-driven {@code special} item
 * models; this is the renderer behind {@code theleadage:leaded_glass_trapdoor}, one instance per
 * bake, carrying the stack's design via {@link #extractArgument}.
 */
@Environment(EnvType.CLIENT)
public class LeadedGlassTrapdoorSpecialRenderer implements SpecialModelRenderer<LeadedGlassConfig> {
    /** A closed, bottom-half trapdoor's flap (3px slab) — the design's canonical north-facing pose. */
    private static final AABB FLAP = new AABB(0.0, 0.0, 0.0, 1.0, 3.0 / 16.0, 1.0);
    private static final LeadedGlassConfig DEFAULT = new LeadedGlassConfig(LeadedGlassFrame.PLAIN,
            List.of(LeadedGlassConfig.CLEAR));

    @Override
    public LeadedGlassConfig extractArgument(ItemStack stack) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        return config != null ? config : DEFAULT;
    }

    @Override
    public void render(@Nullable LeadedGlassConfig config, ItemDisplayContext context, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay, boolean hasFoil) {
        BlockState frame = ModBlocks.LEADED_GLASS_TRAPDOOR.get().defaultBlockState();
        // 1.21.5: block models are BlockStateModels — quads come per-part via collectParts.
        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(frame);

        // The trapdoor frame (the cut-window overlay texture, cutout).
        VertexConsumer cutout = buffers.getBuffer(RenderType.cutout());
        for (BlockModelPart part : model.collectParts(RandomSource.create(42L))) {
            emit(part, null, cutout, pose, light, overlay);
            for (Direction dir : Direction.values()) {
                emit(part, dir, cutout, pose, light, overlay);
            }
        }

        LeadedGlassSurface.render(config != null ? config : DEFAULT, FLAP, pose, buffers, light, overlay);
    }

    private static void emit(BlockModelPart part, Direction dir, VertexConsumer buffer,
                             PoseStack pose, int light, int overlay) {
        for (BakedQuad quad : part.getQuads(dir)) {
            buffer.putBulkData(pose.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, overlay);
        }
    }

    /** The data-driven side: {@code {"type": "theleadage:leaded_glass_trapdoor"}} in an items/ definition. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<?> bake(net.minecraft.client.model.geom.EntityModelSet entityModels) {
            return new LeadedGlassTrapdoorSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Item renderer (a {@link BlockEntityWithoutLevelRenderer}) for the leaded glass trapdoor, so its
 * inventory/hand icon shows the actual glass design — the glass is drawn by a block-entity renderer
 * in-world, which a static item model can't reach. Draws the trapdoor frame model + the configured
 * glass (see {@link LeadedGlassSurface}). The item model points at {@code builtin/entity} to invoke this.
 */
public class LeadedGlassTrapdoorItemRenderer extends BlockEntityWithoutLevelRenderer {
    /** A closed, bottom-half trapdoor's flap (3px slab) — the design's canonical north-facing pose. */
    private static final AABB FLAP = new AABB(0.0, 0.0, 0.0, 1.0, 3.0 / 16.0, 1.0);
    private static final LeadedGlassConfig DEFAULT = new LeadedGlassConfig(LeadedGlassFrame.PLAIN,
            List.of(LeadedGlassConfig.CLEAR));

    public LeadedGlassTrapdoorItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
        render(stack, context, pose, buffers, light, overlay);
    }

    /** Shared entry point — also used directly as Fabric's {@code DynamicItemRenderer}. */
    public static void render(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                              MultiBufferSource buffers, int light, int overlay) {
        BlockState frame = ModBlocks.LEADED_GLASS_TRAPDOOR.get().defaultBlockState();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(frame);

        // The trapdoor frame (the cut-window overlay texture, cutout).
        VertexConsumer cutout = buffers.getBuffer(RenderType.cutout());
        RandomSource random = RandomSource.create(42L);
        emit(model, frame, null, cutout, pose, light, overlay, random);
        for (Direction dir : Direction.values()) {
            emit(model, frame, dir, cutout, pose, light, overlay, random);
        }

        // The configured glass on the flap (falls back to plain-clear for an unconfigured stack).
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        LeadedGlassSurface.render(config != null ? config : DEFAULT, FLAP, pose, buffers, light, overlay);
    }

    private static void emit(BakedModel model, BlockState state, Direction dir, VertexConsumer buffer,
                             PoseStack pose, int light, int overlay, RandomSource random) {
        for (BakedQuad quad : model.getQuads(state, dir, random)) {
            buffer.putBulkData(pose.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, overlay);
        }
    }
}

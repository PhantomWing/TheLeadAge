package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.Collections;
import java.util.List;

/**
 * Item icon for the dynamic (grid / lattice) panes. Their clear cells live in render data read from
 * the block entity, which an item stack does not have — a static tinted item model would draw every
 * clear cell as the untinted white texture. This renderer draws the pane's canonical (wall, north)
 * state model with the stack's config supplying both region tints and the clear-sprite swap
 * (see {@link LeadedGlassSurface#renderUpright}).
 */
@Environment(EnvType.CLIENT)
public class LeadedGlassPaneItemSpecialRenderer implements SpecialModelRenderer<LeadedGlassConfig> {
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
        if (config != null) {
            return config;
        }
        // An unconfigured stack shows the came type's first orientation, all clear.
        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof LeadedGlassPaneBlock pane) {
            return new LeadedGlassConfig(pane.cameType().frame(0),
                    Collections.nCopies(pane.cameType().regions, LeadedGlassConfig.CLEAR));
        }
        return new LeadedGlassConfig(com.phantomwing.theleadage.block.custom.LeadedGlassFrame.PLAIN,
                List.of(LeadedGlassConfig.CLEAR));
    }

    @Override
    public void render(@Nullable LeadedGlassConfig config, ItemDisplayContext context, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay, boolean hasFoil) {
        if (config != null) {
            LeadedGlassSurface.renderUpright(config, pose, buffers, light, overlay);
        }
    }

    /** {@code {"type": "theleadage:leaded_glass_pane"}} in an items/ definition. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<?> bake(net.minecraft.client.model.geom.EntityModelSet entityModels) {
            return new LeadedGlassPaneItemSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

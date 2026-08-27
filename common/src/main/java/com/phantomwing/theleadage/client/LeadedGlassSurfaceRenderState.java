package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.component.LeadedGlassConfig;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Extracted state for the leaded glass door and trapdoor renderers.
 *
 * <p>1.21.9 split block entity rendering in two: an extract phase that may read the level and the
 * block entity, and a submit phase that may not. Everything {@link LeadedGlassSurface} needs is
 * pulled across here. Render states are pooled and reused, so extract always writes both fields;
 * a null config means there is nothing to draw.</p>
 */
public class LeadedGlassSurfaceRenderState extends BlockEntityRenderState {
    @Nullable
    public LeadedGlassConfig config;
    @Nullable
    public AABB box;
}

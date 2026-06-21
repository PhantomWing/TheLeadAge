package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.block.ModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

/**
 * Fabric-side block render-layer registration.
 *
 * <p>NeoForge bakes the render layer into each generated block-model JSON via its
 * {@code "render_type"} field (see {@code ModBlockStateProvider}). Fabric does not
 * parse that field, so without an explicit mapping these blocks fall back to the
 * solid layer and any transparent pixel renders as opaque black. Registering through
 * {@link BlockRenderLayerMap} here is the Fabric twin of NeoForge's JSON
 * {@code render_type}, keeping the two loaders visually identical.</p>
 */
@Environment(EnvType.CLIENT)
public final class ModRenderLayers {
    private ModRenderLayers() {
    }

    public static void register() {
        // Door, trapdoor and grate all need cutout (transparent pixels around the
        // panel edges / through the grate holes).
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                ModBlocks.LEAD_DOOR.get(),
                ModBlocks.LEAD_TRAPDOOR.get(),
                ModBlocks.LEAD_GRATE.get(),
                ModBlocks.LEAD_CHAIN.get(),
                ModBlocks.LEAD_BARS.get());
        // The configurable panel's glass is translucent (tinted), not cutout.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(), ModBlocks.LEADED_GLASS_PANEL.get());
    }
}

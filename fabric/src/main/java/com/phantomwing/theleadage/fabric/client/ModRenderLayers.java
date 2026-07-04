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
                ModBlocks.LEAD_BARS.get(),
                ModBlocks.LEAD_WEIGHT.get(),  // the hanging variant's chain has transparent link holes
                ModBlocks.CHIPPED_LEAD_WEIGHT.get(),
                ModBlocks.DAMAGED_LEAD_WEIGHT.get());
        // Glass doors/trapdoors need cutout for their overlay window; the BER glass is separate.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                ModBlocks.LEADED_GLASS_DOOR.get(), ModBlocks.LEADED_GLASS_TRAPDOOR.get());
        // The pane glass is translucent (tinted), not cutout — one per came type.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(),
                ModBlocks.LEADED_GLASS_PANEL.get(), ModBlocks.LEADED_GLASS_PANE_SPLIT.get(),
                ModBlocks.LEADED_GLASS_PANE_PLUS.get(), ModBlocks.LEADED_GLASS_PANE_GRID.get(),
                ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(), ModBlocks.LEADED_GLASS_PANE_CROSS.get(), ModBlocks.LEADED_GLASS_PANE_DIAMOND.get(), ModBlocks.LEADED_GLASS_PANE_LATTICE.get(), ModBlocks.LEADED_GLASS_PANE_BARS.get());
    }
}

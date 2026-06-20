package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;

/**
 * Fabric client entrypoint (registered as the {@code "client"} entrypoint in
 * {@code fabric.mod.json}). Fabric has no {@code FMLClientSetupEvent}; client-only
 * setup runs from a {@link ClientModInitializer}.
 */
public final class TheLeadAgeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Map doors / trapdoors / grates to their cutout render layer (NeoForge bakes
        // this into the generated model JSON; Fabric ignores that field).
        ModRenderLayers.register();

        // Heavy Orb reuses the vanilla falling-block renderer (the NeoForge twin is an
        // EntityRenderersEvent subscriber).
        EntityRendererRegistry.register(ModEntities.HEAVY_ORB.get(), FallingBlockRenderer::new);
    }
}

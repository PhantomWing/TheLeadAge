package com.phantomwing.theleadage.fabric.client;

import net.fabricmc.api.ClientModInitializer;

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
    }
}

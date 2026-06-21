package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.client.ModColorHandlers;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.entity.ModEntities;
import com.phantomwing.theleadage.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

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

        // Leaded glass panel tint providers (NeoForge twin: ModColorHandlersNeoForge).
        ColorProviderRegistry.BLOCK.register(ModColorHandlers::blockTint, ModBlocks.LEADED_GLASS_PANEL.get());
        ColorProviderRegistry.ITEM.register(ModColorHandlers::itemTint, ModItems.LEADED_GLASS_PANEL.get());

        // Item-model override properties: split-came icon + clear-glass icon.
        ItemProperties.register(ModItems.LEADED_GLASS_PANEL.get(),
                ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "frame"),
                (stack, level, entity, seed) -> ModColorHandlers.frameProperty(stack));
        ItemProperties.register(ModItems.LEADED_GLASS_PANEL.get(),
                ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "clear"),
                (stack, level, entity, seed) -> ModColorHandlers.clearProperty(stack));
    }
}

package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.entity.ModBlockEntities;
import com.phantomwing.theleadage.client.LeadedGlassDoorRenderer;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorItemRenderer;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorRenderer;
import com.phantomwing.theleadage.client.ModColorHandlers;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.entity.ModEntities;
import com.phantomwing.theleadage.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Fabric client entrypoint (registered as the {@code "client"} entrypoint in
 * {@code fabric.mod.json}). Fabric has no {@code FMLClientSetupEvent}; client-only
 * setup runs from a {@link ClientModInitializer}.
 */
public final class TheLeadAgeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModRenderLayers.register();
        EntityRendererRegistry.register(ModEntities.LEAD_WEIGHT.get(), FallingBlockRenderer::new);

        // Pane tint providers (per came-type block / item). NeoForge twin: ModColorHandlersNeoForge.
        ColorProviderRegistry.BLOCK.register(ModColorHandlers::blockTint, ModBlocks.LEADED_GLASS_PANEL.get(),
                ModBlocks.LEADED_GLASS_PANE_SPLIT.get(), ModBlocks.LEADED_GLASS_PANE_PLUS.get(),
                ModBlocks.LEADED_GLASS_PANE_GRID.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(),
                ModBlocks.LEADED_GLASS_PANE_CROSS.get(), ModBlocks.LEADED_GLASS_PANE_DIAMOND.get(), ModBlocks.LEADED_GLASS_PANE_LATTICE.get(), ModBlocks.LEADED_GLASS_PANE_BARS.get());
        ColorProviderRegistry.ITEM.register(ModColorHandlers::itemTint, ModItems.LEADED_GLASS_PANEL.get(),
                ModItems.LEADED_GLASS_PANE_SPLIT.get(), ModItems.LEADED_GLASS_PANE_PLUS.get(),
                ModItems.LEADED_GLASS_PANE_GRID.get(), ModItems.LEADED_GLASS_PANE_DIAGONAL.get(),
                ModItems.LEADED_GLASS_PANE_CROSS.get(), ModItems.LEADED_GLASS_PANE_DIAMOND.get(), ModItems.LEADED_GLASS_PANE_LATTICE.get(), ModItems.LEADED_GLASS_PANE_BARS.get());

        // Leaded glass door + trapdoor renderers.
        BlockEntityRendererRegistry.register(ModBlockEntities.LEADED_GLASS_DOOR.get(), LeadedGlassDoorRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.LEADED_GLASS_TRAPDOOR.get(), LeadedGlassTrapdoorRenderer::new);

        // Custom item renderer so the trapdoor item shows its glass design (builtin/entity item model).
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.LEADED_GLASS_TRAPDOOR.get(),
                LeadedGlassTrapdoorItemRenderer::render);

        // Per-pattern / clear item-icon predicates (registering all four on each is harmless).
        for (Item item : new Item[]{
                ModItems.LEADED_GLASS_PANEL.get(), ModItems.LEADED_GLASS_PANE_SPLIT.get(),
                ModItems.LEADED_GLASS_PANE_PLUS.get(), ModItems.LEADED_GLASS_PANE_GRID.get(),
                ModItems.LEADED_GLASS_PANE_DIAGONAL.get(),
                ModItems.LEADED_GLASS_PANE_CROSS.get(), ModItems.LEADED_GLASS_PANE_DIAMOND.get(), ModItems.LEADED_GLASS_PANE_LATTICE.get(), ModItems.LEADED_GLASS_PANE_BARS.get(), ModItems.LEADED_GLASS_DOOR.get(),
                ModItems.LEADED_GLASS_TRAPDOOR.get()}) {
            registerPredicates(item);
        }
    }

    private static void registerPredicates(Item item) {
        for (LeadedGlassFrame frame : LeadedGlassFrame.values()) {
            ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, frame.getSerializedName()),
                    (stack, level, entity, seed) -> ModColorHandlers.framePredicate(stack, frame));
        }
        ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "clear"),
                (stack, level, entity, seed) -> ModColorHandlers.clearProperty(stack));
    }
}

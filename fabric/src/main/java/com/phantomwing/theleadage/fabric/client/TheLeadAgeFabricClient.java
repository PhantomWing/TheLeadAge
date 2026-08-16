package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.entity.ModBlockEntities;
import com.phantomwing.theleadage.client.LeadedGlassDoorRenderer;
import com.phantomwing.theleadage.client.LeadedGlassItemModels;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorRenderer;
import com.phantomwing.theleadage.client.ModColorHandlers;
import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric client entrypoint (registered as the {@code "client"} entrypoint in
 * {@code fabric.mod.json}). Fabric has no {@code FMLClientSetupEvent}; client-only
 * setup runs from a {@link ClientModInitializer}.
 *
 * <p>1.21.4: item colors, item-model predicates and the builtin item renderer are gone —
 * the pane/door/trapdoor item models are data-driven ({@code items/*.json}), with the custom
 * property/tint/special types registered by {@link LeadedGlassItemModels#registerTypes()}
 * straight into the vanilla mappers (NeoForge uses its dedicated events instead).</p>
 */
public final class TheLeadAgeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LeadedGlassItemModels.registerTypes();
        LeadedGlassItemModels.registerRenderLayers();

        // The lead torch flame: vanilla flame behaviour over the gray-white sprite.
        ParticleFactoryRegistry.getInstance().register(ModParticles.LEAD_FLAME.get(), FlameParticle.Provider::new);
        EntityRendererRegistry.register(ModEntities.LEAD_WEIGHT.get(), FallingBlockRenderer::new);

        // Pane tint providers (block side only — item tints are data-driven now).
        ColorProviderRegistry.BLOCK.register(ModColorHandlers::blockTint, ModBlocks.LEADED_GLASS_PANEL.get(),
                ModBlocks.LEADED_GLASS_PANE_SPLIT.get(), ModBlocks.LEADED_GLASS_PANE_PLUS.get(),
                ModBlocks.LEADED_GLASS_PANE_GRID.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(),
                ModBlocks.LEADED_GLASS_PANE_CROSS.get(), ModBlocks.LEADED_GLASS_PANE_DIAMOND.get(), ModBlocks.LEADED_GLASS_PANE_LATTICE.get(), ModBlocks.LEADED_GLASS_PANE_BARS.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS.get());

        // Dynamic (grid/lattice) panes: wrap their baked models so clear cells are retextured from the
        // block entity at mesh time (Fabric twin of the NeoForge ModelEvent.ModifyBakingResult wrap).
        ResourceLocation gridId = BuiltInRegistries.BLOCK.getKey(ModBlocks.LEADED_GLASS_PANE_GRID.get());
        ResourceLocation latticeId = BuiltInRegistries.BLOCK.getKey(ModBlocks.LEADED_GLASS_PANE_LATTICE.get());
        // 1.21.4: block-state models get their own modifier phase (modifyBlockModelAfterBake),
        // whose context id is the ModelResourceLocation the old topLevelId() used to expose.
        ModelLoadingPlugin.register(ctx -> ctx.modifyBlockModelAfterBake().register((model, context) -> {
            if (model != null && !(model instanceof LeadedGlassPaneModelFabric)
                    && (context.id().id().equals(gridId) || context.id().id().equals(latticeId))) {
                return new LeadedGlassPaneModelFabric(model);
            }
            return model;
        }));

        // Leaded glass door + trapdoor renderers.
        BlockEntityRendererRegistry.register(ModBlockEntities.LEADED_GLASS_DOOR.get(), LeadedGlassDoorRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.LEADED_GLASS_TRAPDOOR.get(), LeadedGlassTrapdoorRenderer::new);
    }
}

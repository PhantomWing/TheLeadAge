package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.entity.ModBlockEntities;
import com.phantomwing.theleadage.client.LeadedGlassDoorRenderer;
import com.phantomwing.theleadage.client.LeadedGlassItemModels;
import com.phantomwing.theleadage.client.LeadedGlassPaneItemSpecialRenderer;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorRenderer;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorSpecialRenderer;
import com.phantomwing.theleadage.client.ModColorHandlers;
import com.phantomwing.theleadage.particle.ModParticles;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

import java.util.Map;

/**
 * Leaded glass client hooks on NeoForge: block tints, the pane/trapdoor special model renderers
 * (registered via NeoForge's mod-bus event, which fires before item models are parsed), the door +
 * trapdoor renderers, and particles.
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModColorHandlersNeoForge {
    private ModColorHandlersNeoForge() {
    }

    @SubscribeEvent
    static void blockColors(RegisterColorHandlersEvent.Block event) {
        event.register(ModColorHandlers::blockTint, ModBlocks.LEADED_GLASS_PANEL.get(),
                ModBlocks.LEADED_GLASS_PANE_SPLIT.get(), ModBlocks.LEADED_GLASS_PANE_PLUS.get(),
                ModBlocks.LEADED_GLASS_PANE_GRID.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(),
                ModBlocks.LEADED_GLASS_PANE_CROSS.get(), ModBlocks.LEADED_GLASS_PANE_DIAMOND.get(), ModBlocks.LEADED_GLASS_PANE_LATTICE.get(), ModBlocks.LEADED_GLASS_PANE_BARS.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS.get());
    }

    @SubscribeEvent
    static void specialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(LeadedGlassItemModels.TRAPDOOR_SPECIAL_ID, LeadedGlassTrapdoorSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(LeadedGlassItemModels.PANE_SPECIAL_ID, LeadedGlassPaneItemSpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    static void modifyModels(ModelEvent.ModifyBakingResult event) {
        // Dynamic (grid/lattice) panes: wrap their baked models so clear cells are retextured from the
        // block entity at mesh time (see LeadedGlassPaneModel). Other pane types stay on block states.
        wrapPane(event, ModBlocks.LEADED_GLASS_PANE_GRID.get());
        wrapPane(event, ModBlocks.LEADED_GLASS_PANE_LATTICE.get());
    }

    private static void wrapPane(ModelEvent.ModifyBakingResult event, Block block) {
        // 1.21.4: the event exposes the whole BakingResult; block-state models live in its map.
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        for (Map.Entry<ModelResourceLocation, BakedModel> e : event.getBakingResult().blockStateModels().entrySet()) {
            if (e.getKey().id().equals(id) && !(e.getValue() instanceof LeadedGlassPaneModel)) {
                e.setValue(new LeadedGlassPaneModel(e.getValue()));
            }
        }
    }

    @SubscribeEvent
    static void particleProviders(RegisterParticleProvidersEvent event) {
        // The lead torch flame: vanilla flame behaviour over the gray-white sprite.
        event.registerSpriteSet(ModParticles.LEAD_FLAME.get(), FlameParticle.Provider::new);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LEADED_GLASS_DOOR.get(), LeadedGlassDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEADED_GLASS_TRAPDOOR.get(), LeadedGlassTrapdoorRenderer::new);
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        // 1.21.4 dropped the model-JSON render_type our old datagen emitted; layers register in code.
        event.enqueueWork(LeadedGlassItemModels::registerRenderLayers);
    }
}

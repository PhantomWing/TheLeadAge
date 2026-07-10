package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.entity.ModBlockEntities;
import com.phantomwing.theleadage.client.LeadedGlassDoorRenderer;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorItemRenderer;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorRenderer;
import com.phantomwing.theleadage.client.ModColorHandlers;
import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.Map;

/**
 * Registers the leaded glass client hooks on NeoForge: pane tint providers (per came-type block /
 * item), the leaded glass door + trapdoor renderers, and the item-model override predicates.
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID, value = Dist.CLIENT)
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
    static void modifyModels(ModelEvent.ModifyBakingResult event) {
        // Dynamic (grid/lattice) panes: wrap their baked models so clear cells are retextured from the
        // block entity at mesh time (see LeadedGlassPaneModel). Other pane types stay on block states.
        wrapPane(event, ModBlocks.LEADED_GLASS_PANE_GRID.get());
        wrapPane(event, ModBlocks.LEADED_GLASS_PANE_LATTICE.get());
    }

    private static void wrapPane(ModelEvent.ModifyBakingResult event, Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        for (Map.Entry<ModelResourceLocation, BakedModel> e : event.getModels().entrySet()) {
            if (e.getKey().id().equals(id) && !(e.getValue() instanceof LeadedGlassPaneModel)) {
                e.setValue(new LeadedGlassPaneModel(e.getValue()));
            }
        }
    }

    @SubscribeEvent
    static void itemColors(RegisterColorHandlersEvent.Item event) {
        event.register(ModColorHandlers::itemTint, ModItems.LEADED_GLASS_PANEL.get(),
                ModItems.LEADED_GLASS_PANE_SPLIT.get(), ModItems.LEADED_GLASS_PANE_PLUS.get(),
                ModItems.LEADED_GLASS_PANE_GRID.get(), ModItems.LEADED_GLASS_PANE_DIAGONAL.get(),
                ModItems.LEADED_GLASS_PANE_CROSS.get(), ModItems.LEADED_GLASS_PANE_DIAMOND.get(), ModItems.LEADED_GLASS_PANE_LATTICE.get(), ModItems.LEADED_GLASS_PANE_BARS.get(), ModItems.LEADED_GLASS_PANE_DIAGONAL_BARS.get());
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
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        // Custom item renderer so the trapdoor item shows its actual glass design (builtin/entity item model).
        event.registerItem(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new LeadedGlassTrapdoorItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.LEADED_GLASS_TRAPDOOR.get());
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        // Per-pattern / clear item-icon predicates. Registering all four on every leaded glass item
        // is harmless (an item only uses the ones its model references).
        event.enqueueWork(() -> {
            for (Item item : new Item[]{
                    ModItems.LEADED_GLASS_PANEL.get(), ModItems.LEADED_GLASS_PANE_SPLIT.get(),
                    ModItems.LEADED_GLASS_PANE_PLUS.get(), ModItems.LEADED_GLASS_PANE_GRID.get(),
                    ModItems.LEADED_GLASS_PANE_DIAGONAL.get(),
                    ModItems.LEADED_GLASS_PANE_CROSS.get(), ModItems.LEADED_GLASS_PANE_DIAMOND.get(), ModItems.LEADED_GLASS_PANE_LATTICE.get(), ModItems.LEADED_GLASS_PANE_BARS.get(), ModItems.LEADED_GLASS_PANE_DIAGONAL_BARS.get(), ModItems.LEADED_GLASS_DOOR.get(),
                    ModItems.LEADED_GLASS_TRAPDOOR.get()}) {
                registerPredicates(item);
            }
        });
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

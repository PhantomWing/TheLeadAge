package com.phantomwing.theleadage.forge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.entity.ModBlockEntities;
import com.phantomwing.theleadage.client.LeadFumesParticles;
import com.phantomwing.theleadage.client.LeadSpeckParticle;
import com.phantomwing.theleadage.client.LeadedGlassDoorRenderer;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorRenderer;
import com.phantomwing.theleadage.client.ModColorHandlers;
import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

import java.util.Map;

/**
 * Registers the leaded glass client hooks on Forge: pane tint providers (per came-type block /
 * item), the leaded glass door + trapdoor renderers, and the item-model override predicates. The trapdoor item's own
 * renderer is named by the item itself on Forge 47 ({@code ItemPlatformImpl}).
 *
 * <p>On the MOD bus, named: Forge 47's subscriber defaults to the FORGE bus, where none of these fires.</p>
 */
@Mod.EventBusSubscriber(modid = TheLeadAge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModColorHandlersForge {
    private ModColorHandlersForge() {
    }

    @SubscribeEvent
    public static void blockColors(RegisterColorHandlersEvent.Block event) {
        event.register(ModColorHandlers::blockTint, ModBlocks.LEADED_GLASS_PANEL.get(),
                ModBlocks.LEADED_GLASS_PANE_SPLIT.get(), ModBlocks.LEADED_GLASS_PANE_PLUS.get(),
                ModBlocks.LEADED_GLASS_PANE_GRID.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(),
                ModBlocks.LEADED_GLASS_PANE_CROSS.get(), ModBlocks.LEADED_GLASS_PANE_DIAMOND.get(), ModBlocks.LEADED_GLASS_PANE_LATTICE.get(), ModBlocks.LEADED_GLASS_PANE_BARS.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS.get());
        event.register(ModColorHandlers::stainedGlassTint, ModBlocks.stainedLeadedGlassBlocks());
    }

    @SubscribeEvent
    public static void modifyModels(ModelEvent.ModifyBakingResult event) {
        // Dynamic (grid/lattice) panes: wrap their baked models so clear cells are retextured from the
        // block entity at mesh time (see LeadedGlassPaneModel). Other pane types stay on block states.
        wrapPane(event, ModBlocks.LEADED_GLASS_PANE_GRID.get());
        wrapPane(event, ModBlocks.LEADED_GLASS_PANE_LATTICE.get());
    }

    private static void wrapPane(ModelEvent.ModifyBakingResult event, Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        for (Map.Entry<ResourceLocation, BakedModel> e : event.getModels().entrySet()) {
            if (e.getKey() instanceof ModelResourceLocation model && model.getNamespace().equals(id.getNamespace())
                    && model.getPath().equals(id.getPath()) && !(e.getValue() instanceof LeadedGlassPaneModel)) {
                e.setValue(new LeadedGlassPaneModel(e.getValue()));
            }
        }
    }

    @SubscribeEvent
    public static void itemColors(RegisterColorHandlersEvent.Item event) {
        event.register(ModColorHandlers::itemTint, ModItems.LEADED_GLASS_PANEL.get(),
                ModItems.LEADED_GLASS_PANE_SPLIT.get(), ModItems.LEADED_GLASS_PANE_PLUS.get(),
                ModItems.LEADED_GLASS_PANE_GRID.get(), ModItems.LEADED_GLASS_PANE_DIAGONAL.get(),
                ModItems.LEADED_GLASS_PANE_CROSS.get(), ModItems.LEADED_GLASS_PANE_DIAMOND.get(), ModItems.LEADED_GLASS_PANE_LATTICE.get(), ModItems.LEADED_GLASS_PANE_BARS.get(), ModItems.LEADED_GLASS_PANE_DIAGONAL_BARS.get());
        event.register(ModColorHandlers::stainedGlassItemTint, ModBlocks.stainedLeadedGlassBlocks());
    }

    @SubscribeEvent
    public static void particleProviders(RegisterParticleProvidersEvent event) {
        // The lead torch flame: vanilla flame behaviour over the gray-white sprite.
        event.registerSpriteSet(ModParticles.LEAD_FLAME.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.LEAD_SPECK.get(), LeadSpeckParticle.Speck::new);
        event.registerSpriteSet(ModParticles.LEAD_DUST.get(), LeadSpeckParticle.Dust::new);
        event.registerSpriteSet(ModParticles.LEAD_FUMES.get(), LeadFumesParticles.Provider::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LEADED_GLASS_DOOR.get(), LeadedGlassDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEADED_GLASS_TRAPDOOR.get(), LeadedGlassTrapdoorRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
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
            ItemProperties.register(item, new ResourceLocation(TheLeadAge.MOD_ID, frame.getSerializedName()),
                    (stack, level, entity, seed) -> ModColorHandlers.framePredicate(stack, frame));
        }
        ItemProperties.register(item, new ResourceLocation(TheLeadAge.MOD_ID, "clear"),
                (stack, level, entity, seed) -> ModColorHandlers.clearProperty(stack));
    }
}

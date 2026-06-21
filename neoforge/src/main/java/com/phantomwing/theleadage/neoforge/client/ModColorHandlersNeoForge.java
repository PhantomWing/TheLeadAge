package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.client.ModColorHandlers;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Registers the leaded glass panel tint providers on NeoForge via the proper client event
 * (fires only at client runtime — not during datagen, where there is no Minecraft instance).
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID, value = Dist.CLIENT)
public final class ModColorHandlersNeoForge {
    private ModColorHandlersNeoForge() {
    }

    @SubscribeEvent
    static void blockColors(RegisterColorHandlersEvent.Block event) {
        event.register(ModColorHandlers::blockTint, ModBlocks.LEADED_GLASS_PANEL.get());
    }

    @SubscribeEvent
    static void itemColors(RegisterColorHandlersEvent.Item event) {
        event.register(ModColorHandlers::itemTint, ModItems.LEADED_GLASS_PANEL.get());
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        // Item-model override properties: split-came icon + clear-glass icon.
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.LEADED_GLASS_PANEL.get(),
                    ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "frame"),
                    (stack, level, entity, seed) -> ModColorHandlers.frameProperty(stack));
            ItemProperties.register(ModItems.LEADED_GLASS_PANEL.get(),
                    ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "clear"),
                    (stack, level, entity, seed) -> ModColorHandlers.clearProperty(stack));
        });
    }
}

package com.phantomwing.theleadage.forge.condition;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

public class ModConditions {
    /**
     * Forge 1.20.1 takes condition serializers straight into its table, a plain map. Mods are constructed in parallel,
     * so it is written on the main thread, once construction is done and before any recipe loads.
     */
    public static void register(IEventBus eventBus) {
        eventBus.addListener((FMLConstructModEvent event) ->
                event.enqueueWork(() -> CraftingHelper.register(ConfigBooleanCondition.Serializer.INSTANCE)));
    }
}

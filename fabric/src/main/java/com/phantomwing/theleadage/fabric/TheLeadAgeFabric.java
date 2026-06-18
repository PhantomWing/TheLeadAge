package com.phantomwing.theleadage.fabric;

import com.phantomwing.theleadage.TheLeadAgeCommon;
import com.phantomwing.theleadage.fabric.condition.ConfigBooleanResourceCondition;
import com.phantomwing.theleadage.fabric.config.TheLeadAgeFabricConfig;
import com.phantomwing.theleadage.fabric.loot.LeadLootTableId;
import com.phantomwing.theleadage.fabric.villager.ModVillagerTrades;
import com.phantomwing.theleadage.fabric.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Fabric entrypoint for The Lead Age. Registers the AutoConfig holder first (the
 * world-gen biome injection reads config at init), then the loader-agnostic
 * bootstrap. Fabric biome injection is added in the world-gen phase.
 */
public final class TheLeadAgeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TheLeadAgeFabricConfig.register();

        // Parity twin of the NeoForge theleadage:config_boolean recipe condition,
        // so the shared conditional/fallback recipes gate identically on Fabric.
        ResourceConditions.register(ConfigBooleanResourceCondition.TYPE);

        TheLeadAgeCommon.init();

        // Attach lead ore to overworld biomes (gated by config).
        ModWorldGen.register();

        // Villager / wandering-trader trades (the Fabric twin of the NeoForge
        // village-event handlers). No trades registered yet.
        ModVillagerTrades.register();

        // Once all loot tables are loaded, stamp each with its registry id so the
        // LootTableMixin (the Fabric equivalent of the NeoForge Global Loot
        // Modifiers) knows which LeadLootSpec entries to apply at roll time.
        LootTableEvents.ALL_LOADED.register((resourceManager, lootRegistry) ->
                lootRegistry.entrySet().forEach(entry -> {
                    LootTable table = entry.getValue();
                    if (table instanceof LeadLootTableId holder) {
                        holder.theleadage$setLootTableId(entry.getKey().location());
                    }
                }));
    }
}

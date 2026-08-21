package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAgeCommon;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// GatherDataEvent is a MOD-bus event. 1.21.4 reworked it: no more includeServer()/includeClient()
// gating or ExistingFileHelper — just event.addProvider(...), and the run args decide what emits.
// The event is abstract now; subscribe to the concrete Client subclass (fired by the clientData
// run), whose environment is a full client, so the server data providers run fine alongside the
// model providers.
@EventBusSubscriber(modid = TheLeadAgeCommon.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        event.addProvider(new ModRecipeProvider.Runner(output, lookupProvider));
        event.addProvider(new LootTableProvider(
                output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider));

        event.addProvider(new ModBlockTagsProvider(output, lookupProvider));
        event.addProvider(new ModItemTagsProvider(output, lookupProvider));
        event.addProvider(new ModBiomeTagsProvider(output, lookupProvider));
        event.addProvider(new ModEntityTypeTagsProvider(output, lookupProvider));
        event.addProvider(new ModDatapackProvider(output, lookupProvider));
        event.addProvider(new ModGlobalLootModifierProvider(output, lookupProvider));
        // 1.21.4: NeoForge's AdvancementProvider wrapper is gone — vanilla's takes the sub providers.
        event.addProvider(new AdvancementProvider(output, lookupProvider, List.of(new ModAdvancementProvider())));

        // 1.21.4: block + item models (and the items/ client item definitions) come from a single
        // vanilla-style ModelProvider.
        event.addProvider(new ModModelProvider(output));

        // MUST run last: post-processes the generated recipe JSON, adding a
        // translated fabric:load_conditions block beside every neoforge:conditions
        // block so the conditional/fallback recipes gate identically on both loaders.
        event.addProvider(new FabricConditionsProvider(output));
    }
}

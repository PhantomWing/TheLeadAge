package com.phantomwing.theleadage.forge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.forge.compat.create.ModPressingRecipeGen;
import com.phantomwing.theleadage.forge.compat.create.ModWashingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** On the MOD bus: Forge 47's default is the FORGE bus, where GatherDataEvent never fires. */
@Mod.EventBusSubscriber(modid = TheLeadAge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
        // Create compat (Mechanical Press / fan washing). These extend Create's own recipe gens, so
        // Create must be on the datagen classpath — it is, unless -Penable_create=false. Every recipe
        // they emit is mod_loaded-gated, so the output is inert without Create at runtime.
        generator.addProvider(event.includeServer(), new ModPressingRecipeGen(output));
        generator.addProvider(event.includeServer(), new ModWashingRecipeGen(output));
        generator.addProvider(event.includeServer(), new LootTableProvider(
                output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK))));

        ModBlockTagsProvider blockTags = generator.addProvider(event.includeServer(),
                new ModBlockTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ModItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ModBiomeTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ModEntityTypeTagsProvider(output, lookupProvider, existingFileHelper));
        ModDatapackProvider datapack = generator.addProvider(event.includeServer(), new ModDatapackProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(),
                new ModDamageTypeTagsProvider(output, datapack.getRegistryProvider(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModGlobalLootModifierProvider(output));
        generator.addProvider(event.includeServer(), new ForgeAdvancementProvider(output, lookupProvider, existingFileHelper,
                List.of(new ModAdvancementProvider())));

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));

        // MUST run last: Forge's DataGenerator runs its providers one after another in this order, so every recipe
        // and advancement above is on disk by now. It adds a translated fabric:load_conditions beside every Forge
        // "conditions" there, so the conditional and fallback recipes gate identically on both loaders.
        generator.addProvider(event.includeServer(), new FabricConditionsProvider(output));
    }
}

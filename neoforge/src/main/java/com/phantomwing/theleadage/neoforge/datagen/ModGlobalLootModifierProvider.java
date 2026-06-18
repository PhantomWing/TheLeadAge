package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.loot.LeadLootSpec;
import com.phantomwing.theleadage.neoforge.loot.ReplaceItemModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Generates the lead Global Loot Modifier JSON — one GLM per {@link LeadLootSpec}
 * entry, each gated on {@code neoforge:loot_table_id} + {@code minecraft:random_chance}.
 */
public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, TheLeadAge.MOD_ID);
    }

    @Override
    protected void start() {
        for (LeadLootSpec.Entry entry : LeadLootSpec.entries()) {
            LootItemCondition[] conditions = {
                    new LootTableIdCondition.Builder(entry.targetLootTable()).build(),
                    LootItemRandomChanceCondition.randomChance(entry.chance()).build()
            };
            add(entry.id(), new ReplaceItemModifier(conditions, entry.item().get(),
                    entry.removedItems().stream().map(Supplier::get).toList(),
                    entry.min(), entry.max()));
        }
    }
}

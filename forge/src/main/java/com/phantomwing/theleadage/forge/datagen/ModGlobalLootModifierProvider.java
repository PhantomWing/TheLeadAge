package com.phantomwing.theleadage.forge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.forge.loot.ReplaceItemModifier;
import com.phantomwing.theleadage.loot.LeadLootSpec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import java.util.List;
import java.util.function.Supplier;

/**
 * Generates the lead Global Loot Modifier JSON - one GLM per {@link LeadLootSpec} entry, each gated on
 * {@code forge:loot_table_id} + {@code minecraft:random_chance}.
 */
public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output) {
        super(output, TheLeadAge.MOD_ID);
    }

    @Override
    protected void start() {
        for (LeadLootSpec.Entry entry : LeadLootSpec.entries()) {
            // 1.20.1 has no trial chambers: a modifier for their tables would never be rolled.
            if (entry.targetLootTable().getPath().startsWith("chests/trial_chambers/")) {
                continue;
            }
            LootItemCondition[] conditions = {
                    new LootTableIdCondition.Builder(entry.targetLootTable()).build(),
                    LootItemRandomChanceCondition.randomChance(entry.chance()).build()
            };
            // An entry naming another mod's items (requiredMods) loads without it too: 1.20.1's loot modifiers take no
            // load conditions, so ReplaceItemModifier reads the items it swaps by id as the loot is rolled.
            List<ResourceLocation> removed = entry.removedItems().stream().map(item -> idOf(entry, item)).toList();
            add(entry.id(), ReplaceItemModifier.of(conditions, entry.item().get(), removed, entry.min(), entry.max()));
        }
    }

    /** An item another mod adds is air unless that mod is on datagen's classpath, which would write minecraft:air. */
    private static ResourceLocation idOf(LeadLootSpec.Entry entry, Supplier<Item> item) {
        Item resolved = item.get();
        if (resolved == null || resolved == Items.AIR) {
            throw new IllegalStateException("Loot modifier " + entry.id() + " names an item of " + entry.requiredMods()
                    + ", which isn't loaded in datagen: run it with that mod on the classpath.");
        }
        return BuiltInRegistries.ITEM.getKey(resolved);
    }
}

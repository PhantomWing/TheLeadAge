package com.phantomwing.theleadage.loot;

import com.phantomwing.theleadage.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

/**
 * Loader-agnostic lead loot operations — the single source of truth shared by the
 * NeoForge Global Loot Modifier ({@code ReplaceItemModifier}) and the Fabric loot
 * mixin, so both loaders behave identically. The caller is responsible for the
 * config gate and the loot-table-id / random-chance checks; this only performs the
 * post-roll mutation. (Mirrors The Silver Age's loot algorithms, REPLACE only.)
 */
public final class LeadLootAlgorithms {
    private LeadLootAlgorithms() {
    }

    /**
     * Replace up to {@code [minStacks, maxStacks]} stacks (or all when
     * {@code maxStacks <= 0}) whose item is in {@code removedItems} with
     * {@code item}, keeping the count and carrying durability/enchantments.
     */
    public static void applyReplaceItem(ObjectArrayList<ItemStack> generatedLoot, LootContext lootContext,
                                        Item item, List<Item> removedItems, int minStacks, int maxStacks) {
        // 26.3 moved UniformGenerator under providers.number.ints and made its value Holder-based;
        // a uniform roll over the loot context's own random is the same thing without the wrapper.
        int budget = maxStacks > 0
                ? Mth.nextInt(lootContext.getRandom(), minStacks, maxStacks)
                : Integer.MAX_VALUE;

        // Swap in place over an index loop. Do NOT remove-while-iterating here: fastutil's
        // ObjectArrayList.forEach is `for (i = 0; i < size; i++)` with no modCount check, so a removal
        // shifts the tail left and the very next element is skipped — silently, with no exception. That
        // made a chest holding two replaceable stacks convert only one of them. Replacing in place also
        // keeps each stack where the loot table put it, instead of shuffling them to the end.
        for (int i = 0; i < generatedLoot.size() && budget > 0; i++) {
            ItemStack stack = generatedLoot.get(i);
            if (removedItems.stream().noneMatch(stack::is)) {
                continue;
            }
            try {
                generatedLoot.set(i, ItemUtils.tryTransmuteStack(stack, item));
                budget--;
            } catch (Exception ignored) {
                // Skip a bad replacement, keep the original stack — and don't spend the budget on it.
            }
        }
    }
}

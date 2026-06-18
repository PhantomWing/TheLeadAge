package com.phantomwing.theleadage.loot;

import com.phantomwing.theleadage.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

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
        ObjectArrayList<ItemStack> lootArray = new ObjectArrayList<>();
        int numberOfStacksToAdd = maxStacks > 0
                ? UniformGenerator.between(minStacks, maxStacks).getInt(lootContext)
                : Integer.MAX_VALUE;
        final int[] stacksToAdd = {numberOfStacksToAdd};

        if (numberOfStacksToAdd > 0) {
            generatedLoot.forEach((stack) -> {
                if (removedItems.stream().anyMatch(stack::is) && stacksToAdd[0] > 0) {
                    try {
                        ItemStack toAdd = ItemUtils.tryTransmuteStack(stack, item);
                        generatedLoot.remove(stack);
                        lootArray.add(toAdd);
                    } catch (Exception ignored) {
                        // Skip a bad replacement and keep the original stack.
                    }
                    stacksToAdd[0] = stacksToAdd[0] - 1;
                }
            });
        }

        if (!lootArray.isEmpty()) {
            generatedLoot.addAll(lootArray);
        }
    }
}

package com.phantomwing.theleadage.utils;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public final class ItemUtils {
    /**
     * What the stack a swap replaces keeps beyond its wear and enchantments: what a player, a structure or a trial
     * spawner gave it - a name, lore, a trim. Not its attribute modifiers, which would take the new item's own away.
     */
    private static final List<DataComponentType<?>> KEPT = List.of(DataComponents.CUSTOM_NAME, DataComponents.LORE,
            DataComponents.TRIM, DataComponents.UNBREAKABLE, DataComponents.CUSTOM_DATA);

    private ItemUtils() {
    }

    /** The registry path of an item (e.g. {@code lead_ingot}). */
    public static String getName(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    /**
     * Build a stack of {@code item} that "replaces" {@code from}: same count (capped
     * at the new item's max stack size), worn as far through its durability as {@code from} was,
     * with the enchantments the target item supports and its name, lore and trim. Loader-agnostic
     * (vanilla APIs only).
     */
    public static ItemStack tryTransmuteStack(ItemStack from, ItemLike item) {
        int count = Math.min(new ItemStack(item.asItem()).getMaxStackSize(), from.getCount());
        ItemStack to = new ItemStack(item.asItem(), count);

        // The same share of durability used, not the same damage: loot wears iron by a share of its 250, and
        // as much on a lead tool would break it at once. One use is always left, as loot's wear leaves one.
        if (from.isDamaged() && from.isDamageableItem() && to.isDamageableItem()) {
            float worn = from.getDamageValue() / (float) from.getMaxDamage();
            to.setDamageValue(Math.min(Math.round(worn * to.getMaxDamage()), to.getMaxDamage() - 1));
        }

        // Carry over compatible enchantments.
        if (from.isEnchanted() && to.isEnchantable()) {
            ItemEnchantments enchantments = from.getEnchantments();
            enchantments.keySet().forEach(enchantment -> {
                if (enchantment.value().canEnchant(to)) {
                    to.enchant(enchantment, enchantments.getLevel(enchantment));
                }
            });
        }

        for (DataComponentType<?> type : KEPT) {
            copy(from, to, type);
        }
        return to;
    }

    private static <T> void copy(ItemStack from, ItemStack to, DataComponentType<T> type) {
        T value = from.get(type);
        if (value != null) {
            to.set(type, value);
        }
    }
}

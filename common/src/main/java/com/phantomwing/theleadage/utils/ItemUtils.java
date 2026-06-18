package com.phantomwing.theleadage.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

public final class ItemUtils {
    private ItemUtils() {
    }

    /** The registry path of an item (e.g. {@code lead_ingot}). */
    public static String getName(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    /**
     * Build a stack of {@code item} that "replaces" {@code from}: same count (capped
     * at the new item's max stack size), carrying over durability and enchantments
     * where the target item supports them. Loader-agnostic (vanilla APIs only).
     */
    public static ItemStack tryTransmuteStack(ItemStack from, ItemLike item) {
        int count = Math.min(new ItemStack(item.asItem()).getMaxStackSize(), from.getCount());
        ItemStack to = new ItemStack(item.asItem(), count);

        // Carry over durability if both items are damageable.
        if (from.isDamaged() && to.isDamageableItem()) {
            int durability = Math.min(from.getDamageValue(), to.getMaxDamage());
            to.setDamageValue(durability);
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

        return to;
    }
}

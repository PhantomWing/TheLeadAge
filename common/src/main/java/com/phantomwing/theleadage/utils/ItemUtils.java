package com.phantomwing.theleadage.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ItemUtils {
    /**
     * What the stack a swap replaces keeps beyond its wear and enchantments, as 1.21's swap keeps its name, lore, trim,
     * unbreakable and custom data components: what a player, a structure or a trial spawner gave it. Not its attribute
     * modifiers, which would take the new item's own away, nor anything else vanilla keeps in a stack's tag.
     */
    private static final List<String> KEPT = List.of("Trim", "Unbreakable");
    /** Of {@code display}, the name and lore; not a dyed colour. */
    private static final List<String> KEPT_DISPLAY = List.of(ItemStack.TAG_DISPLAY_NAME, ItemStack.TAG_LORE);
    /**
     * The keys vanilla 1.20.1 keeps in a stack's tag, each of which 1.20.5 made a component of its own; any other key is
     * another mod's or a command's, which 1.21 holds as custom data.
     */
    private static final Set<String> VANILLA = Set.of(ItemStack.TAG_DAMAGE, ItemStack.TAG_ENCH, "StoredEnchantments",
            ItemStack.TAG_DISPLAY, "Unbreakable", "HideFlags", "CanDestroy", "CanPlaceOn", "AttributeModifiers",
            "RepairCost", "CustomModelData", "Trim", "BlockEntityTag", "BlockStateTag", "EntityTag", "Potion",
            "CustomPotionEffects", "CustomPotionColor", "Fireworks", "Explosion", "SkullOwner", "pages", "filtered_pages",
            "title", "filtered_title", "author", "generation", "resolved", "Decorations", "map", "map_scale_direction",
            "map_to_lock", "LodestoneDimension", "LodestonePos", "LodestoneTracked", "Charged", "ChargedProjectiles",
            "Items", "DebugProperty", "BucketVariantTag", "Recipes", "effects", "instrument");

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

        CompoundTag tag = from.getTag();
        if (tag != null) {
            CompoundTag kept = new CompoundTag();
            for (String key : tag.getAllKeys()) {
                if (KEPT.contains(key) || !VANILLA.contains(key)) {
                    kept.put(key, Objects.requireNonNull(tag.get(key)).copy());
                }
            }
            CompoundTag display = tag.getCompound(ItemStack.TAG_DISPLAY);
            CompoundTag keptDisplay = new CompoundTag();
            for (String key : KEPT_DISPLAY) {
                if (display.contains(key)) {
                    keptDisplay.put(key, Objects.requireNonNull(display.get(key)).copy());
                }
            }
            if (!keptDisplay.isEmpty()) {
                kept.put(ItemStack.TAG_DISPLAY, keptDisplay);
            }
            if (!kept.isEmpty()) {
                to.getOrCreateTag().merge(kept);
            }
        }

        // Carry over compatible enchantments.
        if (from.isEnchanted() && to.isEnchantable()) {
            EnchantmentHelper.getEnchantments(from).forEach((enchantment, level) -> {
                if (enchantment.canEnchant(to)) {
                    to.enchant(enchantment, level);
                }
            });
        }
        return to;
    }
}

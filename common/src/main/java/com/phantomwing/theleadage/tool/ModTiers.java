package com.phantomwing.theleadage.tool;

import com.phantomwing.theleadage.tags.CommonTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public class ModTiers {
    // Lead is a glass cannon: it hits for netherite-level damage, but mines only at stone level and as
    // slowly as wood, swings 0.2 slower than netherite, and has the lowest durability of ANY tier —
    // below gold. A lead sword is worth ~28 swings. Repaired with lead ingots.
    //
    // 1.21.2 replaced the Tier interface (+ Tiers enum) with the ToolMaterial record. Constructor order:
    //   (TagKey<Block> incorrectBlocksForDrops, int durability, float speed,
    //    float attackDamageBonus, int enchantmentValue, TagKey<Item> repairItems)
    // The repair input is a TagKey<Item> directly now (was a Supplier<Ingredient>).
    public static final ToolMaterial LEAD = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            28,    // Durability — lowest of any tier (Gold 32, Wood 59, Stone 131, Iron 250, Netherite 2031)
            2.0f,  // Mining speed — matches Wood (Wood 2.0, Stone 4.0, Iron 6.0)
            4.0f,  // Attack damage bonus — matches Netherite (Stone 1, Iron 2, Diamond 3, Netherite 4)
            5,     // Enchantability (Stone 5, Iron 14, Gold 22)
            CommonTags.Items.INGOTS_LEAD);
}

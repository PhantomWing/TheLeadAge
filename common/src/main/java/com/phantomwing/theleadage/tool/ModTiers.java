package com.phantomwing.theleadage.tool;

import com.phantomwing.theleadage.tags.CommonTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class ModTiers {
    // Lead is a glass cannon: stone-level mining, extremely low durability and very
    // slow, but hits hard. Repaired with lead ingots.
    public static final Tier LEAD = new LeadTier(BlockTags.INCORRECT_FOR_STONE_TOOL,
            50,    // Durability (Gold 32, Wood 59, Stone 131, Iron 250)
            2.0f,  // Mining speed (Wood 2.0, Stone 4.0, Iron 6.0)
            5.0f,  // Attack damage bonus (Stone 1, Iron 2, Diamond 3, Netherite 4)
            5,     // Enchantability (Stone 5, Iron 14, Gold 22)
            () -> Ingredient.of(CommonTags.Items.INGOTS_LEAD));

    /** Loader-agnostic {@link Tier} with a lazily-evaluated repair ingredient. */
    private record LeadTier(TagKey<Block> incorrectBlocksForDrops, int uses, float speed,
                            float attackDamageBonus, int enchantmentValue,
                            Supplier<Ingredient> repairIngredient) implements Tier {
        @Override
        public int getUses() {
            return uses;
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        @Override
        public float getAttackDamageBonus() {
            return attackDamageBonus;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return incorrectBlocksForDrops;
        }

        @Override
        public int getEnchantmentValue() {
            return enchantmentValue;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return repairIngredient.get();
        }
    }
}

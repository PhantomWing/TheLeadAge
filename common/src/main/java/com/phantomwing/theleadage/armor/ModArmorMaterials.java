package com.phantomwing.theleadage.armor;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.sound.ModSounds;
import com.phantomwing.theleadage.tags.CommonTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Lead armour's material. On 1.20.1 an armour material is an interface rather than a registry entry, so this is an
 * enum, as vanilla's {@code ArmorMaterials} is. Its name, {@code theleadage:lead}, puts the worn layers at
 * {@code theleadage:textures/models/armor/lead_layer_1.png} and {@code _layer_2}.
 */
public enum ModArmorMaterials implements ArmorMaterial {
    // Heavy plate: diamond/netherite-level protection, but no toughness and a durability factor of 6 (between Leather 5
    // and Gold 7), so extremely low durability. The movement-speed + gravity penalties live on LeadArmorItem. Repaired
    // with lead.
    LEAD("lead",
            6,                      // durability factor: a full set is 330, under a sixth of iron's 825
            new int[]{3, 6, 8, 3},  // boots, leggings, chestplate, helmet: Diamond/Netherite's
            5,                      // enchantment value
            0.0F,                   // toughness — none, unlike Diamond 2 / Netherite 3 (soft metal: it stops a blow, it doesn't blunt one)
            0.0F,                   // knockback resistance
            () -> Ingredient.of(CommonTags.Items.INGOTS_LEAD));

    /** Kept under its 1.21 name, which other code reads. */
    public static final ArmorMaterial LEAD_ARMOR_MATERIAL = LEAD;

    /** The Lead Horse Armor's protection, just above Iron's 5 (Leather 3, Gold 7, Diamond 11). */
    public static final int HORSE_ARMOR_PROTECTION = 6;

    /** Vanilla's durability per piece before the material's factor: boots, leggings, chestplate, helmet. */
    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};

    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, int[] slotProtections, int enchantmentValue,
                      float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.slotProtections = slotProtections;
        this.enchantmentValue = enchantmentValue;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.@NotNull Type type) {
        return HEALTH_PER_SLOT[type.getSlot().getIndex()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.@NotNull Type type) {
        return this.slotProtections[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return ModSounds.ARMOR_EQUIP_LEAD.get();
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public @NotNull String getName() {
        return TheLeadAge.MOD_ID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    /** Nothing to register on 1.20.1, where armour materials have no registry. */
    public static void register() {
    }
}

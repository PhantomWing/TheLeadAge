package com.phantomwing.theleadage.armor;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.tags.CommonTags;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

/**
 * Lead armor material.
 *
 * <p>1.21.2 removed the {@code Registries.ARMOR_MATERIAL} registry: {@link ArmorMaterial} is now a
 * plain record passed straight to {@link net.minecraft.world.item.ArmorItem} (which applies
 * {@link ArmorMaterial#humanoidProperties}), so there is no {@code DeferredRegister}/{@code Holder}
 * indirection left. The {@code List<Layer>} was likewise replaced by a single {@code modelId}
 * pointing at {@code assets/theleadage/models/equipment/lead.json}.</p>
 */
public class ModArmorMaterials {
    // Heavy plate: diamond/netherite-level protection, no toughness, and the lowest durability factor
    // in the mod (6 — between Leather 5 and Gold 7). The movement-speed + gravity penalties live on
    // LeadArmorItem. Repaired with lead.
    public static final ArmorMaterial LEAD_ARMOR_MATERIAL = create(
            "lead",
            6,    // durability factor — multiplied per-slot by ArmorType#getDurability
            3,    // boots      (matches Diamond/Netherite 3)
            6,    // leggings   (matches Diamond/Netherite 6)
            8,    // chestplate (matches Diamond/Netherite 8)
            3,    // helmet     (matches Diamond/Netherite 3)
            6,    // body (horse armor) — just above Iron 5 (Leather 3, Gold 7, Diamond 11)
            0f,   // toughness — none, unlike Diamond 2 / Netherite 3 (soft metal: it stops a blow, it doesn't blunt one)
            0f,   // knockback resistance
            5     // enchantment value
    );

    private static ArmorMaterial create(String name, int durability,
                                        int bootsDefense, int legsDefense, int chestDefense,
                                        int headDefense, int bodyDefense,
                                        float toughness, float knockbackResistance, int enchantmentValue) {
        Map<ArmorType, Integer> defense = Map.of(
                ArmorType.BOOTS, bootsDefense,
                ArmorType.LEGGINGS, legsDefense,
                ArmorType.CHESTPLATE, chestDefense,
                ArmorType.HELMET, headDefense,
                ArmorType.BODY, bodyDefense // Body is for Horse/Wolf armor
        );
        Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_IRON;
        // 1.21.4: the equipment model id is a typed ResourceKey<EquipmentAsset>, resolving to
        // assets/<ns>/equipment/<path>.json (the models/ segment was dropped from the lookup).
        ResourceKey<EquipmentAsset> assetId =
                ResourceKey.create(EquipmentAssets.ROOT_ID, TheLeadAge.resourceLocation(name));

        // ArmorMaterial(int durability, Map<ArmorType,Integer> defense, int enchantmentValue,
        //   Holder<SoundEvent> equipSound, float toughness, float knockbackResistance,
        //   TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> assetId)
        return new ArmorMaterial(durability, defense, enchantmentValue, equipSound,
                toughness, knockbackResistance, CommonTags.Items.INGOTS_LEAD, assetId);
    }

    /**
     * No-op since 1.21.2: armor materials are no longer registry objects, so there is nothing to
     * register. Kept so {@code TheLeadAgeCommon#init()} keeps its existing call site.
     */
    public static void register() {
    }
}

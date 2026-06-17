package com.phantomwing.theleadage.armor;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.tags.CommonTags;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.ARMOR_MATERIAL);

    // Heavy plate: very high protection but extremely low durability. The
    // movement-speed + gravity penalties live on LeadArmorItem. Repaired with lead.
    public static final Holder<ArmorMaterial> LEAD_ARMOR_MATERIAL = register("lead",
            4,    // boots    (Diamond 3, Netherite 3)
            7,    // leggings (Diamond 6, Netherite 6)
            9,    // chestplate (Diamond 8, Netherite 8)
            4,    // helmet   (Diamond 3, Netherite 3)
            8,    // body (horse/wolf)
            3.0f, // toughness (Diamond 2, Netherite 3)
            0,    // knockback resistance
            5,    // enchantment value
            Ingredient.of(CommonTags.Items.INGOTS_LEAD)
    );

    private static Holder<ArmorMaterial> register(String name, int bootsProtection, int legsProtection, int chestProtection, int headProtection, int bodyProtection, float toughness, float knockbackResistance, int enchantmentValue, Ingredient repairIngredient) {
        EnumMap<ArmorItem.Type, Integer> typeProtection = Util.make(new EnumMap<>(ArmorItem.Type.class), attribute -> {
            attribute.put(ArmorItem.Type.BOOTS, bootsProtection);
            attribute.put(ArmorItem.Type.LEGGINGS, legsProtection);
            attribute.put(ArmorItem.Type.CHESTPLATE, chestProtection);
            attribute.put(ArmorItem.Type.HELMET, headProtection);
            attribute.put(ArmorItem.Type.BODY, bodyProtection); // Body is for Horse/Wolf armor
        });

        ResourceLocation location = TheLeadAge.resourceLocation(name);
        Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_IRON;
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(location));

        // RegistrySupplier implements Holder<ArmorMaterial>; expose it as a Holder so
        // the ArmorItem constructor keeps its original signature.
        return ARMOR_MATERIALS.register(name, () ->
                new ArmorMaterial(typeProtection, enchantmentValue, equipSound, () -> repairIngredient, layers, toughness, knockbackResistance));
    }

    public static void register() {
        ARMOR_MATERIALS.register();
    }
}

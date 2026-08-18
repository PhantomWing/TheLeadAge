package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Lead horse armor: solid barding at protection 6, just above iron's 5.
 *
 * <p>It deliberately does <em>not</em> follow the player armour's glass-cannon pattern. That pattern
 * only balances because lead gear breaks almost immediately — and horse armour has no durability at
 * all, so a diamond-tier protection value would simply be a free upgrade with no cost. Hence a plain,
 * modest armour value instead, and none of the Heaviness penalties (no slowness, no added gravity):
 * they made the item fiddly without a downside worth paying for.</p>
 *
 * <p>What survives is the one flavourful upside — the sheer mass of the plate steadies the horse, so
 * it keeps a little knockback resistance. A horse-worn item gets no per-tick hook, so it is applied
 * as a body-slot attribute modifier; the engine gives it to the wearing horse just like the armour
 * value.</p>
 */
public class LeadHorseArmorItem extends Item {
    /** Knockback resistance granted to the wearing horse — the same +10% a full lead set gives a player. */
    private static final double KNOCKBACK_RESISTANCE = 0.1;

    /**
     * Extends {@link Item}, not {@code AnimalArmorItem}: that ctor re-applies the material's BODY
     * attributes to the passed Properties, wiping any custom set (same clobber {@link LeadArmorItem}
     * dodges). All equip behaviour lives in the EQUIPPABLE component {@code animalProperties} builds;
     * the only thing lost is EQUESTRIAN's breaking sound, which is the default ITEM_BREAK anyway.
     */
    public LeadHorseArmorItem(ArmorMaterial material, Properties properties) {
        // 1.21.5: Properties#horseArmor replaces the old animalProperties + HolderSet dance;
        // the custom attribute set still overwrites the material's afterwards, as before.
        super(properties.horseArmor(material).attributes(withKnockbackResistance(material)));
    }

    private static ItemAttributeModifiers withKnockbackResistance(ArmorMaterial material) {
        return material.createAttributes(ArmorType.BODY).withModifierAdded(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(TheLeadAge.resourceLocation("lead_horse_armor_knockback_resistance"),
                        KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(ArmorType.BODY.getSlot()));
    }
}

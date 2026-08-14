package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AnimalArmorItem;
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
public class LeadHorseArmorItem extends AnimalArmorItem {
    /** Knockback resistance granted to the wearing horse — the same +10% a full lead set gives a player. */
    private static final double KNOCKBACK_RESISTANCE = 0.1;

    /**
     * 1.21.2 bakes attributes onto the Properties at construction ({@code getDefaultAttributeModifiers}
     * is gone), so the knockback line is appended to the material's own BODY set here. See
     * {@link LeadArmorItem} for why this applies the material directly instead of via
     * {@code AnimalArmorItem}'s constructor.
     */
    public LeadHorseArmorItem(ArmorMaterial material, BodyType bodyType, Properties properties) {
        super(material, bodyType, properties.attributes(withKnockbackResistance(material)));
    }

    private static ItemAttributeModifiers withKnockbackResistance(ArmorMaterial material) {
        return material.createAttributes(ArmorType.BODY).withModifierAdded(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(TheLeadAge.resourceLocation("lead_horse_armor_knockback_resistance"),
                        KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(ArmorType.BODY.getSlot()));
    }
}

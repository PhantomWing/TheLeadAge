package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Lead horse armor: heavy plate for horses. Beyond the material's protection it
 * weighs the horse down with the same effects a full lead set gives the player —
 * slower movement, increased gravity (faster falling, and faster sinking in water
 * through the normal physics) and some knockback resistance.
 *
 * <p>Unlike the player armor (which hides these behind "Heaviness" and applies them
 * from {@code inventoryTick}), a horse-worn item gets no per-tick hook, so the
 * effects are added as body-slot attribute modifiers — the engine applies them to
 * the wearing horse exactly as it does the armor value. Magnitudes match a full
 * lead set (4 Heaviness).</p>
 */
public class LeadHorseArmorItem extends AnimalArmorItem {
    private ItemAttributeModifiers modifiers;

    public LeadHorseArmorItem(Holder<ArmorMaterial> material, BodyType bodyType, boolean hasOverlay, Properties properties) {
        super(material, bodyType, hasOverlay, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        if (modifiers == null) {
            EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(getEquipmentSlot()); // BODY
            double heaviness = LeadArmorItem.FULL_SET_HEAVINESS;
            modifiers = super.getDefaultAttributeModifiers()
                    .withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                            TheLeadAge.resourceLocation("lead_horse_armor_slowness"),
                            heaviness * LeadArmorItem.SPEED_PENALTY_PER_HEAVINESS,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                    .withModifierAdded(Attributes.GRAVITY, new AttributeModifier(
                            TheLeadAge.resourceLocation("lead_horse_armor_gravity"),
                            heaviness * LeadArmorItem.GRAVITY_BONUS_PER_HEAVINESS,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                    .withModifierAdded(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                            TheLeadAge.resourceLocation("lead_horse_armor_knockback_resistance"),
                            heaviness * LeadArmorItem.KNOCKBACK_RESISTANCE_PER_HEAVINESS,
                            AttributeModifier.Operation.ADD_VALUE), slot);
        }
        return modifiers;
    }
}

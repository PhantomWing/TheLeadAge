package com.phantomwing.theleadage.fabric.compat.vanillabackport;

import com.blackgear.vanillabackport.common.level.items.NautilusArmorItem;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.armor.ModArmorMaterials;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Lead Nautilus Armor as Vanilla Backport's nautilus wears it: its renderer draws any {@link NautilusArmorItem}, from
 * {@code theleadage:textures/entity/nautilus/armor/lead.png}. On the Lead Horse Armor's terms: the material's body
 * protection and the same steadying knockback resistance, as the 1.21.11 line's own. Only loaded when it is installed.
 */
public final class LeadNautilusArmor extends NautilusArmorItem {
    private static final double KNOCKBACK_RESISTANCE = 0.1;

    private ItemAttributeModifiers modifiers;

    private LeadNautilusArmor(Item.Properties properties) {
        super(ModArmorMaterials.LEAD_ARMOR_MATERIAL, properties);
    }

    public static Item create(Item.Properties properties) {
        return new LeadNautilusArmor(properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        if (modifiers == null) {
            modifiers = super.getDefaultAttributeModifiers().withModifierAdded(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(TheLeadAge.resourceLocation("lead_nautilus_armor_knockback_resistance"),
                            KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.bySlot(getEquipmentSlot()));
        }
        return modifiers;
    }
}

package com.phantomwing.theleadage.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;

/**
 * Lead nautilus armor: the nautilus's counterpart of {@link LeadHorseArmorItem}, on the same terms.
 * Nautilus armor takes no damage either, so it gets the same modest protection (the material's BODY
 * value, 6, just above iron's 5) and the same steadying knockback resistance, with no Heaviness.
 */
public class LeadNautilusArmorItem extends Item {
    public LeadNautilusArmorItem(ArmorMaterial material, Properties properties) {
        super(properties.nautilusArmor(material)
                .attributes(LeadHorseArmorItem.withKnockbackResistance(material, "lead_nautilus_armor")));
    }
}

package com.phantomwing.theleadage.fabric.compat.vanillabackport;

import com.blackgear.vanillabackport.common.level.items.NautilusArmorItem;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.armor.ModArmorMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Lead Nautilus Armor as Vanilla Backport's nautilus wears it: its renderer draws any {@link NautilusArmorItem} from the
 * texture it names, here {@code theleadage:textures/entity/nautilus/armor/lead.png}, at the Lead Horse Armor's
 * protection. Vanilla Backport's 1.20.1 nautilus armour carries no attribute modifiers, so it has none of the 1.21.11
 * line's knockback resistance. Only loaded when it is installed.
 */
public final class LeadNautilusArmor extends NautilusArmorItem {
    private static final ResourceLocation TEXTURE = TheLeadAge.resourceLocation("textures/entity/nautilus/armor/lead.png");

    private LeadNautilusArmor(Item.Properties properties) {
        super(ModArmorMaterials.HORSE_ARMOR_PROTECTION, "lead", properties);
    }

    public static Item create(Item.Properties properties) {
        return new LeadNautilusArmor(properties);
    }

    @Override
    public @NotNull ResourceLocation getTexture() {
        return TEXTURE;
    }
}

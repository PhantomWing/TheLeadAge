package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.HorseArmorItem;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
 * it keeps a little knockback resistance. 1.20.1's horse armour carries no attribute modifiers (1.20.5's
 * body slot does), so the horse is given it while it wears the armour, as each loader's {@code LivingEntityMixin}
 * sees what it wears change ({@link #equipmentChanged}).</p>
 *
 * <p>1.20.1's {@link HorseArmorItem} finds its texture in vanilla's namespace from the name it is given, so the mod's
 * own is handed back: {@code theleadage:textures/entity/horse/armor/horse_armor_lead.png}.</p>
 */
public class LeadHorseArmorItem extends HorseArmorItem {
    /** Knockback resistance granted to the wearing horse — the same +10% a full lead set gives a player. */
    private static final double KNOCKBACK_RESISTANCE = 0.1;

    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = TheLeadAge.resourceLocation("lead_horse_armor_knockback_resistance");
    private static final UUID KNOCKBACK_MODIFIER_UUID =
            UUID.nameUUIDFromBytes(KNOCKBACK_MODIFIER_ID.toString().getBytes(StandardCharsets.UTF_8));
    private static final ResourceLocation TEXTURE = TheLeadAge.resourceLocation("textures/entity/horse/armor/horse_armor_lead.png");

    public LeadHorseArmorItem(int protection, Properties properties) {
        super(protection, "lead", properties);
    }

    @Override
    public @NotNull ResourceLocation getTexture() {
        return TEXTURE;
    }

    /** What a horse wears changed, on the server: it is steadied while it wears lead armour, and not otherwise. */
    public static void equipmentChanged(LivingEntity living) {
        if (!(living instanceof AbstractHorse horse)) {
            return;
        }
        AttributeInstance instance = horse.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (instance == null) {
            return;
        }
        boolean wearing = horse.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof LeadHorseArmorItem;
        boolean has = instance.getModifier(KNOCKBACK_MODIFIER_UUID) != null;
        if (wearing && !has) {
            instance.addTransientModifier(new AttributeModifier(KNOCKBACK_MODIFIER_UUID, KNOCKBACK_MODIFIER_ID.toString(),
                    KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADDITION));
        } else if (!wearing && has) {
            instance.removeModifier(KNOCKBACK_MODIFIER_UUID);
        }
    }
}

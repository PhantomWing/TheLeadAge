package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.attribute.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Lead armor: heavy plate. Keeps the material's (very high) protection and adds a
 * single visible "Heaviness" stat in the tooltip. The actual weigh-down effects —
 * slower movement, increased gravity (faster falls / lower jumps) and a faster sink
 * in water — are applied to the wearer and scale with the total Heaviness of the
 * lead armor worn, so they don't clutter the tooltip with raw numbers.
 */
public class LeadArmorItem extends ArmorItem {
    /** Heaviness granted by each lead armor piece (shown as "+N Heaviness"). */
    private static final double HEAVINESS_PER_PIECE = 1.0;

    // Penalties per point of Heaviness worn.
    private static final double SPEED_PENALTY_PER_HEAVINESS = -0.02;   // -2% base speed (-8% full set)
    private static final double GRAVITY_BONUS_PER_HEAVINESS = 0.075;   // +7.5% gravity (+30% full set)
    private static final double WATER_SINK_PER_HEAVINESS = 0.01;       // extra downward velocity/tick while submerged

    private static final ResourceLocation SPEED_MODIFIER_ID = TheLeadAge.resourceLocation("lead_heaviness_slowness");
    private static final ResourceLocation GRAVITY_MODIFIER_ID = TheLeadAge.resourceLocation("lead_heaviness_gravity");
    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private ItemAttributeModifiers modifiers;

    public LeadArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        if (modifiers == null) {
            EquipmentSlot equipmentSlot = getEquipmentSlot();
            // Material protection (armor/toughness) + a single visible Heaviness line.
            modifiers = super.getDefaultAttributeModifiers().withModifierAdded(
                    ModAttributes.HEAVINESS,
                    new AttributeModifier(TheLeadAge.resourceLocation("lead_heaviness_" + equipmentSlot.getName()),
                            HEAVINESS_PER_PIECE, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.bySlot(equipmentSlot));
        }
        return modifiers;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        // Slow + gravity scale with total worn Heaviness. Idempotent, so it's fine
        // that this runs once per worn (or carried) lead piece each tick; it also
        // clears the effect once no lead armor is worn.
        double heaviness = wornHeaviness(living);
        updateModifier(living, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID,
                heaviness * SPEED_PENALTY_PER_HEAVINESS);
        updateModifier(living, Attributes.GRAVITY, GRAVITY_MODIFIER_ID,
                heaviness * GRAVITY_BONUS_PER_HEAVINESS);

        // Faster sink while submerged — applied per worn piece. Skipped entirely
        // while the wearer is actively swimming up (holding jump), so swimming is
        // unaffected; they just sink fast the rest of the time. Applied on the side
        // that owns the entity's movement (client for players, server for mobs) so
        // the local player's swim input is read correctly and there's no desync.
        if (living.getItemBySlot(getEquipmentSlot()) == stack
                && living.isInWater()
                && !living.jumping
                && (living instanceof Player) == level.isClientSide()) {
            Vec3 motion = living.getDeltaMovement();
            living.setDeltaMovement(motion.x, motion.y - HEAVINESS_PER_PIECE * WATER_SINK_PER_HEAVINESS, motion.z);
        }
    }

    /** Total Heaviness from the lead armor pieces this entity is wearing. */
    private static double wornHeaviness(LivingEntity living) {
        double heaviness = 0.0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (living.getItemBySlot(slot).getItem() instanceof LeadArmorItem) {
                heaviness += HEAVINESS_PER_PIECE;
            }
        }
        return heaviness;
    }

    private static void updateModifier(LivingEntity living, Holder<Attribute> attribute, ResourceLocation id, double amount) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (amount == 0.0) {
            instance.removeModifier(id);
        } else {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}

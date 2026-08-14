package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.attribute.ModAttributes;
import dev.architectury.event.events.common.TickEvent;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Lead armor: heavy plate. Keeps the material's (very high) protection and adds a
 * single visible "Heaviness" stat in the tooltip. The actual effects — slower
 * movement, increased gravity (faster falls / lower jumps), a faster sink in water
 * and some knockback resistance — are applied to the wearer and scale with the
 * total Heaviness of the lead armor worn, so they don't clutter the tooltip with
 * raw numbers.
 */
public class LeadArmorItem extends Item {
    /** Heaviness granted by each lead armor piece (shown as "+N Heaviness"). */
    private static final double HEAVINESS_PER_PIECE = 1.0;

    // Penalties per point of Heaviness worn. Player-only: the horse armor deliberately carries no
    // Heaviness (it has no durability cost to offset it) — see LeadHorseArmorItem.
    private static final double SPEED_PENALTY_PER_HEAVINESS = -0.025;       // -2.5% base speed (-10% full set)
    private static final double GRAVITY_BONUS_PER_HEAVINESS = 0.05;         // +5% gravity (+20% full set)
    private static final double KNOCKBACK_RESISTANCE_PER_HEAVINESS = 0.025; // +2.5% knockback resistance (+10% full set)
    private static final double WATER_SINK_PER_HEAVINESS = 0.01;            // extra downward velocity/tick while submerged

    private static final ResourceLocation SPEED_MODIFIER_ID = TheLeadAge.resourceLocation("lead_heaviness_slowness");
    private static final ResourceLocation GRAVITY_MODIFIER_ID = TheLeadAge.resourceLocation("lead_heaviness_gravity");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = TheLeadAge.resourceLocation("lead_heaviness_knockback_resistance");
    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private final ArmorType armorType;

    /**
     * 1.21.2 removed {@code Item#getDefaultAttributeModifiers}: the attribute set is baked onto the
     * Properties at construction. {@code ArmorItem}'s constructor applies
     * {@link ArmorMaterial#humanoidProperties} — which REPLACES the attribute component — so this
     * applies the material itself and then overwrites the set with base + Heaviness. Extending
     * {@link Item} rather than {@code ArmorItem} costs nothing: in 1.21.2 that class is only this
     * constructor, and all armor behaviour comes from the EQUIPPABLE component it sets.
     */
    public LeadArmorItem(ArmorMaterial material, ArmorType type, Properties properties) {
        super(material.humanoidProperties(properties, type).attributes(withHeaviness(material, type)));
        this.armorType = type;
    }

    private static ItemAttributeModifiers withHeaviness(ArmorMaterial material, ArmorType type) {
        EquipmentSlot slot = type.getSlot();
        // Material protection (armor/toughness) + a single visible Heaviness line.
        return material.createAttributes(type).withModifierAdded(
                ModAttributes.HEAVINESS,
                new AttributeModifier(TheLeadAge.resourceLocation("lead_heaviness_" + slot.getName()),
                        HEAVINESS_PER_PIECE, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(slot));
    }

    /**
     * Recompute the Heaviness penalties for a wearer, adding, updating or clearing them as needed.
     *
     * <p>Driven from a <em>player tick</em> ({@link #register}) rather than only from
     * {@link #inventoryTick}. That distinction is the whole point: {@code inventoryTick} runs per
     * lead stack in the inventory, so the moment the last lead piece leaves — taken off and dropped
     * into a chest — nothing is left to run the cleanup and the wearer keeps the slowness and extra
     * gravity for the rest of the session. Ticking the player instead means the clear always happens.</p>
     */
    public static void refreshHeaviness(LivingEntity living) {
        // Heaviness is suspended while the wearer is flying (creative / spectator) so the
        // penalties don't fight free-flight — treated as if no lead armor were worn.
        boolean flying = living instanceof Player player && player.getAbilities().flying;
        double heaviness = flying ? 0.0 : wornHeaviness(living);
        updateModifier(living, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID,
                heaviness * SPEED_PENALTY_PER_HEAVINESS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        updateModifier(living, Attributes.GRAVITY, GRAVITY_MODIFIER_ID,
                heaviness * GRAVITY_BONUS_PER_HEAVINESS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        updateModifier(living, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID,
                heaviness * KNOCKBACK_RESISTANCE_PER_HEAVINESS, AttributeModifier.Operation.ADD_VALUE);
    }

    /** Drives {@link #refreshHeaviness} for players every tick, whether or not they still carry lead. */
    public static void register() {
        TickEvent.PLAYER_POST.register(player -> refreshHeaviness(player));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        // Mobs get their penalties here: the player tick above does not cover them, and a mob only
        // ever loses its armour by dying, so there is no stale-modifier case to clean up.
        boolean flying = living instanceof Player player && player.getAbilities().flying;
        if (!(living instanceof Player)) {
            refreshHeaviness(living);
        }

        // Faster sink while submerged — applied per worn piece. Skipped entirely
        // while the wearer is actively swimming up (holding jump), so swimming is
        // unaffected; they just sink fast the rest of the time. Applied on the side
        // that owns the entity's movement (client for players, server for mobs) so
        // the local player's swim input is read correctly and there's no desync.
        if (!flying
                && living.getItemBySlot(armorType.getSlot()) == stack
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

    private static void updateModifier(LivingEntity living, Holder<Attribute> attribute, ResourceLocation id,
                                       double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (amount == 0.0) {
            instance.removeModifier(id);
        } else {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }
}

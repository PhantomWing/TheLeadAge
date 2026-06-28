package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Lead Sickness — a malaise from breathing lead fumes (mining lead ore). Spreads a handful of small,
 * level-scaling penalties (movement, mining, attack damage, attack speed, and a stamina/hunger drain)
 * rather than one big one. Lead builds up, so repeated exposure stacks it up to III (see
 * {@link LeadFumes}). From level II it also ticks slow, poison-like damage (it won't kill on its own).
 * The queasy screen blur is rendered client-side by {@code LeadSicknessBlur}. Milk cures it.
 */
public class LeadSicknessEffect extends MobEffect {
    private static final int COLOR = 0x55C232;                 // poisonous green (HUD tint + ambient particle cloud)
    // Attribute penalties (per level, via ADD_MULTIPLIED_TOTAL so they scale with the stack).
    private static final double MOVE_PENALTY = -0.05;          // -5% movement speed
    private static final double MINING_PENALTY = -0.10;        // -10% block-break (mining) speed
    private static final double ATTACK_PENALTY = -0.10;        // -10% attack damage (enfeebled)
    private static final double ATTACK_SPEED_PENALTY = -0.10;  // -10% attack speed (sluggish swings)
    // Periodic tick (every TICK_INTERVAL): stamina drain at all levels + poison-like damage at II+.
    private static final int TICK_INTERVAL = 40;               // ~2s
    // Stamina drain applied in 2s chunks; 0.2 ×level per chunk = 0.005 ×level per tick on average,
    // identical to the vanilla Hunger effect's rate (players only).
    private static final float EXHAUSTION_PER_TICK = 0.2f;
    private static final int DAMAGE_MIN_AMPLIFIER = 1;         // damage from level II up
    private static final float TICK_DAMAGE = 1.0f;             // ½ heart ×level per damage tick

    public LeadSicknessEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "lead_sickness_move"),
                MOVE_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.BLOCK_BREAK_SPEED,
                ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "lead_sickness_mining"),
                MINING_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "lead_sickness_attack"),
                ATTACK_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "lead_sickness_attack_speed"),
                ATTACK_SPEED_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % TICK_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Stamina drain: lead saps your energy, so hunger ticks down faster (players only, scales with level).
        if (entity instanceof Player player) {
            player.causeFoodExhaustion(EXHAUSTION_PER_TICK * (amplifier + 1));
        }
        // Poison-like damage from level II up — magic (bypasses armor), but won't drop the entity below 1 HP.
        if (amplifier >= DAMAGE_MIN_AMPLIFIER && entity.getHealth() > 1.0f) {
            entity.hurt(entity.damageSources().magic(), TICK_DAMAGE * amplifier);
        }
        return true;
    }
}

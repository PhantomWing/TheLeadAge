package com.phantomwing.theleadage.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/**
 * The single cross-loader entry point for "a worn/held item just broke".
 *
 * <p>Vanilla has no per-{@link Item} callback for this — {@code ItemStack.hurtAndBreak} funnels every
 * break, tool and armour alike, into {@code LivingEntity#onEquippedItemBroken}. Both loaders mixin
 * there and call through to here, so the behaviour is defined once in common code and the per-loader
 * mixins stay trivial.</p>
 */
public final class LeadEquipmentHooks {
    private LeadEquipmentHooks() {
    }

    /** Called from both loaders' {@code LivingEntityMixin} after vanilla handles the break. */
    public static void onEquippedItemBroken(LivingEntity wearer, Item broken) {
        LeadFumes.equipmentBroken(wearer, broken);
    }
}

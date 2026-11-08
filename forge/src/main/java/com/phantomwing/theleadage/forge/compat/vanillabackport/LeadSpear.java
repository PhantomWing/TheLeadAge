package com.phantomwing.theleadage.forge.compat.vanillabackport;

import com.blackgear.vanillabackport.common.level.items.spear.SpearItem;
import com.phantomwing.theleadage.compat.LeadSpearStats;
import com.phantomwing.theleadage.tool.ModTiers;
import net.minecraft.world.item.Item;

/**
 * The Lead Spear as Vanilla Backport makes a spear (Backported Spears has no 1.20.1 build). Only loaded when Vanilla
 * Backport is; its held model is {@code LeadSpearModels}'s, on the client alone.
 */
public final class LeadSpear {
    private LeadSpear() {
    }

    public static Item create(Item.Properties properties) {
        return new SpearItem(ModTiers.LEAD, LeadSpearStats.ATTACK_DURATION, LeadSpearStats.DAMAGE_MULTIPLIER,
                LeadSpearStats.DELAY, LeadSpearStats.DISMOUNT_TIME, LeadSpearStats.DISMOUNT_THRESHOLD,
                LeadSpearStats.KNOCKBACK_TIME, LeadSpearStats.KNOCKBACK_THRESHOLD, LeadSpearStats.DAMAGE_TIME,
                LeadSpearStats.DAMAGE_THRESHOLD, properties);
    }
}

package com.phantomwing.theleadage.neoforge.compat.vanillabackport;

import com.blackgear.platform.client.GameRendering;
import com.blackgear.vanillabackport.common.level.items.spear.SpearItem;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.LeadSpearStats;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.tool.ModTiers;
import net.minecraft.world.item.Item;

/**
 * The Lead Spear as Vanilla Backport makes a spear, where Backported Spears isn't installed. Only loaded when Vanilla
 * Backport is.
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

    /**
     * Vanilla Backport draws only its own spears held as spears; its Platform library draws the Lead Spear's
     * {@code _in_hand} model in the hand and its own one elsewhere.
     */
    public static void registerHeldModel() {
        GameRendering.registerHandHeldModels(event ->
                event.register(ModItems.LEAD_SPEAR.get(), TheLeadAge.resourceLocation("lead_spear_in_hand")));
    }
}

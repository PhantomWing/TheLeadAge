package com.phantomwing.theleadage.neoforge.compat.spears;

import com.notunanancyowen.spears.Spears;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.LeadSpearStats;
import com.phantomwing.theleadage.tool.ModTiers;
import net.minecraft.world.item.Item;

/** The Lead Spear as Backported Spears makes a spear. Only loaded when it is installed. */
public final class LeadSpear {
    private LeadSpear() {
    }

    /** {@code registerSpearRaw} builds the item without registering it. */
    public static Item create(Item.Properties fallback) {
        return Spears.registerSpearRaw(TheLeadAge.resourceLocation("lead_spear"), ModTiers.LEAD,
                LeadSpearStats.ATTACK_DURATION, LeadSpearStats.DAMAGE_MULTIPLIER, LeadSpearStats.DELAY,
                LeadSpearStats.DISMOUNT_TIME, LeadSpearStats.DISMOUNT_THRESHOLD, LeadSpearStats.KNOCKBACK_TIME,
                LeadSpearStats.KNOCKBACK_THRESHOLD, LeadSpearStats.DAMAGE_TIME, LeadSpearStats.DAMAGE_THRESHOLD);
    }
}

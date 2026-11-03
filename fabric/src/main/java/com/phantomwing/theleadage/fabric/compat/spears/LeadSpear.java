package com.phantomwing.theleadage.fabric.compat.spears;

import com.notunanancyowen.spears.Spears;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.LeadSpearStats;
import com.phantomwing.theleadage.tool.ModTiers;
import net.minecraft.world.item.Item;

/** The Lead Spear as Backported Spears makes a spear. Only loaded when it is installed. */
public final class LeadSpear {
    private LeadSpear() {
    }

    /**
     * Backported Spears registers the components a spear is made of in its own initializer, so on Fabric they exist
     * only when it ran first; otherwise the spear is the plain fallback rather than a crash.
     */
    public static Item create(Item.Properties fallback) {
        if (Spears.USE_EFFECTS == null) {
            TheLeadAge.LOGGER.warn("Backported Spears hasn't registered its spear components yet; the Lead Spear is a plain item");
            return new Item(fallback);
        }
        return Spears.registerSpearRaw(ModTiers.LEAD, LeadSpearStats.ATTACK_DURATION, LeadSpearStats.DAMAGE_MULTIPLIER,
                LeadSpearStats.DELAY, LeadSpearStats.DISMOUNT_TIME, LeadSpearStats.DISMOUNT_THRESHOLD,
                LeadSpearStats.KNOCKBACK_TIME, LeadSpearStats.KNOCKBACK_THRESHOLD, LeadSpearStats.DAMAGE_TIME,
                LeadSpearStats.DAMAGE_THRESHOLD);
    }
}

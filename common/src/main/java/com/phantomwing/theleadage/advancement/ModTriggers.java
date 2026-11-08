package com.phantomwing.theleadage.advancement;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;

/**
 * The advancement triggers for what no vanilla trigger sees. 1.20.1 has no trigger registry (1.20.2's): a trigger is
 * named by its own id and put in {@link CriteriaTriggers}' plain map, from {@code TheLeadAgeCommon.setup()}, which each
 * loader calls on the main thread.
 */
public final class ModTriggers {
    /** A player swam in water raw lead poisons, and breathed it ({@code LeadWater#contaminate}). */
    public static final PlayerTrigger SWAM_IN_POISONED_WATER =
            new PlayerTrigger(TheLeadAge.resourceLocation("swam_in_poisoned_water"));

    private ModTriggers() {
    }

    public static void register() {
        CriteriaTriggers.register(SWAM_IN_POISONED_WATER);
    }
}

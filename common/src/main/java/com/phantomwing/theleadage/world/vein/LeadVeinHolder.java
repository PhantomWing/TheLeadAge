package com.phantomwing.theleadage.world.vein;

import org.jetbrains.annotations.Nullable;

/** A {@code RandomState} carrying its level's lead vein functions, bound by {@link LeadVeins#bind}. Added by mixin. */
public interface LeadVeinHolder {
    @Nullable
    LeadVeins.Functions theleadage$leadVeins();

    void theleadage$setLeadVeins(LeadVeins.Functions functions);
}

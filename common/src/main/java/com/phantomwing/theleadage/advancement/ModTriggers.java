package com.phantomwing.theleadage.advancement;

import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;

/** The advancement triggers for what no vanilla trigger sees. */
public final class ModTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(TheLeadAge.MOD_ID, Registries.TRIGGER_TYPE);

    /** A player swam in water raw lead poisons, and breathed it ({@code LeadWater#contaminate}). */
    public static final RegistrySupplier<PlayerTrigger> SWAM_IN_POISONED_WATER =
            TRIGGERS.register("swam_in_poisoned_water", PlayerTrigger::new);

    private ModTriggers() {
    }

    public static void register() {
        TRIGGERS.register();
    }
}

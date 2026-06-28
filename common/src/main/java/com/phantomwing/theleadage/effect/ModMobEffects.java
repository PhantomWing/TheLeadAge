package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.MOB_EFFECT);

    // Lead Sickness — applied when mining lead ore (see LeadOreBlock); long + subtle (see LeadSicknessClient).
    public static final RegistrySupplier<MobEffect> LEAD_SICKNESS =
            MOB_EFFECTS.register("lead_sickness", LeadSicknessEffect::new);

    /** Holder form (1.21 MobEffectInstance / hasEffect are Holder-based). Resolved lazily, post-registration. */
    public static Holder<MobEffect> leadSicknessHolder() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(LEAD_SICKNESS.get());
    }

    public static void register() {
        MOB_EFFECTS.register();
    }
}

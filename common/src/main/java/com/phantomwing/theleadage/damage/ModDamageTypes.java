package com.phantomwing.theleadage.damage;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

/**
 * The {@code theleadage:lead_weight} damage type used when a falling Lead Weight crushes
 * an entity. Registered through the datapack registry in {@code ModDatapackProvider}
 * (data-driven, so both loaders pick it up). The {@code msgId} "lead_weight" maps to the
 * {@code death.attack.lead_weight(.player)} death-message keys.
 */
public class ModDamageTypes {
    public static final ResourceKey<DamageType> LEAD_WEIGHT =
            ResourceKey.create(Registries.DAMAGE_TYPE, TheLeadAge.resourceLocation("lead_weight"));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(LEAD_WEIGHT,
                new DamageType("lead_weight", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f, DamageEffects.HURT));
    }
}

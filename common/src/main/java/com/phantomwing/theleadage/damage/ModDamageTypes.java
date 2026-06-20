package com.phantomwing.theleadage.damage;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

/**
 * The {@code theleadage:heavy_orb} damage type used when a falling Heavy Orb crushes
 * an entity. Registered through the datapack registry in {@code ModDatapackProvider}
 * (data-driven, so both loaders pick it up). The {@code msgId} "heavy_orb" maps to the
 * {@code death.attack.heavy_orb(.player)} death-message keys.
 */
public class ModDamageTypes {
    public static final ResourceKey<DamageType> HEAVY_ORB =
            ResourceKey.create(Registries.DAMAGE_TYPE, TheLeadAge.resourceLocation("heavy_orb"));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(HEAVY_ORB,
                new DamageType("heavy_orb", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f, DamageEffects.HURT));
    }
}

package com.phantomwing.theleadage.particle;

import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

/**
 * Custom particles. {@code lead_flame} is the lead torch's grayish-white flame — vanilla has no
 * uncoloured flame particle, so this is vanilla's {@code FlameParticle} behaviour (registered per
 * loader) over a recoloured flame sprite.
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> LEAD_FLAME =
            PARTICLES.register("lead_flame", () -> new SimpleParticleType(false) {
            }); // anonymous subclass — the vanilla constructor is protected

    public static void register() {
        PARTICLES.register();
    }
}

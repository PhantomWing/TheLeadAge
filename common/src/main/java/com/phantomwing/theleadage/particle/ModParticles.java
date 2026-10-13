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
 * loader) over a recoloured flame sprite. {@code lead_speck} is a speck of lead drifting in contaminated water, {@code lead_dust} the dust off a block of raw lead,
 * {@code lead_fumes} the fumes rising off a campfire burning over raw lead.
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> LEAD_FLAME =
            PARTICLES.register("lead_flame", () -> new SimpleParticleType(false) {
            }); // anonymous subclass — the vanilla constructor is protected

    public static final RegistrySupplier<SimpleParticleType> LEAD_SPECK =
            PARTICLES.register("lead_speck", () -> new SimpleParticleType(false) {
            });

    public static final RegistrySupplier<SimpleParticleType> LEAD_DUST =
            PARTICLES.register("lead_dust", () -> new SimpleParticleType(false) {
            });

    public static final RegistrySupplier<SimpleParticleType> LEAD_FUMES =
            PARTICLES.register("lead_fumes", () -> new SimpleParticleType(true) {
            });

    public static void register() {
        PARTICLES.register();
    }
}

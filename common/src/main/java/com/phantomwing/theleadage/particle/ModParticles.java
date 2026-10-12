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
 * loader) over a recoloured flame sprite. The lead fumes are vanilla's smoke, tinted lead grey
 * ({@code LeadFumesParticles}).
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> LEAD_FLAME =
            PARTICLES.register("lead_flame", () -> new SimpleParticleType(false) {
            }); // anonymous subclass — the vanilla constructor is protected

    /** The stream off a campfire burning over raw lead, rising as its smoke does. */
    public static final RegistrySupplier<SimpleParticleType> LEAD_FUMES =
            PARTICLES.register("lead_fumes", () -> new SimpleParticleType(true) {
            });

    /** A puff of the fumes over water raw lead contaminates, as sulfur's noxious gas. */
    public static final RegistrySupplier<SimpleParticleType> LEAD_FUMES_GAS =
            PARTICLES.register("lead_fumes_gas", () -> new SimpleParticleType(true) {
            });

    /** Unseen: over contaminated water, it puffs {@link #LEAD_FUMES_GAS} where the fumes reach, as sulfur's gas cloud. */
    public static final RegistrySupplier<SimpleParticleType> LEAD_FUMES_CLOUD =
            PARTICLES.register("lead_fumes_cloud", () -> new SimpleParticleType(true) {
            });

    public static void register() {
        PARTICLES.register();
    }
}

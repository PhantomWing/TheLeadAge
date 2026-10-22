package com.phantomwing.theleadage.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WhiteSmokeParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * Lead fumes - off mined lead ore and raw lead, the lead torch's bursts, crumbling lead gear: vanilla's white smoke in
 * the dark purple of a campfire's smoke over raw lead ({@link LeadCampfireSmoke}), each puff a shade lighter or darker.
 * Registered per loader.
 */
public final class LeadFumesParticles {
    private LeadFumesParticles() {
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final WhiteSmokeParticle.Provider smoke;

        public Provider(SpriteSet sprites) {
            this.smoke = new WhiteSmokeParticle.Provider(sprites);
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            Particle puff = smoke.createParticle(type, level, x, y, z, xSpeed, ySpeed, zSpeed, random);
            // 1.21.9: only a particle drawn as a quad has a colour.
            if (puff instanceof SingleQuadParticle quad) {
                float shade = 0.9F + level.getRandom().nextFloat() * 0.2F;
                quad.setColor(Math.min(1.0F, LeadCampfireSmoke.RED * shade), Math.min(1.0F, LeadCampfireSmoke.GREEN * shade),
                        Math.min(1.0F, LeadCampfireSmoke.BLUE * shade));
            }
            return puff;
        }
    }
}

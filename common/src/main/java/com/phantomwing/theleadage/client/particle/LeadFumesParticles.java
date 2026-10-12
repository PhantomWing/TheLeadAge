package com.phantomwing.theleadage.client.particle;

import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.particle.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

/**
 * Lead fumes: vanilla's smoke, tinted lead grey. The stream off a campfire burning over raw lead behaves as the
 * campfire's own smoke, so it rises as high; the gas over contaminated water as 26.2's sulfur gas, which puffs
 * wherever the fumes reach, so it shows where they do. Registered per loader.
 */
public final class LeadFumesParticles {
    private static final float RED = 0.80F;
    private static final float GREEN = 0.82F;
    private static final float BLUE = 0.88F;

    private LeadFumesParticles() {
    }

    /** The stream: vanilla's cosy campfire smoke. */
    public static class Stream extends TextureSheetParticle {
        Stream(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            super(level, x, y, z);
            scale(3.0F);
            setSize(0.25F, 0.25F);
            lifetime = random.nextInt(50) + 80;
            gravity = 3.0E-6F;
            xd = xSpeed;
            yd = ySpeed + random.nextFloat() / 500.0F;
            zd = zSpeed;
            setColor(RED, GREEN, BLUE);
            setAlpha(0.9F);
        }

        @Override
        public void tick() {
            xo = x;
            yo = y;
            zo = z;
            if (age++ >= lifetime || alpha <= 0.0F) {
                remove();
                return;
            }
            xd += random.nextFloat() / 5000.0F * (random.nextBoolean() ? 1 : -1);
            zd += random.nextFloat() / 5000.0F * (random.nextBoolean() ? 1 : -1);
            yd -= gravity;
            move(xd, yd, zd);
            if (age >= lifetime - 60 && alpha > 0.01F) {
                alpha -= 0.015F;
            }
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                           double xSpeed, double ySpeed, double zSpeed) {
                Stream stream = new Stream(level, x, y, z, xSpeed, ySpeed, zSpeed);
                stream.pickSprite(sprites);
                return stream;
            }
        }
    }

    /** A puff of gas over contaminated water: sulfur's noxious gas, which rises slowly and fades from halfway. */
    public static class Gas extends BaseAshSmokeParticle {
        private static final float SCALE = 3.0F;
        private final float fadeFrom;

        Gas(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
            super(level, x, y, z, 0.1F, 0.1F, 0.1F, xSpeed, ySpeed, zSpeed, SCALE, sprites, 0.3F, 5, -0.02F, true);
            setColor(RED, GREEN, BLUE);
            lifetime = (int) (6.0 / (random.nextFloat() * 0.5 + 0.5) * SCALE);
            fadeFrom = lifetime / 2.0F;
        }

        @Override
        public void tick() {
            super.tick();
            if (age > fadeFrom) {
                setAlpha(Math.max(0.0F, 1.0F - (age - fadeFrom) / (lifetime - fadeFrom)));
            }
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                           double xSpeed, double ySpeed, double zSpeed) {
                return new Gas(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
            }
        }
    }

    /**
     * Unseen, at the spot contaminated water's fumes come out: a puff every other tick at a random point within
     * the fumes' range that they reach, as sulfur's gas cloud. It lives two seconds rather than sulfur's one,
     * since a block's display ticks come about every two.
     */
    public static class Cloud extends NoRenderParticle {
        Cloud(ClientLevel level, double x, double y, double z) {
            super(level, x, y, z);
            lifetime = 40;
        }

        @Override
        public void tick() {
            super.tick();
            if (age % 2 != 0) {
                return;
            }
            BlockPos spot = BlockPos.containing(x, y, z);
            Vec3 direction = new Vec3(random.nextFloat() - 0.5, 0.0, random.nextFloat() - 0.5).normalize();
            Vec3 at = Vec3.atCenterOf(spot).add(direction.scale(random.nextFloat() * LeadWater.RANGE)).subtract(0.0, 0.25, 0.0);
            if (LeadWater.reaches(level, spot, at)) {
                level.addAlwaysVisibleParticle(ModParticles.LEAD_FUMES_GAS.get(), at.x, at.y, at.z, 0.0, 0.0, 0.0);
            }
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                           double xSpeed, double ySpeed, double zSpeed) {
                return new Cloud(level, x, y, z);
            }
        }
    }
}

package com.phantomwing.theleadage.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Lead fumes rising off a campfire burning over raw lead: the lead torch's white smoke in look, rising as fast and as
 * long as the campfire's own smoke on average, so the two climb together. Each puff meanders as it goes - drifting
 * this way and that, turning, swelling as it thins - and fades out over its last three seconds as campfire smoke does.
 * Registered per loader.
 */
public class LeadFumesParticle extends TextureSheetParticle {
    private static final int FADE_OUT = 60;
    /** How hard the drift wanders each tick, and how much of it is kept: together, how far a puff strays. */
    private static final float WANDER = 0.003F;
    private static final float WANDER_KEPT = 0.96F;

    private final float spin;
    private final float startSize;

    LeadFumesParticle(ClientLevel level, SpriteSet sprites, double x, double y, double z, double xSpeed, double ySpeed,
                      double zSpeed) {
        super(level, x, y, z);
        pickSprite(sprites);
        scale(1.2F + random.nextFloat() * 0.8F);
        startSize = quadSize;
        setSize(0.25F, 0.25F);
        lifetime = random.nextInt(50) + 80;
        gravity = 3.0E-6F;
        xd = xSpeed + (random.nextFloat() - 0.5F) * 0.01F;
        yd = ySpeed * (0.85F + random.nextFloat() * 0.3F);
        zd = zSpeed + (random.nextFloat() - 0.5F) * 0.01F;
        spin = (random.nextFloat() - 0.5F) * 0.06F;
        roll = random.nextFloat() * (float) (Math.PI * 2);
        oRoll = roll;
        float shade = 0.86F + random.nextFloat() * 0.1F;
        setColor(shade, shade, shade * 1.03F);
        alpha = 0.0F;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        oRoll = roll;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        xd = (xd + (random.nextFloat() - 0.5F) * WANDER) * WANDER_KEPT;
        zd = (zd + (random.nextFloat() - 0.5F) * WANDER) * WANDER_KEPT;
        yd -= gravity;
        move(xd, yd, zd);
        roll += spin;
        quadSize = startSize * (1.0F + 0.8F * age / lifetime);
        alpha = Math.min(0.85F, Math.min(age / 10.0F, (lifetime - age) / (float) FADE_OUT));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new LeadFumesParticle(level, sprites, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}

package com.phantomwing.theleadage.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Lead fumes rising off a campfire burning over raw lead: the lead torch's white smoke in look, the campfire's own
 * smoke in how it moves - as fast, as long and so as high - so the two rise together. It shrinks as it rises, as white
 * smoke does, and fades out over its last three seconds as campfire smoke does. Registered per loader.
 */
public class LeadFumesParticle extends TextureSheetParticle {
    private static final int FADE_OUT = 60;

    private final SpriteSet sprites;

    LeadFumesParticle(ClientLevel level, SpriteSet sprites, double x, double y, double z, double xSpeed, double ySpeed,
                      double zSpeed) {
        super(level, x, y, z);
        this.sprites = sprites;
        scale(1.6F);
        setSize(0.25F, 0.25F);
        lifetime = random.nextInt(50) + 80;
        gravity = 3.0E-6F;
        xd = xSpeed;
        yd = ySpeed + random.nextFloat() / 500.0F;
        zd = zSpeed;
        float shade = 0.86F + random.nextFloat() * 0.1F;
        setColor(shade, shade, shade * 1.03F);
        alpha = 0.9F;
        setSpriteFromAge(sprites);
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
        if (age >= lifetime - FADE_OUT && alpha > 0.01F) {
            alpha -= 0.015F;
        }
        setSpriteFromAge(sprites);
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

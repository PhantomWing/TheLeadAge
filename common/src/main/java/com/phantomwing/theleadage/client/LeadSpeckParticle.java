package com.phantomwing.theleadage.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;

/**
 * A mote of lead, the size of vanilla's underwater speck, fading in and out so a steady stream of them reads as murk
 * or dust rather than as specks coming and going. Two kinds, registered per loader:
 * <ul>
 *     <li>{@link Speck}, in contaminated water ({@link LeadWaterSpecks}): dark grey, sinking slowly as it drifts, and
 *     gone once out of the water.</li>
 *     <li>{@link Dust}, off a block of raw lead: the raw lead's own grey, lifting slowly off it as it drifts.</li>
 * </ul>
 */
public class LeadSpeckParticle extends TextureSheetParticle {
    private static final int FADE_IN = 10;
    private static final int FADE_OUT = 20;

    private final boolean underwater;

    LeadSpeckParticle(ClientLevel level, SpriteSet sprites, double x, double y, double z, boolean underwater) {
        super(level, x, y, z);
        this.underwater = underwater;
        setSize(0.01F, 0.01F);
        pickSprite(sprites);
        hasPhysics = false;
        gravity = 0.0F;
        friction = 1.0F;
        xd = (random.nextFloat() - 0.5F) * 0.004F;
        zd = (random.nextFloat() - 0.5F) * 0.004F;
        float shade;
        if (underwater) {
            quadSize *= random.nextFloat() * 0.6F + 0.5F;
            lifetime = (int) (60.0F / (random.nextFloat() * 0.6F + 0.4F));
            yd = -0.002F - random.nextFloat() * 0.003F;
            shade = 0.16F + random.nextFloat() * 0.12F;
        } else {
            quadSize *= random.nextFloat() * 0.4F + 0.4F;
            lifetime = (int) (40.0F / (random.nextFloat() * 0.6F + 0.4F));
            yd = 0.003F + random.nextFloat() * 0.004F;
            shade = 0.36F + random.nextFloat() * 0.14F;
        }
        setColor(shade, shade * 1.04F, shade * 1.12F);
        alpha = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (underwater && !level.getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER)) {
            remove();
            return;
        }
        alpha = Math.min(1.0F, Math.min(age / (float) FADE_IN, (lifetime - age) / (float) FADE_OUT));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** A speck of lead in contaminated water. */
    public static class Speck implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Speck(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new LeadSpeckParticle(level, sprites, x, y, z, true);
        }
    }

    /** Lead dust off a block of raw lead. */
    public static class Dust implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Dust(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new LeadSpeckParticle(level, sprites, x, y, z, false);
        }
    }
}

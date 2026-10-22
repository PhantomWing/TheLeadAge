package com.phantomwing.theleadage.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;

/**
 * A mote of lead, the size of vanilla's underwater speck, in the dark purple of the lead's fumes
 * ({@link LeadCampfireSmoke}), fading in and out so a steady stream of them reads as murk or dust rather than as specks
 * coming and going. Two kinds, registered per loader:
 * <ul>
 *     <li>{@link Speck}, in contaminated water ({@link LeadWaterSpecks}): darker, sinking slowly as it drifts, and
 *     gone once out of the water.</li>
 *     <li>{@link Dust}, off a block of raw lead: lighter, lifting slowly off it as it drifts.</li>
 * </ul>
 */
public class LeadSpeckParticle extends SingleQuadParticle {
    private static final int FADE_IN = 10;
    private static final int FADE_OUT = 20;

    private final boolean underwater;

    // 1.21.9: a particle is given its sprite as it is made, the provider picking one of the set.
    LeadSpeckParticle(ClientLevel level, TextureAtlasSprite sprite, double x, double y, double z, boolean underwater) {
        super(level, x, y, z, sprite);
        this.underwater = underwater;
        setSize(0.01F, 0.01F);
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
            shade = 0.55F + random.nextFloat() * 0.3F;
        } else {
            quadSize *= random.nextFloat() * 0.4F + 0.4F;
            lifetime = (int) (40.0F / (random.nextFloat() * 0.6F + 0.4F));
            yd = 0.003F + random.nextFloat() * 0.004F;
            shade = 0.95F + random.nextFloat() * 0.25F;
        }
        setColor(Math.min(1.0F, LeadCampfireSmoke.RED * shade), Math.min(1.0F, LeadCampfireSmoke.GREEN * shade),
                Math.min(1.0F, LeadCampfireSmoke.BLUE * shade));
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
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    /** A speck of lead in contaminated water. */
    public static class Speck implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Speck(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new LeadSpeckParticle(level, sprites.get(random), x, y, z, true);
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
                                       double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new LeadSpeckParticle(level, sprites.get(random), x, y, z, false);
        }
    }
}

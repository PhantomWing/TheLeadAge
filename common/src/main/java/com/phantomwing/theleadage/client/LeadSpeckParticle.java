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
 * A speck of lead in contaminated water: vanilla's underwater speck, dark grey, sinking slowly as it drifts, fading
 * in and out so a steady stream of them reads as murk rather than as specks coming and going. Gone once out of the
 * water. Spawned by {@link LeadWaterSpecks}; registered per loader.
 */
public class LeadSpeckParticle extends TextureSheetParticle {
    private static final int FADE_IN = 10;
    private static final int FADE_OUT = 20;

    LeadSpeckParticle(ClientLevel level, SpriteSet sprites, double x, double y, double z) {
        super(level, x, y, z);
        setSize(0.01F, 0.01F);
        pickSprite(sprites);
        quadSize *= random.nextFloat() * 0.6F + 0.5F;
        lifetime = (int) (60.0F / (random.nextFloat() * 0.6F + 0.4F));
        hasPhysics = false;
        gravity = 0.0F;
        friction = 1.0F;
        xd = (random.nextFloat() - 0.5F) * 0.004F;
        yd = -0.002F - random.nextFloat() * 0.003F;
        zd = (random.nextFloat() - 0.5F) * 0.004F;
        float shade = 0.16F + random.nextFloat() * 0.12F;
        setColor(shade, shade * 1.05F, shade * 1.15F);
        alpha = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (!level.getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER)) {
            remove();
            return;
        }
        alpha = Math.min(1.0F, Math.min(age / (float) FADE_IN, (lifetime - age) / (float) FADE_OUT));
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
            return new LeadSpeckParticle(level, sprites, x, y, z);
        }
    }
}

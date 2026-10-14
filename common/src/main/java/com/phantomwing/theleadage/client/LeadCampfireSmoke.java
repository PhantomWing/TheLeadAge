package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.effect.CampfireFumes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;

/**
 * The smoke of a campfire burning over raw lead, tainted by its fumes: vanilla's own campfire smoke, in the dark purple
 * of the lead's pixel art (the Block of Lead's {@code #5f5e83}), as the water raw lead leaches into is tinted. Spawned in
 * place of the plain smoke by each loader's {@code CampfireBlockMixin}.
 */
public final class LeadCampfireSmoke {
    static final float RED = 0x5F / 255.0F;
    static final float GREEN = 0x5E / 255.0F;
    static final float BLUE = 0x83 / 255.0F;

    private LeadCampfireSmoke() {
    }

    /**
     * Spawns a puff of the campfire at {@code pos}'s smoke, tainted, where it burns over raw lead. Whether it did: the
     * plain puff is spawned where it didn't.
     */
    public static boolean spawn(Level level, BlockPos pos, ParticleOptions smoke, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed) {
        if (!level.isClientSide() || !CampfireFumes.isFuming(level, pos, level.getBlockState(pos))) {
            return false;
        }
        // What an always-visible particle goes through anyway, and it hands the particle back to be tinted.
        Particle puff = Minecraft.getInstance().particleEngine.createParticle(smoke, x, y, z, xSpeed, ySpeed, zSpeed);
        if (puff != null) {
            puff.setColor(RED, GREEN, BLUE);
        }
        return true;
    }
}

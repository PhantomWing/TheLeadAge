package com.phantomwing.theleadage.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.phantomwing.theleadage.client.LeadCampfireSmoke;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Taints the smoke of a campfire burning over raw lead ({@link LeadCampfireSmoke}): each puff of its campfire smoke is
 * spawned tinted rather than plain. The Fabric twin is the identically-named mixin in the {@code fabric} source set.
 */
@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    @WrapOperation(method = "makeParticles", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V"))
    private static void theleadage$leadSmoke(Level level, ParticleOptions smoke, boolean force, double x, double y, double z,
                                            double xSpeed, double ySpeed, double zSpeed, Operation<Void> original,
                                            @Local(argsOnly = true) BlockPos pos) {
        if (!LeadCampfireSmoke.spawn(level, pos, smoke, x, y, z, xSpeed, ySpeed, zSpeed)) {
            original.call(level, smoke, force, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}

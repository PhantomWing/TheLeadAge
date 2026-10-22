package com.phantomwing.theleadage.neoforge.mixin;

import com.phantomwing.theleadage.client.LeadWaterParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tints splashes and bubbles in contaminated water as they are added ({@link LeadWaterParticles}). */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Inject(method = "add", at = @At("HEAD"))
    private void theleadage$leadWater(Particle particle, CallbackInfo ci) {
        LeadWaterParticles.tint(particle);
    }
}

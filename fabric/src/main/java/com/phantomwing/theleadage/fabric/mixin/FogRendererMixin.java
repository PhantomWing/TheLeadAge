package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.client.LeadWaterTint;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns the fog grey inside the water raw lead contaminates ({@link LeadWaterTint#fog}), which is all that shows of its
 * tint from under the surface. Set once everything else has, NeoForge's fog colour event included. From 26.1 the colour
 * is written into the fog data {@code setupFog} hands it rather than returned, and that is both what the frame is
 * cleared to and the fog colour the shaders get. The NeoForge twin is the identically-named mixin in the {@code neoforge}
 * source set.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "computeFogColor", at = @At("RETURN"))
    private void theleadage$leadMurk(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                     float darkenWorld, Vector4f fog, CallbackInfo ci) {
        float[] murk = LeadWaterTint.fog(camera, level, fog.x(), fog.y(), fog.z());
        if (murk != null) {
            fog.set(murk[0], murk[1], murk[2], fog.w());
        }
    }
}

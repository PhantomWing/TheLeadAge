package com.phantomwing.theleadage.neoforge.mixin;

import com.phantomwing.theleadage.client.LeadWaterTint;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Turns the fog grey inside the water raw lead contaminates ({@link LeadWaterTint#fog}), which is all that shows of its
 * tint from under the surface. Set once everything else has, NeoForge's fog colour event included; the colour this
 * returns is both what the frame is cleared to and the fog colour {@code setupFog} writes for the shaders. The Fabric
 * twin is the identically-named mixin in the {@code fabric} source set.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "computeFogColor", at = @At("RETURN"), cancellable = true)
    private void theleadage$leadMurk(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                     float darkenWorld, CallbackInfoReturnable<Vector4f> cir) {
        Vector4f fog = cir.getReturnValue();
        float[] murk = LeadWaterTint.fog(camera, level, fog.x(), fog.y(), fog.z());
        if (murk != null) {
            cir.setReturnValue(new Vector4f(murk[0], murk[1], murk[2], fog.w()));
        }
    }
}

package com.phantomwing.theleadage.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.phantomwing.theleadage.client.LeadWaterTint;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns the fog grey inside the water raw lead contaminates ({@link LeadWaterTint#fog}), which is all that shows of its
 * tint from under the surface. The fog colour is also the shaders' {@code fogColor}. The NeoForge twin is the
 * identically-named mixin in the {@code neoforge} source set.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void theleadage$leadMurk(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                           float darkenWorld, CallbackInfo ci) {
        float[] murk = LeadWaterTint.fog(camera, level, fogRed, fogGreen, fogBlue);
        if (murk != null) {
            fogRed = murk[0];
            fogGreen = murk[1];
            fogBlue = murk[2];
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
        }
    }
}

package com.phantomwing.theleadage.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.effect.ModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Lead Sickness screen effect: a custom radial blur post-process that slowly pulses inward from the
 * screen edges toward the centre (centre stays sharp) while the effect is active — a queasy "vision
 * swimming" feel with no darkness. Driven before the HUD renders so the world blurs but the HUD stays
 * crisp. Data-driven post-shader ({@code shaders/post/lead_sickness_blur.json}), so no mixin.
 */
public final class LeadSicknessBlur {
    private static final ResourceLocation POST =
            ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "shaders/post/lead_sickness_blur.json");
    private static final Logger LOGGER = LoggerFactory.getLogger("TheLeadAge/LeadSicknessBlur");

    private static final int SLOW_PERIOD = 30;     // heartbeat cycle (ticks) at level I — slow, heavy
    private static final int FAST_PERIOD = 16;     // heartbeat cycle at level III — racing
    private static final float MIN_RADIUS = 5.0f;  // blur radius (px) between beats
    private static final float MAX_RADIUS = 16.0f; // blur radius (px) on the beat
    private static final float FADE_STEP = 0.06f;  // fade per tick (~0.8s to ramp the effect in/out)

    private static PostChain chain;
    private static boolean failed;
    private static int width = -1;
    private static int height = -1;
    private static float envelope;                 // 0..1 fade level, eased toward 1 while sick
    private static int lastTick = Integer.MIN_VALUE;
    private static int lastAmplifier;              // remembered stack level (drives intensity during fade-out)

    private LeadSicknessBlur() {
    }

    public static void render() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (failed || player == null) {
            envelope = 0.0f;
            lastTick = Integer.MIN_VALUE;
            return;
        }
        // Ease the fade envelope toward 1 while sick, 0 otherwise — stepped per game tick so the fade
        // in/out is smooth and frame-rate independent. The blur radius (and the vignette) scale by it.
        var sicknessEffect = player.getEffect(ModMobEffects.leadSicknessHolder());
        boolean sick = sicknessEffect != null;
        if (sick) {
            lastAmplifier = sicknessEffect.getAmplifier(); // kept so the fade-out holds the level's intensity
        }
        if (player.tickCount != lastTick) {
            int steps = (lastTick == Integer.MIN_VALUE || player.tickCount < lastTick)
                    ? 1 : player.tickCount - lastTick;
            lastTick = player.tickCount;
            float target = sick ? 1.0f : 0.0f;
            if (envelope < target) {
                envelope = Math.min(target, envelope + FADE_STEP * steps);
            } else if (envelope > target) {
                envelope = Math.max(target, envelope - FADE_STEP * steps);
            }
        }
        if (envelope <= 0.001f) {
            return; // fully faded out — nothing to draw
        }
        RenderTarget main = minecraft.getMainRenderTarget();
        if (!ensureChain(minecraft, main)) {
            return;
        }
        // Stronger and faster-beating at higher levels; the fade envelope eases it in and out.
        float levelScale = (lastAmplifier + 1) / 3.0f;    // level I 0.33, II 0.67, III 1.0
        float intensity = envelope * levelScale;
        int period = Math.round(Mth.lerp(levelScale, (float) SLOW_PERIOD, (float) FAST_PERIOD));
        float phase = (player.tickCount % period) / (float) period;
        float radius = Mth.lerp(heartbeat(phase), MIN_RADIUS, MAX_RADIUS) * intensity;

        chain.setUniform("Radius", radius);
        chain.setUniform("Intensity", intensity); // drives the vignette strength (fade × level)
        chain.process(0.0f); // partialTick only feeds an unused Time uniform
        main.bindWrite(false); // restore the main target so the HUD draws into it afterwards
    }

    /** A double-thump heartbeat (lub-dub): two sharp spikes early in the cycle, then rest. */
    private static float heartbeat(float phase) {
        float lub = (float) Math.exp(-square(phase - 0.10f) / (2.0f * 0.040f * 0.040f));
        float dub = 0.55f * (float) Math.exp(-square(phase - 0.26f) / (2.0f * 0.045f * 0.045f));
        return Math.min(1.0f, lub + dub);
    }

    private static float square(float x) {
        return x * x;
    }

    /** Lazily build the post chain and keep it sized to the window; disables itself on load failure. */
    private static boolean ensureChain(Minecraft minecraft, RenderTarget main) {
        if (chain != null && main.width == width && main.height == height) {
            return true;
        }
        try {
            if (chain != null) {
                chain.close();
            }
            chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), main, POST);
            chain.resize(main.width, main.height);
            width = main.width;
            height = main.height;
            return true;
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Failed to load lead_sickness_blur post effect; disabling the wobble.", e);
            failed = true;
            chain = null;
            return false;
        }
    }
}

package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.client.LeadWaterParticles;
import com.phantomwing.theleadage.client.MurkyWater;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

/**
 * Registers the sprite sources contaminated water's textures are made by ({@link MurkyWater}), and its splashes' and
 * bubbles' ({@link LeadWaterParticles}), before the atlases load; the water's textures are swapped in by
 * {@code FluidRendererMixin}, the particles' by {@code ParticleEngineMixin}.
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID, value = Dist.CLIENT)
public final class MurkyWaterNeoForge {
    private MurkyWaterNeoForge() {
    }

    @SubscribeEvent
    public static void registerSpriteSources(RegisterSpriteSourcesEvent event) {
        event.register(MurkyWater.TYPE_ID, MurkyWater.CODEC);
        event.register(LeadWaterParticles.TYPE_ID, LeadWaterParticles.CODEC);
    }
}

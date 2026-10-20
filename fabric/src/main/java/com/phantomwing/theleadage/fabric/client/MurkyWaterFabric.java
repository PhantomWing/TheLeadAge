package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.client.LeadWaterParticles;
import com.phantomwing.theleadage.client.MurkyWater;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;

/**
 * Fabric's side of the murkier textures contaminated water is drawn with ({@link MurkyWater}): their sprite source,
 * and that of the splashes' and bubbles' ({@link LeadWaterParticles}), registered before the atlases load. The sprites
 * reach the water through {@code WaterRenderHandlerMixin}. The NeoForge twin is {@code MurkyWaterNeoForge}.
 */
public final class MurkyWaterFabric {
    private MurkyWaterFabric() {
    }

    public static void register() {
        MurkyWater.type = new SpriteSourceType(MurkyWater.CODEC);
        SpriteSources.TYPES.put(MurkyWater.TYPE_ID, MurkyWater.type);
        LeadWaterParticles.type = new SpriteSourceType(LeadWaterParticles.CODEC);
        SpriteSources.TYPES.put(LeadWaterParticles.TYPE_ID, LeadWaterParticles.type);
    }
}

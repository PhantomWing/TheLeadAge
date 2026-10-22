package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.client.LeadWaterParticles;
import com.phantomwing.theleadage.client.MurkyWater;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;

/**
 * Fabric's side of the murkier textures contaminated water is drawn with ({@link MurkyWater}): their sprite source,
 * and that of the splashes' and bubbles' ({@link LeadWaterParticles}), registered before the atlases load. The sprites
 * reach the water through {@code FluidRendererMixin}. The NeoForge twin is {@code MurkyWaterNeoForge}.
 *
 * <p>Up to 1.21.11 this also registered a render handler for water that handed everything on to Fabric's own, since
 * Sodium on Fabric blended water's colour only under a mod's handler. 26.1's Fabric API registers a fluid's handler with
 * a model of its own, which would take water's over, and this line has no Sodium to show what it does now, so there is
 * none.</p>
 */
public final class MurkyWaterFabric {
    private MurkyWaterFabric() {
    }

    public static void register() {
        // 1.21.5: a sprite source type is its codec, in vanilla's late-bound id mapper.
        SpriteSources.ID_MAPPER.put(MurkyWater.TYPE_ID, MurkyWater.CODEC);
        SpriteSources.ID_MAPPER.put(LeadWaterParticles.TYPE_ID, LeadWaterParticles.CODEC);
    }
}

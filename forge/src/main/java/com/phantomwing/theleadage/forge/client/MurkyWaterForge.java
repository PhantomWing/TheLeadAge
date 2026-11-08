package com.phantomwing.theleadage.forge.client;

import com.phantomwing.theleadage.client.LeadWaterParticles;
import com.phantomwing.theleadage.client.MurkyWater;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;

/**
 * Registers the sprite sources contaminated water's textures are made by ({@link MurkyWater}), and its splashes' and
 * bubbles' ({@link LeadWaterParticles}), before the atlases load; the water's textures are swapped in by
 * {@code ForgeHooksClientMixin}, the particles' by {@code ParticleEngineMixin}. Forge 47 has no event for a mod's
 * sprite source type (NeoForge's {@code RegisterSpriteSourceTypesEvent}), so they go in vanilla's map, as Fabric's do,
 * during mod construction, which comes before the first resource load.
 */
public final class MurkyWaterForge {
    private MurkyWaterForge() {
    }

    public static void register() {
        MurkyWater.type = new SpriteSourceType(MurkyWater.CODEC.codec());
        SpriteSources.TYPES.put(MurkyWater.TYPE_ID, MurkyWater.type);
        LeadWaterParticles.type = new SpriteSourceType(LeadWaterParticles.CODEC.codec());
        SpriteSources.TYPES.put(LeadWaterParticles.TYPE_ID, LeadWaterParticles.type);
    }
}

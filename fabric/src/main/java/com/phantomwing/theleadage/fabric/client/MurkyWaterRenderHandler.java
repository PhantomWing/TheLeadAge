package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.client.LeadWaterParticles;
import com.phantomwing.theleadage.client.MurkyWater;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric's water render handler, drawing contaminated water with its murkier textures ({@link MurkyWater}): vanilla's
 * fluid renderer and Sodium's both ask the registered handler for a block's sprites. Everything else is the handler it
 * wraps.
 */
public final class MurkyWaterRenderHandler implements FluidRenderHandler {
    private final FluidRenderHandler water;

    private MurkyWaterRenderHandler(FluidRenderHandler water) {
        this.water = water;
    }

    /**
     * Registers the textures' sprite source, and that of the splashes' and bubbles' ({@link LeadWaterParticles}), before
     * the atlases load, and wraps the water handler.
     */
    public static void register() {
        MurkyWater.type = new SpriteSourceType(MurkyWater.CODEC);
        SpriteSources.TYPES.put(MurkyWater.TYPE_ID, MurkyWater.type);
        LeadWaterParticles.type = new SpriteSourceType(LeadWaterParticles.CODEC);
        SpriteSources.TYPES.put(LeadWaterParticles.TYPE_ID, LeadWaterParticles.type);
        FluidRenderHandler water = FluidRenderHandlerRegistry.INSTANCE.get(Fluids.WATER);
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.WATER, Fluids.FLOWING_WATER, new MurkyWaterRenderHandler(water));
    }

    @Override
    public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
        TextureAtlasSprite[] sprites = water.getFluidSprites(view, pos, state);
        TextureAtlasSprite[] murky = pos == null ? null : MurkyWater.sprites(pos, state, sprites);
        return murky != null ? murky : sprites;
    }

    @Override
    public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
        return water.getFluidColor(view, pos, state);
    }

    @Override
    public void reloadTextures(TextureAtlas textureAtlas) {
        water.reloadTextures(textureAtlas);
    }
}

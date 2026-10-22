package com.phantomwing.theleadage.fabric.client;

import com.phantomwing.theleadage.client.LeadWaterParticles;
import com.phantomwing.theleadage.client.MurkyWater;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric's side of the murkier textures contaminated water is drawn with ({@link MurkyWater}): their sprite source,
 * and that of the splashes' and bubbles' ({@link LeadWaterParticles}), registered before the atlases load. The sprites
 * reach the water through {@code WaterRenderHandlerMixin}. The NeoForge twin is {@code MurkyWaterNeoForge}.
 */
public final class MurkyWaterFabric {
    private MurkyWaterFabric() {
    }

    public static void register() {
        // 1.21.5: a sprite source type is its codec, in vanilla's late-bound id mapper.
        SpriteSources.ID_MAPPER.put(MurkyWater.TYPE_ID, MurkyWater.CODEC);
        SpriteSources.ID_MAPPER.put(LeadWaterParticles.TYPE_ID, LeadWaterParticles.CODEC);
        // Sodium on Fabric blends water's colour from corner to corner only where a mod registered a render handler for
        // it; without one it paints each block one colour, and the purple tint stepped at every block. A mod that
        // registered one already has it blended, and keeps its own.
        if (FluidRenderHandlerRegistry.INSTANCE.getOverride(Fluids.WATER) == null) {
            FluidRenderHandler water = FluidRenderHandlerRegistry.INSTANCE.get(Fluids.WATER);
            FluidRenderHandlerRegistry.INSTANCE.register(Fluids.WATER, Fluids.FLOWING_WATER, new SameWater(water));
        }
    }

    /** Fabric's own water handler, registered as a mod's: everything is the handler's. */
    private static final class SameWater implements FluidRenderHandler {
        private final FluidRenderHandler water;

        private SameWater(FluidRenderHandler water) {
            this.water = water;
        }

        @Override
        public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
            return water.getFluidSprites(view, pos, state);
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
}

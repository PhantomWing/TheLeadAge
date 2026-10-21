package com.phantomwing.theleadage.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.phantomwing.theleadage.client.MurkyWater;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Contaminated water drawn with its murkier textures ({@link MurkyWater}), through Fabric's own water render handler,
 * which vanilla's fluid renderer and Sodium's both ask for a block's sprites, also through the mod's own pass-through
 * handler ({@code MurkyWaterFabric}). Here rather than in a handler of the mod's, which would wrap a handler another mod
 * registers for water and lose its own rendering. Fabric's handler is internal, so this is allowed not to apply: the
 * water then draws with vanilla's textures, tinted.
 */
@Mixin(targets = "net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl$WaterRenderHandler")
public abstract class WaterRenderHandlerMixin {
    @ModifyReturnValue(method = "getFluidSprites", at = @At("RETURN"), require = 0)
    private TextureAtlasSprite[] theleadage$murkyWater(TextureAtlasSprite[] sprites, @Local(argsOnly = true) BlockPos pos,
                                                       @Local(argsOnly = true) FluidState state) {
        TextureAtlasSprite[] murky = pos == null ? null : MurkyWater.sprites(pos, state, sprites);
        return murky != null ? murky : sprites;
    }
}

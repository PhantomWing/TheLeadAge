package com.phantomwing.theleadage.neoforge.mixin;

import com.phantomwing.theleadage.client.MurkyWater;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Draws contaminated water with its murkier textures ({@link MurkyWater}): every NeoForge fluid renderer asks this for
 * a block's sprites - vanilla's, and Sodium's and Embeddium's.
 */
@Mixin(value = FluidSpriteCache.class, remap = false)
public class FluidSpriteCacheMixin {
    @Inject(method = "getFluidSprites", at = @At("RETURN"), cancellable = true)
    private static void theleadage$murkyWater(BlockAndTintGetter level, BlockPos pos, FluidState state,
                                              CallbackInfoReturnable<TextureAtlasSprite[]> cir) {
        TextureAtlasSprite[] murky = MurkyWater.sprites(pos, state, cir.getReturnValue());
        if (murky != null) {
            cir.setReturnValue(murky);
        }
    }
}

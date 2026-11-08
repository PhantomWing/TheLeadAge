package com.phantomwing.theleadage.forge.mixin;

import com.phantomwing.theleadage.client.MurkyWater;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Draws contaminated water with its murkier textures ({@link MurkyWater}): Forge 47's patched fluid renderer asks this
 * for a block's sprites (NeoForge's {@code FluidSpriteCache} from 1.20.2).
 */
@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {
    @Inject(method = "getFluidSprites", at = @At("RETURN"), cancellable = true)
    private static void theleadage$murkyWater(BlockAndTintGetter level, BlockPos pos, FluidState state,
                                              CallbackInfoReturnable<TextureAtlasSprite[]> cir) {
        TextureAtlasSprite[] murky = MurkyWater.sprites(pos, state, cir.getReturnValue());
        if (murky != null) {
            cir.setReturnValue(murky);
        }
    }
}

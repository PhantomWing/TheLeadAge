package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.client.LeadWaterTint;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the water around raw lead again when its spreading moves on or the water it leaks through changes
 * ({@link LeadWaterTint#blockChanged}), which a block change only does for the block's own neighbours: water further
 * out kept its old tint. The NeoForge twin is the identically-named mixin in the {@code neoforge} source set.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "blockChanged", at = @At("HEAD"))
    private void theleadage$retintWater(BlockGetter level, BlockPos pos, BlockState oldState, BlockState newState, int flags,
                                        CallbackInfo ci) {
        LeadWaterTint.blockChanged((LevelRenderer) (Object) this, pos, oldState, newState);
    }
}

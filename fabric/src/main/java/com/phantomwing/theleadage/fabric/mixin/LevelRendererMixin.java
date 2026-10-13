package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.client.LeadWaterTint;
import com.phantomwing.theleadage.effect.LeadWater;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the water around raw lead again when its reach changes, which a block change only does for the block's own
 * neighbours: water further out kept its old tint. The NeoForge twin is the identically-named mixin in the
 * {@code neoforge} source set.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "blockChanged", at = @At("HEAD"))
    private void theleadage$retintWater(BlockGetter level, BlockPos pos, BlockState oldState, BlockState newState, int flags,
                                        CallbackInfo ci) {
        if (LeadWaterTint.changesTint(oldState, newState)) {
            int reach = LeadWater.MAX_RADIUS;
            ((LevelRenderer) (Object) this).setBlocksDirty(pos.getX() - reach, pos.getY() - reach, pos.getZ() - reach,
                    pos.getX() + reach, pos.getY() + reach, pos.getZ() + reach);
        }
    }
}

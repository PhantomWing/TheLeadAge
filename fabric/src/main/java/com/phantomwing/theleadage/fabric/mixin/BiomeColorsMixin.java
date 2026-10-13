package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.client.LeadWaterTint;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tints the water raw lead contaminates ({@link LeadWaterTint}): every loader's water takes its colour from here.
 * The NeoForge twin is the identically-named mixin in the {@code neoforge} source set.
 */
@Mixin(BiomeColors.class)
public class BiomeColorsMixin {
    @Inject(method = "getAverageWaterColor", at = @At("RETURN"), cancellable = true)
    private static void theleadage$leadTint(BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(LeadWaterTint.tint(level, pos, cir.getReturnValueI()));
    }
}

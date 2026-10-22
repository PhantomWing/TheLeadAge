package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.effect.CampfireFumes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A campfire's smoke calms the bees of a hive over it; one giving off lead fumes doesn't, it sickens them
 * ({@link CampfireFumes#fumesHive}). Vanilla asks this of a hive alone. The NeoForge twin is the identically-named mixin
 * in the {@code neoforge} source set.
 */
@Mixin(CampfireBlock.class)
public class CampfireSmokeMixin {
    @Inject(method = "isSmokeyPos", at = @At("RETURN"), cancellable = true)
    private static void theleadage$leadFumesCalmNoBees(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && CampfireFumes.fumesHive(level, pos)) {
            cir.setReturnValue(false);
        }
    }
}

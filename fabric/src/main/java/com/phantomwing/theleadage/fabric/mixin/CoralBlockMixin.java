package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.effect.LeadWater;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CoralBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Coral blocks beside water raw lead contaminates die as out of water ({@link LeadWater#poisonsCoral}). */
@Mixin(CoralBlock.class)
public class CoralBlockMixin {
    @Inject(method = "scanForWater", at = @At("RETURN"), cancellable = true)
    private void theleadage$poisonedWater(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && LeadWater.poisonsCoral(level, pos, null)) {
            cir.setReturnValue(false);
        }
    }
}

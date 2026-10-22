package com.phantomwing.theleadage.neoforge.mixin;

import com.phantomwing.theleadage.effect.LeadWater;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseCoralPlantTypeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Coral plants and fans in water raw lead contaminates die as out of water ({@link LeadWater#poisonsCoral}). */
@Mixin(BaseCoralPlantTypeBlock.class)
public class BaseCoralPlantTypeBlockMixin {
    @Inject(method = "scanForWater", at = @At("RETURN"), cancellable = true)
    private static void theleadage$poisonedWater(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && LeadWater.poisonsCoral(level, pos, state)) {
            cir.setReturnValue(false);
        }
    }
}

package com.phantomwing.theleadage.neoforge.mixin;

import com.phantomwing.theleadage.effect.CampfireFumes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A lit campfire's server tick ({@code cookTick} runs only while it is lit), where one burning over lead gives
 * off its fumes ({@link CampfireFumes}). The Fabric twin is the identically-named mixin in the {@code fabric}
 * source set.
 */
@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void theleadage$leadFumes(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
                                             CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            CampfireFumes.tick(serverLevel, pos, state);
        }
    }
}

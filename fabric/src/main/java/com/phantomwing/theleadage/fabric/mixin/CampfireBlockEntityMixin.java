package com.phantomwing.theleadage.fabric.mixin;

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
 * A lit campfire's ticks ({@code cookTick} on the server and {@code particleTick} on the client run only while it is
 * lit), where one burning over raw lead gives off its fumes ({@link CampfireFumes}). The NeoForge twin is the identically-named mixin in the {@code neoforge}
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

    @Inject(method = "particleTick", at = @At("HEAD"))
    private static void theleadage$leadFumeStream(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
                                                  CallbackInfo ci) {
        CampfireFumes.particleTick(level, pos, state);
    }
}

package com.phantomwing.theleadage.neoforge.mixin;

import com.phantomwing.theleadage.effect.CampfireFumes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A campfire's server ticks, where one burning over raw lead gives off its fumes ({@link CampfireFumes}):
 * {@code cookTick} while it is lit and {@code cooldownTick} while it isn't, so the last of the fumes clear once it goes
 * out. How its smoke looks is {@code CampfireBlockMixin}'s. The Fabric twin is the identically-named mixin in the
 * {@code fabric} source set.
 */
@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void theleadage$leadFumes(ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
                                             RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipes,
                                             CallbackInfo ci) {
        CampfireFumes.tick(level, pos, state, campfire);
    }

    @Inject(method = "cooldownTick", at = @At("HEAD"))
    private static void theleadage$leadFumesClear(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
                                                  CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            CampfireFumes.tick(serverLevel, pos, state, campfire);
        }
    }
}

package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.effect.CampfireFumes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A lit campfire's server tick ({@code cookTick} runs only while it is lit), where one burning over lead gives
 * off its fumes ({@link CampfireFumes}). The NeoForge twin is the identically-named mixin in the {@code neoforge}
 * source set.
 */
@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void theleadage$leadFumes(ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity campfire,
                                             RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipes,
                                             CallbackInfo ci) {
        CampfireFumes.tick(level, pos, state);
    }
}

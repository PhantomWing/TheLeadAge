package com.phantomwing.theleadage.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.phantomwing.theleadage.client.MurkyWater;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Draws contaminated water with its murkier textures ({@link MurkyWater}). 26.1 draws a fluid from one model of its
 * sprites for all of it, which the renderer looks up for each block it draws; Fabric API's render handlers draw through
 * the same method. The NeoForge twin is the identically-named mixin in the {@code neoforge} source set.
 */
@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
    @WrapOperation(method = "tesselate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/FluidStateModelSet;get(Lnet/minecraft/world/level/material/FluidState;)Lnet/minecraft/client/renderer/block/FluidModel;"))
    private FluidModel theleadage$murkyWater(FluidStateModelSet models, FluidState state, Operation<FluidModel> original,
                                             @Local(argsOnly = true) BlockPos pos) {
        return MurkyWater.model(pos, state, original.call(models, state), (model, still, flowing) ->
                new FluidModel(model.layer(), still, flowing, model.overlayMaterial(), model.tintSource()));
    }
}

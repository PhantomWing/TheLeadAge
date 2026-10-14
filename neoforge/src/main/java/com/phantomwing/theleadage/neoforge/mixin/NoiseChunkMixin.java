package com.phantomwing.theleadage.neoforge.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

/**
 * Adds the lead veins to a chunk's terrain, after vanilla's copper and iron veins, which so come first where they
 * meet. The rules are tried in order and the first block one gives is placed; a block none gives is stone.
 */
@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin {
    @Shadow
    protected abstract DensityFunction wrap(DensityFunction function);

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/material/MaterialRuleList;<init>(Ljava/util/List;)V"))
    private List<NoiseChunk.BlockStateFiller> theleadage$addLeadVeins(List<NoiseChunk.BlockStateFiller> rules,
                                                                      @Local(argsOnly = true) RandomState random,
                                                                      @Local(argsOnly = true) NoiseGeneratorSettings settings) {
        NoiseChunk.BlockStateFiller veins = LeadVeins.filler(random, settings, this::wrap);
        return veins == null ? rules : ImmutableList.<NoiseChunk.BlockStateFiller>builder().addAll(rules).add(veins).build();
    }
}

package com.phantomwing.theleadage.fabric.mixin;

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

import java.util.Arrays;

/**
 * Adds the lead veins to a chunk's terrain, after vanilla's copper and iron veins, which so come first where they
 * meet. The rules are tried in order and the first block one gives is placed; a block none gives is stone. Optional
 * ({@code require = 0}): a worldgen mod that builds the terrain its own way leaves the world without lead veins, not
 * the game unable to start.
 */
@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin {
    @Shadow
    protected abstract DensityFunction wrap(DensityFunction function);

    // From 1.21.2 the rules are an array.
    @ModifyArg(method = "<init>", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/material/MaterialRuleList;<init>([Lnet/minecraft/world/level/levelgen/NoiseChunk$BlockStateFiller;)V"))
    private NoiseChunk.BlockStateFiller[] theleadage$addLeadVeins(NoiseChunk.BlockStateFiller[] rules,
                                                                  @Local(argsOnly = true) RandomState random,
                                                                  @Local(argsOnly = true) NoiseGeneratorSettings settings) {
        NoiseChunk.BlockStateFiller veins = LeadVeins.filler(random, settings, this::wrap);
        if (veins == null) {
            return rules;
        }
        NoiseChunk.BlockStateFiller[] withVeins = Arrays.copyOf(rules, rules.length + 1);
        withVeins[rules.length] = veins;
        return withVeins;
    }
}

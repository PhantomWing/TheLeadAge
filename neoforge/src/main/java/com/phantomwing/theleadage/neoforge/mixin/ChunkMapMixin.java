package com.phantomwing.theleadage.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Binds the lead veins to the level as its {@code RandomState} is made, here because the level's registries, which hold
 * the veins' material rule, are at hand: {@code RandomState} itself is given only the noises. Optional, as
 * {@code MaterialSystemMixin} is: unbound, the veins are left out.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @WrapOperation(method = "<init>", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/core/HolderGetter;JLnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;)Lnet/minecraft/world/level/levelgen/RandomState;"))
    private RandomState theleadage$bindLeadVeins(HolderGetter<NormalNoise> noises, long seed, NoiseGeneratorSettings settings,
                                                  Operation<RandomState> original, @Local(argsOnly = true) ServerLevel level) {
        RandomState random = original.call(noises, seed, settings);
        LeadVeins.bind(random, level.registryAccess());
        return random;
    }
}

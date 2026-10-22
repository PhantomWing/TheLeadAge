package com.phantomwing.theleadage.neoforge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.material.MaterialSystem;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Adds the lead veins to the material rule a chunk's terrain is built with, right after vanilla's iron vein, so
 * vanilla's veins come first where they meet. From 26.3 vanilla's veins are rules of the noise settings' material rule,
 * tried in order for each solid block, the first block one gives being placed: in a chunk's surface, and in the top
 * material a carver leaves. Optional ({@code require = 0}): a worldgen mod that builds the terrain its own way leaves
 * the world without lead veins, not the game unable to start.
 */
@Mixin(MaterialSystem.class)
public abstract class MaterialSystemMixin {
    @ModifyVariable(method = "buildSurface", at = @At("HEAD"), argsOnly = true, require = 0)
    private MaterialRule theleadage$addLeadVeins(MaterialRule rule, @Local(argsOnly = true) RandomState random) {
        return LeadVeins.withLeadVeins(rule, random);
    }

    @ModifyVariable(method = "topMaterial", at = @At("HEAD"), argsOnly = true, require = 0)
    private MaterialRule theleadage$addLeadVeinsToTop(MaterialRule rule, @Local(argsOnly = true) RandomState random) {
        return LeadVeins.withLeadVeins(rule, random);
    }
}

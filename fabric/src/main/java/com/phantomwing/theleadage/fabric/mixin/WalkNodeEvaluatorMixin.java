package com.phantomwing.theleadage.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.phantomwing.theleadage.effect.PoisonedWaterAvoidance;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A walking mob's path through poisoned water - a cow's, a villager's, an axolotl's or a turtle's, whose evaluators are
 * the walker's - costs as much as one through fire ({@link PoisonedWaterAvoidance#costed}), on top of whatever the block
 * cost already.
 * The NeoForge twin is the identically-named mixin in the {@code neoforge} source set.
 */
@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin extends NodeEvaluator {
    @ModifyReturnValue(method = "findAcceptedNode(IIIIDLnet/minecraft/core/Direction;Lnet/minecraft/world/level/pathfinder/PathType;)Lnet/minecraft/world/level/pathfinder/Node;",
            at = @At("RETURN"), require = 0)
    private Node theleadage$poisonedWaterCostsMore(Node node) {
        return PoisonedWaterAvoidance.costed(node, this.mob);
    }
}

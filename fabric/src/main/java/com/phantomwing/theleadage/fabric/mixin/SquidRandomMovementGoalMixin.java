package com.phantomwing.theleadage.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.phantomwing.theleadage.effect.PoisonedWaterAvoidance;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A squid plans no paths: it sets off in a random direction now and then. It turns away from the raw lead where that
 * direction would take it into poisoned water, and swims out of poisoned water it is in
 * ({@link PoisonedWaterAvoidance#steer}, {@link PoisonedWaterAvoidance#swimOut}). Glow squid too. The NeoForge twin is
 * the identically-named mixin in the {@code neoforge} source set.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Squid$SquidRandomMovementGoal")
public abstract class SquidRandomMovementGoalMixin {
    @Shadow
    @Final
    private Squid squid;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void theleadage$swimOutOfPoisonedWater(CallbackInfo ci) {
        PoisonedWaterAvoidance.swimOut(this.squid);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Squid;setMovementVector(FFF)V", ordinal = 1), require = 0)
    private void theleadage$turnFromPoisonedWater(Squid squid, float x, float y, float z, Operation<Void> original) {
        Vec3 way = PoisonedWaterAvoidance.steer(squid, x, y, z);
        original.call(squid, (float) way.x, (float) way.y, (float) way.z);
    }
}

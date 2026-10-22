package com.phantomwing.theleadage.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.phantomwing.theleadage.effect.PoisonedWaterAvoidance;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A squid plans no paths: it sets off in a random direction now and then. It turns away from the raw lead where that
 * direction would take it into poisoned water, and swims out of poisoned water it is in
 * ({@link PoisonedWaterAvoidance#steer}, {@link PoisonedWaterAvoidance#swimOut}). Glow squid too. The Fabric twin is
 * the identically-named mixin in the {@code fabric} source set.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.squid.Squid$SquidRandomMovementGoal")
public abstract class SquidRandomMovementGoalMixin {
    @Shadow
    @Final
    private Squid squid;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void theleadage$swimOutOfPoisonedWater(CallbackInfo ci) {
        PoisonedWaterAvoidance.swimOut(this.squid);
    }

    // From 1.21.2 the goal sets the squid's movementVector itself: the first set stops it, the second sets it off.
    @WrapOperation(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/world/entity/animal/squid/Squid;movementVector:Lnet/minecraft/world/phys/Vec3;", ordinal = 1),
            require = 0)
    private void theleadage$turnFromPoisonedWater(Squid squid, Vec3 way, Operation<Void> original) {
        original.call(squid, PoisonedWaterAvoidance.steer(squid, (float) way.x, (float) way.y, (float) way.z));
    }
}

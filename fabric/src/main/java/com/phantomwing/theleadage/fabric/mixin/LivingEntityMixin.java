package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.effect.LeadEquipmentHooks;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Releases lead fumes when a piece of lead gear wears out. {@code onEquippedItemBroken} is the single
 * funnel vanilla routes every durability break through — tools and armour alike — so hooking it once
 * covers the whole lead set. Purely additive (injects at {@code TAIL}, cancels nothing). The NeoForge
 * twin is the identically-named mixin in the {@code neoforge} source set.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onEquippedItemBroken", at = @At("TAIL"))
    private void theleadage$leadGearFumes(Item broken, EquipmentSlot slot, CallbackInfo ci) {
        LeadEquipmentHooks.onEquippedItemBroken((LivingEntity) (Object) this, broken);
    }
}

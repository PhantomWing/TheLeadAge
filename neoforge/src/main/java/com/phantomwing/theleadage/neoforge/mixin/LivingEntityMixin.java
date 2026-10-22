package com.phantomwing.theleadage.neoforge.mixin;

import com.phantomwing.theleadage.effect.LeadEquipmentHooks;
import com.phantomwing.theleadage.item.custom.LeadArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Lead gear on any wearer, through the two funnels vanilla routes it through. {@code onEquippedItemBroken} takes every
 * durability break, tools and armour alike, and a lead piece wearing out releases fumes. {@code handleEquipmentChanges}
 * takes every change to what a living entity wears, on the server, including its first tick after loading, since the
 * equipment it last saw starts empty: a mob's lead armour weighs on it from there ({@link LeadArmorItem#equipmentChanged}).
 * Purely additive (injects at {@code TAIL}, cancels nothing). The Fabric twin is the identically-named mixin in the
 * {@code fabric} source set.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onEquippedItemBroken", at = @At("TAIL"))
    private void theleadage$leadGearFumes(ItemStack broken, EquipmentSlot slot, CallbackInfo ci) {
        LeadEquipmentHooks.onEquippedItemBroken((LivingEntity) (Object) this, broken.getItem());
    }

    @Inject(method = "handleEquipmentChanges", at = @At("TAIL"))
    private void theleadage$leadArmorWeighs(Map<EquipmentSlot, ItemStack> changes, CallbackInfo ci) {
        LeadArmorItem.equipmentChanged((LivingEntity) (Object) this);
    }
}

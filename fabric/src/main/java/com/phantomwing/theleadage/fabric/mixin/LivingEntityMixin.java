package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.effect.LeadEquipmentHooks;
import com.phantomwing.theleadage.item.custom.LeadArmorItem;
import com.phantomwing.theleadage.item.custom.LeadHorseArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Lead gear on any wearer, through the funnels vanilla routes it through. {@code broadcastBreakEvent} is called for
 * every durability break, tools and armour alike, while the broken stack is still in its slot, and a lead piece wearing
 * out releases fumes (1.21's {@code onEquippedItemBroken}). {@code handleEquipmentChanges} takes every change to what a
 * living entity wears, on the server, including its first tick after loading, since the equipment it last saw starts
 * empty: a mob's lead armour weighs on it from there ({@link LeadArmorItem#equipmentChanged}), and a horse's lead armour
 * steadies it. The Forge twin is the identically-named mixin in the {@code forge} source set.
 *
 * <p>1.20.1 has no gravity attribute (1.20.5 added it), and Fabric adds none, so the weight of lead armour is put on
 * vanilla's gravity in {@code travel} here, where Forge's twin has its {@code forge:entity_gravity}.</p>
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "broadcastBreakEvent(Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"))
    private void theleadage$leadGearFumes(EquipmentSlot slot, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        LeadEquipmentHooks.onEquippedItemBroken(self, self.getItemBySlot(slot).getItem());
    }

    @Inject(method = "handleEquipmentChanges", at = @At("TAIL"))
    private void theleadage$leadArmorWeighs(Map<EquipmentSlot, ItemStack> changes, CallbackInfo ci) {
        LeadArmorItem.equipmentChanged((LivingEntity) (Object) this);
        LeadHorseArmorItem.equipmentChanged((LivingEntity) (Object) this);
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.08))
    private double theleadage$leadArmorGravity(double gravity) {
        return gravity * LeadArmorItem.gravityScale((LivingEntity) (Object) this);
    }
}

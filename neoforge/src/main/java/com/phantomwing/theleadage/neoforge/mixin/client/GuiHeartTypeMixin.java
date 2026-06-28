package com.phantomwing.theleadage.neoforge.mixin.client;

import com.phantomwing.theleadage.effect.ModMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Shows the vanilla poisoned-heart UI while a player has Lead Sickness. The heart sprite is chosen in
 * {@code Gui.HeartType.forPlayer} from {@code hasEffect(POISON)}; this adds our own Lead Sickness
 * condition — scoped to that one method, so vanilla Poison detection everywhere else is untouched. The
 * redirect leaves the Wither check (also a {@code hasEffect}) alone by matching only Poison.
 */
@Mixin(targets = "net.minecraft.client.gui.Gui$HeartType")
public class GuiHeartTypeMixin {
    @Redirect(method = "forPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private static boolean theleadage$leadSicknessPoisonHearts(Player player, Holder<MobEffect> effect) {
        boolean has = player.hasEffect(effect);
        if (!has && effect.value() == MobEffects.POISON.value()
                && player.hasEffect(ModMobEffects.leadSicknessHolder())) {
            return true; // our own check: Lead Sickness also draws the poisoned hearts
        }
        return has;
    }
}

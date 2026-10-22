package com.phantomwing.theleadage.neoforge.mixin;

import com.phantomwing.theleadage.client.LeadWaterSpecks;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the lead specks loose in contaminated water ({@link LeadWaterSpecks}) with the level's ambient particles, once a
 * tick around the player. The Fabric twin is the identically-named mixin in the {@code fabric} source set.
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "animateTick", at = @At("TAIL"))
    private void theleadage$leadSpecks(int x, int y, int z, CallbackInfo ci) {
        LeadWaterSpecks.tick((ClientLevel) (Object) this, x, y, z);
    }
}

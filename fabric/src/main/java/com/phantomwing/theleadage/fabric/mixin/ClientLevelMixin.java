package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.client.LeadWaterSpecks;
import com.phantomwing.theleadage.client.LeadWaterTint;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the lead specks loose in contaminated water ({@link LeadWaterSpecks}) with the level's ambient particles, once a
 * tick around the player, and draws the water around raw lead again when its spreading moves on or the water it leaks
 * through changes ({@link LeadWaterTint#blockChanged}), which a block change only does for the block's own neighbours:
 * water further out kept its old tint. From 26.2 the level hands a block change to its LevelExtractor without the old
 * and new states, so that is taken here. The NeoForge twin is the identically-named mixin in the {@code neoforge} source set.
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Shadow
    @Final
    private LevelExtractor levelExtractor;

    @Inject(method = "animateTick", at = @At("TAIL"))
    private void theleadage$leadSpecks(int x, int y, int z, CallbackInfo ci) {
        LeadWaterSpecks.tick((ClientLevel) (Object) this, x, y, z);
    }

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"))
    private void theleadage$retintWater(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        LeadWaterTint.blockChanged(levelExtractor, pos, oldState, newState);
    }
}

package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets vanilla iron bars / glass panes — and the mod's lead bars, all {@link IronBarsBlock} — grow a
 * connection arm toward a wall-mounted leaded glass pane (which isn't an {@code IronBarsBlock} and so
 * is otherwise invisible to the vanilla connection check).
 *
 * <p>Deliberately additive: it injects at {@code RETURN} and only escalates the existing result from
 * {@code false} to {@code true} for our case — it never turns a connection off, and composes with
 * other mods' {@code RETURN} injectors (boolean OR is order-independent). No {@code @Overwrite}. The
 * NeoForge twin is the identically-named mixin in the {@code neoforge} source set.</p>
 */
@Mixin(IronBarsBlock.class)
public class IronBarsBlockMixin {
    @Inject(method = "attachsTo", at = @At("RETURN"), cancellable = true)
    private void theleadage$connectToLeadedGlass(BlockState neighbor, boolean solidSide,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && LeadedGlassPaneBlock.barsConnectTo(neighbor)) {
            cir.setReturnValue(true);
        }
    }
}

package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.pack.DyedLeadedGlassPack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Offers the dyed leaded glass pack beside vanilla's own, to the client's resource packs and the server's
 * data packs alike: both repositories have a built-in source, which knows its pack type. Fabric has no
 * hook for a pack made at runtime; NeoForge's is {@code AddPackFindersEvent}.
 */
@Mixin(BuiltInPackSource.class)
public abstract class BuiltInPackSourceMixin {
    @Shadow
    @Final
    private PackType packType;

    @Inject(method = "loadPacks", at = @At("TAIL"))
    private void theleadage$offerDyedLeadedGlass(Consumer<Pack> packs, CallbackInfo ci) {
        Pack pack = DyedLeadedGlassPack.create(packType);
        if (pack != null) {
            packs.accept(pack);
        }
    }
}

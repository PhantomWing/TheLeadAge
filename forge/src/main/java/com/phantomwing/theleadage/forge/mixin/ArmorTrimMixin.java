package com.phantomwing.theleadage.forge.mixin;

import com.phantomwing.theleadage.armor.TrimPalettes;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Without it a lead trim on worn lead armour draws in lead's own palette rather than its darker one. */
@Mixin(ArmorTrim.class)
public abstract class ArmorTrimMixin {
    @Inject(method = "getColorPaletteSuffix", at = @At("HEAD"), cancellable = true)
    private void theleadage$darkerOnOwnMetal(ArmorMaterial armor, CallbackInfoReturnable<String> cir) {
        String palette = TrimPalettes.overrideFor(((ArmorTrim) (Object) this).material(), armor);
        if (palette != null) {
            cir.setReturnValue(palette);
        }
    }
}

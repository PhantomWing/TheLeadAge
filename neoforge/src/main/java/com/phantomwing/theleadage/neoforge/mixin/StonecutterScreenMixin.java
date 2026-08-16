package com.phantomwing.theleadage.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.phantomwing.theleadage.recipe.ColoredPaneStonecutterRecipe;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Colours the stonecutter recipe previews (the button icons and their hover tooltips) for the
 * coloured pane recipes. 1.21.4 resolves those previews from synced {@link SlotDisplay}s, which
 * can't see the live input (and carry no recipe object on servers), so this wraps the resolve and
 * recolours any configured-pane preview from the input — see
 * {@link ColoredPaneStonecutterRecipe#colorPreview}.
 *
 * <p>{@code require = 0}: purely cosmetic, so a target drift must never crash a client — but that
 * also makes drift SILENT (previews just lose their colour, as happened when 1.21.4 replaced the
 * old getResultItem call sites). The stonecutter preview is a standing cell on the per-port
 * in-game checklist for exactly that reason.</p>
 */
@Mixin(StonecutterScreen.class)
public abstract class StonecutterScreenMixin {
    @WrapOperation(
            method = {"renderRecipes", "renderTooltip"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/display/SlotDisplay;resolveForFirstStack(Lnet/minecraft/util/context/ContextMap;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    private ItemStack theleadage$colourPreview(SlotDisplay display, ContextMap context, Operation<ItemStack> original) {
        ItemStack preview = original.call(display, context);
        ItemStack input = ((StonecutterScreen) (Object) this).getMenu().container.getItem(StonecutterMenu.INPUT_SLOT);
        return input.isEmpty() ? preview : ColoredPaneStonecutterRecipe.colorPreview(preview, input);
    }
}

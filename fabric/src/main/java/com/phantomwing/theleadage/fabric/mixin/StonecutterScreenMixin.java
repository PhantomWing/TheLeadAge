package com.phantomwing.theleadage.fabric.mixin;

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
 * coloured pane recipes. From 1.21.2 the screen resolves those previews from synced
 * {@link SlotDisplay}s, which can't see the live input, and the client has no recipe to ask; so this
 * wraps the resolve and recolours any configured-pane preview from the input, as
 * {@link ColoredPaneStonecutterRecipe#colorPreview} does for the cut itself.
 *
 * <p>{@code require = 0}: purely cosmetic, so a target drift must never crash a client. That makes a
 * drift silent - the previews just stay clear, as they did while this wrapped 1.21.1's getResultItem -
 * so the stonecutter-shows-pane-colours scenario reads the list's tooltip and expects the input's colour.
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

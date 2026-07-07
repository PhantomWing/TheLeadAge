package com.phantomwing.theleadage.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.phantomwing.theleadage.recipe.ColoredPaneStonecutterRecipe;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Colours the stonecutter recipe previews (the button icons and their hover tooltips) for our
 * {@link ColoredPaneStonecutterRecipe}. Those previews render the recipe's static {@code result}
 * (an all-clear pane) via {@code getResultItem}, which can't see the input; we wrap that to
 * {@code assemble} on the live input so the preview matches the actually-cut, coloured output.
 * Every other stonecutter recipe falls through to the original call.
 *
 * <p>Uses MixinExtras {@code @WrapOperation} (composes with other mods wrapping the same call,
 * unlike {@code @Redirect}) and is non-required — purely cosmetic, so if it ever fails to apply
 * the previews just stay uncoloured and the (mixin-free) coloured output still works.
 */
@Mixin(StonecutterScreen.class)
public abstract class StonecutterScreenMixin {
    @WrapOperation(
            method = {"renderRecipes", "renderTooltip"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/StonecutterRecipe;getResultItem(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    private ItemStack theleadage$colourPreview(StonecutterRecipe recipe, HolderLookup.Provider registries,
                                               Operation<ItemStack> original) {
        if (recipe instanceof ColoredPaneStonecutterRecipe colored) {
            ItemStack input = ((StonecutterScreen) (Object) this).getMenu().container.getItem(StonecutterMenu.INPUT_SLOT);
            if (!input.isEmpty()) {
                return colored.assemble(new SingleRecipeInput(input), registries);
            }
        }
        return original.call(recipe, registries);
    }
}

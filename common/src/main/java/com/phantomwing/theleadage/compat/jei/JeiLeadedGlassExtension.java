package com.phantomwing.theleadage.compat.jei;

import com.phantomwing.theleadage.compat.LeadedGlassDisplayRecipe;
import com.phantomwing.theleadage.compat.LeadedGlassSubtypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Shows {@link LeadedGlassDisplayRecipe} in JEI's crafting category. JEI 20 lays a crafting extension out from its
 * slot displays alone, the inputs from {@link #getIngredients} and the result from the recipe's
 * {@link LeadedGlassDisplayRecipe#display()}, with no grid helper or focus links. Every slot of a recipe reads one
 * shared cycle, so the colour variants (clear and each dye), index-aligned across the cycling cells and the result,
 * still cycle in step: a red input shows beside the red result. A focus narrows only the slots it matches, so
 * {@link #onDisplayedIngredientsUpdate} pins the others to the colour the focused one shows.
 */
public class JeiLeadedGlassExtension implements ICraftingCategoryExtension<LeadedGlassDisplayRecipe> {

    /** How JEI indexes the recipe for lookups, and what it draws in the grid: a composite per cycling cell. */
    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<LeadedGlassDisplayRecipe> recipeHolder) {
        return recipeHolder.value().inputDisplays();
    }

    @Override
    public int getWidth(RecipeHolder<LeadedGlassDisplayRecipe> recipeHolder) {
        return recipeHolder.value().gridWidth();
    }

    @Override
    public int getHeight(RecipeHolder<LeadedGlassDisplayRecipe> recipeHolder) {
        return recipeHolder.value().gridHeight();
    }

    /**
     * With a focus (looking up a red pane's recipes, or what red dye makes), JEI shows the focused slot only the
     * variants it matches while the rest cycle every colour. The cycling slots - those holding one stack per colour -
     * are pinned to the colour the focused one shows. JEI clears these overrides before each call.
     */
    @Override
    public void onDisplayedIngredientsUpdate(RecipeHolder<LeadedGlassDisplayRecipe> recipeHolder,
                                             List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
        int variants = recipeHolder.value().resultVariants().size();
        if (focuses.isEmpty() || variants < 2) {
            return;
        }
        List<IFocus<ItemStack>> stackFocuses = focuses.getFocuses(VanillaTypes.ITEM_STACK).toList();
        int shown = -1;
        for (IRecipeSlotDrawable slot : recipeSlots) {
            List<ItemStack> all = stacksOf(slot);
            if (all.size() != variants || !isFocused(slot, all, stackFocuses)) {
                continue;
            }
            shown = indexOf(all, slot.getDisplayedItemStack());
            if (shown >= 0) {
                break;
            }
        }
        if (shown < 0) {
            return;
        }
        for (IRecipeSlotDrawable slot : recipeSlots) {
            List<ItemStack> all = stacksOf(slot);
            if (all.size() == variants && indexOf(all, slot.getDisplayedItemStack()) != shown) {
                slot.createDisplayOverrides().add(all.get(shown));
            }
        }
    }

    private static boolean isFocused(IRecipeSlotDrawable slot, List<ItemStack> all, List<IFocus<ItemStack>> focuses) {
        return focuses.stream().anyMatch(focus -> focus.getRole() == slot.getRole()
                && all.stream().anyMatch(stack -> isSameStack(stack, focus.getTypedValue().getIngredient())));
    }

    /**
     * Whether JEI takes the two for one stack: by design where {@link LeadedGlassSubtypes} tells them apart, so a pane
     * with no design is the clear one, as it is to JEI's focus; else by item and components.
     */
    private static boolean isSameStack(ItemStack a, ItemStack b) {
        if (!a.is(b.getItem())) {
            return false;
        }
        if (LeadedGlassSubtypes.items().contains(a.getItem())) {
            return Objects.equals(LeadedGlassSubtypes.designOf(a), LeadedGlassSubtypes.designOf(b));
        }
        return ItemStack.isSameItemSameComponents(a, b);
    }

    /** Every stack the slot was given, in order, unnarrowed by any focus. */
    private static List<ItemStack> stacksOf(IRecipeSlotDrawable slot) {
        return slot.getAllIngredientsList().stream()
                .map(ingredient -> ingredient == null ? ItemStack.EMPTY
                        : ingredient.getItemStack().orElse(ItemStack.EMPTY))
                .toList();
    }

    private static int indexOf(List<ItemStack> stacks, Optional<ItemStack> shown) {
        if (shown.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < stacks.size(); i++) {
            if (isSameStack(stacks.get(i), shown.get())) {
                return i;
            }
        }
        return -1;
    }
}

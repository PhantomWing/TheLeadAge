package com.phantomwing.theleadage.compat.jei;

import com.phantomwing.theleadage.compat.LeadedGlassDisplayRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders {@link LeadedGlassDisplayRecipe} in JEI's crafting category. Every colour-bearing input
 * slot and the output are focus-linked, so the 17 colour variants (clear + each dye) cycle in
 * lockstep — a red input visibly produces the red result. Constant slots (the lead ingot / door /
 * trapdoor) show a single stack and stay outside the link.
 */
public class JeiLeadedGlassExtension implements ICraftingCategoryExtension<LeadedGlassDisplayRecipe> {
    @Override
    public int getWidth(RecipeHolder<LeadedGlassDisplayRecipe> recipeHolder) {
        return recipeHolder.value().gridWidth();
    }

    @Override
    public int getHeight(RecipeHolder<LeadedGlassDisplayRecipe> recipeHolder) {
        return recipeHolder.value().gridHeight();
    }

    @Override
    public void setRecipe(RecipeHolder<LeadedGlassDisplayRecipe> recipeHolder, IRecipeLayoutBuilder builder,
                          ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        LeadedGlassDisplayRecipe recipe = recipeHolder.value();
        int width = recipe.gridWidth();
        int height = recipe.gridHeight();
        List<List<ItemStack>> inputs = recipe.inputVariants();
        // Always returns 9 slots (a full 3x3); input i is placed at craftingIndex(i), not slot i.
        List<IRecipeSlotBuilder> slots = craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
        IRecipeSlotBuilder output = craftingGridHelper.createAndSetOutputs(builder, recipe.resultVariants());

        List<IIngredientAcceptor<?>> linked = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            if (recipe.cyclesAt(i)) {
                linked.add(slots.get(craftingIndex(i, width, height)));
            }
        }
        linked.add(output);
        builder.createFocusLink(linked.toArray(new IIngredientAcceptor<?>[0]));
    }

    /**
     * Where JEI's {@code CraftingGridHelper} places input {@code i} within its fixed 3x3 slot list —
     * sub-3x3 recipes are anchored into specific cells, so the returned slot for input {@code i} is
     * {@code slots.get(craftingIndex(i))}, not {@code slots.get(i)}. Mirrors JEI's private mapping.
     */
    private static int craftingIndex(int i, int width, int height) {
        if (width == 1) {
            return (height == 2 || height == 3) ? (i * 3) + 1 : 4;
        } else if (height == 1) {
            return i + 3;
        } else if (width == 2) {
            int index = i;
            if (i > 1) {
                index++;
                if (i > 3) {
                    index++;
                }
            }
            return index;
        } else if (height == 2) {
            return i + 3;
        } else {
            return i;
        }
    }
}

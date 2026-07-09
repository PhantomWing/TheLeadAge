package com.phantomwing.theleadage.compat.jei;

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
 * Renders {@link JeiLeadedGlassRecipe} in JEI's crafting category. Every colour-bearing input slot
 * and the output are focus-linked, so the 17 colour variants (clear + each dye) cycle in lockstep —
 * a red input visibly produces the red result. Constant slots (the lead ingot / door / trapdoor)
 * show a single stack and stay outside the link.
 */
public class JeiLeadedGlassExtension implements ICraftingCategoryExtension<JeiLeadedGlassRecipe> {
    @Override
    public int getWidth(RecipeHolder<JeiLeadedGlassRecipe> recipeHolder) {
        return recipeHolder.value().gridWidth();
    }

    @Override
    public int getHeight(RecipeHolder<JeiLeadedGlassRecipe> recipeHolder) {
        return recipeHolder.value().gridHeight();
    }

    @Override
    public void setRecipe(RecipeHolder<JeiLeadedGlassRecipe> recipeHolder, IRecipeLayoutBuilder builder,
                          ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        JeiLeadedGlassRecipe recipe = recipeHolder.value();
        List<List<ItemStack>> inputs = recipe.inputVariants();
        List<IRecipeSlotBuilder> slots = craftingGridHelper.createAndSetInputs(
                builder, inputs, recipe.gridWidth(), recipe.gridHeight());
        IRecipeSlotBuilder output = craftingGridHelper.createAndSetOutputs(builder, recipe.resultVariants());

        List<IIngredientAcceptor<?>> linked = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            if (recipe.cyclesAt(i)) {
                linked.add(slots.get(i));
            }
        }
        linked.add(output);
        builder.createFocusLink(linked.toArray(new IIngredientAcceptor<?>[0]));
    }
}

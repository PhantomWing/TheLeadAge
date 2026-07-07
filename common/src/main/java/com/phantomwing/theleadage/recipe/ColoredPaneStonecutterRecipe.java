package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import java.util.Collections;

/**
 * A stonecutter recipe that cuts a plain leaded glass pane into a patterned pane, carrying the
 * input pane's colour onto every region of the result — where a vanilla {@link StonecutterRecipe}
 * would give the fixed all-clear result baked into the recipe. Cutting a red pane yields an
 * all-red pattern; cutting a clear pane still yields a clear pattern (the vanilla behaviour).
 *
 * <p>This extends {@code StonecutterRecipe} rather than {@code SingleItemRecipe} on purpose:
 * {@code StonecutterMenu} casts the selected recipe to the concrete {@code StonecutterRecipe}
 * class before calling {@link #assemble}, so a plain {@code SingleItemRecipe}/{@code CustomRecipe}
 * would crash on selection. The recipe <i>type</i> stays {@code minecraft:stonecutting} (inherited)
 * so the menu lists it alongside the others; only the serializer is ours, so datapack loading
 * rebuilds this subclass instead of a vanilla stonecutter recipe.
 *
 * <p>The static {@code result} (an all-clear patterned pane) is still what the recipe-list buttons,
 * the recipe book and JEI show; {@link #assemble} recolours a copy of it from the live input for
 * the output slot. {@code StonecutterScreenMixin} redirects the button/tooltip preview through
 * {@code assemble} so the icons match the coloured output too.
 */
public class ColoredPaneStonecutterRecipe extends StonecutterRecipe {
    public ColoredPaneStonecutterRecipe(String group, Ingredient ingredient, ItemStack result) {
        super(group, ingredient, result);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        ItemStack output = super.assemble(input, registries); // the target frame's all-clear pane
        LeadedGlassConfig target = output.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (target == null) {
            return output; // not a configured pane (shouldn't happen for our recipes)
        }
        int color = primaryColor(input.getItem(0));
        if (color == LeadedGlassConfig.CLEAR) {
            return output; // clear input → the static all-clear result is already correct
        }
        output.set(ModDataComponents.LEADED_GLASS_CONFIG.get(),
                new LeadedGlassConfig(target.frame(),
                        Collections.nCopies(target.frame().regions(), color)));
        return output;
    }

    /** The plain input pane's single region colour, or {@link LeadedGlassConfig#CLEAR} if unset. */
    private static int primaryColor(ItemStack input) {
        LeadedGlassConfig config = input.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null || config.colors().isEmpty()) {
            return LeadedGlassConfig.CLEAR;
        }
        return config.colors().get(0);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COLORED_PANE_STONECUTTING.get();
    }

    /** Reuses the vanilla single-item codec (group + ingredient + result); only the class differs. */
    public static class Serializer extends SingleItemRecipe.Serializer<ColoredPaneStonecutterRecipe> {
        public Serializer() {
            super(ColoredPaneStonecutterRecipe::new);
        }
    }
}

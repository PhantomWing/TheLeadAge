package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Splits a leaded glass trapdoor back into a lead trapdoor + the leaded glass pane it was made with. */
public class LeadedGlassTrapdoorSplitRecipe extends LeadedGlassSplitRecipe {
    public LeadedGlassTrapdoorSplitRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    protected Item source() {
        return ModItems.LEADED_GLASS_TRAPDOOR.get();
    }

    @Override
    protected Item base() {
        return ModItems.LEAD_TRAPDOOR.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEADED_GLASS_TRAPDOOR_SPLIT.get();
    }
}

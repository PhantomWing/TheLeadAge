package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Splits a leaded glass door back into a lead door + the leaded glass pane it was made with. */
public class LeadedGlassDoorSplitRecipe extends LeadedGlassSplitRecipe {
    public LeadedGlassDoorSplitRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    protected Item source() {
        return ModItems.LEADED_GLASS_DOOR.get();
    }

    @Override
    protected Item base() {
        return ModItems.LEAD_DOOR.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEADED_GLASS_DOOR_SPLIT.get();
    }
}

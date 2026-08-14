package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Combines a lead trapdoor with a configured leaded glass pane into a leaded glass trapdoor,
 * copying the pane's design (frame + colours) onto the trapdoor's flap.
 */
public class LeadedGlassTrapdoorRecipe extends CustomRecipe {
    public LeadedGlassTrapdoorRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findPane(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack pane = findPane(input);
        if (pane == null) {
            return ItemStack.EMPTY;
        }
        ItemStack trapdoor = new ItemStack(ModItems.LEADED_GLASS_TRAPDOOR.get());
        LeadedGlassConfig config = pane.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config != null) {
            trapdoor.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), config);
        }
        return trapdoor;
    }

    /** The pane stack iff the grid holds exactly one lead trapdoor + one leaded glass pane. */
    @Nullable
    private static ItemStack findPane(CraftingInput input) {
        ItemStack pane = null;
        boolean hasTrapdoor = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.LEAD_TRAPDOOR.get())) {
                if (hasTrapdoor) {
                    return null;
                }
                hasTrapdoor = true;
            } else if (ModItems.isPaneItem(stack)) {
                if (pane != null) {
                    return null;
                }
                pane = stack;
            } else {
                return null;
            }
        }
        return hasTrapdoor && pane != null ? pane : null;
    }

    @Override
    public RecipeSerializer<LeadedGlassTrapdoorRecipe> getSerializer() {
        return ModRecipes.LEADED_GLASS_TRAPDOOR.get();
    }
}

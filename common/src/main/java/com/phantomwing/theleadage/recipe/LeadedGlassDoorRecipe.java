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
 * Combines a lead door with a configured leaded glass pane into a leaded glass door, copying
 * the pane's design (frame + colours) onto the door so its top half shows that exact pattern.
 */
public class LeadedGlassDoorRecipe extends CustomRecipe {
    public LeadedGlassDoorRecipe(CraftingBookCategory category) {
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
        ItemStack door = new ItemStack(ModItems.LEADED_GLASS_DOOR.get());
        LeadedGlassConfig config = pane.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config != null) {
            door.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), config);
        }
        return door;
    }

    /** The pane stack iff the grid holds exactly one lead door + one leaded glass pane (no extras). */
    @Nullable
    private static ItemStack findPane(CraftingInput input) {
        ItemStack pane = null;
        boolean hasDoor = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.LEAD_DOOR.get())) {
                if (hasDoor) {
                    return null;
                }
                hasDoor = true;
            } else if (ModItems.isPaneItem(stack)) {
                if (pane != null) {
                    return null;
                }
                pane = stack;
            } else {
                return null; // an unrelated item is present
            }
        }
        return hasDoor && pane != null ? pane : null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEADED_GLASS_DOOR.get();
    }
}

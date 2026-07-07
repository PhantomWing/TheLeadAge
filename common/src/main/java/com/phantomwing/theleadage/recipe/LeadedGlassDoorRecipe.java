package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
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
 * Crafts a leaded glass door from a lead door + two leaded glass panes (shapeless): the first pane
 * (in grid reading order) becomes the door's upper half, the second its lower half. The panes may
 * be any frame/colour; each half shows that pane's exact design. There is no reverse recipe — a
 * door would have to hand back three items (lead door + both panes), more than crafting allows.
 */
public class LeadedGlassDoorRecipe extends CustomRecipe {
    public LeadedGlassDoorRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return doorConfig(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        LeadedGlassDoorConfig config = doorConfig(input);
        if (config == null) {
            return ItemStack.EMPTY;
        }
        ItemStack door = new ItemStack(ModItems.LEADED_GLASS_DOOR.get());
        door.set(ModDataComponents.LEADED_GLASS_DOOR_CONFIG.get(), config);
        return door;
    }

    /** The door design iff the grid holds exactly one lead door + two panes (first = top), else null. */
    @Nullable
    private static LeadedGlassDoorConfig doorConfig(CraftingInput input) {
        LeadedGlassConfig top = null;
        LeadedGlassConfig bottom = null;
        boolean hasDoor = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.LEAD_DOOR.get())) {
                if (hasDoor) {
                    return null; // a second lead door
                }
                hasDoor = true;
            } else if (ModItems.isPaneItem(stack)) {
                LeadedGlassConfig pane = paneConfig(stack);
                if (top == null) {
                    top = pane;
                } else if (bottom == null) {
                    bottom = pane;
                } else {
                    return null; // more than two panes
                }
            } else {
                return null; // an unrelated item is present
            }
        }
        return hasDoor && bottom != null ? new LeadedGlassDoorConfig(top, bottom) : null;
    }

    /** A pane's design, or a clear plain pane when the item carries no component. */
    private static LeadedGlassConfig paneConfig(ItemStack pane) {
        LeadedGlassConfig config = pane.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        return config != null ? config : LeadedGlassDoorConfig.DEFAULT.top();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEADED_GLASS_DOOR.get();
    }
}

package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Combines a lead trapdoor with a configured leaded glass pane into a leaded glass trapdoor,
 * copying the pane's design (frame + colours) onto the trapdoor's flap.
 */
public class LeadedGlassTrapdoorRecipe extends CustomRecipe {
    public LeadedGlassTrapdoorRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        CraftingInput input = CraftingInput.of(container);
        return findPane(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        CraftingInput input = CraftingInput.of(container);
        ItemStack pane = findPane(input);
        if (pane == null) {
            return ItemStack.EMPTY;
        }
        ItemStack trapdoor = new ItemStack(ModItems.LEADED_GLASS_TRAPDOOR.get());
        LeadedGlassConfig config = ModDataComponents.LEADED_GLASS_CONFIG.get(pane);
        if (config != null) {
            ModDataComponents.LEADED_GLASS_CONFIG.set(trapdoor, config);
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEADED_GLASS_TRAPDOOR.get();
    }
}

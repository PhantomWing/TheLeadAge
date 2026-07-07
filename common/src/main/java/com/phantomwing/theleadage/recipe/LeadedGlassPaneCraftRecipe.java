package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
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

import java.util.List;
import java.util.Optional;

/**
 * Crafts plain leaded glass panes from vanilla glass panes: eight glass panes around the edge of
 * the 3×3 grid with a lead ingot in the centre yields eight {@link LeadedGlassFrame#PLAIN} panes,
 * taking their colour from the glass panes (a clear glass pane → clear; a stained pane → that dye).
 * The eight edge panes must all be the same colour. One recipe covers all 17 colours by reading
 * the input rather than baking a colour into the result.
 */
public class LeadedGlassPaneCraftRecipe extends CustomRecipe {
    private static final int CENTRE = 4;
    private static final int OUTPUT_COUNT = 8;

    public LeadedGlassPaneCraftRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return colorOf(input).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return colorOf(input).map(color -> {
            ItemStack pane = new ItemStack(ModItems.LEADED_GLASS_PANEL.get(), OUTPUT_COUNT);
            pane.set(ModDataComponents.LEADED_GLASS_CONFIG.get(),
                    new LeadedGlassConfig(LeadedGlassFrame.PLAIN, List.of(color)));
            return pane;
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEADED_GLASS_PANE_CRAFT.get();
    }

    /** The shared colour of the eight edge glass panes (centre must be a lead ingot), or empty if invalid. */
    private static Optional<Integer> colorOf(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return Optional.empty(); // must be the full 3×3 (centre ingot + eight edge panes)
        }
        if (!input.getItem(CENTRE).is(ModItems.LEAD_INGOT.get())) {
            return Optional.empty();
        }
        Integer color = null;
        for (int i = 0; i < input.size(); i++) {
            if (i == CENTRE) {
                continue;
            }
            Integer c = LeadedGlassColors.glassPaneColorIdOf(input.getItem(i));
            if (c == null) {
                return Optional.empty(); // an empty cell or a non-glass-pane on an edge
            }
            if (color == null) {
                color = c;
            } else if (!color.equals(c)) {
                return Optional.empty(); // all eight edge panes must be the same colour
            }
        }
        return Optional.ofNullable(color);
    }
}

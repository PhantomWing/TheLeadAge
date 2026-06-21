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
 * Builds configured leaded glass panes by reading the grid <i>arrangement</i> — the shape
 * decides the frame (came) and the items decide the per-region colours. One recipe covers
 * every frame × colour combination: no recipe explosion, no came item.
 *
 * <ul>
 *   <li>3×2 of identical leaded glass <i>blocks</i> → 16 {@link LeadedGlassFrame#PLAIN} panes
 *       of that colour (the vanilla glass-pane cut).</li>
 *   <li>1×2 (side by side) of plain leaded glass <i>panes</i> →
 *       {@link LeadedGlassFrame#SPLIT_H} (left / right).</li>
 * </ul>
 */
public class LeadedGlassCombineRecipe extends CustomRecipe {
    public LeadedGlassCombineRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return parse(input).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return parse(input).map(result -> {
            ItemStack pane = new ItemStack(ModItems.LEADED_GLASS_PANEL.get(), result.count());
            pane.set(ModDataComponents.LEADED_GLASS_CONFIG.get(),
                    new LeadedGlassConfig(result.frame(), result.colors()));
            return pane;
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEADED_GLASS_COMBINE.get();
    }

    /** Reads the grid into a frame + region colours (+ output count), or empty if invalid. */
    private static Optional<Result> parse(CraftingInput input) {
        int width = input.width();
        int height = input.height();

        // Pane-cut: a full 3×2 of identical leaded glass blocks → 16 plain panes of that colour.
        if (width == 3 && height == 2) {
            Integer color = null;
            for (int i = 0; i < input.size(); i++) {
                Integer c = LeadedGlassColors.blockColorIdOf(input.getItem(i));
                if (c == null) {
                    return Optional.empty(); // empty cell or non-leaded-glass block
                }
                if (color == null) {
                    color = c;
                } else if (!color.equals(c)) {
                    return Optional.empty(); // all six must be the same colour
                }
            }
            return Optional.of(new Result(LeadedGlassFrame.PLAIN, List.of(color), 16));
        }

        // Combine: two plain panes side by side → one split pane (left | right).
        if (width == 2 && height == 1) {
            Integer left = LeadedGlassColors.plainPaneColorIdOf(input.getItem(0));
            Integer right = LeadedGlassColors.plainPaneColorIdOf(input.getItem(1));
            if (left == null || right == null) {
                return Optional.empty();
            }
            return Optional.of(new Result(LeadedGlassFrame.SPLIT_H, List.of(left, right), 1));
        }

        return Optional.empty();
    }

    private record Result(LeadedGlassFrame frame, List<Integer> colors, int count) {
    }
}

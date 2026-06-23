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
            ItemStack pane = new ItemStack(ModItems.paneItemFor(result.frame()), result.count());
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

        // Combine: two plain panes side by side → two split panes (left | right).
        if (width == 2 && height == 1) {
            return combine(input, LeadedGlassFrame.SPLIT_H, 2);
        }
        // Two plain panes stacked → two vertical-split panes (top / bottom).
        if (width == 1 && height == 2) {
            return combine(input, LeadedGlassFrame.SPLIT_V, 2);
        }
        // A 2×2 square: four panes → grid; two diagonal panes → a "/" or "\" diagonal.
        if (width == 2 && height == 2) {
            return parseSquare(input);
        }
        // A full 3×3 of panes → a 3×3 grid; the 3×3 "plus" (edge mids only) → an X cross.
        if (width == 3 && height == 3) {
            Optional<Result> grid3 = parseGrid3(input);
            return grid3.isPresent() ? grid3 : parseCross(input);
        }

        return Optional.empty();
    }

    /** 3×3 fully filled with plain panes → a {@link LeadedGlassFrame#GRID_3} (nine regions, row-major). */
    private static Optional<Result> parseGrid3(CraftingInput input) {
        Integer[] colors = new Integer[9];
        for (int i = 0; i < 9; i++) {
            colors[i] = LeadedGlassColors.plainPaneColorIdOf(input.getItem(i));
            if (colors[i] == null) {
                return Optional.empty();
            }
        }
        return Optional.of(new Result(LeadedGlassFrame.GRID_3, List.of(colors), 9));
    }

    /** 2×2: all four filled → grid; the two cells of one diagonal filled (the rest empty) → diagonal. */
    private static Optional<Result> parseSquare(CraftingInput input) {
        // Cells: 0 = top-left, 1 = top-right, 2 = bottom-left, 3 = bottom-right.
        Integer[] c = new Integer[4];
        boolean[] empty = new boolean[4];
        for (int i = 0; i < 4; i++) {
            ItemStack stack = input.getItem(i);
            empty[i] = stack.isEmpty();
            c[i] = LeadedGlassColors.plainPaneColorIdOf(stack);
            if (!empty[i] && c[i] == null) {
                return Optional.empty(); // a non-plain-pane item is present
            }
        }
        if (c[0] != null && c[1] != null && c[2] != null && c[3] != null) {
            return Optional.of(new Result(LeadedGlassFrame.GRID, List.of(c[0], c[1], c[2], c[3]), 4));
        }
        // Diagonal "/" — top-left + bottom-right filled (0 = upper-left region, 1 = lower-right).
        if (c[0] != null && c[3] != null && empty[1] && empty[2]) {
            return Optional.of(new Result(LeadedGlassFrame.DIAGONAL_A, List.of(c[0], c[3]), 2));
        }
        // Diagonal "\" — top-right + bottom-left filled (0 = upper-right region, 1 = lower-left).
        if (c[1] != null && c[2] != null && empty[0] && empty[3]) {
            return Optional.of(new Result(LeadedGlassFrame.DIAGONAL_B, List.of(c[1], c[2]), 2));
        }
        return Optional.empty();
    }

    /** 3×3 plus: panes at the four edge-midpoints (top/left/right/bottom), the rest empty → cross. */
    private static Optional<Result> parseCross(CraftingInput input) {
        // Edge mids: 1 = top, 3 = left, 5 = right, 7 = bottom. Corners (0,2,6,8) + centre (4) empty.
        for (int i : new int[]{0, 2, 4, 6, 8}) {
            if (!input.getItem(i).isEmpty()) {
                return Optional.empty();
            }
        }
        Integer top = LeadedGlassColors.plainPaneColorIdOf(input.getItem(1));
        Integer left = LeadedGlassColors.plainPaneColorIdOf(input.getItem(3));
        Integer right = LeadedGlassColors.plainPaneColorIdOf(input.getItem(5));
        Integer bottom = LeadedGlassColors.plainPaneColorIdOf(input.getItem(7));
        if (top == null || left == null || right == null || bottom == null) {
            return Optional.empty();
        }
        // Regions: 0 = top, 1 = right, 2 = bottom, 3 = left.
        return Optional.of(new Result(LeadedGlassFrame.CROSS, List.of(top, right, bottom, left), 4));
    }

    /**
     * Reads {@code count} plain panes (row-major) into a configured result of the given frame,
     * yielding {@code count} of them — combining N panes returns N patterned panes, not one.
     */
    private static Optional<Result> combine(CraftingInput input, LeadedGlassFrame frame, int count) {
        Integer[] colors = new Integer[count];
        for (int i = 0; i < count; i++) {
            colors[i] = LeadedGlassColors.plainPaneColorIdOf(input.getItem(i));
            if (colors[i] == null) {
                return Optional.empty();
            }
        }
        return Optional.of(new Result(frame, List.of(colors), count));
    }

    private record Result(LeadedGlassFrame frame, List<Integer> colors, int count) {
    }
}

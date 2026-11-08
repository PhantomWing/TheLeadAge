package com.phantomwing.theleadage.recipe;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21's {@code CraftingInput}, which 1.20.1 doesn't have: a crafting grid trimmed to the smallest box holding every
 * item in it, so the recipes read a shape the same wherever in the grid it was laid. 1.20.1 hands a recipe the whole
 * grid ({@link CraftingContainer}), which this is made from.
 */
public final class CraftingInput {
    private static final CraftingInput EMPTY = new CraftingInput(0, 0, List.of());

    private final int width;
    private final int height;
    private final List<ItemStack> items;
    private final int ingredientCount;

    private CraftingInput(int width, int height, List<ItemStack> items) {
        this.width = width;
        this.height = height;
        this.items = items;
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        this.ingredientCount = count;
    }

    public static CraftingInput of(CraftingContainer container) {
        int gridWidth = container.getWidth();
        int gridHeight = container.getHeight();
        int left = gridWidth;
        int right = -1;
        int top = gridHeight;
        int bottom = -1;
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                if (!container.getItem(x + y * gridWidth).isEmpty()) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        if (right < 0) {
            return EMPTY;
        }
        int width = right - left + 1;
        int height = bottom - top + 1;
        List<ItemStack> items = new ArrayList<>(width * height);
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                items.add(container.getItem(x + y * gridWidth));
            }
        }
        return new CraftingInput(width, height, items);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int size() {
        return items.size();
    }

    public ItemStack getItem(int index) {
        return items.get(index);
    }

    public ItemStack getItem(int x, int y) {
        return items.get(x + y * width);
    }

    public List<ItemStack> items() {
        return items;
    }

    public int ingredientCount() {
        return ingredientCount;
    }

    public boolean isEmpty() {
        return ingredientCount == 0;
    }
}

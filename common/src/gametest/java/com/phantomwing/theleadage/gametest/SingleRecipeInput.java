package com.phantomwing.theleadage.gametest;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * 1.21's {@code SingleRecipeInput} for this line's tests, which 1.20.1 doesn't have: one stack, as a furnace or a
 * stonecutter matches it, held as the container 1.20.1's recipes take. Only on this line.
 */
public record SingleRecipeInput(ItemStack item) implements RecipeInput {
    @Override
    public SimpleContainer container() {
        return new SimpleContainer(item);
    }
}

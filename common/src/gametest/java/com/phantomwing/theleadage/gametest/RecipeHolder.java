package com.phantomwing.theleadage.gametest;

import net.minecraft.resources.ResourceLocation;

/**
 * 1.20.2's {@code RecipeHolder} for this line's tests: a recipe and its id, which a 1.20.1 recipe carries itself. What
 * {@link TestCompat#recipeFor} and {@link TestCompat#recipesFor} find. Only on this line.
 */
public record RecipeHolder<T>(ResourceLocation id, T value) {
}

package com.phantomwing.theleadage.gametest;

import net.minecraft.world.Container;

/**
 * 1.21's {@code RecipeInput} for this line's tests, which 1.20.1 doesn't have: what a recipe is matched against, as the
 * container 1.20.1's recipes take ({@link TestCompat#recipeFor}, {@link TestCompat#assemble}). Only on this line; the
 * bodies name it as they name vanilla's on the others.
 */
public interface RecipeInput {
    Container container();
}

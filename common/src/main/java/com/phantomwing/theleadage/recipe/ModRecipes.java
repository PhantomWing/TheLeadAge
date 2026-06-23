package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class ModRecipes {
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<SimpleCraftingRecipeSerializer<LeadedGlassCombineRecipe>> LEADED_GLASS_COMBINE =
            SERIALIZERS.register("leaded_glass_combine",
                    () -> new SimpleCraftingRecipeSerializer<>(LeadedGlassCombineRecipe::new));

    public static final RegistrySupplier<SimpleCraftingRecipeSerializer<LeadedGlassDoorRecipe>> LEADED_GLASS_DOOR =
            SERIALIZERS.register("leaded_glass_door",
                    () -> new SimpleCraftingRecipeSerializer<>(LeadedGlassDoorRecipe::new));

    public static final RegistrySupplier<SimpleCraftingRecipeSerializer<LeadedGlassTrapdoorRecipe>> LEADED_GLASS_TRAPDOOR =
            SERIALIZERS.register("leaded_glass_trapdoor",
                    () -> new SimpleCraftingRecipeSerializer<>(LeadedGlassTrapdoorRecipe::new));

    public static void register() {
        SERIALIZERS.register();
    }
}

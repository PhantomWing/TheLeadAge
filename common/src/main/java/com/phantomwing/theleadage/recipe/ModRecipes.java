package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.CustomRecipe;

public class ModRecipes {
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<CustomRecipe.Serializer<LeadedGlassCombineRecipe>> LEADED_GLASS_COMBINE =
            SERIALIZERS.register("leaded_glass_combine",
                    () -> new CustomRecipe.Serializer<>(LeadedGlassCombineRecipe::new));

    public static final RegistrySupplier<CustomRecipe.Serializer<LeadedGlassDoorRecipe>> LEADED_GLASS_DOOR =
            SERIALIZERS.register("leaded_glass_door",
                    () -> new CustomRecipe.Serializer<>(LeadedGlassDoorRecipe::new));

    public static final RegistrySupplier<CustomRecipe.Serializer<LeadedGlassTrapdoorRecipe>> LEADED_GLASS_TRAPDOOR =
            SERIALIZERS.register("leaded_glass_trapdoor",
                    () -> new CustomRecipe.Serializer<>(LeadedGlassTrapdoorRecipe::new));

    // Stonecutter recipe that carries the input plain pane's colour onto the cut pattern. Reuses
    // the vanilla minecraft:stonecutting recipe type; only the serializer (this) is custom.
    public static final RegistrySupplier<ColoredPaneStonecutterRecipe.Serializer> COLORED_PANE_STONECUTTING =
            SERIALIZERS.register("colored_pane_stonecutting", ColoredPaneStonecutterRecipe.Serializer::new);

    // Crafts plain leaded glass panes from vanilla glass panes (+ a lead ingot), keeping the pane colour.
    public static final RegistrySupplier<CustomRecipe.Serializer<LeadedGlassPaneCraftRecipe>> LEADED_GLASS_PANE_CRAFT =
            SERIALIZERS.register("leaded_glass_pane_craft",
                    () -> new CustomRecipe.Serializer<>(LeadedGlassPaneCraftRecipe::new));

    public static void register() {
        SERIALIZERS.register();
    }
}

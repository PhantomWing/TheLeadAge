package com.phantomwing.theleadage.recipe;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeSerializer<LeadedGlassCombineRecipe>> LEADED_GLASS_COMBINE =
            SERIALIZERS.register("leaded_glass_combine", () -> special(LeadedGlassCombineRecipe::new));

    public static final RegistrySupplier<RecipeSerializer<LeadedGlassDoorRecipe>> LEADED_GLASS_DOOR =
            SERIALIZERS.register("leaded_glass_door", () -> special(LeadedGlassDoorRecipe::new));

    public static final RegistrySupplier<RecipeSerializer<LeadedGlassTrapdoorRecipe>> LEADED_GLASS_TRAPDOOR =
            SERIALIZERS.register("leaded_glass_trapdoor", () -> special(LeadedGlassTrapdoorRecipe::new));

    // Stonecutter recipe that carries the input plain pane's colour onto the cut pattern. Reuses
    // the vanilla minecraft:stonecutting recipe type; only the serializer (this) is custom.
    public static final RegistrySupplier<RecipeSerializer<ColoredPaneStonecutterRecipe>> COLORED_PANE_STONECUTTING =
            SERIALIZERS.register("colored_pane_stonecutting", ColoredPaneStonecutterRecipe::serializer);

    // Crafts plain leaded glass panes from vanilla glass panes (+ a lead ingot), keeping the pane colour.
    public static final RegistrySupplier<RecipeSerializer<LeadedGlassPaneCraftRecipe>> LEADED_GLASS_PANE_CRAFT =
            SERIALIZERS.register("leaded_glass_pane_craft", () -> special(LeadedGlassPaneCraftRecipe::new));

    /**
     * 26.1: {@code CustomRecipe.Serializer} is gone and {@link RecipeSerializer} is a record of a
     * map codec plus a stream codec. These recipes read the crafting grid and carry no data of
     * their own, so both codecs are unit codecs over a single stateless instance.
     */
    private static <T extends CustomRecipe> RecipeSerializer<T> special(Supplier<T> factory) {
        T recipe = factory.get();
        return new RecipeSerializer<>(MapCodec.unit(recipe), StreamCodec.unit(recipe));
    }

    public static void register() {
        SERIALIZERS.register();
    }
}

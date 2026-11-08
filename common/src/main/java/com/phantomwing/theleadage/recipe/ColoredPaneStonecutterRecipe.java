package com.phantomwing.theleadage.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import java.util.Collections;

/**
 * A stonecutter recipe that cuts a plain leaded glass pane into a patterned pane, carrying the
 * input pane's colour onto every region of the result — where a vanilla {@link StonecutterRecipe}
 * would give the fixed all-clear result baked into the recipe. Cutting a red pane yields an
 * all-red pattern; cutting a clear pane still yields a clear pattern (the vanilla behaviour).
 *
 * <p>This extends {@code StonecutterRecipe} rather than {@code SingleItemRecipe} on purpose:
 * {@code StonecutterMenu} casts the selected recipe to the concrete {@code StonecutterRecipe}
 * class before calling {@link #assemble}, so a plain {@code SingleItemRecipe}/{@code CustomRecipe}
 * would crash on selection. The recipe <i>type</i> stays {@code minecraft:stonecutting} (inherited)
 * so the menu lists it alongside the others; only the serializer is ours, so datapack loading
 * rebuilds this subclass instead of a vanilla stonecutter recipe.
 *
 * <p>The static {@code result} (an all-clear patterned pane) is still what the recipe-list buttons,
 * the recipe book and JEI show; {@link #assemble} recolours a copy of it from the live input for
 * the output slot. {@code StonecutterScreenMixin} redirects the button/tooltip preview through
 * {@code assemble} so the icons match the coloured output too.
 */
public class ColoredPaneStonecutterRecipe extends StonecutterRecipe {
    public ColoredPaneStonecutterRecipe(ResourceLocation id, String group, Ingredient ingredient, ItemStack result) {
        super(id, group, ingredient, result);
    }

    @Override
    public ItemStack assemble(Container input, RegistryAccess registries) {
        ItemStack output = super.assemble(input, registries); // the target frame's all-clear pane
        LeadedGlassConfig target = ModDataComponents.LEADED_GLASS_CONFIG.get(output);
        if (target == null) {
            return output; // not a configured pane (shouldn't happen for our recipes)
        }
        int color = primaryColor(input.getItem(0));
        if (color == LeadedGlassConfig.CLEAR) {
            return output; // clear input → the static all-clear result is already correct
        }
        ModDataComponents.LEADED_GLASS_CONFIG.set(output, new LeadedGlassConfig(target.frame(),
                        Collections.nCopies(target.frame().regions(), color)));
        return output;
    }

    /** The plain input pane's single region colour, or {@link LeadedGlassConfig#CLEAR} if unset. */
    private static int primaryColor(ItemStack input) {
        LeadedGlassConfig config = ModDataComponents.LEADED_GLASS_CONFIG.get(input);
        if (config == null || config.colors().isEmpty()) {
            return LeadedGlassConfig.CLEAR;
        }
        return config.colors().get(0);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COLORED_PANE_STONECUTTING.get();
    }

    /**
     * Vanilla's stonecutting JSON (group + ingredient + result), but a result that carries the pane's design in its tag,
     * where vanilla's is an item id and a count: {@code {"item": ..., "count": 1, "nbt": "{...}"}}, as Forge writes a
     * crafting result's. 1.20.1's own single-item serializer can't be reused from here, its factory being package-private.
     */
    public static class Serializer implements RecipeSerializer<ColoredPaneStonecutterRecipe> {
        @Override
        public ColoredPaneStonecutterRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient ingredient = GsonHelper.isArrayNode(json, "ingredient")
                    ? Ingredient.fromJson(GsonHelper.getAsJsonArray(json, "ingredient"), false)
                    : Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"), false);
            JsonObject result = GsonHelper.getAsJsonObject(json, "result");
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(result, "item"))),
                    GsonHelper.getAsInt(result, "count", 1));
            if (result.has("nbt")) {
                try {
                    stack.setTag(TagParser.parseTag(GsonHelper.getAsString(result, "nbt")));
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException("Invalid nbt in " + id + ": " + e.getMessage());
                }
            }
            return new ColoredPaneStonecutterRecipe(id, group, ingredient, stack);
        }

        @Override
        public ColoredPaneStonecutterRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            return new ColoredPaneStonecutterRecipe(id, group, ingredient, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ColoredPaneStonecutterRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            recipe.getIngredients().get(0).toNetwork(buffer);
            buffer.writeItem(recipe.result);
        }
    }
}

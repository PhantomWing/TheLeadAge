package com.phantomwing.theleadage.neoforge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.tags.CommonTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The recipes Create makes of lead, in the format of Create Fly, the Fabric port of Create, which keeps Create's mod
 * id and recipe types but not its JSON: one {@code ingredient} rather than a list. They go to the Fabric jar alone
 * ({@code fabric/src/generated/resources}, the path the data run passes as {@code theleadage.datagen.fabricOutput}),
 * since NeoForge's Create would fail to parse them, and are written as JSON here because nothing on NeoForge has
 * Create Fly's classes. As The Silver Age's are.
 *
 * <p>The same recipes as 1.21.1's NeoForge Create compat: the Press makes Lead Sheets, and Crushed Raw Lead is washed
 * into nuggets or smelted into an ingot. Create Fly leaves out Create's crushing of other mods' lead, so the mod's
 * ore and raw lead get crushing recipes of its own, as Create crushes them on NeoForge.</p>
 */
public class CreateFlyRecipeProvider implements DataProvider {
    private static final String CRUSHED_RAW_LEAD = "create:crushed_raw_lead";
    /** Fabric's top-level conditions key ({@code ResourceConditions.CONDITIONS_KEY}). */
    private static final String FABRIC_CONDITIONS = "fabric:load_conditions";
    /** As raw lead smelts, and 1.21.1's crushed lead did. */
    private static final float SMELTING_EXPERIENCE = 0.7f;

    private final PackOutput.PathProvider recipes;
    private final PackOutput.PathProvider advancements;

    public CreateFlyRecipeProvider(PackOutput fabricOutput) {
        this.recipes = fabricOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.advancements = fabricOutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        List<CompletableFuture<?>> saved = new ArrayList<>();

        JsonObject pressing = recipe("create:pressing");
        pressing.addProperty("ingredient", tag(CommonTags.Items.INGOTS_LEAD));
        bothResultForms(pressing, output(id(ModItems.LEAD_SHEET.get()), 1, 1));
        saved.add(save(cache, "pressing/lead_sheet", pressing));

        JsonObject splashing = recipe("create:splashing");
        splashing.addProperty("ingredient", CRUSHED_RAW_LEAD);
        splashing.add("results", outputs(output(id(ModItems.LEAD_NUGGET.get()), 9, 1)));
        saved.add(save(cache, "splashing/crushed_raw_lead", splashing));

        // Create's own crushing of lead, which Create Fly doesn't ship.
        saved.add(save(cache, "crushing/lead_ore", crushing("#c:ores/lead",
                output(CRUSHED_RAW_LEAD, 1, 1), output(CRUSHED_RAW_LEAD, 1, 0.75f),
                output("create:experience_nugget", 1, 0.75f))));
        saved.add(save(cache, "crushing/raw_lead", crushing("#c:raw_materials/lead",
                output(CRUSHED_RAW_LEAD, 1, 1), output("create:experience_nugget", 1, 0.75f))));
        saved.add(save(cache, "crushing/raw_lead_block", crushing("#c:storage_blocks/raw_lead",
                output(CRUSHED_RAW_LEAD, 9, 1), output("create:experience_nugget", 9, 0.75f))));

        saved.addAll(cooking(cache, "minecraft:smelting", "smelting", 200));
        saved.addAll(cooking(cache, "minecraft:blasting", "blasting", 100));

        return CompletableFuture.allOf(saved.toArray(CompletableFuture[]::new));
    }

    /** Crushed Raw Lead smelted into a Lead Ingot, as raw lead is, with its recipe book unlock. */
    private List<CompletableFuture<?>> cooking(CachedOutput cache, String type, String kind, int time) {
        String name = "lead_ingot_from_crushed_raw_lead_" + kind;
        JsonObject cooking = recipe(type);
        cooking.addProperty("category", "misc");
        cooking.addProperty("cookingtime", time);
        cooking.addProperty("experience", SMELTING_EXPERIENCE);
        cooking.addProperty("ingredient", CRUSHED_RAW_LEAD);
        cooking.add("result", output(id(ModItems.LEAD_INGOT.get()), 1, 1));

        String recipeId = TheLeadAge.MOD_ID + ":" + name;
        JsonObject unlock = new JsonObject();
        unlock.addProperty("parent", "minecraft:recipes/root");
        JsonObject criteria = new JsonObject();
        JsonObject hasItem = new JsonObject();
        hasItem.addProperty("trigger", "minecraft:inventory_changed");
        JsonObject itemConditions = new JsonObject();
        JsonArray items = new JsonArray();
        JsonObject predicate = new JsonObject();
        predicate.addProperty("items", CRUSHED_RAW_LEAD);
        items.add(predicate);
        itemConditions.add("items", items);
        hasItem.add("conditions", itemConditions);
        criteria.add("has_crushed_raw_lead", hasItem);
        JsonObject hasRecipe = new JsonObject();
        hasRecipe.addProperty("trigger", "minecraft:recipe_unlocked");
        JsonObject recipeConditions = new JsonObject();
        recipeConditions.addProperty("recipe", recipeId);
        hasRecipe.add("conditions", recipeConditions);
        criteria.add("has_the_recipe", hasRecipe);
        unlock.add("criteria", criteria);
        JsonArray requirements = new JsonArray();
        JsonArray anyOf = new JsonArray();
        anyOf.add("has_the_recipe");
        anyOf.add("has_crushed_raw_lead");
        requirements.add(anyOf);
        unlock.add("requirements", requirements);
        JsonObject rewards = new JsonObject();
        JsonArray rewardRecipes = new JsonArray();
        rewardRecipes.add(recipeId);
        rewards.add("recipes", rewardRecipes);
        unlock.add("rewards", rewards);
        unlock.add(FABRIC_CONDITIONS, conditions());

        return List.of(save(cache, name, cooking),
                DataProvider.saveStable(cache, unlock, advancements.json(TheLeadAge.resourceLocation("recipes/misc/" + name))));
    }

    private static JsonObject crushing(String input, JsonObject... results) {
        JsonObject crushing = recipe("create:crushing");
        crushing.addProperty("ingredient", input);
        crushing.addProperty("processing_time", 400);
        crushing.add("results", outputs(results));
        return crushing;
    }

    private static JsonObject recipe(String type) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", type);
        recipe.add(FABRIC_CONDITIONS, conditions());
        return recipe;
    }

    /** Create Fly loaded. */
    private static JsonArray conditions() {
        JsonArray conditions = new JsonArray();
        JsonObject create = new JsonObject();
        create.addProperty("condition", "fabric:all_mods_loaded");
        JsonArray mods = new JsonArray();
        mods.add(ModIds.CREATE);
        create.add("values", mods);
        conditions.add(create);
        return conditions;
    }

    /** One of Create Fly's {@code ProcessingOutput}s: the defaults left out. */
    private static JsonObject output(String id, int count, float chance) {
        JsonObject output = new JsonObject();
        output.addProperty("id", id);
        if (count != 1) {
            output.addProperty("count", count);
        }
        if (chance != 1) {
            output.addProperty("chance", chance);
        }
        return output;
    }

    /**
     * A Press's one result, as both {@code result} and {@code results}: Create Fly 6.0.9-2 (and its 1.21.10, 26.1 and
     * 26.2 builds) reads a list, every build before it (and its 1.21.11 build) one item stack, and each ignores the key
     * it doesn't read.
     */
    private static void bothResultForms(JsonObject recipe, JsonObject result) {
        recipe.add("result", result);
        recipe.add("results", outputs(result.deepCopy()));
    }

    private static JsonArray outputs(JsonObject... outputs) {
        JsonArray array = new JsonArray();
        for (JsonObject output : outputs) {
            array.add(output);
        }
        return array;
    }

    private CompletableFuture<?> save(CachedOutput cache, String name, JsonObject json) {
        return DataProvider.saveStable(cache, json, recipes.json(TheLeadAge.resourceLocation(name)));
    }

    private static String id(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private static String tag(TagKey<Item> tag) {
        return "#" + tag.location();
    }

    @Override
    public @NotNull String getName() {
        return "The Lead Age Create Fly recipes";
    }
}

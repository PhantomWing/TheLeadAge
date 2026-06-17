package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.neoforge.Configuration;
import com.phantomwing.theleadage.neoforge.condition.ConfigBooleanCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    private static final float ORE_XP = 0.7f; // iron-like

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Raw lead + both ores smelt/blast into ingots.
        oreSmeltAndBlast(output, ModItems.RAW_LEAD.get(), ModItems.LEAD_INGOT.get());
        oreSmeltAndBlast(output, ModItems.LEAD_ORE.get(), ModItems.LEAD_INGOT.get());
        oreSmeltAndBlast(output, ModItems.DEEPSLATE_LEAD_ORE.get(), ModItems.LEAD_INGOT.get());

        // 9 <-> storage block, 9 nuggets <-> ingot.
        storage(output, ModItems.RAW_LEAD.get(), ModItems.RAW_LEAD_BLOCK.get(), RecipeCategory.BUILDING_BLOCKS);
        storage(output, ModItems.LEAD_INGOT.get(), ModItems.LEAD_BLOCK.get(), RecipeCategory.BUILDING_BLOCKS);
        storage(output, ModItems.LEAD_NUGGET.get(), ModItems.LEAD_INGOT.get(), RecipeCategory.MISC);

        // Lead tools + armor, crafted from lead ingots.
        ItemLike ingot = ModItems.LEAD_INGOT.get();
        sword(output, ModItems.LEAD_SWORD.get(), ingot);
        pickaxe(output, ModItems.LEAD_PICKAXE.get(), ingot);
        axe(output, ModItems.LEAD_AXE.get(), ingot);
        shovel(output, ModItems.LEAD_SHOVEL.get(), ingot);
        hoe(output, ModItems.LEAD_HOE.get(), ingot);
        helmet(output, ModItems.LEAD_HELMET.get(), ingot);
        chestplate(output, ModItems.LEAD_CHESTPLATE.get(), ingot);
        leggings(output, ModItems.LEAD_LEGGINGS.get(), ingot);
        boots(output, ModItems.LEAD_BOOTS.get(), ingot);

        // Conditional recipe overrides. Each is gated on the master toggle AND its
        // own per-recipe toggle, so either switch turns it off.
        ICondition master = new ConfigBooleanCondition(Configuration.ENABLE_RECIPE_OVERRIDES_ID);
        ICondition fishingRodOverride = new AndCondition(List.of(
                master, new ConfigBooleanCondition(Configuration.OVERRIDE_FISHING_ROD_RECIPE_ID)));
        ICondition heavyCoreOverride = new AndCondition(List.of(
                master, new ConfigBooleanCondition(Configuration.OVERRIDE_HEAVY_CORE_RECIPE_ID)));

        RecipeOutput fishingRodOutput = output.withConditions(fishingRodOverride);
        RecipeOutput fishingRodFallbackOutput = output.withConditions(new NotCondition(fishingRodOverride));
        RecipeOutput heavyCoreOutput = output.withConditions(heavyCoreOverride);

        // Fishing Rod: the bottom-right string becomes a lead nugget. Saved at
        // minecraft:fishing_rod so it replaces the vanilla recipe when enabled.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.FISHING_ROD)
                .pattern("  #")
                .pattern(" #X")
                .pattern("# N")
                .define('#', Items.STICK)
                .define('X', Items.STRING)
                .define('N', ModItems.LEAD_NUGGET.get())
                .unlockedBy(getHasName(ModItems.LEAD_NUGGET.get()), has(ModItems.LEAD_NUGGET.get()))
                .save(fishingRodOutput);
        // Original vanilla recipe, kept (as a _fallback) when the override is off.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.FISHING_ROD)
                .pattern("  #")
                .pattern(" #X")
                .pattern("# X")
                .define('#', Items.STICK)
                .define('X', Items.STRING)
                .unlockedBy(getHasName(Items.STRING), has(Items.STRING))
                .save(fishingRodFallbackOutput, "minecraft:fishing_rod_fallback");

        // Heavy Core: uncraftable in vanilla, so this is an ADD (no fallback) — a
        // dense lead construct around a netherite core. Unlocked once the player
        // has picked up a Heavy Core.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.HEAVY_CORE)
                .pattern("LLL")
                .pattern("LNL")
                .pattern("LLL")
                .define('L', ModItems.LEAD_BLOCK.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy(getHasName(Items.HEAVY_CORE), has(Items.HEAVY_CORE))
                .save(heavyCoreOutput);
    }

    private static String name(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    private static String id(String path) {
        return TheLeadAge.MOD_ID + ":" + path;
    }

    private void oreSmeltAndBlast(RecipeOutput output, ItemLike material, ItemLike result) {
        cook(output, material, result, 200, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, "smelting");
        cook(output, material, result, 100, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, "blasting");
    }

    private <T extends AbstractCookingRecipe> void cook(RecipeOutput output, ItemLike material, ItemLike result, int time,
                                                        RecipeSerializer<T> serializer, AbstractCookingRecipe.Factory<T> factory, String suffix) {
        SimpleCookingRecipeBuilder
                .generic(Ingredient.of(material), RecipeCategory.MISC, result, ORE_XP, time, serializer, factory)
                .unlockedBy(getHasName(material), has(material))
                .save(output, id(name(result) + "_from_" + name(material) + "_" + suffix));
    }

    private void sword(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern("M").pattern("M").pattern("S")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void pickaxe(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
                .pattern("MMM").pattern(" S ").pattern(" S ")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void axe(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
                .pattern("MM").pattern("MS").pattern(" S")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void shovel(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
                .pattern("M").pattern("S").pattern("S")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void hoe(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
                .pattern("MM").pattern(" S").pattern(" S")
                .define('M', material).define('S', Items.STICK)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void helmet(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern("MMM").pattern("M M")
                .define('M', material)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void chestplate(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern("M M").pattern("MMM").pattern("MMM")
                .define('M', material)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void leggings(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern("MMM").pattern("M M").pattern("M M")
                .define('M', material)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void boots(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern("M M").pattern("M M")
                .define('M', material)
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void storage(RecipeOutput output, ItemLike item, ItemLike block, RecipeCategory packedCategory) {
        // 9 item -> block
        ShapedRecipeBuilder.shaped(packedCategory, block)
                .pattern("###").pattern("###").pattern("###")
                .define('#', item)
                .unlockedBy(getHasName(item), has(item))
                .save(output, id(name(block) + "_from_" + name(item)));
        // block -> 9 item
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item, 9)
                .requires(block)
                .unlockedBy(getHasName(block), has(block))
                .save(output, id(name(item) + "_from_" + name(block)));
    }
}

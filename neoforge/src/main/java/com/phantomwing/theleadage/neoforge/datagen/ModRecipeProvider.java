package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
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
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
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

        // Decorative lead blocks (crafting + stonecutter paths, mirroring The
        // Silver Age and vanilla copper/stone-brick families).
        ItemLike block = ModItems.LEAD_BLOCK.get();
        ItemLike cut = ModItems.CUT_LEAD.get();
        ItemLike bricks = ModItems.LEAD_BRICKS.get();

        twoBytwo(output, RecipeCategory.BUILDING_BLOCKS, cut, block, 4);
        stoneCutting(output, cut, block, 4);

        twoBytwo(output, RecipeCategory.BUILDING_BLOCKS, bricks, ingot, 4);
        stoneCutting(output, bricks, block, 4);

        stairsWithCutting(output, ModItems.LEAD_BRICK_STAIRS.get(), bricks);
        stoneCutting(output, ModItems.LEAD_BRICK_STAIRS.get(), block, 4);
        slabWithCutting(output, ModItems.LEAD_BRICK_SLAB.get(), bricks);
        stoneCutting(output, ModItems.LEAD_BRICK_SLAB.get(), block, 8);

        stairsWithCutting(output, ModItems.CUT_LEAD_STAIRS.get(), cut);
        stoneCutting(output, ModItems.CUT_LEAD_STAIRS.get(), block, 4);
        slabWithCutting(output, ModItems.CUT_LEAD_SLAB.get(), cut);
        stoneCutting(output, ModItems.CUT_LEAD_SLAB.get(), block, 8);

        // Chiseled: 2 cut lead slabs -> 1 (vanilla chiseled-stone-bricks ratio).
        oneBytwo(output, RecipeCategory.BUILDING_BLOCKS, ModItems.CHISELED_LEAD.get(), ModItems.CUT_LEAD_SLAB.get(), 1);
        stoneCutting(output, ModItems.CHISELED_LEAD.get(), cut, 1);
        stoneCutting(output, ModItems.CHISELED_LEAD.get(), block, 4);

        // Pillar: 2 lead blocks -> 2 (vanilla quartz-pillar ratio), or stonecut 1 -> 1.
        oneBytwo(output, RecipeCategory.BUILDING_BLOCKS, ModItems.LEAD_PILLAR.get(), block, 2);
        stoneCutting(output, ModItems.LEAD_PILLAR.get(), block, 1);

        grateWithCutting(output, ModItems.LEAD_GRATE.get(), block);

        door(output, ModItems.LEAD_DOOR.get(), ingot);
        trapdoor(output, ModItems.LEAD_TRAPDOOR.get(), ingot);

        // Leaded glass: a glass block reinforced with 8 lead nuggets around it.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.LEADED_GLASS.get())
                .pattern("NNN").pattern("NGN").pattern("NNN")
                .define('N', ModItems.LEAD_NUGGET.get())
                .define('G', Items.GLASS)
                .unlockedBy(getHasName(ModItems.LEAD_NUGGET.get()), has(ModItems.LEAD_NUGGET.get()))
                .save(output);
        leadedGlassFamily(output);

        // Heavy Orb: 8 lead ingots around a lead block.
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HEAVY_ORB.get())
                .pattern("III").pattern("ILI").pattern("III")
                .define('I', ModItems.LEAD_INGOT.get())
                .define('L', ModItems.LEAD_BLOCK.get())
                .unlockedBy(getHasName(ModItems.LEAD_BLOCK.get()), has(ModItems.LEAD_BLOCK.get()))
                .save(output);

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

    /** Leaded glass pane + the 16 dyed leaded glass blocks & panes, with their recipes. */
    private void leadedGlassFamily(RecipeOutput output) {
        // Plain leaded glass pane: 6 leaded glass -> 16 panes (vanilla glass-pane ratio).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.LEADED_GLASS_PANE.get(), 16)
                .pattern("###").pattern("###")
                .define('#', ModItems.LEADED_GLASS.get())
                .unlockedBy(getHasName(ModItems.LEADED_GLASS.get()), has(ModItems.LEADED_GLASS.get()))
                .save(output);

        for (DyeColor color : DyeColor.values()) {
            ItemLike dye = DyeItem.byColor(color);
            ItemLike stainedGlass = vanillaItem(color.getName() + "_stained_glass");
            ItemLike leaded = ModBlocks.STAINED_LEADED_GLASS.get(color).get();
            ItemLike leadedPane = ModBlocks.STAINED_LEADED_GLASS_PANE.get(color).get();

            // Dye plain leaded glass: 8 leaded glass + 1 dye -> 8 dyed (vanilla stained-glass ratio).
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, leaded, 8)
                    .pattern("###").pattern("#D#").pattern("###")
                    .define('#', ModItems.LEADED_GLASS.get())
                    .define('D', dye)
                    .unlockedBy(getHasName(ModItems.LEADED_GLASS.get()), has(ModItems.LEADED_GLASS.get()))
                    .save(output);
            // "Lead" a vanilla stained glass: surround it with 8 lead nuggets -> 1.
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, leaded)
                    .pattern("NNN").pattern("NGN").pattern("NNN")
                    .define('N', ModItems.LEAD_NUGGET.get())
                    .define('G', stainedGlass)
                    .unlockedBy(getHasName(ModItems.LEAD_NUGGET.get()), has(ModItems.LEAD_NUGGET.get()))
                    .save(output, id(name(leaded) + "_from_" + name(stainedGlass)));

            // Dyed pane from dyed block: 6 -> 16.
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, leadedPane, 16)
                    .pattern("###").pattern("###")
                    .define('#', leaded)
                    .unlockedBy(getHasName(leaded), has(leaded))
                    .save(output);
            // Dye plain leaded glass panes: 8 panes + 1 dye -> 8 dyed panes.
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, leadedPane, 8)
                    .pattern("###").pattern("#D#").pattern("###")
                    .define('#', ModItems.LEADED_GLASS_PANE.get())
                    .define('D', dye)
                    .unlockedBy(getHasName(ModItems.LEADED_GLASS_PANE.get()), has(ModItems.LEADED_GLASS_PANE.get()))
                    .save(output, id(name(leadedPane) + "_from_" + name(ModItems.LEADED_GLASS_PANE.get())));
        }
    }

    private static ItemLike vanillaItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(path));
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

    private void twoBytwo(RecipeOutput output, RecipeCategory category, ItemLike result, ItemLike material, int count) {
        ShapedRecipeBuilder.shaped(category, result, count)
                .pattern("##").pattern("##")
                .define('#', material)
                .unlockedBy(getHasName(material), has(material))
                .save(output, id(name(result) + "_from_" + name(material)));
    }

    private void oneBytwo(RecipeOutput output, RecipeCategory category, ItemLike result, ItemLike material, int count) {
        ShapedRecipeBuilder.shaped(category, result, count)
                .pattern("#").pattern("#")
                .define('#', material)
                .unlockedBy(getHasName(material), has(material))
                .save(output, id(name(result) + "_from_" + name(material)));
    }

    private void stairsWithCutting(RecipeOutput output, ItemLike result, ItemLike material) {
        stoneCutting(output, result, material, 1);
        stairBuilder(result, Ingredient.of(material))
                .group(name(material))
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void slabWithCutting(RecipeOutput output, ItemLike result, ItemLike material) {
        stoneCutting(output, result, material, 2);
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, result, Ingredient.of(material))
                .group(name(material))
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void grateWithCutting(RecipeOutput output, ItemLike result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4)
                .pattern(" # ").pattern("# #").pattern(" # ")
                .define('#', material)
                .unlockedBy(getHasName(material), has(material))
                .save(output, id(name(result) + "_from_" + name(material)));
        stoneCutting(output, result, material, 4);
    }

    private void door(RecipeOutput output, ItemLike result, ItemLike material) {
        doorBuilder(result, Ingredient.of(material))
                .group(name(material))
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void trapdoor(RecipeOutput output, ItemLike result, ItemLike material) {
        trapdoorBuilder(result, Ingredient.of(material))
                .group(name(material))
                .unlockedBy(getHasName(material), has(material))
                .save(output);
    }

    private void stoneCutting(RecipeOutput output, ItemLike result, ItemLike material, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(material), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy(getHasName(material), has(material))
                .save(output, id(name(result) + "_from_" + name(material) + "_stonecutting"));
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

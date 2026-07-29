package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.item.ModItems;
import com.simibubi.create.AllItems;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
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
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.recipe.ColoredPaneStonecutterRecipe;
import com.phantomwing.theleadage.recipe.LeadedGlassCombineRecipe;
import com.phantomwing.theleadage.recipe.LeadedGlassPaneCraftRecipe;
import com.phantomwing.theleadage.recipe.LeadedGlassDoorRecipe;
import com.phantomwing.theleadage.recipe.LeadedGlassTrapdoorRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;

import java.util.Collections;
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

        // Create compat: smelt/blast Create's crushed_raw_lead into our ingot. Create's Crushing Wheels
        // already turn our ores and raw lead into crushed_raw_lead (its crushing recipes are gated on the
        // c:ores/lead + c:raw_materials/lead tags, which we populate), but Create only bridges the crushed
        // ore BACK to an ingot for Immersive Engineering / Mekanism / Oreganized / Thermal — so without
        // this the crushed lead would be a dead end.
        oreSmeltAndBlast(output.withConditions(new ModLoadedCondition(ModIds.CREATE)),
                AllItems.CRUSHED_LEAD.get(), ModItems.LEAD_INGOT.get());

        // 9 <-> storage block, 9 nuggets <-> ingot.
        storage(output, ModItems.RAW_LEAD.get(), ModItems.RAW_LEAD_BLOCK.get(), RecipeCategory.BUILDING_BLOCKS);
        storage(output, ModItems.LEAD_INGOT.get(), ModItems.LEAD_BLOCK.get(), RecipeCategory.BUILDING_BLOCKS);
        storage(output, ModItems.LEAD_NUGGET.get(), ModItems.LEAD_INGOT.get(), RecipeCategory.MISC);

        // Farmer's Delight compat: the Lead Knife (ingot over a stick). Gated on FD — without it the
        // item is a hidden SwordItem fallback, so it deliberately has no recipe.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.LEAD_KNIFE.get(), 1)
                .pattern("X")
                .pattern("I")
                .define('X', ModItems.LEAD_INGOT.get())
                .define('I', Items.STICK)
                .unlockedBy(getHasName(ModItems.LEAD_INGOT.get()), has(ModItems.LEAD_INGOT.get()))
                .save(output.withConditions(new ModLoadedCondition(ModIds.FARMERS_DELIGHT)));

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

        wallWithCutting(output, ModItems.LEAD_BRICK_WALL.get(), bricks);
        stoneCutting(output, ModItems.LEAD_BRICK_WALL.get(), block, 4);

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

        // Leaded glass: 8 glass blocks around a lead ingot -> 8 leaded glass (matches the pane recipe).
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.LEADED_GLASS.get(), 8)
                .pattern("GGG").pattern("GIG").pattern("GGG")
                .define('G', Items.GLASS)
                .define('I', ModItems.LEAD_INGOT.get())
                .unlockedBy(getHasName(ModItems.LEAD_INGOT.get()), has(ModItems.LEAD_INGOT.get()))
                .save(output);
        leadedGlassFamily(output);

        // Lead torch: a torch column (stick + coal) tipped with a lead nugget — the salts burn
        // grayish-white (and toxic). Charcoal works too, like the vanilla torch.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.LEAD_TORCH.get())
                .pattern("N").pattern("C").pattern("S")
                .define('N', ModItems.LEAD_NUGGET.get())
                .define('C', Ingredient.of(Items.COAL, Items.CHARCOAL))
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.LEAD_NUGGET.get()), has(ModItems.LEAD_NUGGET.get()))
                .save(output);
        // Lead lantern: 8 lead nuggets around a lead torch (the vanilla lantern pattern) — the
        // enclosure makes it safe to stand beside.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.LEAD_LANTERN.get())
                .pattern("NNN").pattern("NTN").pattern("NNN")
                .define('N', ModItems.LEAD_NUGGET.get())
                .define('T', ModItems.LEAD_TORCH.get())
                .unlockedBy(getHasName(ModItems.LEAD_TORCH.get()), has(ModItems.LEAD_TORCH.get()))
                .save(output);

        // Lead Bulb: vanilla's copper bulb recipe, with lead blocks in place of copper — including
        // its yield of 4.
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.LEAD_BULB.get(), 4)
                .pattern(" L ").pattern("LBL").pattern(" R ")
                .define('L', ModItems.LEAD_BLOCK.get())
                .define('B', Items.BLAZE_ROD)
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.LEAD_BLOCK.get()), has(ModItems.LEAD_BLOCK.get()))
                .save(output);

        // Lead Weight: 8 lead ingots around a lead block.
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LEAD_WEIGHT.get())
                .pattern("III").pattern("ILI").pattern("III")
                .define('I', ModItems.LEAD_INGOT.get())
                .define('L', ModItems.LEAD_BLOCK.get())
                .unlockedBy(getHasName(ModItems.LEAD_BLOCK.get()), has(ModItems.LEAD_BLOCK.get()))
                .save(output);

        // Lead Chain: nugget / ingot / nugget (vanilla chain ratio).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.LEAD_CHAIN.get())
                .pattern("N").pattern("I").pattern("N")
                .define('N', ModItems.LEAD_NUGGET.get())
                .define('I', ModItems.LEAD_INGOT.get())
                .unlockedBy(getHasName(ModItems.LEAD_INGOT.get()), has(ModItems.LEAD_INGOT.get()))
                .save(output);
        // Lead Bars: 6 lead ingots -> 16 (vanilla iron-bars ratio).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.LEAD_BARS.get(), 16)
                .pattern("III").pattern("III")
                .define('I', ModItems.LEAD_INGOT.get())
                .unlockedBy(getHasName(ModItems.LEAD_INGOT.get()), has(ModItems.LEAD_INGOT.get()))
                .save(output);

        // Configurable leaded glass panel: code-matched (reads the grid arrangement → frame + colours).
        SpecialRecipeBuilder.special(LeadedGlassCombineRecipe::new).save(output, id("leaded_glass_combine"));
        // Glass panes around a lead ingot → 8 plain leaded glass panes of that glass's colour.
        SpecialRecipeBuilder.special(LeadedGlassPaneCraftRecipe::new).save(output, id("leaded_glass_pane_craft"));
        // Leaded glass door: lead door + a configured pane → door carrying that pane's design.
        SpecialRecipeBuilder.special(LeadedGlassDoorRecipe::new).save(output, id("leaded_glass_door"));
        // Leaded glass trapdoor: lead trapdoor + a configured pane → trapdoor carrying that design.
        SpecialRecipeBuilder.special(LeadedGlassTrapdoorRecipe::new).save(output, id("leaded_glass_trapdoor"));

        // Stonecutter path: a plain leaded glass pane cuts into any pattern's pane, 1:1, keeping
        // the input pane's colour on every region (ColoredPaneStonecutterRecipe; a clear input
        // gives a clear pattern). One entry per frame so both orientations of split/diagonal/bars
        // are pickable directly; plain itself is skipped (it's the input).
        for (LeadedGlassFrame frame : LeadedGlassFrame.values()) {
            if (frame != LeadedGlassFrame.PLAIN) {
                paneStonecutting(output, frame);
            }
        }

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

    /** The 16 dyed leaded glass blocks (the colour palette). Panes are built from these
     *  via the code-matched leaded_glass_pane recipes, not shaped recipes. */
    private void leadedGlassFamily(RecipeOutput output) {
        for (DyeColor color : DyeColor.values()) {
            ItemLike dye = DyeItem.byColor(color);
            ItemLike stainedGlass = vanillaItem(color.getName() + "_stained_glass");
            ItemLike leaded = ModBlocks.STAINED_LEADED_GLASS.get(color).get();

            // Dye plain leaded glass: 8 leaded glass + 1 dye -> 8 dyed (vanilla stained-glass ratio).
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, leaded, 8)
                    .pattern("###").pattern("#D#").pattern("###")
                    .define('#', ModItems.LEADED_GLASS.get())
                    .define('D', dye)
                    .unlockedBy(getHasName(ModItems.LEADED_GLASS.get()), has(ModItems.LEADED_GLASS.get()))
                    .save(output);
            // "Lead" vanilla stained glass: 8 stained glass around a lead ingot -> 8 of that colour.
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, leaded, 8)
                    .pattern("GGG").pattern("GIG").pattern("GGG")
                    .define('G', stainedGlass)
                    .define('I', ModItems.LEAD_INGOT.get())
                    .unlockedBy(getHasName(ModItems.LEAD_INGOT.get()), has(ModItems.LEAD_INGOT.get()))
                    .save(output, id(name(leaded) + "_from_" + name(stainedGlass)));
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

    private void wallWithCutting(RecipeOutput output, ItemLike result, ItemLike material) {
        stoneCutting(output, result, material, 1);
        wallBuilder(RecipeCategory.BUILDING_BLOCKS, result, Ingredient.of(material))
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

    /**
     * Plain leaded glass pane → one all-clear pane of the given frame. Built directly (not via
     * {@link SingleItemRecipeBuilder}) because the result carries the frame in its
     * leaded_glass_config component, which the builder cannot attach.
     */
    private void paneStonecutting(RecipeOutput output, LeadedGlassFrame frame) {
        ItemStack result = new ItemStack(ModItems.paneItemFor(frame));
        result.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), new LeadedGlassConfig(frame,
                Collections.nCopies(frame.regions(), LeadedGlassConfig.CLEAR)));
        output.accept(ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID,
                        "leaded_glass_pane_" + frame.getSerializedName() + "_from_stonecutting"),
                new ColoredPaneStonecutterRecipe("leaded_glass_pane",
                        Ingredient.of(ModItems.LEADED_GLASS_PANEL.get()), result), null);
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

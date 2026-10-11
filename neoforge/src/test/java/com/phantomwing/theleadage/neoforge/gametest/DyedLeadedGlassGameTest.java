package com.phantomwing.theleadage.neoforge.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassDyes;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassShatter;
import com.phantomwing.theleadage.compat.LeadedGlassDisplayRecipe;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.pack.DyedLeadedGlassPack;
import com.phantomwing.theleadage.recipe.StainedLeadedGlassRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassBlock;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.phantomwing.theleadage.neoforge.gametest.TestCompat.assemble;
import static com.phantomwing.theleadage.neoforge.gametest.TestCompat.fail;

/**
 * Dyed leaded glass in any dye colour. A dye mod's colours can't be added in a test, so these hold the parts
 * those colours go through to what vanilla's sixteen do, which are known to work.
 */
public final class DyedLeadedGlassGameTest {
    private DyedLeadedGlassGameTest() {
    }

    /**
     * The pack that gives a dye mod's colour its files writes them as datagen writes vanilla's: white glass's,
     * written by the pack, is the loot table datagen made for it, and each tag the pack adds to lists it.
     */
    public static void dyedGlassPackMatchesDatagen(GameTestHelper helper) {
        ResourceManager resources = helper.getLevel().getServer().getResourceManager();
        // var keeps the id's type out of the body: it is Identifier from 1.21.11.
        for (var file : DyedLeadedGlassPack.files(PackType.SERVER_DATA, List.of("white")).entrySet()) {
            var id = file.getKey();
            List<Resource> stack = resources.getResourceStack(id);
            if (stack.isEmpty()) {
                fail(helper, "Datagen wrote no " + id + " for white leaded glass");
                return;
            }
            boolean isTag = id.toString().contains(":tags/");
            boolean found = false;
            for (Resource resource : stack) {
                JsonObject json = read(resource);
                found |= isTag
                        ? json.getAsJsonArray("values").toString().contains("\"theleadage:white_leaded_glass\"")
                        : json.equals(file.getValue());
            }
            if (!found) {
                fail(helper, isTag
                        ? "The tag " + id + " doesn't list white leaded glass, so the pack shouldn't add the others to it"
                        : "The pack writes " + id + " as " + file.getValue() + ", datagen as " + read(stack.get(0)));
                return;
            }
        }
        helper.succeed();
    }

    /**
     * Vanilla's colours keep their crafting recipes in data: eight leaded glass around a dye, and eight
     * stained glass around a lead ingot. The recipe for a dye mod's colours leaves them alone.
     */
    public static void dyedGlassRecipesStayData(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack glass = new ItemStack(ModItems.LEADED_GLASS.get());
        ItemStack stained = new ItemStack(Items.RED_STAINED_GLASS);
        ItemStack ingot = new ItemStack(ModItems.LEAD_INGOT.get());
        List<ItemStack> byDye = new ArrayList<>(Collections.nCopies(9, glass));
        byDye.set(4, new ItemStack(Items.RED_DYE));
        List<ItemStack> byLead = new ArrayList<>(Collections.nCopies(9, stained));
        byLead.set(4, ingot);
        StainedLeadedGlassRecipe special = new StainedLeadedGlassRecipe(CraftingBookCategory.BUILDING);
        Block red = ModBlocks.STAINED_LEADED_GLASS.get(DyeColor.RED).get();
        for (List<ItemStack> grid : List.of(byDye, byLead)) {
            CraftingInput input = CraftingInput.of(3, 3, grid);
            if (special.matches(input, level)) {
                fail(helper, "The dye mods' recipe takes " + grid.get(4) + ", whose colour has a recipe of its own");
                return;
            }
            Optional<RecipeHolder<CraftingRecipe>> match =
                    level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
            ItemStack result = match.isEmpty() ? ItemStack.EMPTY : assemble(match.get().value(), input, level);
            if (!result.is(red.asItem()) || result.getCount() != 8) {
                fail(helper, "Red leaded glass around " + grid.get(4) + " made " + result + ", not 8 red leaded glass");
                return;
            }
        }
        helper.succeed();
    }

    /** Leaded glass that shatters reports its colour's stained glass, vanilla's for vanilla's colours. */
    public static void shatteredGlassIsItsColour(GameTestHelper helper) {
        for (DyeColor color : LeadedGlassDyes.colors().stream().filter(LeadedGlassDyes::isVanilla).toList()) {
            Block shattered = LeadedGlassShatter.shatteredGlass(color).getBlock();
            if (!(shattered instanceof StainedGlassBlock glass) || glass.getColor() != color
                    || !BuiltInRegistries.BLOCK.getKey(shattered).getNamespace().equals("minecraft")) {
                fail(helper, color.getName() + " leaded glass shatters into " + shattered);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * Vanilla's colours are named by their own translations, never the pattern a dye mod's take: the block, a
     * plain pane and a plain trapdoor ("Red Leaded Glass Pane", which issue #3 had as one word in Japanese).
     */
    public static void dyedGlassNamesAreTheirOwn(GameTestHelper helper) {
        for (DyeColor color : LeadedGlassDyes.colors().stream().filter(LeadedGlassDyes::isVanilla).toList()) {
            String name = color.getName();
            ItemStack pane = LeadedGlassDisplayRecipe.pane(LeadedGlassFrame.PLAIN, color.getId(), 1);
            ItemStack trapdoor = LeadedGlassDisplayRecipe.trapdoor(color.getId());
            ItemStack block = new ItemStack(ModBlocks.STAINED_LEADED_GLASS.get(color).get());
            for (var expected : List.of(
                    Map.entry(block, "block.theleadage." + name + "_leaded_glass"),
                    Map.entry(pane, "block.theleadage." + name + "_leaded_glass_pane"),
                    Map.entry(trapdoor, "block.theleadage." + name + "_leaded_glass_trapdoor"))) {
                String key = keyOf(expected.getKey().getHoverName());
                if (!expected.getValue().equals(key)) {
                    fail(helper, name + " " + expected.getKey().getItem() + " is named by " + key
                            + ", not " + expected.getValue());
                    return;
                }
            }
        }
        helper.succeed();
    }

    private static String keyOf(Component name) {
        return name.getContents() instanceof TranslatableContents translatable ? translatable.getKey() : name.getString();
    }

    private static JsonObject read(Resource resource) {
        try (Reader reader = resource.openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

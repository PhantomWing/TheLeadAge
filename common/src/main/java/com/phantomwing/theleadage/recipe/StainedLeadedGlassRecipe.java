package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassDyes;
import com.phantomwing.theleadage.block.custom.LeadedGlassRecolor;
import com.phantomwing.theleadage.block.custom.StainedLeadedGlassBlock;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Dyed leaded glass in a colour a dye mod adds, crafted as vanilla's colours are: eight leaded glass around
 * the dye, or eight of that colour's stained glass around a lead ingot, for eight. Vanilla's sixteen have a
 * recipe each in data; this reads the colour off the ingredients, so it covers any colour without one, and
 * leaves vanilla's to theirs.
 */
public class StainedLeadedGlassRecipe extends CustomRecipe {
    private static final int CENTRE = 4;
    private static final int OUTPUT_COUNT = 8;

    public StainedLeadedGlassRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return resultOf(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Block result = resultOf(input);
        return result == null ? ItemStack.EMPTY : new ItemStack(result, OUTPUT_COUNT);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.STAINED_LEADED_GLASS.get();
    }

    /** The leaded glass the grid makes, or {@code null} when it isn't one of the two shapes in a dye mod's colour. */
    @Nullable
    private static Block resultOf(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return null;
        }
        ItemStack centre = input.getItem(CENTRE);
        DyeColor dye = LeadedGlassRecolor.dyeOf(centre);
        if (dye != null) {
            return edgesAreLeadedGlass(input) ? leadedGlassOf(dye) : null;
        }
        if (!centre.is(ModItems.LEAD_INGOT.get())) {
            return null;
        }
        // The edges may mix two mods' stained glass of one colour name, since both make the same leaded glass.
        Block result = null;
        for (int i = 0; i < input.size(); i++) {
            if (i == CENTRE) {
                continue;
            }
            Block made = input.getItem(i).getItem() instanceof BlockItem item
                    && item.getBlock() instanceof StainedGlassBlock glass && !(glass instanceof StainedLeadedGlassBlock)
                    ? leadedGlassOf(glass.getColor()) : null;
            if (made == null || (result != null && made != result)) {
                return null;
            }
            result = made;
        }
        return result;
    }

    private static boolean edgesAreLeadedGlass(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            if (i != CENTRE && !input.getItem(i).is(ModItems.LEADED_GLASS.get())) {
                return false;
            }
        }
        return true;
    }

    /** A dye mod's colour's leaded glass, or {@code null} for vanilla's sixteen, whose recipes are data. */
    @Nullable
    private static Block leadedGlassOf(DyeColor color) {
        return LeadedGlassDyes.isVanilla(color) ? null : ModBlocks.STAINED_LEADED_GLASS.get(color).get();
    }
}

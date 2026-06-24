package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * The reverse of {@link LeadedGlassDoorRecipe} / {@link LeadedGlassTrapdoorRecipe}: a leaded glass
 * door/trapdoor on its own in the grid splits back into the plain lead door/trapdoor (the crafting
 * result) plus the leaded glass pane it was made with — that pane reappears in the grid carrying the
 * design's frame + colours, so the split is lossless.
 */
public abstract class LeadedGlassSplitRecipe extends CustomRecipe {
    protected LeadedGlassSplitRecipe(CraftingBookCategory category) {
        super(category);
    }

    /** The combined item to split (the leaded glass door / trapdoor). */
    protected abstract Item source();

    /** The plain base handed back as the crafting result (the lead door / trapdoor). */
    protected abstract Item base();

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findSource(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return findSource(input) != null ? new ItemStack(base()) : ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(source())) {
                remaining.set(i, paneFrom(stack)); // the pane reappears where the door/trapdoor was
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    /** The single source stack iff the grid holds exactly one of it and nothing else. */
    @Nullable
    private ItemStack findSource(CraftingInput input) {
        ItemStack found = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(source()) && found == null) {
                found = stack;
            } else {
                return null; // a second source, or any unrelated item
            }
        }
        return found;
    }

    /** The leaded glass pane the source carried — its frame's pane item, with the design copied over. */
    private static ItemStack paneFrom(ItemStack source) {
        LeadedGlassConfig config = source.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        LeadedGlassFrame frame = config != null ? config.frame() : LeadedGlassFrame.PLAIN;
        ItemStack pane = new ItemStack(ModItems.paneItemFor(frame));
        if (config != null) {
            pane.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), config);
        }
        return pane;
    }
}

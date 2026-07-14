package com.phantomwing.theleadage.compat;

import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The in-world leaded-glass interactions that aren't recipes at all (dyeing / shearing / rotating a
 * pane, re-glazing a door or trapdoor). Recipe viewers show these as information entries — JEI via
 * {@code addIngredientInfo}, EMI via {@code EmiInfoRecipe} — so the text and the stacks they attach
 * to live here once.
 */
public final class LeadedGlassInfo {
    private LeadedGlassInfo() {
    }

    /** One stack per pane item (every came pattern), so the pane info attaches to all of them. */
    public static List<ItemStack> paneStacks() {
        return List.of(
                new ItemStack(ModItems.LEADED_GLASS_PANEL.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_SPLIT.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_PLUS.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_GRID.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_DIAGONAL.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_CROSS.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_DIAMOND.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_LATTICE.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_BARS.get()),
                new ItemStack(ModItems.LEADED_GLASS_PANE_DIAGONAL_BARS.get()));
    }

    public static ItemStack doorStack() {
        return new ItemStack(ModItems.LEADED_GLASS_DOOR.get());
    }

    public static ItemStack trapdoorStack() {
        return new ItemStack(ModItems.LEADED_GLASS_TRAPDOOR.get());
    }

    /** The info text for a kind ({@code pane} / {@code door} / {@code trapdoor}). */
    public static Component text(String kind) {
        return Component.translatable("theleadage.info." + kind);
    }
}

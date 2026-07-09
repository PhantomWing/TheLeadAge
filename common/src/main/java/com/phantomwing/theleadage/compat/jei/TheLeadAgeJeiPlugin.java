package com.phantomwing.theleadage.compat.jei;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI integration. The mod's leaded-glass crafting recipes are code-matched (the grid arrangement
 * decides the pattern, and colours ride on data components), which JEI cannot introspect — so this
 * plugin hands JEI display-only recipe entries ({@link JeiLeadedGlassRecipe}) rendered by a
 * crafting-category extension with all colour variants cycling in sync, plus info entries for the
 * in-world interactions that aren't recipes at all (dyeing/shearing/rotating panes, swapping the
 * glass of doors and trapdoors).
 *
 * <p>Lives in common — the JEI plugin API is loader-agnostic, and this class is only classloaded
 * by JEI itself, so JEI remains an optional dependency.</p>
 */
@JeiPlugin
public class TheLeadAgeJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(JeiLeadedGlassRecipe.class, new JeiLeadedGlassExtension());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, JeiLeadedGlassRecipe.displayRecipes());

        // In-world interactions that aren't recipes: shown as JEI information entries.
        registration.addIngredientInfo(paneStacks(), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.theleadage.info.pane"));
        registration.addIngredientInfo(new ItemStack(ModItems.LEADED_GLASS_DOOR.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.theleadage.info.door"));
        registration.addIngredientInfo(new ItemStack(ModItems.LEADED_GLASS_TRAPDOOR.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.theleadage.info.trapdoor"));
    }

    /** One stack per pane item (every came pattern), so the info entry attaches to all of them. */
    private static List<ItemStack> paneStacks() {
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
}

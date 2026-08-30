package com.phantomwing.theleadage.compat.jei;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.LeadedGlassDisplayRecipe;
import com.phantomwing.theleadage.compat.LeadedGlassInfo;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.Identifier;

/**
 * JEI integration. The mod's leaded-glass crafting recipes are code-matched (the grid arrangement
 * decides the pattern, and colours ride on data components), which JEI cannot introspect — so this
 * plugin hands JEI the shared display arrangements ({@link LeadedGlassDisplayRecipe}) rendered by a
 * crafting-category extension with all colour variants cycling in sync, plus info entries for the
 * in-world interactions that aren't recipes at all (see {@link LeadedGlassInfo}).
 *
 * <p>Lives in common — the JEI plugin API is loader-agnostic, and this class is only classloaded
 * by JEI itself, so JEI remains an optional dependency. The EMI plugin is its counterpart.</p>
 */
@JeiPlugin
public class TheLeadAgeJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(TheLeadAge.MOD_ID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(LeadedGlassDisplayRecipe.class, new JeiLeadedGlassExtension());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, LeadedGlassDisplayRecipe.displayRecipes());

        // In-world interactions that aren't recipes: shown as JEI information entries.
        registration.addIngredientInfo(LeadedGlassInfo.paneStacks(), VanillaTypes.ITEM_STACK,
                LeadedGlassInfo.text("pane"));
        registration.addIngredientInfo(LeadedGlassInfo.doorStack(), VanillaTypes.ITEM_STACK,
                LeadedGlassInfo.text("door"));
        registration.addIngredientInfo(LeadedGlassInfo.trapdoorStack(), VanillaTypes.ITEM_STACK,
                LeadedGlassInfo.text("trapdoor"));
    }
}

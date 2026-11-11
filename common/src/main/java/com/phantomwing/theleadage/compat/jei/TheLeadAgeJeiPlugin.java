package com.phantomwing.theleadage.compat.jei;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.LeadInfo;
import com.phantomwing.theleadage.compat.LeadedGlassInfo;
import com.phantomwing.theleadage.compat.LeadedGlassSubtypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * JEI integration: info entries for the in-world interactions that aren't recipes at all ({@link LeadedGlassInfo},
 * {@link LeadInfo}), and each leaded glass design as a stack of its own.
 *
 * <p>The code-matched leaded-glass crafting arrangements ({@code LeadedGlassDisplayRecipe}) aren't shown on this line:
 * JEI 20.0.0.4, its only 1.21.4 build, lays a crafting extension out from its slot displays alone, with no grid
 * helper and no focus links, so a pane's colours would cycle out of step with the result's.</p>
 *
 * <p>Lives in common - the JEI plugin API is loader-agnostic, and this class is only classloaded by JEI itself, so JEI
 * remains an optional dependency. JEI has only a NeoForge build for 1.21.4, and EMI none.</p>
 */
@JeiPlugin
public class TheLeadAgeJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    /**
     * Each design its own stack in the ingredient list ({@link LeadedGlassSubtypes}). Looking up recipes stays by
     * item: what the stonecutter and the info entries take is plain glass.
     */
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        ISubtypeInterpreter<ItemStack> design = (stack, context) ->
                context == UidContext.Ingredient ? LeadedGlassSubtypes.designOf(stack) : null;
        for (Item item : LeadedGlassSubtypes.items()) {
            registration.registerSubtypeInterpreter(item, design);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // In-world interactions that aren't recipes: shown as JEI information entries.
        registration.addIngredientInfo(LeadedGlassInfo.paneStacks(), VanillaTypes.ITEM_STACK,
                LeadedGlassInfo.text("pane"));
        registration.addIngredientInfo(LeadedGlassInfo.doorStack(), VanillaTypes.ITEM_STACK,
                LeadedGlassInfo.text("door"));
        registration.addIngredientInfo(LeadedGlassInfo.trapdoorStack(), VanillaTypes.ITEM_STACK,
                LeadedGlassInfo.text("trapdoor"));
        for (LeadInfo page : LeadInfo.all()) {
            registration.addIngredientInfo(page.stacks(), VanillaTypes.ITEM_STACK, page.text());
        }
    }
}

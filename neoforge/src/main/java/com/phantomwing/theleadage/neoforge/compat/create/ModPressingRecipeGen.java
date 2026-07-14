package com.phantomwing.theleadage.neoforge.compat.create;

import java.util.concurrent.CompletableFuture;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.tags.CommonTags;
import com.simibubi.create.api.data.recipe.PressingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

/**
 * Create "pressing" recipes: a lead ingot in a Mechanical Press yields a Lead Sheet, giving lead
 * parity with Create's copper / iron / gold / brass sheets. Create only ships a lead plate recipe
 * for Immersive Engineering, so without this there is no generic lead sheet.
 *
 * <p>The input is the {@code c:ingots/lead} common tag, so ingots contributed by other lead mods
 * press too — the same pattern Create itself uses for its metals.</p>
 */
public class ModPressingRecipeGen extends PressingRecipeGen {

    @SuppressWarnings("unused")
    GeneratedRecipe LEAD_SHEET = create("lead_sheet", b -> b
            .require(CommonTags.Items.INGOTS_LEAD)
            .output(ModItems.LEAD_SHEET.get()));

    public ModPressingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, TheLeadAge.MOD_ID);
    }

    /**
     * Gate every recipe on Create being loaded, so without Create the game silently skips them
     * instead of erroring on the unknown {@code create:pressing} serializer.
     */
    @Override
    public void buildRecipes(RecipeOutput recipeOutput) {
        super.buildRecipes(recipeOutput.withConditions(new ModLoadedCondition(ModIds.CREATE)));
    }
}

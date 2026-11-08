package com.phantomwing.theleadage.forge.compat.create;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.forge.datagen.RecipeOutput;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.tags.CommonTags;
import com.simibubi.create.api.data.recipe.PressingRecipeGen;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

import java.util.function.Consumer;

/**
 * Create "pressing" recipes: a lead ingot in a Mechanical Press yields a Lead Sheet, giving lead
 * parity with Create's copper / iron / gold / brass sheets. Create only ships a lead plate recipe
 * for Immersive Engineering, so without this there is no generic lead sheet.
 *
 * <p>The input is the {@code forge:ingots/lead} tag, so ingots contributed by other lead mods
 * press too — the same pattern Create itself uses for its metals.</p>
 */
public class ModPressingRecipeGen extends PressingRecipeGen {

    @SuppressWarnings("unused")
    GeneratedRecipe LEAD_SHEET = create("lead_sheet", b -> b
            .require(CommonTags.Items.INGOTS_LEAD)
            .output(ModItems.LEAD_SHEET.get()));

    public ModPressingRecipeGen(PackOutput output) {
        super(output, TheLeadAge.MOD_ID);
    }

    /**
     * Gate every recipe on Create being loaded, so without Create the game silently skips them
     * instead of erroring on the unknown {@code create:pressing} serializer.
     */
    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        super.buildRecipes(RecipeOutput.of(consumer).withConditions(new ModLoadedCondition(ModIds.CREATE)));
    }
}

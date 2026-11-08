package com.phantomwing.theleadage.forge.compat.create;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.forge.datagen.RecipeOutput;
import com.phantomwing.theleadage.item.ModItems;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.WashingRecipeGen;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

import java.util.function.Consumer;

/**
 * Create "splashing" (washing) recipes: bathing Create's {@code crushed_raw_lead} in a fan-blown
 * water stream yields 9 lead nuggets. Create ships this bridge only for Immersive Engineering /
 * Mekanism / Oreganized / Thermal — each hard-wired to that mod's own nugget — so lead from this
 * mod needs its own. The yield mirrors those recipes exactly.
 *
 * <p>Note the crushed lead itself needs no recipe from us: Create's crushing recipes are gated on
 * the {@code forge:ores/lead}, {@code forge:raw_materials/lead} and {@code forge:storage_blocks/raw_lead} tags being
 * non-empty, which this mod populates, so its ores and raw lead crush into {@code create:crushed_raw_lead} for free.</p>
 */
public class ModWashingRecipeGen extends WashingRecipeGen {

    @SuppressWarnings("unused")
    GeneratedRecipe CRUSHED_RAW_LEAD = create("crushed_raw_lead", b -> b
            .require(AllItems.CRUSHED_LEAD.get())
            .output(ModItems.LEAD_NUGGET.get(), 9));

    public ModWashingRecipeGen(PackOutput output) {
        super(output, TheLeadAge.MOD_ID);
    }

    /**
     * Gate every recipe on Create being loaded, so without Create the game silently skips them
     * instead of erroring on the unknown {@code create:splashing} serializer.
     */
    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        super.buildRecipes(RecipeOutput.of(consumer).withConditions(new ModLoadedCondition(ModIds.CREATE)));
    }
}

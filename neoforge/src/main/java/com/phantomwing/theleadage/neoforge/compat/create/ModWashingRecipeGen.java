package com.phantomwing.theleadage.neoforge.compat.create;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.item.ModItems;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.WashingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.OrCondition;

/**
 * Create "splashing" (washing) recipes: bathing Create's {@code crushed_raw_lead} in a fan-blown
 * water stream yields 9 lead nuggets. Create ships this bridge only for Immersive Engineering /
 * Mekanism / Oreganized / Thermal — each hard-wired to that mod's own nugget — so lead from this
 * mod needs its own. The yield mirrors those recipes exactly.
 *
 * <p>Note the crushed lead itself needs no recipe from us: Create's crushing recipes are gated on
 * the {@code c:ores/lead} / {@code c:raw_materials/lead} tags being non-empty, which this mod
 * already populates, so its ores and raw lead crush into {@code create:crushed_raw_lead} for free.</p>
 */
public class ModWashingRecipeGen extends WashingRecipeGen {

    @SuppressWarnings("unused")
    GeneratedRecipe CRUSHED_RAW_LEAD = create("crushed_raw_lead", b -> b
            .require(AllItems.CRUSHED_LEAD.get())
            .output(ModItems.LEAD_NUGGET.get(), 9));

    public ModWashingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, TheLeadAge.MOD_ID);
    }

    /**
     * None of the mods Create 6.0.9 smelts, blasts and washes its crushed raw lead into the lead of itself is loaded.
     * Where one is, this mod's recipes for it step aside: a furnace or fan with two recipes for one input gives
     * whichever the recipe map lists first, which changes from pack to pack.
     */
    public static ICondition withoutCreatesOwnLead() {
        return new NotCondition(new OrCondition(List.of(new ModLoadedCondition(ModIds.MEKANISM),
                new ModLoadedCondition(ModIds.THERMAL), new ModLoadedCondition(ModIds.OREGANIZED),
                new ModLoadedCondition(ModIds.IMMERSIVE_ENGINEERING))));
    }

    /**
     * Gate every recipe on Create being loaded, so without Create the game silently skips them
     * instead of erroring on the unknown {@code create:splashing} serializer, and on Create having no washing of its
     * own for the crushed lead ({@link #withoutCreatesOwnLead()}).
     */
    @Override
    public void buildRecipes(RecipeOutput recipeOutput) {
        super.buildRecipes(recipeOutput.withConditions(new ModLoadedCondition(ModIds.CREATE), withoutCreatesOwnLead()));
    }
}

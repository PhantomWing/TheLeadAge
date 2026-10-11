package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassRecolor;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Eight clear plain leaded glass panes around a dye make eight panes of the dye's colour, as vanilla's eight
 * glass panes around a dye do. Code-matched because the panes' colour is a component; it takes any dye, a dye
 * mod's too.
 */
public class StainedLeadedGlassPaneRecipe extends CustomRecipe {
    private static final int CENTRE = 4;
    private static final int OUTPUT_COUNT = 8;

    public StainedLeadedGlassPaneRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return dyeOf(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        DyeColor dye = dyeOf(input);
        if (dye == null) {
            return ItemStack.EMPTY;
        }
        ItemStack panes = new ItemStack(ModItems.LEADED_GLASS_PANEL.get(), OUTPUT_COUNT);
        panes.set(ModDataComponents.LEADED_GLASS_CONFIG.get(),
                new LeadedGlassConfig(LeadedGlassFrame.PLAIN, List.of(dye.getId())));
        return panes;
    }

    @Override
    public RecipeSerializer<StainedLeadedGlassPaneRecipe> getSerializer() {
        return ModRecipes.STAINED_LEADED_GLASS_PANE.get();
    }

    /** The dye in the centre of eight clear plain panes, or {@code null} when the grid isn't that. */
    @Nullable
    private static DyeColor dyeOf(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return null;
        }
        for (int i = 0; i < input.size(); i++) {
            if (i == CENTRE) {
                continue;
            }
            Integer color = LeadedGlassColors.plainPaneColorIdOf(input.getItem(i));
            if (color == null || color != LeadedGlassConfig.CLEAR) {
                return null; // vanilla dyes clear glass panes only
            }
        }
        return LeadedGlassRecolor.dyeOf(input.getItem(CENTRE));
    }
}

package com.phantomwing.theleadage.compat.emi;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.compat.LeadedGlassDisplayRecipe;
import com.phantomwing.theleadage.compat.LeadedGlassInfo;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * EMI integration. Mirrors the JEI plugin: the leaded-glass crafting recipes are code-matched (the
 * grid arrangement picks the came pattern, colours ride on data components), which no viewer can
 * introspect, so the shared {@link LeadedGlassDisplayRecipe} arrangements are handed to EMI here.
 *
 * <p>Unlike JEI, EMI has no focus-link: an {@code EmiCraftingRecipe}'s output is a single stack, so
 * a cycling output can't stay in sync with cycling inputs. Instead each arrangement is emitted once
 * <em>per colour</em> (17 variants), which is both exact and individually searchable — looking up a
 * red grid pane finds precisely the recipe that makes it.</p>
 *
 * <p>Lives in common — EMI's plugin API is loader-agnostic, and this class is only classloaded by
 * EMI's own {@link EmiEntrypoint} scan, so EMI stays an optional dependency on both loaders.</p>
 */
@EmiEntrypoint
public class TheLeadAgeEmiPlugin implements EmiPlugin {
    /** EMI lays an input list out row-major across the 3-wide crafting grid. */
    private static final int GRID_WIDTH = 3;
    private static final int GRID_SIZE = 9;

    @Override
    public void register(@NotNull EmiRegistry registry) {
        for (LeadedGlassDisplayRecipe recipe : LeadedGlassDisplayRecipe.all()) {
            addArrangement(registry, recipe);
        }

        addWorldInteractions(registry);

        // The interactions still need prose (which region a click hits, that a turn reads clockwise
        // from either side, ...), so keep the information entries alongside the world recipes.
        addInfo(registry, "pane", LeadedGlassInfo.paneStacks());
        addInfo(registry, "door", List.of(LeadedGlassInfo.doorStack()));
        addInfo(registry, "trapdoor", List.of(LeadedGlassInfo.trapdoorStack()));
    }

    /**
     * The in-world interactions, as EMI "world interaction" recipes: left slot = the placed block,
     * right slot = what you're holding (a catalyst is not consumed), output = what you end up with.
     *
     * <p>Dyeing and shearing act on <em>one region</em> of a pane, so they're shown on the plain
     * (single-region) pane, where that region <em>is</em> the pane — for the multi-region patterns
     * the same click colours just the region you hit, which the info entry explains.</p>
     */
    private static void addWorldInteractions(EmiRegistry registry) {
        int clear = LeadedGlassConfig.CLEAR;

        // Dye a glass region — the dye is consumed.
        for (DyeColor dye : DyeColor.values()) {
            addRecipe(registry, EmiWorldInteractionRecipe.builder()
                    .id(LeadedGlassDisplayRecipe.id("world/dye/" + dye.getName()))
                    .leftInput(EmiStack.of(LeadedGlassDisplayRecipe.plainPane(clear)))
                    .rightInput(EmiStack.of(DyeItem.byColor(dye)), false)
                    .output(EmiStack.of(LeadedGlassDisplayRecipe.plainPane(dye.getId())))
                    .build());
        }

        // Shear a region back to clear — the shears only take durability, so they're a catalyst.
        List<EmiIngredient> dyedPanes = new ArrayList<>();
        for (DyeColor dye : DyeColor.values()) {
            dyedPanes.add(EmiStack.of(LeadedGlassDisplayRecipe.plainPane(dye.getId())));
        }
        addRecipe(registry, EmiWorldInteractionRecipe.builder()
                .id(LeadedGlassDisplayRecipe.id("world/shear"))
                .leftInput(EmiIngredient.of(dyedPanes))
                .rightInput(EmiStack.of(Items.SHEARS), true)
                .output(EmiStack.of(LeadedGlassDisplayRecipe.plainPane(clear)))
                .build());

        // Sneak-right-click with an empty hand turns the came a quarter turn (hence no right input).
        // Only the orientable patterns change shape; the symmetric ones just cycle their colours,
        // which would render as an identical before/after here — the info entry covers those.
        for (LeadedGlassPaneBlock.CameType type : LeadedGlassPaneBlock.CameType.values()) {
            if (type.orientation == null) {
                continue;
            }
            for (int i = 0; i < type.orientations; i++) {
                LeadedGlassFrame from = type.frame(i);
                LeadedGlassFrame to = type.frame(i + 1); // frame() wraps, so this closes the cycle
                addRecipe(registry, EmiWorldInteractionRecipe.builder()
                        .id(LeadedGlassDisplayRecipe.id("world/rotate/" + frameName(from)))
                        .leftInput(EmiStack.of(LeadedGlassDisplayRecipe.pane(from, clear, 1)))
                        .output(EmiStack.of(LeadedGlassDisplayRecipe.pane(to, clear, 1)))
                        .build());
            }
        }

        // Re-glazing: sneak-right-click a placed door/trapdoor with a pane in hand. The held pane
        // goes in and the old one is handed back — hence the second output.
        for (DyeColor dye : DyeColor.values()) {
            int color = dye.getId();
            addRecipe(registry, EmiWorldInteractionRecipe.builder()
                    .id(LeadedGlassDisplayRecipe.id("world/reglaze_trapdoor/" + dye.getName()))
                    .leftInput(EmiStack.of(LeadedGlassDisplayRecipe.trapdoor(clear)))
                    .rightInput(EmiStack.of(LeadedGlassDisplayRecipe.plainPane(color)), false)
                    .output(EmiStack.of(LeadedGlassDisplayRecipe.trapdoor(color)))
                    .output(EmiStack.of(LeadedGlassDisplayRecipe.plainPane(clear)))
                    .build());
            // A door has two halves; only the half you clicked is re-glazed (shown here as the top).
            addRecipe(registry, EmiWorldInteractionRecipe.builder()
                    .id(LeadedGlassDisplayRecipe.id("world/reglaze_door/" + dye.getName()))
                    .leftInput(EmiStack.of(LeadedGlassDisplayRecipe.door(clear, clear)))
                    .rightInput(EmiStack.of(LeadedGlassDisplayRecipe.plainPane(color)), false)
                    .output(EmiStack.of(LeadedGlassDisplayRecipe.door(color, clear)))
                    .output(EmiStack.of(LeadedGlassDisplayRecipe.plainPane(clear)))
                    .build());
        }
    }

    private static String frameName(LeadedGlassFrame frame) {
        return frame.name().toLowerCase(Locale.ROOT);
    }

    /** One EMI recipe per colour variant of an arrangement. */
    private static void addArrangement(EmiRegistry registry, LeadedGlassDisplayRecipe recipe) {
        List<List<ItemStack>> inputs = recipe.inputVariants();
        List<ItemStack> results = recipe.resultVariants();

        for (int color = 0; color < results.size(); color++) {
            // Pad to the full 3x3: the arrangement sits top-left, gaps stay empty.
            List<EmiIngredient> grid = new ArrayList<>(Collections.nCopies(GRID_SIZE, EmiStack.EMPTY));
            for (int cell = 0; cell < inputs.size(); cell++) {
                List<ItemStack> variants = inputs.get(cell);
                if (variants == null || variants.isEmpty()) {
                    continue; // an empty cell of the pattern
                }
                // Cycling cells have one stack per colour; constants (ingot/door/trapdoor) have one.
                ItemStack stack = recipe.cyclesAt(cell) ? variants.get(color) : variants.get(0);
                int row = cell / recipe.gridWidth();
                int col = cell % recipe.gridWidth();
                grid.set(row * GRID_WIDTH + col, EmiStack.of(stack));
            }
            addRecipe(registry, new EmiCraftingRecipe(grid, EmiStack.of(results.get(color)),
                    LeadedGlassDisplayRecipe.id(recipe.displayName() + "/" + color)));
        }
    }

    private static void addInfo(EmiRegistry registry, String kind, List<ItemStack> stacks) {
        List<EmiIngredient> ingredients = stacks.stream().map(s -> (EmiIngredient) EmiStack.of(s)).toList();
        addRecipe(registry, new EmiInfoRecipe(ingredients, List.of(LeadedGlassInfo.text(kind)),
                LeadedGlassDisplayRecipe.id("info/" + kind)));
    }

    /** Defensive: a single bad entry must not take down the whole plugin (mirrors EMI's addRecipeSafe). */
    private static void addRecipe(EmiRegistry registry, EmiRecipe recipe) {
        try {
            registry.addRecipe(recipe);
        } catch (Throwable t) {
            TheLeadAge.LOGGER.warn("Failed to register EMI recipe {}", recipe.getId(), t);
        }
    }
}

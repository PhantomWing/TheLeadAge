package com.phantomwing.theleadage.compat.jei;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.recipe.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A display-only recipe for JEI: one instance per leaded-glass crafting arrangement, never added
 * to the {@code RecipeManager} (it {@link #matches never matches}), only handed to JEI so the
 * code-matched recipes become browsable. Each instance carries a small grid of {@link Cell cells}
 * plus what it produces; {@code JeiLeadedGlassExtension} renders it with all 17 colour variants
 * (clear + every dye) cycling in lockstep across the inputs and the result.
 */
public class JeiLeadedGlassRecipe extends CustomRecipe {
    /** What sits in one crafting-grid cell of the displayed arrangement. */
    public enum Cell {
        EMPTY, PLAIN_PANE, LEADED_GLASS_BLOCK, GLASS_PANE, LEAD_INGOT, LEAD_DOOR, LEAD_TRAPDOOR;

        /** Whether this cell cycles with the colour variants (constant cells show one stack). */
        boolean cycles() {
            return this == PLAIN_PANE || this == LEADED_GLASS_BLOCK || this == GLASS_PANE;
        }
    }

    /** What the arrangement produces (the pane's frame rides alongside for PANE results). */
    public enum ResultKind { PANE, DOOR, TRAPDOOR }

    /** The displayed colour variants, in creative-tab order: clear first, then every dye. */
    private static final List<Integer> COLORS = buildColors();

    private final int width;
    private final int height;
    private final List<Cell> cells;
    private final ResultKind resultKind;
    @Nullable
    private final LeadedGlassFrame frame;
    private final int resultCount;

    private JeiLeadedGlassRecipe(int width, int height, List<Cell> cells,
                                 ResultKind resultKind, @Nullable LeadedGlassFrame frame, int resultCount) {
        super(CraftingBookCategory.MISC);
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.resultKind = resultKind;
        this.frame = frame;
        this.resultCount = resultCount;
    }

    public int gridWidth() {
        return width;
    }

    public int gridHeight() {
        return height;
    }

    /** Whether the grid cell at {@code index} (row-major) cycles with the colour variants. */
    public boolean cyclesAt(int index) {
        return cells.get(index).cycles();
    }

    /** Per grid cell: the stacks it shows (17 for cycling cells, 1 for constants, null for empty). */
    public List<@Nullable List<ItemStack>> inputVariants() {
        List<List<ItemStack>> inputs = new ArrayList<>(cells.size());
        for (Cell cell : cells) {
            inputs.add(switch (cell) {
                case EMPTY -> null;
                case LEAD_INGOT -> List.of(new ItemStack(ModItems.LEAD_INGOT.get()));
                case LEAD_DOOR -> List.of(new ItemStack(ModItems.LEAD_DOOR.get()));
                case LEAD_TRAPDOOR -> List.of(new ItemStack(ModItems.LEAD_TRAPDOOR.get()));
                case PLAIN_PANE -> COLORS.stream().map(JeiLeadedGlassRecipe::plainPane).toList();
                case LEADED_GLASS_BLOCK -> COLORS.stream().map(JeiLeadedGlassRecipe::leadedGlassBlock).toList();
                case GLASS_PANE -> COLORS.stream().map(JeiLeadedGlassRecipe::vanillaGlassPane).toList();
            });
        }
        return inputs;
    }

    /** The result stack per colour variant (index-aligned with the cycling inputs). */
    public List<ItemStack> resultVariants() {
        return COLORS.stream().map(this::result).toList();
    }

    private ItemStack result(int color) {
        switch (resultKind) {
            case DOOR -> {
                ItemStack door = new ItemStack(ModItems.LEADED_GLASS_DOOR.get());
                LeadedGlassConfig pane = plainConfig(color);
                door.set(ModDataComponents.LEADED_GLASS_DOOR_CONFIG.get(), new LeadedGlassDoorConfig(pane, pane));
                return door;
            }
            case TRAPDOOR -> {
                ItemStack trapdoor = new ItemStack(ModItems.LEADED_GLASS_TRAPDOOR.get());
                trapdoor.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), plainConfig(color));
                return trapdoor;
            }
            default -> {
                LeadedGlassFrame resultFrame = this.frame != null ? this.frame : LeadedGlassFrame.PLAIN;
                ItemStack pane = new ItemStack(ModItems.paneItemFor(resultFrame), resultCount);
                pane.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), new LeadedGlassConfig(resultFrame,
                        Collections.nCopies(resultFrame.regions(), color)));
                return pane;
            }
        }
    }

    // ---- The displayed arrangements ----

    /** Every display entry, in browse order. */
    public static List<RecipeHolder<CraftingRecipe>> displayRecipes() {
        List<RecipeHolder<CraftingRecipe>> holders = new ArrayList<>();
        // Pane cutting: a full 3x2 of leaded glass blocks -> 16 plain panes of that colour.
        holders.add(holder("pane_cut", new JeiLeadedGlassRecipe(3, 2,
                Collections.nCopies(6, Cell.LEADED_GLASS_BLOCK), ResultKind.PANE, LeadedGlassFrame.PLAIN, 16)));
        // Glass panes around a lead ingot -> 8 plain leaded panes of that colour.
        holders.add(holder("glass_pane_craft", new JeiLeadedGlassRecipe(3, 3, List.of(
                Cell.GLASS_PANE, Cell.GLASS_PANE, Cell.GLASS_PANE,
                Cell.GLASS_PANE, Cell.LEAD_INGOT, Cell.GLASS_PANE,
                Cell.GLASS_PANE, Cell.GLASS_PANE, Cell.GLASS_PANE), ResultKind.PANE, LeadedGlassFrame.PLAIN, 8)));
        // Combine arrangements: plain panes in a shape -> that came pattern (colours carry per region).
        holders.add(arrangement("split_h", LeadedGlassFrame.SPLIT_H, 2, 2, 1, "PP"));
        holders.add(arrangement("split_v", LeadedGlassFrame.SPLIT_V, 2, 1, 2, "P", "P"));
        holders.add(arrangement("plus", LeadedGlassFrame.PLUS, 4, 2, 2, "PP", "PP"));
        holders.add(arrangement("grid", LeadedGlassFrame.GRID, 9, 3, 3, "PPP", "PPP", "PPP"));
        holders.add(arrangement("diagonal_a", LeadedGlassFrame.DIAGONAL_A, 2, 2, 2, "P.", ".P"));
        holders.add(arrangement("diagonal_b", LeadedGlassFrame.DIAGONAL_B, 2, 2, 2, ".P", "P."));
        holders.add(arrangement("cross", LeadedGlassFrame.CROSS, 4, 3, 3, ".P.", "P.P", ".P."));
        holders.add(arrangement("diamond", LeadedGlassFrame.DIAMOND, 5, 3, 3, "P.P", ".P.", "P.P"));
        holders.add(arrangement("bars_h", LeadedGlassFrame.BARS_H, 3, 3, 1, "PPP"));
        holders.add(arrangement("bars_v", LeadedGlassFrame.BARS_V, 3, 1, 3, "P", "P", "P"));
        // Door: a lead door + two panes (the first pane becomes the top half).
        holders.add(holder("door", new JeiLeadedGlassRecipe(1, 3,
                List.of(Cell.PLAIN_PANE, Cell.PLAIN_PANE, Cell.LEAD_DOOR), ResultKind.DOOR, null, 1)));
        // Trapdoor: a lead trapdoor + one pane.
        holders.add(holder("trapdoor", new JeiLeadedGlassRecipe(1, 2,
                List.of(Cell.PLAIN_PANE, Cell.LEAD_TRAPDOOR), ResultKind.TRAPDOOR, null, 1)));
        return holders;
    }

    /** A pane arrangement from a mini pattern ('P' = plain pane, '.' = empty), row per string. */
    private static RecipeHolder<CraftingRecipe> arrangement(String name, LeadedGlassFrame frame, int count,
                                                            int width, int height, String... rows) {
        List<Cell> cells = new ArrayList<>(width * height);
        for (String row : rows) {
            for (int x = 0; x < width; x++) {
                cells.add(row.charAt(x) == 'P' ? Cell.PLAIN_PANE : Cell.EMPTY);
            }
        }
        return holder(name, new JeiLeadedGlassRecipe(width, height, cells, ResultKind.PANE, frame, count));
    }

    private static RecipeHolder<CraftingRecipe> holder(String name, JeiLeadedGlassRecipe recipe) {
        return new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "jei/" + name), recipe);
    }

    // ---- Stack builders ----

    private static LeadedGlassConfig plainConfig(int color) {
        return new LeadedGlassConfig(LeadedGlassFrame.PLAIN, List.of(color));
    }

    private static ItemStack plainPane(int color) {
        ItemStack pane = new ItemStack(ModItems.LEADED_GLASS_PANEL.get());
        pane.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), plainConfig(color));
        return pane;
    }

    private static ItemStack leadedGlassBlock(int color) {
        return new ItemStack(color < 0
                ? ModBlocks.LEADED_GLASS.get()
                : ModBlocks.STAINED_LEADED_GLASS.get(DyeColor.byId(color)).get());
    }

    private static ItemStack vanillaGlassPane(int color) {
        return new ItemStack(color < 0
                ? BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("glass_pane"))
                : BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(
                        DyeColor.byId(color).getName() + "_stained_glass_pane")));
    }

    private static List<Integer> buildColors() {
        List<Integer> colors = new ArrayList<>();
        colors.add(LeadedGlassConfig.CLEAR);
        for (DyeColor dye : DyeColor.values()) {
            colors.add(dye.getId());
        }
        return List.copyOf(colors);
    }

    // ---- Never a real recipe: display only ----

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int gridWidth, int gridHeight) {
        return gridWidth >= width && gridHeight >= height;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        // Never serialized (these holders are handed straight to JEI); any serializer satisfies the API.
        return ModRecipes.LEADED_GLASS_COMBINE.get();
    }
}

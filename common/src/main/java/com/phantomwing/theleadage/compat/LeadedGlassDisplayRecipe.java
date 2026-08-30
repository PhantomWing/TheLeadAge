package com.phantomwing.theleadage.compat;

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
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
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
 * A display-only description of the leaded-glass crafting arrangements, shared by every recipe
 * viewer (see {@code compat.jei} and {@code compat.emi}). The real recipes are code-matched — the
 * grid arrangement decides the came pattern, and colours ride on data components — which no viewer
 * can introspect, so each arrangement is described here once and handed to the viewers.
 *
 * <p>It extends {@link CustomRecipe} only so JEI can take it as a {@code RecipeHolder<CraftingRecipe>};
 * it is never added to the {@code RecipeManager} and {@link #matches never matches}. Each instance
 * carries a small grid of {@link Cell cells} plus what it produces, in all 17 colour variants
 * (clear + every dye) — index-aligned across {@link #inputVariants()} and {@link #resultVariants()}.</p>
 */
public class LeadedGlassDisplayRecipe extends CustomRecipe {
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

    private final String name;
    private final int width;
    private final int height;
    private final List<Cell> cells;
    private final ResultKind resultKind;
    @Nullable
    private final LeadedGlassFrame frame;
    private final int resultCount;

    private LeadedGlassDisplayRecipe(String name, int width, int height, List<Cell> cells,
                                     ResultKind resultKind, @Nullable LeadedGlassFrame frame, int resultCount) {
        super(CraftingBookCategory.MISC);
        this.name = name;
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.resultKind = resultKind;
        this.frame = frame;
        this.resultCount = resultCount;
    }

    /** This arrangement's id path (unique among the display recipes). */
    public String displayName() {
        return name;
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

    /** How many colour variants each cycling cell / the result has (clear + every dye). */
    public static int colorCount() {
        return COLORS.size();
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
                case PLAIN_PANE -> COLORS.stream().map(LeadedGlassDisplayRecipe::plainPane).toList();
                case LEADED_GLASS_BLOCK -> COLORS.stream().map(LeadedGlassDisplayRecipe::leadedGlassBlock).toList();
                case GLASS_PANE -> COLORS.stream().map(LeadedGlassDisplayRecipe::vanillaGlassPane).toList();
            });
        }
        return inputs;
    }

    /** The result stack per colour variant (index-aligned with the cycling inputs). */
    public List<ItemStack> resultVariants() {
        return COLORS.stream().map(this::result).toList();
    }

    private ItemStack result(int color) {
        return switch (resultKind) {
            case DOOR -> door(color, color);
            case TRAPDOOR -> trapdoor(color);
            default -> pane(this.frame != null ? this.frame : LeadedGlassFrame.PLAIN, color, resultCount);
        };
    }

    // ---- Shared stack builders (also used by the EMI world-interaction entries) ----

    /** A pane of {@code frame} with every region set to {@code color} ({@link LeadedGlassConfig#CLEAR} = clear). */
    public static ItemStack pane(LeadedGlassFrame frame, int color, int count) {
        ItemStack pane = new ItemStack(ModItems.paneItemFor(frame), count);
        pane.set(ModDataComponents.LEADED_GLASS_CONFIG.get(),
                new LeadedGlassConfig(frame, Collections.nCopies(frame.regions(), color)));
        return pane;
    }

    /** A leaded glass trapdoor whose flap is glazed in {@code color}. */
    public static ItemStack trapdoor(int color) {
        ItemStack trapdoor = new ItemStack(ModItems.LEADED_GLASS_TRAPDOOR.get());
        trapdoor.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), plainConfig(color));
        return trapdoor;
    }

    /** A leaded glass door, each half glazed in its own colour. */
    public static ItemStack door(int topColor, int bottomColor) {
        ItemStack door = new ItemStack(ModItems.LEADED_GLASS_DOOR.get());
        door.set(ModDataComponents.LEADED_GLASS_DOOR_CONFIG.get(),
                new LeadedGlassDoorConfig(plainConfig(topColor), plainConfig(bottomColor)));
        return door;
    }

    // ---- The displayed arrangements ----

    /** Every arrangement, in browse order. Viewer-agnostic. */
    public static List<LeadedGlassDisplayRecipe> all() {
        List<LeadedGlassDisplayRecipe> recipes = new ArrayList<>();
        // Pane cutting: a full 3x2 of leaded glass blocks -> 16 plain panes of that colour.
        recipes.add(new LeadedGlassDisplayRecipe("pane_cut", 3, 2,
                Collections.nCopies(6, Cell.LEADED_GLASS_BLOCK), ResultKind.PANE, LeadedGlassFrame.PLAIN, 16));
        // Glass panes around a lead ingot -> 8 plain leaded panes of that colour.
        recipes.add(new LeadedGlassDisplayRecipe("glass_pane_craft", 3, 3, List.of(
                Cell.GLASS_PANE, Cell.GLASS_PANE, Cell.GLASS_PANE,
                Cell.GLASS_PANE, Cell.LEAD_INGOT, Cell.GLASS_PANE,
                Cell.GLASS_PANE, Cell.GLASS_PANE, Cell.GLASS_PANE), ResultKind.PANE, LeadedGlassFrame.PLAIN, 8));
        // Combine arrangements: plain panes in a shape -> that came pattern (colours carry per region).
        recipes.add(arrangement("split_h", LeadedGlassFrame.SPLIT_H, 2, 2, 1, "PP"));
        recipes.add(arrangement("split_v", LeadedGlassFrame.SPLIT_V, 2, 1, 2, "P", "P"));
        recipes.add(arrangement("plus", LeadedGlassFrame.PLUS, 4, 2, 2, "PP", "PP"));
        recipes.add(arrangement("grid", LeadedGlassFrame.GRID, 9, 3, 3, "PPP", "PPP", "PPP"));
        recipes.add(arrangement("diagonal_a", LeadedGlassFrame.DIAGONAL_A, 2, 2, 2, "P.", ".P"));
        recipes.add(arrangement("diagonal_b", LeadedGlassFrame.DIAGONAL_B, 2, 2, 2, ".P", "P."));
        recipes.add(arrangement("cross", LeadedGlassFrame.CROSS, 4, 3, 3, ".P.", "P.P", ".P."));
        recipes.add(arrangement("diamond", LeadedGlassFrame.DIAMOND, 5, 3, 3, "P.P", ".P.", "P.P"));
        recipes.add(arrangement("bars_h", LeadedGlassFrame.BARS_H, 3, 3, 1, "PPP"));
        recipes.add(arrangement("bars_v", LeadedGlassFrame.BARS_V, 3, 1, 3, "P", "P", "P"));
        // Door: a lead door + two panes (the first pane becomes the top half).
        recipes.add(new LeadedGlassDisplayRecipe("door", 1, 3,
                List.of(Cell.PLAIN_PANE, Cell.PLAIN_PANE, Cell.LEAD_DOOR), ResultKind.DOOR, null, 1));
        // Trapdoor: a lead trapdoor + one pane.
        recipes.add(new LeadedGlassDisplayRecipe("trapdoor", 1, 2,
                List.of(Cell.PLAIN_PANE, Cell.LEAD_TRAPDOOR), ResultKind.TRAPDOOR, null, 1));
        return recipes;
    }

    /** The same arrangements wrapped as recipe holders (what JEI's crafting category takes). */
    public static List<RecipeHolder<CraftingRecipe>> displayRecipes() {
        List<RecipeHolder<CraftingRecipe>> holders = new ArrayList<>();
        for (LeadedGlassDisplayRecipe recipe : all()) {
            holders.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id(recipe.name)), recipe));
        }
        return holders;
    }

    /** A synthetic id for one arrangement — these are display entries, never real recipes. */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TheLeadAge.MOD_ID, "leaded_glass/" + path);
    }

    /** A pane arrangement from a mini pattern ('P' = plain pane, '.' = empty), row per string. */
    private static LeadedGlassDisplayRecipe arrangement(String name, LeadedGlassFrame frame, int count,
                                                        int width, int height, String... rows) {
        List<Cell> cells = new ArrayList<>(width * height);
        for (String row : rows) {
            for (int x = 0; x < width; x++) {
                cells.add(row.charAt(x) == 'P' ? Cell.PLAIN_PANE : Cell.EMPTY);
            }
        }
        return new LeadedGlassDisplayRecipe(name, width, height, cells, ResultKind.PANE, frame, count);
    }

    // ---- Stack builders ----

    private static LeadedGlassConfig plainConfig(int color) {
        return new LeadedGlassConfig(LeadedGlassFrame.PLAIN, List.of(color));
    }

    /** The single-region plain pane in {@code color} — the unit the came patterns are built from. */
    public static ItemStack plainPane(int color) {
        return pane(LeadedGlassFrame.PLAIN, color, 1);
    }

    private static ItemStack leadedGlassBlock(int color) {
        return new ItemStack(color < 0
                ? ModBlocks.LEADED_GLASS.get()
                : ModBlocks.STAINED_LEADED_GLASS.get(DyeColor.byId(color)).get());
    }

    private static ItemStack vanillaGlassPane(int color) {
        return new ItemStack(color < 0
                ? BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("glass_pane"))
                : BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(
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
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        // Never serialized (these are handed straight to the viewers); any serializer satisfies the API.
        return ModRecipes.LEADED_GLASS_COMBINE.get();
    }
}

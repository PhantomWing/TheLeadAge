package com.phantomwing.theleadage.compat;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassDyes;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.recipe.LeadedGlassColors;
import com.phantomwing.theleadage.recipe.ModRecipes;
import com.phantomwing.theleadage.tags.CommonTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
 * carries a small grid of {@link Cell cells} plus what it produces, in each of its colours (clear and
 * every dye for the panes, every dye for the panes dyed in the grid, a dye mod's colours for the blocks only it has
 * a recipe for) — index-aligned
 * across {@link #inputVariants()} and {@link #resultVariants()}.</p>
 */
public class LeadedGlassDisplayRecipe extends CustomRecipe {
    /** What sits in one crafting-grid cell of the displayed arrangement. */
    public enum Cell {
        EMPTY, PLAIN_PANE, CLEAR_PANE, LEADED_GLASS_BLOCK, GLASS_PANE, DYE, STAINED_GLASS, CLEAR_LEADED_GLASS,
        LEAD_INGOT, LEAD_DOOR, LEAD_TRAPDOOR;

        /** Whether this cell cycles with the colour variants (constant cells show one stack). */
        boolean cycles() {
            return this == PLAIN_PANE || this == LEADED_GLASS_BLOCK || this == GLASS_PANE || this == DYE
                    || this == STAINED_GLASS;
        }
    }

    /** What the arrangement produces (the pane's frame rides alongside for PANE results). */
    public enum ResultKind { PANE, DOOR, TRAPDOOR, BLOCK }

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
    private final List<Integer> colors;

    private LeadedGlassDisplayRecipe(String name, int width, int height, List<Cell> cells,
                                     ResultKind resultKind, @Nullable LeadedGlassFrame frame, int resultCount) {
        this(name, width, height, cells, resultKind, frame, resultCount, COLORS);
    }

    private LeadedGlassDisplayRecipe(String name, int width, int height, List<Cell> cells, ResultKind resultKind,
                                     @Nullable LeadedGlassFrame frame, int resultCount, List<Integer> colors) {
        super(CraftingBookCategory.MISC);
        this.name = name;
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.resultKind = resultKind;
        this.frame = frame;
        this.resultCount = resultCount;
        // Only the colours it can be made in: a dye mod's colour without a dye, a stained glass or a pane of its
        // own would show an empty slot making the glass.
        this.colors = colors.stream().filter(color -> cells.stream().filter(Cell::cycles)
                .noneMatch(cell -> cyclingStack(cell, color).isEmpty())).toList();
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

    /** The tag a grid cell takes, which a viewer that shows tags can show by its name; {@code null} for an item. */
    @Nullable
    public TagKey<Item> tagAt(int index) {
        return cells.get(index) == Cell.LEAD_INGOT ? CommonTags.Items.INGOTS_LEAD : null;
    }

    /** Per grid cell: the stacks it shows (one per colour for cycling cells, null for empty). */
    public List<@Nullable List<ItemStack>> inputVariants() {
        List<List<ItemStack>> inputs = new ArrayList<>(cells.size());
        for (Cell cell : cells) {
            inputs.add(switch (cell) {
                case EMPTY -> null;
                case LEAD_INGOT -> leadIngots();
                case CLEAR_PANE -> List.of(plainPane(LeadedGlassConfig.CLEAR));
                case LEAD_DOOR -> List.of(new ItemStack(ModItems.LEAD_DOOR.get()));
                case LEAD_TRAPDOOR -> List.of(new ItemStack(ModItems.LEAD_TRAPDOOR.get()));
                case CLEAR_LEADED_GLASS -> List.of(new ItemStack(ModItems.LEADED_GLASS.get()));
                default -> colors.stream().map(color -> cyclingStack(cell, color)).toList();
            });
        }
        return inputs;
    }

    /** What a cell that cycles with the colours shows for {@code color}; empty where no mod has one. */
    private static ItemStack cyclingStack(Cell cell, int color) {
        return switch (cell) {
            case PLAIN_PANE -> plainPane(color);
            case LEADED_GLASS_BLOCK -> leadedGlassBlock(color);
            case GLASS_PANE -> vanillaGlassPane(color);
            case DYE -> new ItemStack(LeadedGlassDyes.dyeOf(DyeColor.byId(color)));
            case STAINED_GLASS -> stainedGlass(color);
            default -> throw new IllegalArgumentException(cell + " doesn't cycle with the colours");
        };
    }

    /** The result stack per colour variant (index-aligned with the cycling inputs). */
    public List<ItemStack> resultVariants() {
        return colors.stream().map(this::result).toList();
    }

    private ItemStack result(int color) {
        return switch (resultKind) {
            case DOOR -> door(color, color);
            case TRAPDOOR -> trapdoor(color);
            case BLOCK -> leadedGlassBlock(color).copyWithCount(resultCount);
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
        // Eight clear panes around a dye -> eight plain panes of its colour, as vanilla dyes glass panes.
        recipes.add(new LeadedGlassDisplayRecipe("pane_dye", 3, 3, List.of(
                Cell.CLEAR_PANE, Cell.CLEAR_PANE, Cell.CLEAR_PANE,
                Cell.CLEAR_PANE, Cell.DYE, Cell.CLEAR_PANE,
                Cell.CLEAR_PANE, Cell.CLEAR_PANE, Cell.CLEAR_PANE), ResultKind.PANE, LeadedGlassFrame.PLAIN, 8,
                LeadedGlassDyes.colors().stream().map(DyeColor::getId).toList()));
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
        // Dyed leaded glass in a dye mod's colours, whose recipe reads the colour off the grid; vanilla's
        // colours have shaped recipes the viewers show by themselves.
        List<Integer> extra = LeadedGlassDyes.extraColors().stream().map(DyeColor::getId).toList();
        if (!extra.isEmpty()) {
            recipes.add(new LeadedGlassDisplayRecipe("dyed_by_dye", 3, 3, List.of(
                    Cell.CLEAR_LEADED_GLASS, Cell.CLEAR_LEADED_GLASS, Cell.CLEAR_LEADED_GLASS,
                    Cell.CLEAR_LEADED_GLASS, Cell.DYE, Cell.CLEAR_LEADED_GLASS,
                    Cell.CLEAR_LEADED_GLASS, Cell.CLEAR_LEADED_GLASS, Cell.CLEAR_LEADED_GLASS),
                    ResultKind.BLOCK, null, 8, extra));
            recipes.add(new LeadedGlassDisplayRecipe("dyed_by_lead", 3, 3, List.of(
                    Cell.STAINED_GLASS, Cell.STAINED_GLASS, Cell.STAINED_GLASS,
                    Cell.STAINED_GLASS, Cell.LEAD_INGOT, Cell.STAINED_GLASS,
                    Cell.STAINED_GLASS, Cell.STAINED_GLASS, Cell.STAINED_GLASS),
                    ResultKind.BLOCK, null, 8, extra));
        }
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

    /** Every lead ingot the recipes take, this mod's first. */
    private static List<ItemStack> leadIngots() {
        List<ItemStack> ingots = new ArrayList<>(List.of(new ItemStack(ModItems.LEAD_INGOT.get())));
        for (Holder<Item> ingot : BuiltInRegistries.ITEM.getTagOrEmpty(CommonTags.Items.INGOTS_LEAD)) {
            if (ingot.value() != ModItems.LEAD_INGOT.get()) {
                ingots.add(new ItemStack(ingot));
            }
        }
        return ingots;
    }

    private static ItemStack leadedGlassBlock(int color) {
        return new ItemStack(color < 0
                ? ModBlocks.LEADED_GLASS.get()
                : ModBlocks.STAINED_LEADED_GLASS.get(DyeColor.byId(color)).get());
    }

    /** The stained glass of the colour, vanilla's or a dye mod's; empty for a colour no mod has one of. */
    private static ItemStack stainedGlass(int color) {
        Block glass = LeadedGlassDyes.stainedGlassOf(DyeColor.byId(color));
        return glass == null ? ItemStack.EMPTY : new ItemStack(glass);
    }

    /** The glass pane of the colour, vanilla's or a dye mod's; empty for a colour no mod has one of. */
    private static ItemStack vanillaGlassPane(int color) {
        return new ItemStack(LeadedGlassColors.glassPaneOf(color));
    }

    private static List<Integer> buildColors() {
        List<Integer> colors = new ArrayList<>();
        colors.add(LeadedGlassConfig.CLEAR);
        // One colour of each name, as each name has one block: two dye mods' corals share it.
        for (DyeColor dye : LeadedGlassDyes.colors()) {
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

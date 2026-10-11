package com.phantomwing.theleadage.block.custom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassBlock;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The colours leaded glass comes in: every {@link DyeColor}, not only vanilla's sixteen. A dye mod that adds
 * colours to the enum (Dye Depot, Delicate Dyes) gets a dyed leaded glass block for each of them.
 *
 * <p>The jar carries files, recipes and translations for vanilla's sixteen only. The other colours' files are
 * made at runtime ({@code DyedLeadedGlassPack}), their recipe reads the colour off its ingredients
 * ({@code StainedLeadedGlassRecipe}), and their names follow one pattern per language, since a translation
 * can't be written for a colour nobody knows of yet.</p>
 */
public final class LeadedGlassDyes {
    private static final Set<String> VANILLA = Set.of("white", "orange", "magenta", "light_blue", "yellow", "lime",
            "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");

    private static List<DyeColor> colors;
    @Nullable
    private static Map<DyeColor, Item> dyes;
    @Nullable
    private static Map<DyeColor, Block> stainedGlass;

    private LeadedGlassDyes() {
    }

    /** Whether this is one of vanilla's sixteen colours, which the jar has files and translations for. */
    public static boolean isVanilla(DyeColor color) {
        return VANILLA.contains(color.getName());
    }

    /**
     * The colours that get a block of their own, in {@link DyeColor} order. Two dye mods can each add a colour
     * of the same name (both Dye Depot and Delicate Dyes add coral and rose), and a block's id can only be
     * registered once, so the first colour of a name has the block and the other shares it.
     */
    public static List<DyeColor> colors() {
        if (colors == null) {
            Set<String> names = new HashSet<>();
            List<DyeColor> found = new ArrayList<>();
            for (DyeColor color : DyeColor.values()) {
                if (names.add(color.getName())) {
                    found.add(color);
                }
            }
            colors = List.copyOf(found);
        }
        return colors;
    }

    /** The colours that have a block but no files in the jar: those a dye mod added. */
    public static List<DyeColor> extraColors() {
        return colors().stream().filter(color -> !isVanilla(color)).toList();
    }

    /** The dye of a colour, vanilla's or its dye mod's, or air when no mod has one. */
    public static Item dyeOf(DyeColor color) {
        if (dyes == null) {
            Map<DyeColor, Item> found = new EnumMap<>(DyeColor.class);
            for (Item item : BuiltInRegistries.ITEM) {
                DyeColor dye = LeadedGlassRecolor.dyeOf(item.getDefaultInstance());
                if (dye != null && (isFromVanilla(BuiltInRegistries.ITEM.getKey(item).getNamespace()) || !found.containsKey(dye))) {
                    found.put(dye, item);
                }
            }
            dyes = found;
        }
        return dyes.getOrDefault(color, Items.AIR);
    }

    /** The stained glass of a colour, vanilla's or its dye mod's, or {@code null} when no mod has one. */
    @Nullable
    public static Block stainedGlassOf(DyeColor color) {
        if (stainedGlass == null) {
            Map<DyeColor, Block> found = new EnumMap<>(DyeColor.class);
            for (Block block : BuiltInRegistries.BLOCK) {
                if (block instanceof StainedGlassBlock glass && !(block instanceof StainedLeadedGlassBlock)
                        && (isFromVanilla(BuiltInRegistries.BLOCK.getKey(block).getNamespace()) || !found.containsKey(glass.getColor()))) {
                    found.put(glass.getColor(), block);
                }
            }
            stainedGlass = found;
        }
        return stainedGlass.get(color);
    }

    private static boolean isFromVanilla(String namespace) {
        return namespace.equals("minecraft");
    }

    /** The colour's name, as its dye mod translates it, else its id in words ("light_coral" as "Light Coral"). */
    public static Component colorName(DyeColor color) {
        return Component.translatableWithFallback("color.minecraft." + color.getName(), words(color.getName()));
    }

    /**
     * The name of leaded glass of a {@code kind} ({@code leaded_glass}, {@code leaded_glass_pane} or
     * {@code leaded_glass_trapdoor}) in a colour: its own translation for vanilla's sixteen ("Red Leaded Glass
     * Pane"), else the kind's pattern around the colour's name. The pattern keeps the colour out of the way of
     * agreement, which the languages that inflect a colour for its noun can't do for a word they don't know.
     */
    public static Component name(String kind, DyeColor color) {
        return isVanilla(color)
                ? Component.translatable("block.theleadage." + color.getName() + "_" + kind)
                : Component.translatable("block.theleadage." + kind + ".colored", colorName(color));
    }

    private static String words(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("_")) {
            if (!word.isEmpty()) {
                if (!out.isEmpty()) {
                    out.append(' ');
                }
                out.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
            }
        }
        return out.toString();
    }
}

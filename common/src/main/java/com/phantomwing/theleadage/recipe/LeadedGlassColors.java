package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps the leaded glass crafting inputs to a region colour id (a dye id, or
 * {@link LeadedGlassConfig#CLEAR}). Sources: the leaded glass <i>blocks</i> (the palette for
 * cutting panes), a <i>plain</i> leaded glass pane (for combining into a split), and <i>glass panes</i>
 * (crafting leaded panes from glass — clear, or stained by vanilla or a dye mod).
 */
public final class LeadedGlassColors {
    @Nullable
    private static Map<Item, Integer> blockMap;
    @Nullable
    private static Map<Item, Integer> glassPaneMap;

    private LeadedGlassColors() {
    }

    private static Map<Item, Integer> blockMap() {
        if (blockMap == null) {
            Map<Item, Integer> built = new HashMap<>();
            built.put(ModBlocks.LEADED_GLASS.get().asItem(), LeadedGlassConfig.CLEAR);
            for (Block block : ModBlocks.stainedLeadedGlassBlocks()) {
                built.put(block.asItem(), ((StainedGlassBlock) block).getColor().getId());
            }
            blockMap = built;
        }
        return blockMap;
    }

    /** Every stained glass pane, a dye mod's too, by the colour it carries. */
    private static Map<Item, Integer> glassPaneMap() {
        if (glassPaneMap == null) {
            Map<Item, Integer> built = new HashMap<>();
            built.put(Items.GLASS_PANE, LeadedGlassConfig.CLEAR);
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof StainedGlassPaneBlock pane) {
                    built.put(item, pane.getColor().getId());
                }
            }
            glassPaneMap = built;
        }
        return glassPaneMap;
    }

    /** The glass pane of a colour id (vanilla's where there is one), or air when no mod has one. */
    public static Item glassPaneOf(int color) {
        if (color < 0) {
            return Items.GLASS_PANE;
        }
        Item found = Items.AIR;
        for (Map.Entry<Item, Integer> entry : glassPaneMap().entrySet()) {
            if (entry.getValue() == color && (found == Items.AIR
                    || BuiltInRegistries.ITEM.getKey(entry.getKey()).getNamespace().equals("minecraft"))) {
                found = entry.getKey();
            }
        }
        return found;
    }

    /** Colour id for a leaded glass <i>block</i> stack (pane-cutting), or {@code null} if it isn't one. */
    @Nullable
    public static Integer blockColorIdOf(ItemStack stack) {
        return stack.isEmpty() ? null : blockMap().get(stack.getItem());
    }

    /** Colour id for a <i>glass pane</i> stack (clear or stained), or {@code null} if it isn't one. */
    @Nullable
    public static Integer glassPaneColorIdOf(ItemStack stack) {
        return stack.isEmpty() ? null : glassPaneMap().get(stack.getItem());
    }

    /** Colour id for a <i>plain</i> leaded glass pane stack (combining), or {@code null} if it isn't one. */
    @Nullable
    public static Integer plainPaneColorIdOf(ItemStack stack) {
        if (!stack.is(ModItems.LEADED_GLASS_PANEL.get())) {
            return null;
        }
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null) {
            return LeadedGlassConfig.CLEAR; // a default (unconfigured) pane = clear
        }
        if (config.frame() != LeadedGlassFrame.PLAIN) {
            return null; // only single-colour panes can be combined into a split
        }
        return config.colors().isEmpty() ? LeadedGlassConfig.CLEAR : config.colors().get(0);
    }
}

package com.phantomwing.theleadage.recipe;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps the leaded glass crafting inputs to a region colour id (a dye id, or
 * {@link LeadedGlassConfig#CLEAR}). Sources: the leaded glass <i>blocks</i> (the palette for
 * cutting panes), a <i>plain</i> leaded glass pane (for combining into a split), and vanilla
 * <i>glass panes</i> (crafting leaded panes from glass — clear or stained).
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
            ModBlocks.STAINED_LEADED_GLASS.forEach((dye, block) -> built.put(block.get().asItem(), dye.getId()));
            blockMap = built;
        }
        return blockMap;
    }

    private static Map<Item, Integer> glassPaneMap() {
        if (glassPaneMap == null) {
            Map<Item, Integer> built = new HashMap<>();
            built.put(Items.GLASS_PANE, LeadedGlassConfig.CLEAR);
            for (DyeColor dye : DyeColor.values()) {
                Item pane = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(dye.getName() + "_stained_glass_pane"));
                built.put(pane, dye.getId());
            }
            glassPaneMap = built;
        }
        return glassPaneMap;
    }

    /** Colour id for a leaded glass <i>block</i> stack (pane-cutting), or {@code null} if it isn't one. */
    @Nullable
    public static Integer blockColorIdOf(ItemStack stack) {
        return stack.isEmpty() ? null : blockMap().get(stack.getItem());
    }

    /** Colour id for a vanilla <i>glass pane</i> stack (clear or stained), or {@code null} if it isn't one. */
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

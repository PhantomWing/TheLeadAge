package com.phantomwing.theleadage.compat;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * What tells one stack of a leaded glass item from another in a recipe viewer: its design. Without it JEI and EMI take
 * every colour and frame of a pane for the same stack - JEI's list keeps one of them, and filling a recipe in can put a
 * clear pane where a red one was shown. A stack with no design and one with the design it would place anyway, all clear,
 * are the same: what a recipe takes as plain lead glass matches the panes a player has.
 */
public final class LeadedGlassSubtypes {
    private LeadedGlassSubtypes() {
    }

    /** The items whose stacks differ by design: every pane, the door and the trapdoor. */
    public static List<Item> items() {
        Set<Item> items = new LinkedHashSet<>();
        for (LeadedGlassFrame frame : LeadedGlassFrame.values()) {
            items.add(ModItems.paneItemFor(frame));
        }
        items.add(ModItems.LEADED_GLASS_DOOR.get());
        items.add(ModItems.LEADED_GLASS_TRAPDOOR.get());
        return List.copyOf(items);
    }

    /** The stack's design, or null for an all-clear one in the frame it would place without one. */
    @Nullable
    public static Object designOf(ItemStack stack) {
        LeadedGlassDoorConfig door = stack.get(ModDataComponents.LEADED_GLASS_DOOR_CONFIG.get());
        if (door != null) {
            return isPlainClear(door.top(), LeadedGlassFrame.PLAIN) && isPlainClear(door.bottom(), LeadedGlassFrame.PLAIN)
                    ? null : door;
        }
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null) {
            return null;
        }
        LeadedGlassFrame placed = stack.getItem() instanceof BlockItem item && item.getBlock() instanceof LeadedGlassPaneBlock pane
                ? pane.cameType().frame(0) : LeadedGlassFrame.PLAIN;
        return isPlainClear(config, placed) ? null : config;
    }

    private static boolean isPlainClear(LeadedGlassConfig config, LeadedGlassFrame placed) {
        return config.frame() == placed && config.colors().stream().allMatch(color -> color == LeadedGlassConfig.CLEAR);
    }
}

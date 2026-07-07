package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Shared rendering of a leaded glass item's design (frame + colours), used by the pane, door and
 * trapdoor items. A plain pane shows nothing extra — its single colour goes in the name instead
 * (e.g. "Red Leaded Glass Pane"); every other frame shows its distinct colours as a comma-separated
 * list (including "Clear").
 */
public final class LeadedGlassTooltip {
    private static final Component SEPARATOR = Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY);

    private LeadedGlassTooltip() {
    }

    /** Appends the frame line + the distinct colours applied. No-op for plain (see {@link #name}). */
    public static void append(ItemStack stack, List<Component> tooltip) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null || config.frame() == LeadedGlassFrame.PLAIN) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.theleadage.frame." + config.frame().getSerializedName())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(colorSummary(config));
    }

    /** A door lists both of its panes (top then bottom), each labelled with its half. */
    public static void appendDoor(ItemStack stack, List<Component> tooltip) {
        LeadedGlassDoorConfig config = stack.get(ModDataComponents.LEADED_GLASS_DOOR_CONFIG.get());
        if (config == null) {
            return;
        }
        appendHalf(tooltip, "tooltip.theleadage.door.top", config.top());
        appendHalf(tooltip, "tooltip.theleadage.door.bottom", config.bottom());
    }

    /** One door half: "<label>: <frame or colour>", then the distinct colours for patterned panes. */
    private static void appendHalf(List<Component> tooltip, String labelKey, LeadedGlassConfig config) {
        MutableComponent label = Component.translatable(labelKey).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY));
        if (config.frame() == LeadedGlassFrame.PLAIN) {
            tooltip.add(label.append(colorName(config, 0)));
            return;
        }
        tooltip.add(label.append(Component.translatable(
                "tooltip.theleadage.frame." + config.frame().getSerializedName()).withStyle(ChatFormatting.GRAY)));
        tooltip.add(colorSummary(config));
    }

    /** The distinct region colours (in first-applied order, including "Clear"), comma-separated. */
    private static MutableComponent colorSummary(LeadedGlassConfig config) {
        MutableComponent line = Component.empty();
        boolean first = true;
        for (int id : new LinkedHashSet<>(config.colors())) {
            if (!first) {
                line.append(SEPARATOR);
            }
            first = false;
            Component name = id < 0
                    ? Component.translatable("tooltip.theleadage.color.clear")
                    : Component.translatable("color.minecraft." + DyeColor.byId(id).getName());
            line.append(name.copy().withStyle(ChatFormatting.DARK_GRAY));
        }
        return line;
    }

    /** A plain pane carries its colour in the name ("Red Leaded Glass Pane"); otherwise the base name. */
    public static Component name(ItemStack stack, Component baseName) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null || config.frame() != LeadedGlassFrame.PLAIN) {
            return baseName;
        }
        DyeColor dye = config.colorAt(0);
        if (dye == null) {
            return baseName; // clear plain pane keeps its plain name
        }
        return Component.translatable("item.theleadage.tinted",
                Component.translatable("color.minecraft." + dye.getName()), baseName);
    }

    private static Component colorName(LeadedGlassConfig config, int region) {
        DyeColor dye = config.colorAt(region);
        Component name = dye == null
                ? Component.translatable("tooltip.theleadage.color.clear")
                : Component.translatable("color.minecraft." + dye.getName());
        return name.copy().withStyle(ChatFormatting.DARK_GRAY);
    }
}

package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Shared rendering of a leaded glass item's design (frame + colours), used by the pane, door and
 * trapdoor items. A plain pane shows nothing extra — its single colour goes in the name instead
 * (e.g. "Red Leaded Glass Pane"); every other frame lists its colours laid out per row.
 */
public final class LeadedGlassTooltip {
    private static final Component DOT = Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY);

    private LeadedGlassTooltip() {
    }

    /** Appends the frame line + one colour line per pattern row. No-op for plain (see {@link #name}). */
    public static void append(ItemStack stack, List<Component> tooltip) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null || config.frame() == LeadedGlassFrame.PLAIN) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.theleadage.frame." + config.frame().getSerializedName())
                .withStyle(ChatFormatting.GRAY));
        for (int[] row : config.frame().rows()) {
            MutableComponent line = Component.empty();
            for (int i = 0; i < row.length; i++) {
                if (i > 0) {
                    line.append(DOT);
                }
                line.append(colorName(config, row[i]));
            }
            tooltip.add(line);
        }
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

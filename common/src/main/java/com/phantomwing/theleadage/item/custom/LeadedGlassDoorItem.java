package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Item form of the leaded glass door. Carries the top-half glass design in the
 * {@code leaded_glass_config} data component (taken from the pane it was crafted with) and
 * shows it in a tooltip — the frame plus each region's colour.
 */
public class LeadedGlassDoorItem extends BlockItem {
    public LeadedGlassDoorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.theleadage.frame." + config.frame().getSerializedName())
                .withStyle(ChatFormatting.GRAY));

        StringBuilder colours = new StringBuilder();
        for (int region = 0; region < config.colors().size(); region++) {
            if (region > 0) {
                colours.append(" / ");
            }
            DyeColor dye = config.colorAt(region);
            colours.append(dye == null ? "Clear" : capitalise(dye.getName()));
        }
        tooltip.add(Component.literal(colours.toString()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String capitalise(String name) {
        String spaced = name.replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}

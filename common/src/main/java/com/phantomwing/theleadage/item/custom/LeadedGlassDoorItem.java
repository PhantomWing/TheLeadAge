package com.phantomwing.theleadage.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Item form of the leaded glass door. Carries the top-half glass design in the
 * {@code leaded_glass_config} data component (taken from the pane it was crafted with); a plain
 * design shows its colour in the name, every other frame lists its colours per row in the tooltip
 * (see {@link LeadedGlassTooltip}).
 */
public class LeadedGlassDoorItem extends BlockItem {
    public LeadedGlassDoorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return LeadedGlassTooltip.name(stack, super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LeadedGlassTooltip.append(stack, tooltip);
    }
}

package com.phantomwing.theleadage.item.custom;

import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Item form of the leaded glass door. Carries both halves' glass designs in the
 * {@code leaded_glass_door_config} data component (from the two panes it was crafted with); the
 * tooltip lists each half (see {@link LeadedGlassTooltip#appendDoor}).
 */
public class LeadedGlassDoorItem extends BlockItem {
    public LeadedGlassDoorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        LeadedGlassTooltip.appendDoor(stack, tooltip);
    }
}

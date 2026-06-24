package com.phantomwing.theleadage.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Item form of the leaded glass trapdoor; carries the glass design. A plain design shows its
 * colour in the name, every other frame lists its colours per row (see {@link LeadedGlassTooltip}).
 */
public class LeadedGlassTrapdoorItem extends BlockItem {
    public LeadedGlassTrapdoorItem(Block block, Properties properties) {
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

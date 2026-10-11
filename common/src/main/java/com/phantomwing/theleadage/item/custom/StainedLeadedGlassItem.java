package com.phantomwing.theleadage.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Dyed leaded glass's item, named as its block is: by pattern for a colour a dye mod adds. */
public class StainedLeadedGlassItem extends BlockItem {
    public StainedLeadedGlassItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getBlock().getName();
    }
}

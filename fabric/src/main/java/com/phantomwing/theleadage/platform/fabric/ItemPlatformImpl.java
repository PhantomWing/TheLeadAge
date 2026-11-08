package com.phantomwing.theleadage.platform.fabric;

import com.phantomwing.theleadage.item.custom.LeadedGlassTrapdoorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Fabric implementation of {@link com.phantomwing.theleadage.platform.ItemPlatform}: the common items as they are. */
public final class ItemPlatformImpl {
    private ItemPlatformImpl() {
    }

    public static Item createLeadedGlassTrapdoorItem(Block block, Item.Properties properties) {
        return new LeadedGlassTrapdoorItem(block, properties);
    }
}

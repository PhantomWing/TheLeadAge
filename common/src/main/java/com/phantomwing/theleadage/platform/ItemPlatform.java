package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Items a loader makes its own kind of. Forge 47 gives an item its custom renderer from the item itself
 * ({@code Item#initializeClient}, where NeoForge 21 has an event), so its leaded glass trapdoor is a subclass that
 * names the renderer; Fabric's registers the renderer apart and takes the common item.
 */
public final class ItemPlatform {
    private ItemPlatform() {
    }

    @ExpectPlatform
    public static Item createLeadedGlassTrapdoorItem(Block block, Item.Properties properties) {
        throw new AssertionError();
    }
}

package com.phantomwing.theleadage.platform.neoforge;

import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.neoforge.compat.spears.LeadSpear;
import dev.architectury.platform.Platform;
import net.minecraft.world.item.Item;

/** NeoForge implementation of {@link com.phantomwing.theleadage.platform.BackportPlatform}. */
public final class BackportPlatformImpl {
    private BackportPlatformImpl() {
    }

    public static Item createLeadSpear(Item.Properties fallback) {
        // A static factory, not an inline `new`, so verifying this method never loads the other mod's classes.
        if (Platform.isModLoaded(ModIds.BACKPORTED_SPEARS)) {
            return LeadSpear.create(fallback);
        }
        return new Item(fallback);
    }
}

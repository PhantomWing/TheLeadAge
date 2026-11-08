package com.phantomwing.theleadage.platform.forge;

import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.forge.compat.vanillabackport.LeadNautilusArmor;
import dev.architectury.platform.Platform;
import net.minecraft.world.item.Item;

/** Forge implementation of {@link com.phantomwing.theleadage.platform.BackportPlatform}. */
public final class BackportPlatformImpl {
    private BackportPlatformImpl() {
    }

    public static Item createLeadSpear(Item.Properties fallback) {
        // Static factories, not an inline `new`, so verifying this method never loads the other mod's classes.
        if (Platform.isModLoaded(ModIds.VANILLA_BACKPORT)) {
            return com.phantomwing.theleadage.forge.compat.vanillabackport.LeadSpear.create(fallback);
        }
        return new Item(fallback);
    }

    public static Item createLeadNautilusArmor(Item.Properties properties) {
        if (Platform.isModLoaded(ModIds.VANILLA_BACKPORT)) {
            return LeadNautilusArmor.create(properties);
        }
        return new Item(properties);
    }
}

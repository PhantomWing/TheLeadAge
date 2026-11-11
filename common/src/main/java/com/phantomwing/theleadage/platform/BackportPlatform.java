package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;

/**
 * {@code @ExpectPlatform} bridge that builds the Lead Spear, 1.21.11's, which Backported Spears backports to this
 * version.
 *
 * <p>It is always registered, so a world keeps it when the mod goes, and is the mod's own kind of spear when the mod
 * is installed, a plain item otherwise. As with the Lead Knife, the class that names the mod is only loaded behind
 * an {@code isModLoaded} check, through a static factory.</p>
 */
public final class BackportPlatform {
    private BackportPlatform() {
    }

    /** {@code fallback} carries the item's id, as every item's properties must from 1.21.2. */
    @ExpectPlatform
    public static Item createLeadSpear(Item.Properties fallback) {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }
}

package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;

/**
 * {@code @ExpectPlatform} bridge that builds the items 1.21.11 added, which mods backport to this version: the Lead
 * Spear through Backported Spears or Vanilla Backport, and Lead Nautilus Armor through Vanilla Backport.
 *
 * <p>Each is always registered, so a world keeps it when the mod goes, and is the mod's own kind of item when the mod
 * is installed, a plain item otherwise. As with the Lead Knife, the class that names the mod is only loaded behind
 * an {@code isModLoaded} check, through a static factory.</p>
 */
public final class BackportPlatform {
    private BackportPlatform() {
    }

    @ExpectPlatform
    public static Item createLeadSpear(Item.Properties fallback) {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }

    @ExpectPlatform
    public static Item createLeadNautilusArmor(Item.Properties properties) {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }
}

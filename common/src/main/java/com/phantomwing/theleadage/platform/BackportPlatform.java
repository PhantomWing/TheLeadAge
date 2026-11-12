package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

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

    /**
     * Registers the spear {@code spear} makes, in {@code items}; on Fabric with Backported Spears, not before that has
     * made its own spears, which a spear is built from.
     */
    @ExpectPlatform
    public static RegistrySupplier<Item> registerLeadSpear(DeferredRegister<Item> items, String name, Supplier<Item> spear) {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
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

package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * {@code @ExpectPlatform} bridge that adds to an existing creative tab. Architectury's
 * {@code CreativeTabRegistry.modify} listens on Architectury's own mod bus on Forge, so it runs before the mods
 * whose items {@code VanillaTabs} inserts after (Backported Spears, Mekanism) have filled the tab, whatever
 * {@code mods.toml} orders. Forge's implementation listens on this mod's bus instead.
 */
public final class CreativeTabPlatform {
    private CreativeTabPlatform() {
    }

    /** Puts one stack after another in a tab; throws {@link IllegalArgumentException} where Forge's tab doesn't list the anchor. */
    public interface Inserter {
        void insertAfter(ItemStack anchor, ItemStack stack);
    }

    @ExpectPlatform
    public static void modify(ResourceKey<CreativeModeTab> tab, Consumer<Inserter> contents) {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }
}

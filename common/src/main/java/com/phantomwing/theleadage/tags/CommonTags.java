package com.phantomwing.theleadage.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Conventional ({@code c:}) tags. Mod-namespaced tags live in {@link ModTags}. */
public final class CommonTags {
    private CommonTags() {
    }

    public static final class Items {
        /** {@code c:ingots/lead} — also the tool/armor repair ingredient. */
        public static final TagKey<Item> INGOTS_LEAD = tag("ingots/lead");

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }

        private Items() {
        }
    }
}

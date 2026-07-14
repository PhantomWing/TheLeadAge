package com.phantomwing.theleadage.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

/** Conventional ({@code c:}) tags. Mod-namespaced tags live in {@link ModTags}. */
public final class CommonTags {
    private CommonTags() {
    }

    public static final class Biomes {
        /** {@code c:is_swamp} — vanilla swamp + mangrove swamp, plus any modded swamps (populated by both loaders). */
        public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");

        private static TagKey<Biome> tag(String path) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", path));
        }

        private Biomes() {
        }
    }

    public static final class Items {
        /** {@code c:ingots/lead} — also the tool/armor repair ingredient. */
        public static final TagKey<Item> INGOTS_LEAD = tag("ingots/lead");
        /** {@code c:plates/lead} — the Create-compat Lead Sheet (Create's convention for pressed metal). */
        public static final TagKey<Item> PLATES_LEAD = tag("plates/lead");
        /** {@code c:tools/knife} — the conventional knife tag, alongside Farmer's Delight's own. */
        public static final TagKey<Item> TOOLS_KNIFE = tag("tools/knife");

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }

        private Items() {
        }
    }
}

package com.phantomwing.theleadage.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * Fabric API's conventional tags on 1.20.1 ({@code c:lead_ingots}), which Fabric's mods share lead through. The mod's
 * lead goes in them so those mods take it, and each of {@link CommonTags}' Forge tags takes them in, so theirs counts in
 * the mod's recipes. 1.21 moved both loaders onto one set of {@code c:} tags.
 */
public final class FabricTags {
    private static final String NAMESPACE = "c";

    private FabricTags() {
    }

    public static final class Biomes {
        /** Fabric API's swamps, which {@code forge:is_swamp} is on Forge. */
        public static final TagKey<Biome> SWAMP = TagKey.create(Registries.BIOME, new ResourceLocation(NAMESPACE, "swamp"));

        private Biomes() {
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> LEAD_BLOCKS = tag("lead_blocks");
        public static final TagKey<Block> RAW_LEAD_BLOCKS = tag("raw_lead_blocks");
        public static final TagKey<Block> LEAD_ORES = tag("lead_ores");

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(NAMESPACE, path));
        }

        private Blocks() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> LEAD_BLOCKS = tag("lead_blocks");
        public static final TagKey<Item> RAW_LEAD_BLOCKS = tag("raw_lead_blocks");
        public static final TagKey<Item> LEAD_ORES = tag("lead_ores");
        public static final TagKey<Item> RAW_LEAD_ORES = tag("raw_lead_ores");
        public static final TagKey<Item> LEAD_NUGGETS = tag("lead_nuggets");
        public static final TagKey<Item> LEAD_INGOTS = tag("lead_ingots");
        public static final TagKey<Item> LEAD_PLATES = tag("lead_plates");
        public static final TagKey<Item> KNIVES = tag("tools/knives");

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(NAMESPACE, path));
        }

        private Items() {
        }
    }
}

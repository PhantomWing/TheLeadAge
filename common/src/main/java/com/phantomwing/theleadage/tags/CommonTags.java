package com.phantomwing.theleadage.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * The tags other mods share lead through. On 1.20.1 those are Forge's ({@code forge:ingots/lead}), which recipes and
 * code read on both loaders, since the mod ships them; Fabric's mods share theirs under Fabric API's own names
 * ({@code c:lead_ingots}, {@link FabricTags}), which each Forge tag takes in too, so their lead counts here as well, as
 * The Silver Age's 1.20.1 line does. 1.21 moved both loaders onto one set of {@code c:} tags. Mod-namespaced tags live in
 * {@link ModTags}.
 */
public final class CommonTags {
    private static final String NAMESPACE = "forge";

    private CommonTags() {
    }

    public static final class Biomes {
        /** {@code forge:is_swamp} — vanilla swamp + mangrove swamp, plus any modded swamps. Fabric's is {@code c:swamp}. */
        public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");

        private static TagKey<Biome> tag(String path) {
            return TagKey.create(Registries.BIOME, new ResourceLocation(NAMESPACE, path));
        }

        private Biomes() {
        }
    }

    public static final class Blocks {
        /** {@code forge:ores/lead}. Fabric's is {@code c:lead_ores}. */
        public static final TagKey<Block> ORES_LEAD = tag("ores/lead");
        /** {@code forge:storage_blocks/lead}. Fabric's is {@code c:lead_blocks}. */
        public static final TagKey<Block> STORAGE_BLOCKS_LEAD = tag("storage_blocks/lead");
        /** {@code forge:storage_blocks/raw_lead}. Fabric's is {@code c:raw_lead_blocks}. */
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_LEAD = tag("storage_blocks/raw_lead");

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(NAMESPACE, path));
        }

        private Blocks() {
        }
    }

    public static final class Items {
        /** {@code forge:ores/lead} - Create's and Mekanism's crushing and enriching take it. */
        public static final TagKey<Item> ORES_LEAD = tag("ores/lead");
        /** {@code forge:raw_materials/lead}. */
        public static final TagKey<Item> RAW_MATERIALS_LEAD = tag("raw_materials/lead");
        /** {@code forge:storage_blocks/lead}. */
        public static final TagKey<Item> STORAGE_BLOCKS_LEAD = tag("storage_blocks/lead");
        /** {@code forge:storage_blocks/raw_lead}. */
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_LEAD = tag("storage_blocks/raw_lead");
        /** {@code forge:plates}, the parent of {@link #PLATES_LEAD}; Forge itself has none. */
        public static final TagKey<Item> PLATES = tag("plates");
        /**
         * {@code c:armors/nautilus}, 1.21.11's nautilus armour, which the 1.21.11 lines and Vanilla Backport share here
         * under the name they share there.
         */
        public static final TagKey<Item> ARMORS_NAUTILUS = TagKey.create(Registries.ITEM, new ResourceLocation("c", "armors/nautilus"));
        /** {@code forge:ingots/lead} — also the tool/armor repair ingredient. */
        public static final TagKey<Item> INGOTS_LEAD = tag("ingots/lead");
        /** {@code forge:nuggets/lead}. */
        public static final TagKey<Item> NUGGETS_LEAD = tag("nuggets/lead");
        /** {@code forge:plates/lead} — the Create-compat Lead Sheet (Create's convention for pressed metal). */
        public static final TagKey<Item> PLATES_LEAD = tag("plates/lead");
        /** {@code forge:tools/knives} — plural on 1.20.1, the tag Farmer's Delight's cutting recipes accept. */
        public static final TagKey<Item> TOOLS_KNIFE = tag("tools/knives");

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(NAMESPACE, path));
        }

        private Items() {
        }
    }
}

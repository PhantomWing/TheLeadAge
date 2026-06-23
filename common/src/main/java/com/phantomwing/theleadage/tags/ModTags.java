package com.phantomwing.theleadage.tags;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {
    }

    public static final class Blocks {
        /** Solid, full-cube lead blocks (always a complete block). */
        public static final TagKey<Block> SOLID_LEAD_BLOCKS = tag("solid_lead_blocks");
        /** Lead slab blocks. */
        public static final TagKey<Block> LEAD_SLABS = tag("lead_slabs");
        /** Lead stair blocks. */
        public static final TagKey<Block> LEAD_STAIRS = tag("lead_stairs");
        /** Lead door blocks. */
        public static final TagKey<Block> LEAD_DOORS = tag("lead_doors");
        /** Lead trapdoor blocks. */
        public static final TagKey<Block> LEAD_TRAPDOORS = tag("lead_trapdoors");
        /** The lead building set: solid blocks + slabs + stairs. Feeds occludes_vibration_signals. */
        public static final TagKey<Block> LEAD_BLOCKS = tag("lead_blocks");
        /** Full leaded glass blocks (clear + stained), excluding panes. Mirrors vanilla glass. */
        public static final TagKey<Block> LEADED_GLASS_BLOCKS = tag("leaded_glass_blocks");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
        }

        private Blocks() {
        }
    }

    public static final class EntityTypes {
        /** Mobs whose naturally-spawned iron armor may be swapped for lead armor. */
        public static final TagKey<EntityType<?>> CAN_WEAR_LEAD_ARMOR = tag("can_wear_lead_armor");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
        }

        private EntityTypes() {
        }
    }

    public static final class Biomes {
        /** Biomes that lead ore generates in (all overworld biomes by default). */
        public static final TagKey<Biome> HAS_LEAD_ORE = tag("has_lead_ore");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
        }

        private Biomes() {
        }
    }
}

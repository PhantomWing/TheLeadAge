package com.phantomwing.theleadage.tags;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {
    }

    public static final class Blocks {
        /**
         * Solid, full-cube lead blocks (always a complete block). These are piston-proof — as is the
         * raw lead block, which is not in this tag (it is raw ore, not refined lead).
         */
        public static final TagKey<Block> SOLID_LEAD_BLOCKS = tag("solid_lead_blocks");
        /** Lead slab blocks. */
        public static final TagKey<Block> LEAD_SLABS = tag("lead_slabs");
        /** Lead stair blocks. */
        public static final TagKey<Block> LEAD_STAIRS = tag("lead_stairs");
        /** Lead walls (currently just the lead brick wall). */
        public static final TagKey<Block> LEAD_WALLS = tag("lead_walls");
        /** Lead door blocks. */
        public static final TagKey<Block> LEAD_DOORS = tag("lead_doors");
        /** Lead trapdoor blocks. */
        public static final TagKey<Block> LEAD_TRAPDOORS = tag("lead_trapdoors");
        /** The lead building set: solid blocks + slabs + stairs. Feeds occludes_vibration_signals. */
        public static final TagKey<Block> LEAD_BLOCKS = tag("lead_blocks");
        /** The lead weight tiers — small but solid lead, the densest thing in the mod. */
        public static final TagKey<Block> LEAD_WEIGHTS = tag("lead_weights");
        /** Open-work lead (grate, bars): mostly air, so far lighter than a solid lead block. */
        public static final TagKey<Block> LEAD_LATTICE = tag("lead_lattice");
        /** Full leaded glass blocks (clear + stained), excluding panes. Mirrors vanilla glass. */
        public static final TagKey<Block> LEADED_GLASS_BLOCKS = tag("leaded_glass_blocks");
        /**
         * Blocks creepers flee from — the lead torches and lantern. The mod-side answer to vanilla's
         * {@code #minecraft:piglin_repellents}: that tag only works because piglins are brain-based,
         * and creepers are not, so this one is read by {@code AvoidRepellentBlockGoal} instead.
         * Datapack-extensible all the same.
         */
        public static final TagKey<Block> CREEPER_REPELLENTS = tag("creeper_repellents");
        /**
         * Blocks pillagers flee from. Kept separate from {@link #CREEPER_REPELLENTS} so the two mobs can
         * be tuned independently — a pack may well want a block that scares one but not the other.
         */
        public static final TagKey<Block> PILLAGER_REPELLENTS = tag("pillager_repellents");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
        }

        private Blocks() {
        }
    }

    public static final class Items {
        /** Full leaded glass blocks (clear + stained) as items — mirrors {@link Blocks#LEADED_GLASS_BLOCKS}. */
        public static final TagKey<Item> LEADED_GLASS_BLOCKS = tag("leaded_glass_blocks");
        /**
         * The item form of {@link Blocks#CREEPER_REPELLENTS}, mirroring vanilla's
         * {@code #minecraft:piglin_repellents} item tag (which sits alongside the block tag of the same
         * name). Note the wall torch has no item, so — exactly as in vanilla — this holds the standing
         * torch and the lantern only.
         *
         * <p>Purely declarative for now: it is the datapack/mod-facing contract for "this item is a
         * creeper repellent". Nothing in this mod reads it yet, because the vanilla tag's one consumer
         * ({@code PiglinAi.wantsToPickup} — piglins refuse to pick the item up) has no analogue here:
         * creepers cannot pick up items at all.</p>
         */
        public static final TagKey<Item> CREEPER_REPELLENTS = tag("creeper_repellents");
        /** The item form of {@link Blocks#PILLAGER_REPELLENTS}. See {@link #CREEPER_REPELLENTS}. */
        public static final TagKey<Item> PILLAGER_REPELLENTS = tag("pillager_repellents");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
        }

        private Items() {
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
        /** Lead-rich biomes that get an extra dense band of small veins (swamps — lead leaches into the stagnant water). */
        public static final TagKey<Biome> HAS_EXTRA_LEAD_ORE = tag("has_extra_lead_ore");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
        }

        private Biomes() {
        }
    }
}

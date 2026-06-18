package com.phantomwing.theleadage.tags;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;

public final class ModTags {
    private ModTags() {
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

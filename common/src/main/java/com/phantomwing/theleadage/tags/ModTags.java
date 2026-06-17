package com.phantomwing.theleadage.tags;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ModTags {
    private ModTags() {
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

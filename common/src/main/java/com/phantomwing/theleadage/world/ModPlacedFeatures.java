package com.phantomwing.theleadage.world;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> ORE_LEAD = registerKey("ore_lead");
    public static final ResourceKey<PlacedFeature> ORE_LEAD_EXTRA = registerKey("ore_lead_extra");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // Iron-style: one smooth triangle, but pulled up into the mid/upper stone (peak ~y28, reaching
        // surface outcrops in hills) and never air-discarded — so it reads as a common, visible "dig
        // through it" ore. That keeps it clear of The Silver Age's silver, which is a deep, air-hidden
        // deepslate ore (centre ~y-10); the two only overlap down in deepslate, like real galena/silver.
        context.register(ORE_LEAD, new PlacedFeature(
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ORE_LEAD),
                List.of(
                        CountPlacement.of(10),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(80)),
                        BiomeFilter.biome())));

        // Lead-rich biomes (swamps) get this extra band on TOP of the main one: many small veins
        // biased deep, so swamp lead is both more common and reaches further into the deepslate.
        // Attached only to #theleadage:has_extra_lead_ore (see the biome modifier / Fabric ModWorldGen).
        context.register(ORE_LEAD_EXTRA, new PlacedFeature(
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ORE_LEAD_SMALL),
                List.of(
                        CountPlacement.of(12),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(16)),
                        BiomeFilter.biome())));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
    }
}

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

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // A handful of veins per chunk, concentrated in the lower overworld.
        context.register(ORE_LEAD, new PlacedFeature(
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ORE_LEAD),
                List.of(
                        CountPlacement.of(7),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(72)),
                        BiomeFilter.biome())));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
    }
}

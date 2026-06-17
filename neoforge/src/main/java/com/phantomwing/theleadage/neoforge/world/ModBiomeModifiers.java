package com.phantomwing.theleadage.neoforge.world;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.TheLeadAgeCommon;
import com.phantomwing.theleadage.tags.ModTags;
import com.phantomwing.theleadage.world.ModPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModBiomeModifiers {
    // Serializer registry (runtime).
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TheLeadAgeCommon.MOD_ID);

    public static final Supplier<MapCodec<LeadOreBiomeModifier>> LEAD_ORE =
            SERIALIZERS.register("lead_ore", () -> LeadOreBiomeModifier.CODEC);

    // The biome-modifier instance (data, emitted by ModDatapackProvider).
    public static final ResourceKey<BiomeModifier> ADD_LEAD_ORE = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(TheLeadAgeCommon.MOD_ID, "add_lead_ore"));

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(ADD_LEAD_ORE, new LeadOreBiomeModifier(
                biomes.getOrThrow(ModTags.Biomes.HAS_LEAD_ORE),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.ORE_LEAD))));
    }

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}

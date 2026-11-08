package com.phantomwing.theleadage.forge.world;

import com.mojang.serialization.Codec;
import com.phantomwing.theleadage.TheLeadAgeCommon;
import com.phantomwing.theleadage.tags.ModTags;
import com.phantomwing.theleadage.world.ModPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModBiomeModifiers {
    // Serializer registry (runtime).
    public static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TheLeadAgeCommon.MOD_ID);

    public static final Supplier<Codec<LeadOreBiomeModifier>> LEAD_ORE =
            SERIALIZERS.register("lead_ore", () -> LeadOreBiomeModifier.CODEC);

    // The biome-modifier instances (data, emitted by ModDatapackProvider).
    public static final ResourceKey<BiomeModifier> ADD_LEAD_ORE = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(TheLeadAgeCommon.MOD_ID, "add_lead_ore"));
    public static final ResourceKey<BiomeModifier> ADD_LEAD_ORE_EXTRA = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(TheLeadAgeCommon.MOD_ID, "add_lead_ore_extra"));

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        // Main band: every overworld biome.
        context.register(ADD_LEAD_ORE, new LeadOreBiomeModifier(
                biomes.getOrThrow(ModTags.Biomes.HAS_LEAD_ORE),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.ORE_LEAD))));

        // Extra dense small-vein band: only the lead-rich biomes (swamps), on top of the main band.
        context.register(ADD_LEAD_ORE_EXTRA, new LeadOreBiomeModifier(
                biomes.getOrThrow(ModTags.Biomes.HAS_EXTRA_LEAD_ORE),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.ORE_LEAD_EXTRA))));
    }

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}

package com.phantomwing.theleadage.forge.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

/**
 * Config-gated {@code AddFeatures} biome modifier. Adds the lead-ore placed
 * feature(s) to the given biomes at the {@code UNDERGROUND_ORES} step, but only
 * when {@link CommonConfig#generateLeadOre()} is true — the Forge equivalent of
 * the config guard the Fabric {@code ModWorldGen} applies (Forge biome
 * modifiers are data-driven, so a vanilla {@code AddFeaturesBiomeModifier} could
 * not honour the toggle).
 */
public record LeadOreBiomeModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features) implements BiomeModifier {
    public static final Codec<LeadOreBiomeModifier> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(LeadOreBiomeModifier::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(LeadOreBiomeModifier::features)
    ).apply(inst, LeadOreBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, BiomeModifier.Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == BiomeModifier.Phase.ADD && this.biomes.contains(biome) && CommonConfig.generateLeadOre()) {
            for (Holder<PlacedFeature> feature : this.features) {
                builder.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}

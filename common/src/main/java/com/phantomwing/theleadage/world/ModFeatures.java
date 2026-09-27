package com.phantomwing.theleadage.world;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

/**
 * 26.3 folded ConfiguredFeature into Feature: a Feature instance carries its own configuration and
 * is registered directly in {@code Registries.FEATURE}. This was {@code ModConfiguredFeatures}.
 */
public class ModFeatures {
    public static final ResourceKey<Feature> ORE_LEAD = registerKey("ore_lead");
    public static final ResourceKey<Feature> ORE_LEAD_SMALL = registerKey("ore_lead_small");

    public static void bootstrap(BootstrapContext<Feature> context) {
        RuleTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<BlockReplacement> targets = List.of(
                BlockReplacement.replace(stone, ModBlocks.LEAD_ORE.get().defaultBlockState()),
                BlockReplacement.replace(deepslate, ModBlocks.DEEPSLATE_LEAD_ORE.get().defaultBlockState()));

        // Main veins: size 8 (iron is 9); no air-discard.
        context.register(ORE_LEAD, new OreFeature(targets, 8));
        // Small veins for the lead-rich "extra" band (size 4, like iron/copper small).
        context.register(ORE_LEAD_SMALL, new OreFeature(targets, 4));
    }

    private static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
    }
}

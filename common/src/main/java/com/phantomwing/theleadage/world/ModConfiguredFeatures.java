package com.phantomwing.theleadage.world;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_LEAD = registerKey("ore_lead");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> targets = List.of(
                OreConfiguration.target(stone, ModBlocks.LEAD_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslate, ModBlocks.DEEPSLATE_LEAD_ORE.get().defaultBlockState()));

        // Vein size 8 (iron is 9); no air-discard.
        context.register(ORE_LEAD, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, 8)));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, name));
    }
}

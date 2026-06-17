package com.phantomwing.theleadage.fabric.world;

import com.phantomwing.theleadage.platform.CommonConfig;
import com.phantomwing.theleadage.tags.ModTags;
import com.phantomwing.theleadage.world.ModPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * Fabric parity for the NeoForge config-gated biome modifier. The configured/
 * placed features + biome tag are loader-agnostic generated data; the biome ->
 * feature link is loader-specific. Fabric has no data biome modifiers, so this
 * attaches the lead-ore placed feature to {@code #theleadage:has_lead_ore} biomes
 * via {@link BiomeModifications}, gated on the {@code generate_lead_ore} config
 * (read once at init — toggling it takes effect on restart).
 */
public final class ModWorldGen {
    private ModWorldGen() {
    }

    public static void register() {
        if (!CommonConfig.generateLeadOre()) {
            return;
        }
        BiomeModifications.addFeature(
                BiomeSelectors.tag(ModTags.Biomes.HAS_LEAD_ORE),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ModPlacedFeatures.ORE_LEAD);
    }
}

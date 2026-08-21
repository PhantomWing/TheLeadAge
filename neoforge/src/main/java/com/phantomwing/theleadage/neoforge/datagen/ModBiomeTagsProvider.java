package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.tags.CommonTags;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
// 1.21.6: the generic TagsProvider no longer exposes tag(); vanilla's dedicated BiomeTagsProvider
// (a KeyTagProvider) does, and it takes the mod id directly.
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TheLeadAge.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Lead ore generates in every overworld biome.
        tag(ModTags.Biomes.HAS_LEAD_ORE).addTag(BiomeTags.IS_OVERWORLD);
        // Swamps are extra rich in lead — the toxic metal leaches into the stagnant water.
        // c:is_swamp covers vanilla swamp + mangrove swamp (and any modded swamps).
        tag(ModTags.Biomes.HAS_EXTRA_LEAD_ORE).addTag(CommonTags.Biomes.IS_SWAMP);
    }
}

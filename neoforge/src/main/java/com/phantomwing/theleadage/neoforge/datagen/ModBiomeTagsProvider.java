package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.tags.CommonTags;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagsProvider extends TagsProvider<Biome> {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BIOME, lookupProvider, TheLeadAge.MOD_ID, existingFileHelper);
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

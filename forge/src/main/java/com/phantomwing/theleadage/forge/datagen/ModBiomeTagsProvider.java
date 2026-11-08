package com.phantomwing.theleadage.forge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.tags.CommonTags;
import com.phantomwing.theleadage.tags.FabricTags;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagsProvider extends TagsProvider<Biome> {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BIOME, lookupProvider, TheLeadAge.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Lead ore generates in every overworld biome.
        tag(ModTags.Biomes.HAS_LEAD_ORE).addTag(BiomeTags.IS_OVERWORLD);
        // Swamps are extra rich in lead — the toxic metal leaches into the stagnant water. Vanilla's two by name, as
        // 1.20.1 has no swamp tag of its own, and any modded ones through Forge's and Fabric's swamp tags.
        tag(ModTags.Biomes.HAS_EXTRA_LEAD_ORE)
                .add(Biomes.SWAMP, Biomes.MANGROVE_SWAMP)
                .addOptionalTag(CommonTags.Biomes.IS_SWAMP.location())
                .addOptionalTag(FabricTags.Biomes.SWAMP.location());
    }
}

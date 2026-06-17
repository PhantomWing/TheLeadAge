package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TheLeadAge.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.LEAD_ORE.get(), ModBlocks.DEEPSLATE_LEAD_ORE.get(),
                ModBlocks.RAW_LEAD_BLOCK.get(), ModBlocks.LEAD_BLOCK.get());
        tag(BlockTags.NEEDS_STONE_TOOL).add(
                ModBlocks.LEAD_ORE.get(), ModBlocks.DEEPSLATE_LEAD_ORE.get(),
                ModBlocks.RAW_LEAD_BLOCK.get(), ModBlocks.LEAD_BLOCK.get());
        tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.LEAD_BLOCK.get());
    }
}

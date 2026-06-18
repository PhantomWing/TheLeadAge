package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
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
        Block[] mineable = {
                ModBlocks.LEAD_ORE.get(), ModBlocks.DEEPSLATE_LEAD_ORE.get(),
                ModBlocks.RAW_LEAD_BLOCK.get(), ModBlocks.LEAD_BLOCK.get(),
                ModBlocks.CUT_LEAD.get(), ModBlocks.LEAD_BRICKS.get(),
                ModBlocks.LEAD_BRICK_SLAB.get(), ModBlocks.LEAD_BRICK_STAIRS.get(),
                ModBlocks.CUT_LEAD_SLAB.get(), ModBlocks.CUT_LEAD_STAIRS.get(),
                ModBlocks.CHISELED_LEAD.get(), ModBlocks.LEAD_PILLAR.get(),
                ModBlocks.LEAD_GRATE.get(),
                ModBlocks.LEAD_TRAPDOOR.get(), ModBlocks.LEAD_DOOR.get()};
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(mineable);
        tag(BlockTags.NEEDS_STONE_TOOL).add(mineable);

        tag(BlockTags.SLABS).add(ModBlocks.LEAD_BRICK_SLAB.get(), ModBlocks.CUT_LEAD_SLAB.get());
        tag(BlockTags.STAIRS).add(ModBlocks.LEAD_BRICK_STAIRS.get(), ModBlocks.CUT_LEAD_STAIRS.get());
        tag(BlockTags.DOORS).add(ModBlocks.LEAD_DOOR.get());
        tag(BlockTags.TRAPDOORS).add(ModBlocks.LEAD_TRAPDOOR.get());

        tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.LEAD_BLOCK.get());
    }
}

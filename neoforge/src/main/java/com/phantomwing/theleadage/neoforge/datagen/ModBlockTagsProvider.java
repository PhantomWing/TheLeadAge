package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
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
                ModBlocks.LEAD_BRICK_WALL.get(),
                ModBlocks.CUT_LEAD_SLAB.get(), ModBlocks.CUT_LEAD_STAIRS.get(),
                ModBlocks.CHISELED_LEAD.get(), ModBlocks.LEAD_PILLAR.get(),
                ModBlocks.LEAD_GRATE.get(),
                ModBlocks.LEAD_TRAPDOOR.get(), ModBlocks.LEAD_DOOR.get(),
                ModBlocks.LEAD_CHAIN.get(), ModBlocks.LEAD_BARS.get(), ModBlocks.LEAD_LANTERN.get(),
                ModBlocks.LEADED_GLASS_DOOR.get(), ModBlocks.LEADED_GLASS_TRAPDOOR.get(),
                ModBlocks.LEAD_WEIGHT.get(), ModBlocks.CHIPPED_LEAD_WEIGHT.get(),
                ModBlocks.DAMAGED_LEAD_WEIGHT.get()};
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(mineable);
        tag(BlockTags.NEEDS_STONE_TOOL).add(mineable);

        tag(BlockTags.SLABS).addTag(ModTags.Blocks.LEAD_SLABS);
        tag(BlockTags.STAIRS).addTag(ModTags.Blocks.LEAD_STAIRS);
        // Walls connect to each other through #minecraft:walls.
        tag(BlockTags.WALLS).addTag(ModTags.Blocks.LEAD_WALLS);
        tag(ModTags.Blocks.LEAD_DOORS).add(ModBlocks.LEAD_DOOR.get());
        tag(ModTags.Blocks.LEAD_TRAPDOORS).add(ModBlocks.LEAD_TRAPDOOR.get());
        // Lead doors via the reusable tag; the leaded glass door/trapdoor belong to the glass family.
        tag(BlockTags.DOORS).addTag(ModTags.Blocks.LEAD_DOORS).add(ModBlocks.LEADED_GLASS_DOOR.get());
        tag(BlockTags.TRAPDOORS).addTag(ModTags.Blocks.LEAD_TRAPDOORS).add(ModBlocks.LEADED_GLASS_TRAPDOOR.get());

        tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.LEAD_BLOCK.get());

        // A standing lead torch on a wall forces the wall's centre post, like a vanilla torch
        // (vanilla lists only the standing torches here, not the wall variants).
        tag(BlockTags.WALL_POST_OVERRIDE).add(ModBlocks.LEAD_TORCH.get());

        // The lead building set, grouped into sub-tags rolled up under #lead_blocks.
        tag(ModTags.Blocks.SOLID_LEAD_BLOCKS).add(
                ModBlocks.LEAD_BLOCK.get(), ModBlocks.CUT_LEAD.get(), ModBlocks.LEAD_BRICKS.get(),
                ModBlocks.CHISELED_LEAD.get(), ModBlocks.LEAD_PILLAR.get());
        tag(ModTags.Blocks.LEAD_SLABS).add(ModBlocks.LEAD_BRICK_SLAB.get(), ModBlocks.CUT_LEAD_SLAB.get());
        tag(ModTags.Blocks.LEAD_STAIRS).add(ModBlocks.LEAD_BRICK_STAIRS.get(), ModBlocks.CUT_LEAD_STAIRS.get());
        tag(ModTags.Blocks.LEAD_WALLS).add(ModBlocks.LEAD_BRICK_WALL.get());
        tag(ModTags.Blocks.LEAD_BLOCKS)
                .addTag(ModTags.Blocks.SOLID_LEAD_BLOCKS)
                .addTag(ModTags.Blocks.LEAD_SLABS)
                .addTag(ModTags.Blocks.LEAD_STAIRS)
                .addTag(ModTags.Blocks.LEAD_WALLS);

        // Lead is dense, so the building set blocks vibrations passing through (occludes_vibration_signals)
        // like wool — but unlike wool it is NOT in dampens_vibrations, so footsteps on lead still emit a
        // detectable vibration. NOTE: the vanilla occlusion is shape-blind, so even a half-slab or a single
        // stair step will fully block sound (not just double-slabs / full arrangements).
        tag(BlockTags.OCCLUDES_VIBRATION_SIGNALS).addTag(ModTags.Blocks.LEAD_BLOCKS);

        // Full leaded glass blocks (clear + stained, no panes), grouped like vanilla glass.
        tag(ModTags.Blocks.LEADED_GLASS_BLOCKS).add(ModBlocks.LEADED_GLASS.get());
        for (DyeColor color : DyeColor.values()) {
            tag(ModTags.Blocks.LEADED_GLASS_BLOCKS).add(ModBlocks.STAINED_LEADED_GLASS.get(color).get());
        }

        Block[] leadedGlassPanes = {
                ModBlocks.LEADED_GLASS_PANEL.get(),
                ModBlocks.LEADED_GLASS_PANE_SPLIT.get(), ModBlocks.LEADED_GLASS_PANE_PLUS.get(),
                ModBlocks.LEADED_GLASS_PANE_GRID.get(),
                ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(), ModBlocks.LEADED_GLASS_PANE_CROSS.get(),
                ModBlocks.LEADED_GLASS_PANE_DIAMOND.get(), ModBlocks.LEADED_GLASS_PANE_LATTICE.get(),
                ModBlocks.LEADED_GLASS_PANE_BARS.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS.get()};

        // Dense lead and the full leaded glass blocks (like vanilla glass) are impermeable: no water
        // droplets drip through, and they seal cleanly against water. NOTE: panes are deliberately NOT
        // here — vanilla glass panes aren't impermeable either. Panes stop rain by blocking motion
        // (MOTION_BLOCKING heightmap) via paneProps().forceSolidOn(), not through this tag.
        tag(BlockTags.IMPERMEABLE)
                .addTag(ModTags.Blocks.LEAD_BLOCKS)
                .addTag(ModTags.Blocks.LEADED_GLASS_BLOCKS);

        // Leaded glass + the panes (incl. all dyed glass) need a pickaxe to drop, but any
        // tier (fragile like glass), so they're only mineable/pickaxe — not NEEDS_STONE_TOOL.
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.LEADED_GLASS.get()).add(leadedGlassPanes);
        for (DyeColor color : DyeColor.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.STAINED_LEADED_GLASS.get(color).get());
        }
    }
}

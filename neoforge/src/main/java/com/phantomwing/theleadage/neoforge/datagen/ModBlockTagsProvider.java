package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    // Sable (Create Aeronautics' weight/volume system). Sable is not a compile-time dependency, so
    // its tags are referenced by id; the JSONs merge additively with Sable's own when it is installed
    // and are inert otherwise.
    private static final TagKey<Block> SABLE_HEAVY = sable("heavy");
    private static final TagKey<Block> SABLE_LIGHT = sable("light");
    private static final TagKey<Block> SABLE_SUPER_LIGHT = sable("super_light");
    private static final TagKey<Block> SABLE_HALF_VOLUME = sable("half_volume");
    private static final TagKey<Block> SABLE_QUARTER_VOLUME = sable("quarter_volume");

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TheLeadAge.MOD_ID);
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
                ModBlocks.LEAD_BULB.get(),
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

        // Creepers and pillagers both give burning lead a wide berth (see LeadFumeRepellent) — the
        // torches and the lantern alike. The lantern is enclosed, so it never doses players with fumes,
        // but it is still a lead flame and they still want nothing to do with it. The two tags are kept
        // separate so a pack can scare one mob without scaring the other.
        Block[] burningLead = {
                ModBlocks.LEAD_TORCH.get(), ModBlocks.LEAD_WALL_TORCH.get(), ModBlocks.LEAD_LANTERN.get()};
        tag(ModTags.Blocks.CREEPER_REPELLENTS).add(burningLead);
        tag(ModTags.Blocks.PILLAGER_REPELLENTS).add(burningLead);

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
        tag(ModTags.Blocks.LEAD_WEIGHTS).add(
                ModBlocks.LEAD_WEIGHT.get(), ModBlocks.CHIPPED_LEAD_WEIGHT.get(),
                ModBlocks.DAMAGED_LEAD_WEIGHT.get());
        tag(ModTags.Blocks.LEAD_LATTICE).add(ModBlocks.LEAD_GRATE.get(), ModBlocks.LEAD_BARS.get());

        addCommonTags();
        addSableTags();

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

    /**
     * Conventional ({@code c:}) <b>block</b> tags. The item side already had these; the block side had
     * none, which quietly cut lead out of everything keyed on block tags — including Sable, which
     * classifies {@code #c:storage_blocks} as heavy.
     */
    private void addCommonTags() {
        tag(c("ores")).add(ModBlocks.LEAD_ORE.get(), ModBlocks.DEEPSLATE_LEAD_ORE.get());
        tag(c("ores/lead")).add(ModBlocks.LEAD_ORE.get(), ModBlocks.DEEPSLATE_LEAD_ORE.get());
        tag(c("ores_in_ground/stone")).add(ModBlocks.LEAD_ORE.get());
        tag(c("ores_in_ground/deepslate")).add(ModBlocks.DEEPSLATE_LEAD_ORE.get());
        // Both ores drop exactly one raw lead (createOreDrop).
        tag(c("ore_rates/singular")).add(ModBlocks.LEAD_ORE.get(), ModBlocks.DEEPSLATE_LEAD_ORE.get());

        tag(c("storage_blocks")).add(ModBlocks.LEAD_BLOCK.get(), ModBlocks.RAW_LEAD_BLOCK.get());
        tag(c("storage_blocks/lead")).add(ModBlocks.LEAD_BLOCK.get());
        tag(c("storage_blocks/raw_lead")).add(ModBlocks.RAW_LEAD_BLOCK.get());
    }

    /**
     * Sable (Create Aeronautics' weight/volume system) compatibility.
     *
     * <p>Sable classifies plenty of this mod already, by referencing vanilla aggregation tags:
     * {@code #minecraft:doors} / {@code #minecraft:trapdoors} are {@code sable:light} +
     * {@code super_light} + {@code quarter_volume}; {@code #minecraft:slabs} / {@code #minecraft:stairs}
     * are {@code sable:light} + {@code half_volume}; {@code #c:storage_blocks} is {@code sable:heavy}.
     * So the lead doors/trapdoors and slabs/stairs, and (now that the block tag exists) the lead and
     * raw lead blocks, are already classified. Nothing here re-states those — a block sitting in two
     * different weight classes at once has no defined meaning.</p>
     *
     * <p>What's left are the gaps. Lead is dense, so every full-mass solid lead block is
     * {@code heavy} — including the walls, at half volume for their post shape, and the lead weights,
     * which are small but solid lead (a quarter volume of heavy, the densest thing in the mod). The
     * open-work lead — the grate and the bars — is mostly air, so it stays light despite the metal:
     * a quarter volume at {@code light} rather than Sable's {@code super_light} for iron bars, since
     * lead outweighs iron for the same shape. The chain and lantern are thin trim, so {@code super_light}.</p>
     *
     * <p>Generated unconditionally: without Sable the JSONs are inert, and with it they merge
     * additively (no {@code "replace"}).</p>
     */
    private void addSableTags() {
        tag(SABLE_HEAVY)
                .addTag(ModTags.Blocks.SOLID_LEAD_BLOCKS)
                .addTag(ModTags.Blocks.LEAD_WALLS)
                .addTag(ModTags.Blocks.LEAD_WEIGHTS)
                .add(ModBlocks.RAW_LEAD_BLOCK.get());

        // A wall is a post, not a full cube.
        tag(SABLE_HALF_VOLUME).addTag(ModTags.Blocks.LEAD_WALLS);

        // Solid lead, but only an 8x8x8 ball of it.
        tag(SABLE_QUARTER_VOLUME).addTag(ModTags.Blocks.LEAD_WEIGHTS);

        // Open-work lead: heavier than iron bars, but still mostly air.
        tag(SABLE_LIGHT).addTag(ModTags.Blocks.LEAD_LATTICE);
        tag(SABLE_QUARTER_VOLUME).addTag(ModTags.Blocks.LEAD_LATTICE);

        // Thin trim.
        tag(SABLE_SUPER_LIGHT).add(ModBlocks.LEAD_CHAIN.get(), ModBlocks.LEAD_LANTERN.get());
        tag(SABLE_QUARTER_VOLUME).add(ModBlocks.LEAD_CHAIN.get(), ModBlocks.LEAD_LANTERN.get());
    }

    private static TagKey<Block> c(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static TagKey<Block> sable(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sable", path));
    }
}

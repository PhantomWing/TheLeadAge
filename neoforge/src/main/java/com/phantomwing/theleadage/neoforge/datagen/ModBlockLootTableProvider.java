package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.item.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), lookupProvider);
    }

    @Override
    protected void generate() {
        // Ores drop raw lead (silk touch -> ore block; fortune bonus applies).
        add(ModBlocks.LEAD_ORE.get(), b -> createOreDrop(b, ModItems.RAW_LEAD.get()));
        add(ModBlocks.DEEPSLATE_LEAD_ORE.get(), b -> createOreDrop(b, ModItems.RAW_LEAD.get()));

        dropSelf(ModBlocks.RAW_LEAD_BLOCK.get());
        dropSelf(ModBlocks.LEAD_BLOCK.get());

        // Decorative blocks.
        dropSelf(ModBlocks.CUT_LEAD.get());
        dropSelf(ModBlocks.LEAD_BRICKS.get());
        dropSelf(ModBlocks.LEAD_BRICK_STAIRS.get());
        dropSelf(ModBlocks.CUT_LEAD_STAIRS.get());
        dropSelf(ModBlocks.CHISELED_LEAD.get());
        dropSelf(ModBlocks.LEAD_PILLAR.get());
        dropSelf(ModBlocks.LEAD_GRATE.get());
        dropSelf(ModBlocks.LEAD_TRAPDOOR.get());
        // Slabs drop two when double; doors only drop once (from the lower half).
        add(ModBlocks.LEAD_BRICK_SLAB.get(), this::createSlabItemTable);
        add(ModBlocks.CUT_LEAD_SLAB.get(), this::createSlabItemTable);
        add(ModBlocks.LEAD_DOOR.get(), this::createDoorTable);
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (RegistrySupplier<Block> entry : ModBlocks.BLOCKS) {
            blocks.add(entry.get());
        }
        return blocks;
    }
}

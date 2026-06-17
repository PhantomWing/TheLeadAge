package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TheLeadAge.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.LEAD_ORE.get(), cubeAll(ModBlocks.LEAD_ORE.get()));
        simpleBlockWithItem(ModBlocks.DEEPSLATE_LEAD_ORE.get(), cubeAll(ModBlocks.DEEPSLATE_LEAD_ORE.get()));
        simpleBlockWithItem(ModBlocks.RAW_LEAD_BLOCK.get(), cubeAll(ModBlocks.RAW_LEAD_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.LEAD_BLOCK.get(), cubeAll(ModBlocks.LEAD_BLOCK.get()));
    }
}

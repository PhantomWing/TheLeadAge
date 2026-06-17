package com.phantomwing.theleadage.block;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadOreBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(TheLeadAge.MOD_ID, Registries.BLOCK);

    // Lead ore generates like iron, drops no experience, and emits a brief
    // Nausea ("lead fumes") effect when mined for its raw drops (see LeadOreBlock).
    public static final RegistrySupplier<Block> LEAD_ORE = register("lead_ore", () ->
            new LeadOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final RegistrySupplier<Block> DEEPSLATE_LEAD_ORE = register("deepslate_lead_ore", () ->
            new LeadOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));

    public static final RegistrySupplier<Block> RAW_LEAD_BLOCK = register("raw_lead_block", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistrySupplier<Block> LEAD_BLOCK = register("lead_block", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_GRAY)));

    private static <T extends Block> RegistrySupplier<T> register(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static void register() {
        BLOCKS.register();
    }
}

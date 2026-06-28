package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadWeightBlock;
import com.phantomwing.theleadage.block.custom.HorizontalFacingBlock;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
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

        // Decorative lead blocks.
        simpleBlockWithItem(ModBlocks.CUT_LEAD.get(), cubeAll(ModBlocks.CUT_LEAD.get()));
        simpleBlockWithItem(ModBlocks.LEAD_BRICKS.get(), cubeAll(ModBlocks.LEAD_BRICKS.get()));
        slab(ModBlocks.LEAD_BRICK_SLAB, ModBlocks.LEAD_BRICKS);
        stairs(ModBlocks.LEAD_BRICK_STAIRS, ModBlocks.LEAD_BRICKS);
        slab(ModBlocks.CUT_LEAD_SLAB, ModBlocks.CUT_LEAD);
        stairs(ModBlocks.CUT_LEAD_STAIRS, ModBlocks.CUT_LEAD);
        chiseled(ModBlocks.CHISELED_LEAD);
        pillar(ModBlocks.LEAD_PILLAR);
        cutoutCube(ModBlocks.LEAD_GRATE);
        trapdoor(ModBlocks.LEAD_TRAPDOOR);
        trapdoor(ModBlocks.LEADED_GLASS_TRAPDOOR);
        door(ModBlocks.LEAD_DOOR);
        // The glass door reuses the vanilla door models with its overlay textures (cutout, so the
        // top half's window is transparent); the glass itself is drawn by the block-entity renderer.
        door(ModBlocks.LEADED_GLASS_DOOR);

        leadWeight();

        // Leaded glass blocks. Plain = cutout (like vanilla glass), dyed = translucent
        // (like vanilla stained glass). The leaded_glass_pane blockstate is hand-authored.
        cutoutCube(ModBlocks.LEADED_GLASS);
        for (DyeColor color : DyeColor.values()) {
            translucentCube(ModBlocks.STAINED_LEADED_GLASS.get(color));
        }
    }

    private void leadWeight() {
        // One blockstate per tier; each references its own hand-authored models (the chipped/damaged
        // models just re-texture the base via parent). HANGING picks the chained variant.
        leadWeightTier(ModBlocks.LEAD_WEIGHT, "lead_weight");
        leadWeightTier(ModBlocks.CHIPPED_LEAD_WEIGHT, "chipped_lead_weight");
        leadWeightTier(ModBlocks.DAMAGED_LEAD_WEIGHT, "damaged_lead_weight");
    }

    private void leadWeightTier(RegistrySupplier<Block> block, String name) {
        ModelFile weight = models().getExistingFile(modLoc("block/" + name));
        ModelFile hanging = models().getExistingFile(modLoc("block/" + name + "_hanging"));
        getVariantBuilder(block.get())
                .partialState().with(LeadWeightBlock.HANGING, false).setModels(new ConfiguredModel(weight))
                .partialState().with(LeadWeightBlock.HANGING, true).setModels(new ConfiguredModel(hanging));
    }

    private void translucentCube(RegistrySupplier<Block> block) {
        ModelFile model = models().cubeAll(blockName(block), blockTexture(block.get())).renderType(RenderType.translucent().name);
        getVariantBuilder(block.get()).partialState().setModels(new ConfiguredModel(model));
    }

    private void glassPane(RegistrySupplier<Block> block, String bodyTexture, String renderType) {
        paneBlockWithRenderType((IronBarsBlock) block.get(),
                modLoc("block/" + bodyTexture), modLoc("block/leaded_glass_pane_top"), renderType);
    }

    private void slab(RegistrySupplier<SlabBlock> slab, RegistrySupplier<Block> parentBlock) {
        ResourceLocation texture = blockTexture(parentBlock.get());
        slabBlock(slab.get(), texture, texture);
    }

    private void stairs(RegistrySupplier<StairBlock> stairs, RegistrySupplier<Block> parentBlock) {
        stairsBlock(stairs.get(), blockTexture(parentBlock.get()));
    }

    private void chiseled(RegistrySupplier<HorizontalFacingBlock> block) {
        horizontalBlock(block.get(), cubeAll(block.get()));
    }

    private void pillar(RegistrySupplier<RotatedPillarBlock> block) {
        String name = blockName(block);
        axisBlock(block.get(), modLoc("block/" + name), modLoc("block/" + name + "_top"));
    }

    private void cutoutCube(RegistrySupplier<Block> block) {
        // Cube rendered with the cutout render type (transparent like glass / the grate).
        ModelFile model = models().cubeAll(blockName(block), blockTexture(block.get())).renderType(RenderType.cutout().name);
        getVariantBuilder(block.get()).partialState().setModels(new ConfiguredModel(model));
    }

    private void door(RegistrySupplier<? extends Block> doorBlock) {
        doorBlockWithRenderType((DoorBlock) doorBlock.get(),
                modLoc("block/" + blockName(doorBlock) + "_bottom"),
                modLoc("block/" + blockName(doorBlock) + "_top"),
                RenderType.cutout().name);
    }

    private void trapdoor(RegistrySupplier<? extends Block> trapdoor) {
        trapdoorBlockWithRenderType((TrapDoorBlock) trapdoor.get(),
                modLoc("block/" + blockName(trapdoor)),
                true,
                RenderType.cutout().name);
    }

    private static String blockName(RegistrySupplier<? extends Block> block) {
        return block.getId().getPath();
    }
}

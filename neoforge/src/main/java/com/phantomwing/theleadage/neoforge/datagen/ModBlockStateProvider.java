package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadWeightBlock;
import com.phantomwing.theleadage.block.custom.HorizontalFacingBlock;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
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

        leadChain();
        leadBars();
        leadWeight();

        // Leaded glass blocks. Plain = cutout (like vanilla glass), dyed = translucent
        // (like vanilla stained glass).
        cutoutCube(ModBlocks.LEADED_GLASS);
        for (DyeColor color : DyeColor.values()) {
            translucentCube(ModBlocks.STAINED_LEADED_GLASS.get(color));
        }

        leadedGlassPanes();
    }

    // Vanilla chain models/blockstate with the lead chain texture.
    private void leadChain() {
        ModelFile chain = models().withExistingParent("lead_chain", mcLoc("block/chain"))
                .renderType(RenderType.cutout().name)
                .texture("particle", modLoc("block/lead_chain"))
                .texture("all", modLoc("block/lead_chain"));
        getVariantBuilder(ModBlocks.LEAD_CHAIN.get())
                .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.X)
                .setModels(new ConfiguredModel(chain, 90, 90, false))
                .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                .setModels(new ConfiguredModel(chain))
                .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.Z)
                .setModels(new ConfiguredModel(chain, 90, 0, false));
    }

    // Vanilla iron-bars models/blockstate (post/cap/side multipart) with the lead bars texture.
    private void leadBars() {
        ModelFile postEnds = barsModel("lead_bars_post_ends");
        ModelFile post = barsModel("lead_bars_post");
        ModelFile cap = barsModel("lead_bars_cap");
        ModelFile capAlt = barsModel("lead_bars_cap_alt");
        ModelFile side = barsModel("lead_bars_side");
        ModelFile sideAlt = barsModel("lead_bars_side_alt");
        getMultipartBuilder(ModBlocks.LEAD_BARS.get())
                .part().modelFile(postEnds).addModel().end()
                .part().modelFile(post).addModel()
                .condition(CrossCollisionBlock.NORTH, false).condition(CrossCollisionBlock.EAST, false)
                .condition(CrossCollisionBlock.SOUTH, false).condition(CrossCollisionBlock.WEST, false).end()
                .part().modelFile(cap).addModel()
                .condition(CrossCollisionBlock.NORTH, true).condition(CrossCollisionBlock.EAST, false)
                .condition(CrossCollisionBlock.SOUTH, false).condition(CrossCollisionBlock.WEST, false).end()
                .part().modelFile(cap).rotationY(90).addModel()
                .condition(CrossCollisionBlock.NORTH, false).condition(CrossCollisionBlock.EAST, true)
                .condition(CrossCollisionBlock.SOUTH, false).condition(CrossCollisionBlock.WEST, false).end()
                .part().modelFile(capAlt).addModel()
                .condition(CrossCollisionBlock.NORTH, false).condition(CrossCollisionBlock.EAST, false)
                .condition(CrossCollisionBlock.SOUTH, true).condition(CrossCollisionBlock.WEST, false).end()
                .part().modelFile(capAlt).rotationY(90).addModel()
                .condition(CrossCollisionBlock.NORTH, false).condition(CrossCollisionBlock.EAST, false)
                .condition(CrossCollisionBlock.SOUTH, false).condition(CrossCollisionBlock.WEST, true).end()
                .part().modelFile(side).addModel().condition(CrossCollisionBlock.NORTH, true).end()
                .part().modelFile(side).rotationY(90).addModel().condition(CrossCollisionBlock.EAST, true).end()
                .part().modelFile(sideAlt).addModel().condition(CrossCollisionBlock.SOUTH, true).end()
                .part().modelFile(sideAlt).rotationY(90).addModel().condition(CrossCollisionBlock.WEST, true).end();
    }

    private ModelFile barsModel(String name) {
        return models().withExistingParent(name, mcLoc("block/iron_" + name.substring("lead_".length())))
                .renderType(RenderType.cutout().name)
                .texture("particle", modLoc("block/lead_bars"))
                .texture("bars", modLoc("block/lead_bars"))
                .texture("edge", modLoc("block/lead_bars"));
    }

    private void leadWeight() {
        // The base tier's models are hand-authored; the chipped/damaged tiers just re-texture
        // them via parent models. HANGING picks the chained variant.
        ModelFile weight = models().getExistingFile(modLoc("block/lead_weight"));
        ModelFile hanging = models().getExistingFile(modLoc("block/lead_weight_hanging"));
        leadWeightTier(ModBlocks.LEAD_WEIGHT, weight, hanging);
        leadWeightTier(ModBlocks.CHIPPED_LEAD_WEIGHT,
                retexturedWeight("chipped_lead_weight", "lead_weight"),
                retexturedWeight("chipped_lead_weight_hanging", "lead_weight_hanging"));
        leadWeightTier(ModBlocks.DAMAGED_LEAD_WEIGHT,
                retexturedWeight("damaged_lead_weight", "lead_weight"),
                retexturedWeight("damaged_lead_weight_hanging", "lead_weight_hanging"));
    }

    /** A weight model re-texturing a base-tier parent; the texture is the tier name without the hanging suffix. */
    private ModelFile retexturedWeight(String name, String parent) {
        String texture = name.endsWith("_hanging") ? name.substring(0, name.length() - "_hanging".length()) : name;
        return models().withExistingParent(name, modLoc("block/" + parent))
                .renderType(RenderType.cutout().name)
                .texture("particle", modLoc("block/" + texture))
                .texture("all", modLoc("block/" + texture));
    }

    private void leadWeightTier(RegistrySupplier<Block> block, ModelFile weight, ModelFile hanging) {
        getVariantBuilder(block.get())
                .partialState().with(LeadWeightBlock.HANGING, false).setModels(new ConfiguredModel(weight))
                .partialState().with(LeadWeightBlock.HANGING, true).setModels(new ConfiguredModel(hanging));
    }

    // Leaded glass panes: the came geometry lives in hand-authored element models; each pattern's
    // clear_N states pick a generated re-texture of that base model (clear regions swap the tinted
    // white glass texture for the untinted clear one). The blockstate rotates the wall-north model
    // to every face/facing.
    private void leadedGlassPanes() {
        paneVariants(ModBlocks.LEADED_GLASS_PANEL,
                paneFamily("leaded_glass_pane_plain", "leaded_glass",
                        new String[]{"glass"}, new String[]{null, ""}));
        paneVariants(ModBlocks.LEADED_GLASS_PANE_SPLIT,
                paneFamily("leaded_glass_pane_split_h", "leaded_glass_split_h",
                        new String[]{"glass_left", "glass_right"}, new String[]{null, "_left", "_right", "_both"}),
                paneFamily("leaded_glass_pane_split_v", "leaded_glass_split_v",
                        new String[]{"glass_top", "glass_bottom"}, new String[]{null, "_top", "_bottom", "_both"}));
        paneVariants(ModBlocks.LEADED_GLASS_PANE_GRID,
                paneFamily("leaded_glass_pane_grid", "leaded_glass_grid", regionKeys(4), numericClearNames(4)));
        paneVariants(ModBlocks.LEADED_GLASS_PANE_CROSS,
                paneFamily("leaded_glass_pane_cross", "leaded_glass_cross", regionKeys(4), numericClearNames(4)));
        paneVariants(ModBlocks.LEADED_GLASS_PANE_DIAGONAL,
                paneFamily("leaded_glass_pane_diagonal_a", "leaded_glass_diagonal_a", regionKeys(2), numericClearNames(2)),
                paneFamily("leaded_glass_pane_diagonal_b", "leaded_glass_diagonal_b", regionKeys(2), numericClearNames(2)));
        gridPane3();
    }

    /**
     * The models for one pane pattern (one orientation), indexed by the bitmask of clear regions:
     * index 0 is the hand-authored base model, every other subset gets a generated child model
     * that re-textures just those regions to {@code clearTexture}. {@code clearNames[mask]} is the
     * model-name suffix after {@code <base>_clear} for that subset.
     */
    private ModelFile[] paneFamily(String base, String clearTexture, String[] regionKeys, String[] clearNames) {
        ModelFile[] byMask = new ModelFile[1 << regionKeys.length];
        byMask[0] = models().getExistingFile(modLoc("block/" + base));
        for (int mask = 1; mask < byMask.length; mask++) {
            BlockModelBuilder model = models().withExistingParent(base + "_clear" + clearNames[mask], modLoc("block/" + base));
            for (int i = 0; i < regionKeys.length; i++) {
                if ((mask & (1 << i)) != 0) {
                    model.texture(regionKeys[i], modLoc("block/" + clearTexture));
                }
            }
            byMask[mask] = model;
        }
        return byMask;
    }

    private static String[] regionKeys(int regions) {
        String[] keys = new String[regions];
        for (int i = 0; i < regions; i++) {
            keys[i] = "glass_" + i;
        }
        return keys;
    }

    /** "_013"-style model-name suffixes (the clear region indices, ascending), indexed by clear bitmask. */
    private static String[] numericClearNames(int regions) {
        String[] names = new String[1 << regions];
        for (int mask = 1; mask < names.length; mask++) {
            StringBuilder name = new StringBuilder("_");
            for (int i = 0; i < regions; i++) {
                if ((mask & (1 << i)) != 0) {
                    name.append(i);
                }
            }
            names[mask] = name.toString();
        }
        return names;
    }

    /** Variants for a pane block: orientation picks the model family, the clear bitmask the model. */
    private void paneVariants(RegistrySupplier<Block> blockSupplier, ModelFile[]... familiesByOrientation) {
        LeadedGlassPaneBlock block = (LeadedGlassPaneBlock) blockSupplier.get();
        IntegerProperty orientation = block.cameType().orientation;
        int regions = block.cameType().regions;
        getVariantBuilder(block).forAllStates(state -> {
            int family = orientation != null ? state.getValue(orientation) : 0;
            int mask = 0;
            for (int i = 0; i < regions; i++) {
                if (state.getValue(LeadedGlassPaneBlock.CLEAR[i])) {
                    mask |= 1 << i;
                }
            }
            AttachFace face = state.getValue(LeadedGlassPaneBlock.FACE);
            Direction facing = state.getValue(LeadedGlassPaneBlock.FACING);
            return ConfiguredModel.builder()
                    .modelFile(familiesByOrientation[family][mask])
                    .rotationX(paneXRot(face))
                    .rotationY(paneYRot(face, facing))
                    .build();
        });
    }

    // The 3x3 grid would need 2^9 combined models, so it is a multipart instead: the came frame
    // plus one part per cell, each cell picking its plain (hand-authored) or clear (a generated
    // re-texture of it) model.
    private void gridPane3() {
        ModelFile came = models().getExistingFile(modLoc("block/leaded_glass_pane_grid_3_came"));
        ModelFile[][] cells = new ModelFile[9][2];
        for (int cell = 0; cell < 9; cell++) {
            String name = "leaded_glass_pane_grid_3_cell_" + cell;
            cells[cell][0] = models().getExistingFile(modLoc("block/" + name));
            cells[cell][1] = models().withExistingParent(name + "_clear", modLoc("block/" + name))
                    .texture("glass", modLoc("block/leaded_glass_grid_3"));
        }
        MultiPartBlockStateBuilder builder = getMultipartBuilder(ModBlocks.LEADED_GLASS_PANE_GRID_3.get());
        for (AttachFace face : AttachFace.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                int xRot = paneXRot(face);
                int yRot = paneYRot(face, facing);
                builder.part().modelFile(came).rotationX(xRot).rotationY(yRot).addModel()
                        .condition(LeadedGlassPaneBlock.FACE, face)
                        .condition(LeadedGlassPaneBlock.FACING, facing).end();
                for (int cell = 0; cell < 9; cell++) {
                    for (boolean clear : new boolean[]{false, true}) {
                        builder.part().modelFile(cells[cell][clear ? 1 : 0]).rotationX(xRot).rotationY(yRot).addModel()
                                .condition(LeadedGlassPaneBlock.FACE, face)
                                .condition(LeadedGlassPaneBlock.FACING, facing)
                                .condition(LeadedGlassPaneBlock.CLEAR[cell], clear).end();
                    }
                }
            }
        }
    }

    private static int paneXRot(AttachFace face) {
        return switch (face) {
            case WALL -> 0;
            case FLOOR -> 270;
            case CEILING -> 90;
        };
    }

    private static int paneYRot(AttachFace face, Direction facing) {
        int yRot = switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0; // NORTH
        };
        return switch (face) {
            case WALL -> yRot;
            case FLOOR -> (yRot + 90) % 360;
            case CEILING -> (yRot + 270) % 360;
        };
    }

    private void translucentCube(RegistrySupplier<Block> block) {
        ModelFile model = models().cubeAll(blockName(block), blockTexture(block.get())).renderType(RenderType.translucent().name);
        getVariantBuilder(block.get()).partialState().setModels(new ConfiguredModel(model));
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

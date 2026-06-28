package com.phantomwing.theleadage.block;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadWeightBlock;
import com.phantomwing.theleadage.block.custom.HorizontalFacingBlock;
import com.phantomwing.theleadage.block.custom.LeadedGlassDoorBlock;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.block.custom.LeadedGlassTrapdoorBlock;
import com.phantomwing.theleadage.block.custom.LeadOreBlock;
import com.phantomwing.theleadage.sound.ModSoundTypes;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
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

    // Decorative lead blocks (no oxidation). Order matches the creative tab.
    public static final RegistrySupplier<Block> CUT_LEAD = registerLeadBlock("cut_lead");
    public static final RegistrySupplier<Block> LEAD_BRICKS = registerLeadBlock("lead_bricks");
    public static final RegistrySupplier<SlabBlock> LEAD_BRICK_SLAB = registerLeadSlab("lead_brick_slab");
    public static final RegistrySupplier<StairBlock> LEAD_BRICK_STAIRS = registerLeadStairs("lead_brick_stairs");
    public static final RegistrySupplier<SlabBlock> CUT_LEAD_SLAB = registerLeadSlab("cut_lead_slab");
    public static final RegistrySupplier<StairBlock> CUT_LEAD_STAIRS = registerLeadStairs("cut_lead_stairs");
    public static final RegistrySupplier<HorizontalFacingBlock> CHISELED_LEAD = registerLeadChiseled("chiseled_lead");
    public static final RegistrySupplier<RotatedPillarBlock> LEAD_PILLAR = registerLeadPillar("lead_pillar");
    public static final RegistrySupplier<Block> LEAD_GRATE = registerLeadGrate("lead_grate");
    public static final RegistrySupplier<TrapDoorBlock> LEAD_TRAPDOOR = registerLeadTrapdoor("lead_trapdoor");
    public static final RegistrySupplier<DoorBlock> LEAD_DOOR = registerLeadDoor("lead_door");
    public static final RegistrySupplier<RotatedPillarBlock> LEAD_CHAIN = registerLeadChain("lead_chain");
    public static final RegistrySupplier<IronBarsBlock> LEAD_BARS = registerLeadBars("lead_bars");

    // Leaded glass: renders/behaves exactly like glass, but requires a pickaxe to
    // drop itself (requiresCorrectToolForDrops + the mineable/pickaxe tag); broken
    // by hand it still shatters, just without a drop. Keeps the glass sound.
    public static final RegistrySupplier<Block> LEADED_GLASS = register("leaded_glass", () ->
            new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).requiresCorrectToolForDrops()));

    // Leaded glass panes — one static block per came type (LeadedGlassPaneBlock.CameType). Colours
    // live on the block entity (tinted); split is orientable (sneak-right-click toggles h/v).
    public static final RegistrySupplier<Block> LEADED_GLASS_PANEL = register("leaded_glass_pane", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.PLAIN, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_SPLIT = register("leaded_glass_pane_split", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.SPLIT, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_GRID = register("leaded_glass_pane_grid", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.GRID, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_GRID_3 = register("leaded_glass_pane_grid_3", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.GRID_3, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_DIAGONAL = register("leaded_glass_pane_diagonal", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.DIAGONAL, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_CROSS = register("leaded_glass_pane_cross", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.CROSS, paneProps()));

    private static BlockBehaviour.Properties paneProps() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).requiresCorrectToolForDrops().noOcclusion();
    }

    /** The pane block for a came frame (the orientations of a came type share one block). */
    public static Block paneBlockFor(com.phantomwing.theleadage.block.custom.LeadedGlassFrame frame) {
        return switch (frame) {
            case PLAIN -> LEADED_GLASS_PANEL.get();
            case SPLIT_H, SPLIT_V -> LEADED_GLASS_PANE_SPLIT.get();
            case GRID -> LEADED_GLASS_PANE_GRID.get();
            case GRID_3 -> LEADED_GLASS_PANE_GRID_3.get();
            case DIAGONAL_A, DIAGONAL_B -> LEADED_GLASS_PANE_DIAGONAL.get();
            case CROSS -> LEADED_GLASS_PANE_CROSS.get();
        };
    }

    // A lead door whose top half is a configurable leaded glass pane (design on its block entity,
    // drawn by a renderer). See LeadedGlassDoorBlock.
    public static final RegistrySupplier<Block> LEADED_GLASS_DOOR = register("leaded_glass_door", () ->
            new LeadedGlassDoorBlock(ModBlockSetTypes.LEADED_GLASS,
                    leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR)).noOcclusion()));

    // A lead trapdoor whose flap is a configurable leaded glass pane. See LeadedGlassTrapdoorBlock.
    public static final RegistrySupplier<Block> LEADED_GLASS_TRAPDOOR = register("leaded_glass_trapdoor", () ->
            new LeadedGlassTrapdoorBlock(ModBlockSetTypes.LEADED_GLASS,
                    leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_TRAPDOOR)).noOcclusion()));

    // Lead Weight: an 8³ lead ball that falls like an anvil and crushes entities (combat logic in
    // LeadWeightEntity); hangs from a chain when placed under a block. A hard landing can chip it
    // down a tier (lead_weight -> chipped -> damaged -> shatters), anvil-style. See nextWeightTier.
    public static final RegistrySupplier<Block> LEAD_WEIGHT = registerLeadWeight("lead_weight");
    public static final RegistrySupplier<Block> CHIPPED_LEAD_WEIGHT = registerLeadWeight("chipped_lead_weight");
    public static final RegistrySupplier<Block> DAMAGED_LEAD_WEIGHT = registerLeadWeight("damaged_lead_weight");

    // 16 dyed leaded glass blocks (the colour palette for crafting panes). StainedGlassBlock
    // carries the DyeColor (beacon-beam tint); same pickaxe-drop rule as plain leaded glass.
    public static final Map<DyeColor, RegistrySupplier<Block>> STAINED_LEADED_GLASS = new EnumMap<>(DyeColor.class);
    static {
        for (DyeColor color : DyeColor.values()) {
            STAINED_LEADED_GLASS.put(color, registerStainedLeadedGlass(color));
        }
    }

    // ---- Lead block factories (mirroring The Silver Age, without oxidation) ----

    private static RegistrySupplier<Block> registerLeadBlock(String name) {
        return register(name, () -> new Block(leadProps()));
    }

    private static RegistrySupplier<HorizontalFacingBlock> registerLeadChiseled(String name) {
        return register(name, () -> new HorizontalFacingBlock(leadProps()));
    }

    private static RegistrySupplier<RotatedPillarBlock> registerLeadPillar(String name) {
        return register(name, () -> new RotatedPillarBlock(leadProps()));
    }

    private static RegistrySupplier<RotatedPillarBlock> registerLeadChain(String name) {
        return register(name, () -> new ChainBlock(
                leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.CHAIN)).sound(SoundType.CHAIN)));
    }

    private static RegistrySupplier<IronBarsBlock> registerLeadBars(String name) {
        return register(name, () -> new IronBarsBlock(leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS))));
    }

    private static RegistrySupplier<SlabBlock> registerLeadSlab(String name) {
        return register(name, () -> new SlabBlock(leadProps()));
    }

    private static RegistrySupplier<StairBlock> registerLeadStairs(String name) {
        return register(name, () -> new StairBlock(Blocks.IRON_BLOCK.defaultBlockState(), leadProps()));
    }

    private static RegistrySupplier<Block> registerLeadGrate(String name) {
        return register(name, () -> new WaterloggedTransparentBlock(
                leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_GRATE)).sound(ModSoundTypes.LEAD_GRATE)));
    }

    private static RegistrySupplier<TrapDoorBlock> registerLeadTrapdoor(String name) {
        return register(name, () -> new TrapDoorBlock(ModBlockSetTypes.LEAD,
                leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_TRAPDOOR))));
    }

    private static RegistrySupplier<DoorBlock> registerLeadDoor(String name) {
        return register(name, () -> new DoorBlock(ModBlockSetTypes.LEAD,
                leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR))));
    }

    private static RegistrySupplier<Block> registerStainedLeadedGlass(DyeColor color) {
        return register(color.getName() + "_leaded_glass", () ->
                new StainedGlassBlock(color, BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_STAINED_GLASS)
                        .mapColor(color.getMapColor()).requiresCorrectToolForDrops()));
    }

    private static RegistrySupplier<Block> registerLeadWeight(String name) {
        // All three tiers share one falling block; the tier is the block's identity. The degrade
        // chain lives in nextWeightTier, rolled on a hard landing by LeadWeightBlock#onLand.
        return register(name, () -> new LeadWeightBlock(leadProps().noOcclusion().sound(ModSoundTypes.LEAD_WEIGHT)));
    }

    /** The next tier a weight degrades to on a hard landing, or null if it should shatter (the last tier). */
    @Nullable
    public static Block nextWeightTier(Block current) {
        if (current == LEAD_WEIGHT.get()) {
            return CHIPPED_LEAD_WEIGHT.get();
        }
        if (current == CHIPPED_LEAD_WEIGHT.get()) {
            return DAMAGED_LEAD_WEIGHT.get();
        }
        return null;
    }

    private static BlockBehaviour.Properties leadProps() {
        return leadProps(BlockBehaviour.Properties.of());
    }

    private static BlockBehaviour.Properties leadProps(BlockBehaviour.Properties baseProps) {
        return baseProps
                .strength(3.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(ModSoundTypes.LEAD)
                .mapColor(MapColor.COLOR_GRAY)
                .instrument(NoteBlockInstrument.BELL);
    }

    private static <T extends Block> RegistrySupplier<T> register(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static void register() {
        BLOCKS.register();
    }
}

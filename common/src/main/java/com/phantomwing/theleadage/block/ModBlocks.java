package com.phantomwing.theleadage.block;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadWeightBlock;
import com.phantomwing.theleadage.block.custom.HorizontalFacingBlock;
import com.phantomwing.theleadage.block.custom.LeadedGlassDoorBlock;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.block.custom.LeadTorchBlock;
import com.phantomwing.theleadage.block.custom.LeadWallTorchBlock;
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
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
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

    /** Hardness shared by every lead block: lead is soft, so it mines faster than iron (5.0). */
    private static final float LEAD_HARDNESS = 3.0F;

    /**
     * Blast resistance of lead — real lead is dense enough to be used as shielding. A block survives a
     * blast when its resistance clears {@code 4.333 × power − 0.3}, so 17 shrugs off creepers (12.7) and
     * TNT (17.0) while a charged creeper (25.7) still tears through it.
     */
    private static final float LEAD_RESISTANCE = 17.0F;

    /** Bars, chains and the grate are mostly open air, so a blast passes through them far more easily. */
    private static final float OPEN_LEAD_RESISTANCE = 10.0F;

    /** Leaded glass is still glass (0.3) — the came lattice only makes it a touch tougher. */
    private static final float LEADED_GLASS_RESISTANCE = 1.0F;

    // Both are a full cube of lead (raw or refined) — far too heavy for a piston to budge. They copy
    // vanilla's iron props, so strength is overridden back down to lead's.
    public static final RegistrySupplier<Block> RAW_LEAD_BLOCK = register("raw_lead_block", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.COLOR_GRAY)
                    .pushReaction(PushReaction.BLOCK).strength(LEAD_HARDNESS, LEAD_RESISTANCE)));
    public static final RegistrySupplier<Block> LEAD_BLOCK = register("lead_block", () ->
            new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_GRAY)
                    .pushReaction(PushReaction.BLOCK).strength(LEAD_HARDNESS, LEAD_RESISTANCE)));

    // Decorative lead blocks (no oxidation). Order matches the creative tab.
    public static final RegistrySupplier<Block> CUT_LEAD = registerLeadBlock("cut_lead");
    public static final RegistrySupplier<Block> LEAD_BRICKS = registerLeadBlock("lead_bricks");
    public static final RegistrySupplier<SlabBlock> LEAD_BRICK_SLAB = registerLeadSlab("lead_brick_slab");
    public static final RegistrySupplier<StairBlock> LEAD_BRICK_STAIRS = registerLeadStairs("lead_brick_stairs");
    public static final RegistrySupplier<WallBlock> LEAD_BRICK_WALL = registerLeadWall("lead_brick_wall");
    public static final RegistrySupplier<SlabBlock> CUT_LEAD_SLAB = registerLeadSlab("cut_lead_slab");
    public static final RegistrySupplier<StairBlock> CUT_LEAD_STAIRS = registerLeadStairs("cut_lead_stairs");
    public static final RegistrySupplier<HorizontalFacingBlock> CHISELED_LEAD = registerLeadChiseled("chiseled_lead");
    public static final RegistrySupplier<RotatedPillarBlock> LEAD_PILLAR = registerLeadPillar("lead_pillar");
    public static final RegistrySupplier<Block> LEAD_GRATE = registerLeadGrate("lead_grate");
    public static final RegistrySupplier<TrapDoorBlock> LEAD_TRAPDOOR = registerLeadTrapdoor("lead_trapdoor");
    public static final RegistrySupplier<DoorBlock> LEAD_DOOR = registerLeadDoor("lead_door");
    public static final RegistrySupplier<RotatedPillarBlock> LEAD_CHAIN = registerLeadChain("lead_chain");
    public static final RegistrySupplier<IronBarsBlock> LEAD_BARS = registerLeadBars("lead_bars");

    // Lead torch: burns a grayish-white lead-salt flame and slowly sickens anyone lingering
    // beside it (LeadTorchBlockEntity). The lead lantern is the enclosed — and safe — version.
    public static final RegistrySupplier<Block> LEAD_TORCH = register("lead_torch", () ->
            new LeadTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH)));
    public static final RegistrySupplier<Block> LEAD_WALL_TORCH = register("lead_wall_torch", () ->
            new LeadWallTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WALL_TORCH)));
    public static final RegistrySupplier<Block> LEAD_LANTERN = register("lead_lantern", () ->
            new LanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)));

    // Leaded glass: renders/behaves like glass, but requires a pickaxe to drop itself
    // (requiresCorrectToolForDrops + the mineable/pickaxe tag). It's pried from its lead
    // came, not shattered, so it uses the heavy LEAD sound and a lead-gray map colour.
    public static final RegistrySupplier<Block> LEADED_GLASS = register("leaded_glass", () ->
            new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).requiresCorrectToolForDrops()
                    .sound(ModSoundTypes.LEAD).mapColor(MapColor.COLOR_GRAY)
                    .explosionResistance(LEADED_GLASS_RESISTANCE)));

    // Leaded glass panes — one static block per came type (LeadedGlassPaneBlock.CameType). Colours
    // live on the block entity (tinted); split is orientable (sneak-right-click toggles h/v).
    public static final RegistrySupplier<Block> LEADED_GLASS_PANEL = register("leaded_glass_pane", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.PLAIN, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_SPLIT = register("leaded_glass_pane_split", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.SPLIT, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_PLUS = register("leaded_glass_pane_plus", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.PLUS, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_GRID = register("leaded_glass_pane_grid", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.GRID, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_DIAGONAL = register("leaded_glass_pane_diagonal", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.DIAGONAL, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_CROSS = register("leaded_glass_pane_cross", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.CROSS, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_DIAMOND = register("leaded_glass_pane_diamond", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.DIAMOND, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_LATTICE = register("leaded_glass_pane_lattice", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.LATTICE, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_BARS = register("leaded_glass_pane_bars", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.BARS, paneProps()));
    public static final RegistrySupplier<Block> LEADED_GLASS_PANE_DIAGONAL_BARS = register("leaded_glass_pane_diagonal_bars", () ->
            LeadedGlassPaneBlock.of(LeadedGlassPaneBlock.CameType.DIAGONAL_BARS, paneProps()));

    private static BlockBehaviour.Properties paneProps() {
        // forceSolidOn: a pane's thin slab is only "solid" (motion-blocking) when full-height, so a
        // floor/ceiling-mounted pane would otherwise drop out of the MOTION_BLOCKING heightmap and let
        // rain render through it. Forcing solid keeps every orientation in the heightmap (vanilla panes
        // are wall-only, so they never hit this) without changing the actual (thin) collision shape.
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).requiresCorrectToolForDrops()
                .noOcclusion().forceSolidOn().sound(ModSoundTypes.LEAD).mapColor(MapColor.COLOR_GRAY)
                .explosionResistance(LEADED_GLASS_RESISTANCE);
    }

    /** The pane block for a came frame (the orientations of a came type share one block). */
    public static Block paneBlockFor(com.phantomwing.theleadage.block.custom.LeadedGlassFrame frame) {
        return switch (frame) {
            case PLAIN -> LEADED_GLASS_PANEL.get();
            case SPLIT_H, SPLIT_V -> LEADED_GLASS_PANE_SPLIT.get();
            case PLUS -> LEADED_GLASS_PANE_PLUS.get();
            case GRID -> LEADED_GLASS_PANE_GRID.get();
            case DIAGONAL_A, DIAGONAL_B -> LEADED_GLASS_PANE_DIAGONAL.get();
            case CROSS -> LEADED_GLASS_PANE_CROSS.get();
            case DIAMOND -> LEADED_GLASS_PANE_DIAMOND.get();
            case LATTICE -> LEADED_GLASS_PANE_LATTICE.get();
            case BARS_H, BARS_V -> LEADED_GLASS_PANE_BARS.get();
            case DIAGONAL_BARS_A, DIAGONAL_BARS_B -> LEADED_GLASS_PANE_DIAGONAL_BARS.get();
        };
    }

    // A lead door whose top half is a configurable leaded glass pane (design on its block entity,
    // drawn by a renderer). See LeadedGlassDoorBlock. The glass is the weak point, so it blows up as
    // easily as a pane rather than shielding like the plain lead door.
    public static final RegistrySupplier<Block> LEADED_GLASS_DOOR = register("leaded_glass_door", () ->
            new LeadedGlassDoorBlock(ModBlockSetTypes.LEADED_GLASS,
                    leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR)).noOcclusion()
                            .sound(SoundType.COPPER).explosionResistance(LEADED_GLASS_RESISTANCE)));

    // A lead trapdoor whose flap is a configurable leaded glass pane. See LeadedGlassTrapdoorBlock.
    public static final RegistrySupplier<Block> LEADED_GLASS_TRAPDOOR = register("leaded_glass_trapdoor", () ->
            new LeadedGlassTrapdoorBlock(ModBlockSetTypes.LEADED_GLASS,
                    leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_TRAPDOOR)).noOcclusion()
                            .sound(SoundType.COPPER).explosionResistance(LEADED_GLASS_RESISTANCE)));

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
        return register(name, () -> new Block(solidLeadProps()));
    }

    private static RegistrySupplier<HorizontalFacingBlock> registerLeadChiseled(String name) {
        return register(name, () -> new HorizontalFacingBlock(solidLeadProps()));
    }

    private static RegistrySupplier<RotatedPillarBlock> registerLeadPillar(String name) {
        return register(name, () -> new RotatedPillarBlock(solidLeadProps()));
    }

    private static RegistrySupplier<RotatedPillarBlock> registerLeadChain(String name) {
        return register(name, () -> new ChainBlock(leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.CHAIN))
                .sound(SoundType.CHAIN).explosionResistance(OPEN_LEAD_RESISTANCE)));
    }

    private static RegistrySupplier<IronBarsBlock> registerLeadBars(String name) {
        return register(name, () -> new IronBarsBlock(leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS))
                .explosionResistance(OPEN_LEAD_RESISTANCE)));
    }

    private static RegistrySupplier<SlabBlock> registerLeadSlab(String name) {
        return register(name, () -> new SlabBlock(leadProps()));
    }

    private static RegistrySupplier<StairBlock> registerLeadStairs(String name) {
        return register(name, () -> new StairBlock(Blocks.IRON_BLOCK.defaultBlockState(), leadProps()));
    }

    private static RegistrySupplier<WallBlock> registerLeadWall(String name) {
        return register(name, () -> new WallBlock(leadProps()));
    }

    private static RegistrySupplier<Block> registerLeadGrate(String name) {
        return register(name, () -> new WaterloggedTransparentBlock(
                leadProps(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_GRATE))
                        .sound(ModSoundTypes.LEAD_GRATE).explosionResistance(OPEN_LEAD_RESISTANCE)));
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
                        .mapColor(color.getMapColor()).requiresCorrectToolForDrops().sound(ModSoundTypes.LEAD)
                        .explosionResistance(LEADED_GLASS_RESISTANCE)));
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

    /**
     * A full cube of solid lead — too heavy for a piston to shift.
     * {@link PushReaction#BLOCK} stops the piston outright (obsidian's behaviour) rather than
     * letting it break or drop the block, and it also denies slime/honey pulls. Only full cubes of lead
     * get this — the lead and raw lead blocks set it inline above, since they copy vanilla's iron props
     * rather than {@link #leadProps()}. The slabs, stairs, walls, doors, grate, bars and chain are not
     * solid lead through and through, so they push normally.
     */
    private static BlockBehaviour.Properties solidLeadProps() {
        return leadProps().pushReaction(PushReaction.BLOCK);
    }

    private static BlockBehaviour.Properties leadProps(BlockBehaviour.Properties baseProps) {
        return baseProps
                .strength(LEAD_HARDNESS, LEAD_RESISTANCE)
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

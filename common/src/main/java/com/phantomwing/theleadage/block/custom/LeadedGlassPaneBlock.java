package com.phantomwing.theleadage.block.custom;

import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A configurable leaded glass pane, one block per came type ({@link CameType}). The came pattern
 * is the block's identity (so a new pattern is just a new block); per-region colours live on the
 * {@link LeadedGlassPanelBlockEntity} and are tinted, while a per-region {@code clear_N} state
 * picks the clear vs. coloured texture. Orientable types (e.g. split) carry an {@code orientation}
 * that selects a distinct model and can be cycled in place by sneak-right-click.
 */
public class LeadedGlassPaneBlock extends Block implements EntityBlock {
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty[] CLEAR = {
            BooleanProperty.create("clear_0"), BooleanProperty.create("clear_1"),
            BooleanProperty.create("clear_2"), BooleanProperty.create("clear_3"),
            BooleanProperty.create("clear_4"), BooleanProperty.create("clear_5"),
            BooleanProperty.create("clear_6"), BooleanProperty.create("clear_7"),
            BooleanProperty.create("clear_8"), BooleanProperty.create("clear_9"),
            BooleanProperty.create("clear_10"), BooleanProperty.create("clear_11")
    };

    private static final VoxelShape X_SHAPE = Block.box(0.0, 0.0, 7.0, 16.0, 16.0, 9.0);
    private static final VoxelShape Z_SHAPE = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 16.0);
    private static final VoxelShape Y_SHAPE = Block.box(0.0, 7.0, 0.0, 16.0, 9.0, 16.0);

    /** Set just before a {@code new}, so {@link #createBlockStateDefinition} (called from super) sees it. */
    private static CameType pendingType;

    private final CameType type;

    public static LeadedGlassPaneBlock of(CameType type, Properties properties) {
        pendingType = type;
        return new LeadedGlassPaneBlock(properties);
    }

    private LeadedGlassPaneBlock(Properties properties) {
        super(properties);
        this.type = pendingType;
        BlockState state = stateDefinition.any().setValue(FACE, AttachFace.WALL).setValue(FACING, Direction.NORTH);
        for (int i = 0; i < type.regions; i++) {
            state = state.setValue(CLEAR[i], true);
        }
        if (type.orientation != null) {
            state = state.setValue(type.orientation, 0);
        }
        registerDefaultState(state);
    }

    public CameType cameType() {
        return type;
    }

    /** The frame this state represents (orientation chooses split_h vs split_v). */
    public LeadedGlassFrame frame(BlockState state) {
        return type.frame(type.orientation != null ? state.getValue(type.orientation) : 0);
    }

    /**
     * Whether a vanilla-style bar / glass pane (any {@code IronBarsBlock}, incl. lead bars) should grow
     * a connection arm toward this state. True for a wall-mounted leaded glass pane: its full-face plane
     * sits at the shared edge, so the arm meets it cleanly. Called from the per-loader IronBarsBlock mixin.
     */
    public static boolean barsConnectTo(BlockState state) {
        return state.getBlock() instanceof LeadedGlassPaneBlock && state.getValue(FACE) == AttachFace.WALL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING);
        for (int i = 0; i < pendingType.regions; i++) {
            builder.add(CLEAR[i]);
        }
        if (pendingType.orientation != null) {
            builder.add(pendingType.orientation);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(FACE) != AttachFace.WALL) {
            return Y_SHAPE;
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LeadedGlassConfig config = context.getItemInHand().get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        Direction look = context.getNearestLookingDirection();
        AttachFace face = switch (look) {
            case UP -> AttachFace.CEILING;
            case DOWN -> AttachFace.FLOOR;
            default -> AttachFace.WALL;
        };
        BlockState state = defaultBlockState().setValue(FACE, face).setValue(FACING, context.getHorizontalDirection());
        for (int i = 0; i < type.regions; i++) {
            state = state.setValue(CLEAR[i], isClear(config, i));
        }
        if (type.orientation != null) {
            state = state.setValue(type.orientation, type.orientationOf(config != null ? config.frame() : null));
        }
        return state;
    }

    /** Right-clicking a region with a dye recolours just that region; with shears, clears it (front/back faces only). */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        boolean shears = stack.getItem() instanceof ShearsItem;
        if (!(stack.getItem() instanceof DyeItem) && !shears) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int region = regionAt(state, pos, hit);
        if (region < 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; // a thin edge, not the glass face
        }
        // The colour we want this region to become: a dye's colour, or null to clear it (shears).
        DyeColor target = shears ? null : ((DyeItem) stack.getItem()).getDyeColor();
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
            if (pane.colorAt(region) == target) {
                return ItemInteractionResult.SUCCESS; // already this colour / already clear — consume the click, spend nothing
            }
            List<Integer> colors = new ArrayList<>(pane.getColors());
            while (colors.size() < type.regions) {
                colors.add(LeadedGlassConfig.CLEAR);
            }
            colors.set(region, target == null ? LeadedGlassConfig.CLEAR : target.getId());
            pane.setColors(colors);
            level.setBlock(pos, state.setValue(CLEAR[region], target == null), Block.UPDATE_CLIENTS);
            if (shears) {
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
                stack.hurtAndBreak(1, player,
                        hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            } else {
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Which colourable region a click landed on, or {@code -1} if it wasn't on a front/back glass face. */
    private int regionAt(BlockState state, BlockPos pos, BlockHitResult hit) {
        AttachFace face = state.getValue(FACE);
        Direction facing = state.getValue(FACING);
        Direction.Axis thin = face == AttachFace.WALL ? facing.getAxis() : Direction.Axis.Y;
        if (hit.getDirection().getAxis() != thin) {
            return -1; // the click hit a thin edge, not one of the two large faces
        }
        Vec3 loc = hit.getLocation();
        double lx = loc.x - pos.getX();
        double ly = loc.y - pos.getY();
        double lz = loc.z - pos.getZ();
        // Un-rotate the hit into the model's authored 2D space (u: came-left→right, v: bottom→top),
        // matching the blockstate's face/facing rotation so the region matches what is rendered.
        double u;
        double v;
        switch (face) {
            case WALL -> {
                v = ly;
                u = switch (facing) {
                    case EAST -> lz;
                    case SOUTH -> 1 - lx;
                    case WEST -> 1 - lz;
                    default -> lx; // NORTH
                };
            }
            case FLOOR -> {
                switch (facing) {
                    case EAST -> { u = 1 - lx; v = 1 - lz; }
                    case SOUTH -> { u = 1 - lz; v = lx; }
                    case WEST -> { u = lx; v = lz; }
                    default -> { u = lz; v = 1 - lx; } // NORTH
                }
            }
            default -> { // CEILING
                switch (facing) {
                    case EAST -> { u = lx; v = 1 - lz; }
                    case SOUTH -> { u = lz; v = lx; }
                    case WEST -> { u = 1 - lx; v = lz; }
                    default -> { u = 1 - lz; v = 1 - lx; } // NORTH
                }
            }
        }
        return frame(state).regionAt(u, v);
    }

    /**
     * Sneak-right-click rotates a pane a quarter turn in place. Orientable types (split/diagonal/bars)
     * advance their orientation and reverse the region colours on each wrap; the symmetric types
     * (plus, grid, cross) keep their geometry and just rotate the colours. The two faces are
     * mirror images, so clicking the back face turns the design the opposite way round the array —
     * to the player it always reads as turning clockwise, from whichever side they clicked.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        int[] rotation = type.colorRotation();
        if ((type.orientation == null && rotation == null) || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        boolean reverse = isBackFace(state, hit);
        if (!level.isClientSide) {
            if (type.orientation != null) {
                int orientations = type.orientations;
                int current = state.getValue(type.orientation);
                int next = reverse ? (current - 1 + orientations) % orientations : (current + 1) % orientations;
                // Forward reverses on arrival at orientation 0; reversed reverses on leaving it, so the
                // four-state (orientation × region-order) cycle simply runs backwards. Reversing the
                // region order is what the 180° across the wrap looks like — the two 90° turns flip the
                // strips end to end (for the two-region split this is the old swap).
                boolean reverseColors = reverse ? current == 0 : next == 0;
                BlockState rotated = state.setValue(type.orientation, next);
                if (reverseColors && level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
                    List<Integer> colors = new ArrayList<>(pane.getColors());
                    while (colors.size() < type.regions) {
                        colors.add(LeadedGlassConfig.CLEAR);
                    }
                    Collections.reverse(colors);
                    pane.setColors(colors);
                    for (int i = 0; i < type.regions; i++) {
                        rotated = rotated.setValue(CLEAR[i], colors.get(i) == LeadedGlassConfig.CLEAR);
                    }
                }
                level.setBlock(pos, rotated, Block.UPDATE_ALL);
            } else if (level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
                List<Integer> colors = new ArrayList<>(pane.getColors());
                while (colors.size() < type.regions) {
                    colors.add(LeadedGlassConfig.CLEAR);
                }
                List<Integer> rotated = new ArrayList<>(Collections.nCopies(type.regions, LeadedGlassConfig.CLEAR));
                for (int i = 0; i < type.regions; i++) {
                    if (reverse) {
                        rotated.set(rotation[i], colors.get(i));   // inverse permutation — turn the other way
                    } else {
                        rotated.set(i, colors.get(rotation[i]));
                    }
                }
                pane.setColors(rotated);
                BlockState rotatedState = state;
                for (int i = 0; i < type.regions; i++) {
                    rotatedState = rotatedState.setValue(CLEAR[i], rotated.get(i) == LeadedGlassConfig.CLEAR);
                }
                level.setBlock(pos, rotatedState, Block.UPDATE_ALL);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.7f, 1.1f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Whether the player clicked the pane's back face. The front face shows the design as authored
     * (for a wall pane it faces opposite the {@code FACING} it was placed against); the back is its
     * mirror image, so a turn there must run the opposite way to still look clockwise to the viewer.
     */
    private boolean isBackFace(BlockState state, BlockHitResult hit) {
        Direction back = switch (state.getValue(FACE)) {
            case WALL -> state.getValue(FACING);
            case FLOOR -> Direction.UP;
            case CEILING -> Direction.DOWN;
        };
        return hit.getDirection() == back;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config != null && level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
            pane.setColors(config.colors());
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity pane) {
            stack.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), new LeadedGlassConfig(frame(state), pane.getColors()));
        }
        return stack;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(this);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LeadedGlassPanelBlockEntity pane) {
            stack.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), new LeadedGlassConfig(frame(state), pane.getColors()));
        }
        return List.of(stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LeadedGlassPanelBlockEntity(pos, state);
    }

    /** A region is clear when it has no colour (an explicit CLEAR id, or no entry at all). */
    private static boolean isClear(@Nullable LeadedGlassConfig config, int region) {
        if (config == null || region >= config.colors().size()) {
            return true;
        }
        return config.colors().get(region) == LeadedGlassConfig.CLEAR;
    }

    /** A came type = its model family, region (colour) count, and orientation set. */
    public enum CameType {
        PLAIN("plain", 1, null, LeadedGlassFrame.PLAIN),
        SPLIT("split", 2, IntegerProperty.create("orientation", 0, 1), LeadedGlassFrame.SPLIT_H, LeadedGlassFrame.SPLIT_V),
        PLUS("plus", 4, null, LeadedGlassFrame.PLUS),
        GRID("grid", 9, null, LeadedGlassFrame.GRID),
        DIAGONAL("diagonal", 2, IntegerProperty.create("orientation", 0, 1), LeadedGlassFrame.DIAGONAL_A, LeadedGlassFrame.DIAGONAL_B),
        CROSS("cross", 4, null, LeadedGlassFrame.CROSS),
        DIAMOND("diamond", 5, null, LeadedGlassFrame.DIAMOND),
        LATTICE("lattice", 12, null, LeadedGlassFrame.LATTICE),
        BARS("bars", 3, IntegerProperty.create("orientation", 0, 1),
                LeadedGlassFrame.BARS_H, LeadedGlassFrame.BARS_V),
        DIAGONAL_BARS("diagonal_bars", 4, IntegerProperty.create("orientation", 0, 1),
                LeadedGlassFrame.DIAGONAL_BARS_A, LeadedGlassFrame.DIAGONAL_BARS_B);

        public final String id;
        public final int regions;
        public final int orientations;
        /** Null for non-orientable types; otherwise an integer property sized to the orientation count. */
        @Nullable
        public final IntegerProperty orientation;
        private final LeadedGlassFrame[] frames; // one per orientation

        CameType(String id, int regions, @Nullable IntegerProperty orientation, LeadedGlassFrame... frames) {
            this.id = id;
            this.regions = regions;
            this.orientation = orientation;
            this.orientations = frames.length;
            this.frames = frames;
        }

        public LeadedGlassFrame frame(int orientation) {
            return frames[Math.floorMod(orientation, frames.length)];
        }

        /** Which orientation index a crafted frame maps to (so split_h/split_v place correctly). */
        public int orientationOf(@Nullable LeadedGlassFrame frame) {
            for (int i = 0; i < frames.length; i++) {
                if (frames[i] == frame) {
                    return i;
                }
            }
            return 0;
        }

        /**
         * For the symmetric, non-orientable came types, the source region of each region after a 90°
         * clockwise turn ({@code newColor[i] = oldColor[map[i]]}) — a true spatial rotation, so corners
         * cycle among corners and edges among edges (the 3×3 middle is fixed). Null = not rotatable.
         */
        @Nullable
        public int[] colorRotation() {
            return switch (this) {
                case PLUS -> new int[]{2, 0, 3, 1};                      // TL TR BL BR
                case GRID -> new int[]{6, 3, 0, 7, 4, 1, 8, 5, 2};     // row-major 3×3, centre (4) fixed
                case CROSS -> new int[]{3, 0, 1, 2};                     // top right bottom left
                case DIAMOND -> new int[]{3, 0, 2, 4, 1};                // corners cycle, centre (2) fixed
                // Three 4-cycles: border-hugging triangles ×2 and the inner rhombi (N/E/S/W).
                case LATTICE -> new int[]{7, 2, 10, 5, 0, 8, 3, 11, 6, 1, 9, 4};
                default -> null;                                         // plain / orientable types
            };
        }
    }
}

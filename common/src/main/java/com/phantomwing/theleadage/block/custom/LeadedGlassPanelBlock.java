package com.phantomwing.theleadage.block.custom;

import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A thin framed leaded glass panel. Its geometry is two finite blockstate axes —
 * {@link #AXIS orientation} (set from how you place it; no neighbour connections, no
 * corners) and {@link #FRAME came layout} (read from the item's
 * {@link LeadedGlassConfig} component) — while the per-region colours live on the
 * {@link LeadedGlassPanelBlockEntity} and are applied by a tint provider.
 */
public class LeadedGlassPanelBlock extends Block implements EntityBlock {
    // Orientation, button/lever style: FACE is which surface it sits on (wall = upright,
    // floor/ceiling = lying flat) and FACING is the horizontal yaw. Together they give 12
    // placements (4 yaw × {upright, flat-up, flat-down}), each rotated toward the player so an
    // asymmetric came/colour layout reads the same way it does in the item.
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<LeadedGlassFrame> FRAME = EnumProperty.create("frame", LeadedGlassFrame.class);
    // Whether each region is clear (uses the clear leaded_glass texture) vs coloured (the
    // tinted white_leaded_glass texture). Derived from the colours so the model can pick the
    // right texture per region. Only as many as the frame has regions are meaningful.
    public static final BooleanProperty CLEAR_0 = BooleanProperty.create("clear_0");
    public static final BooleanProperty CLEAR_1 = BooleanProperty.create("clear_1");
    public static final BooleanProperty CLEAR_2 = BooleanProperty.create("clear_2");
    public static final BooleanProperty CLEAR_3 = BooleanProperty.create("clear_3");

    private static final VoxelShape X_SHAPE = Block.box(0.0, 0.0, 7.0, 16.0, 16.0, 9.0);
    private static final VoxelShape Z_SHAPE = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 16.0);
    private static final VoxelShape Y_SHAPE = Block.box(0.0, 7.0, 0.0, 16.0, 9.0, 16.0);

    public LeadedGlassPanelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACE, AttachFace.WALL).setValue(FACING, Direction.NORTH)
                .setValue(FRAME, LeadedGlassFrame.PLAIN)
                .setValue(CLEAR_0, true).setValue(CLEAR_1, true).setValue(CLEAR_2, true).setValue(CLEAR_3, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, FRAME, CLEAR_0, CLEAR_1, CLEAR_2, CLEAR_3);
    }

    /** A region is clear when it has no colour (an explicit CLEAR id, or no entry at all). */
    private static boolean isClear(@Nullable LeadedGlassConfig config, int region) {
        if (config == null || region >= config.colors().size()) {
            return true;
        }
        return config.colors().get(region) == LeadedGlassConfig.CLEAR;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Flat slab on floor/ceiling (thin y); upright on a wall (thin z for N/S, thin x for E/W).
        if (state.getValue(FACE) != AttachFace.WALL) {
            return Y_SHAPE;
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LeadedGlassConfig config = context.getItemInHand().get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        LeadedGlassFrame frame = config != null ? config.frame() : LeadedGlassFrame.PLAIN;
        // Looking at a floor → lie flat facing up; ceiling → flat facing down; otherwise upright.
        // FACING is the horizontal yaw in every case, so even flat panes rotate with how you look.
        Direction look = context.getNearestLookingDirection();
        AttachFace face = switch (look) {
            case UP -> AttachFace.CEILING;
            case DOWN -> AttachFace.FLOOR;
            default -> AttachFace.WALL;
        };
        return defaultBlockState().setValue(FRAME, frame).setValue(FACE, face)
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(CLEAR_0, isClear(config, 0)).setValue(CLEAR_1, isClear(config, 1))
                .setValue(CLEAR_2, isClear(config, 2)).setValue(CLEAR_3, isClear(config, 3));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config != null && level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity panel) {
            panel.setColors(config.colors());
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity panel) {
            stack.set(ModDataComponents.LEADED_GLASS_CONFIG.get(),
                    new LeadedGlassConfig(state.getValue(FRAME), panel.getColors()));
        }
        return stack;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        // Drop the configured panel (frame + colours), so the design is preserved.
        // Only reached with the correct tool (requiresCorrectToolForDrops gates this).
        ItemStack stack = new ItemStack(this);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LeadedGlassPanelBlockEntity panel) {
            stack.set(ModDataComponents.LEADED_GLASS_CONFIG.get(),
                    new LeadedGlassConfig(state.getValue(FRAME), panel.getColors()));
        }
        return List.of(stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LeadedGlassPanelBlockEntity(pos, state);
    }
}

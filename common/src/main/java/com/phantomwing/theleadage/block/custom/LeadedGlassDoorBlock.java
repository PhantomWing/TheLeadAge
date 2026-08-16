package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.ModBlockSetTypes;
import com.phantomwing.theleadage.block.entity.LeadedGlassDoorBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A lead door whose top half is a configurable leaded glass pane. The glass design (frame +
 * colours) lives on a {@link LeadedGlassDoorBlockEntity} on both halves and is drawn by a
 * block-entity renderer; the block itself is an ordinary vanilla door otherwise.
 */
public class LeadedGlassDoorBlock extends DoorBlock implements EntityBlock {
    // BlockSetType is fixed (leaded glass), so a Properties-only codec is enough.
    public static final MapCodec<LeadedGlassDoorBlock> CODEC =
            simpleCodec(props -> new LeadedGlassDoorBlock(ModBlockSetTypes.LEADED_GLASS, props));

    public LeadedGlassDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    /**
     * Dye or shear one region of the clicked half's glass, exactly as on a placed pane. Only a click
     * that lands on the design does anything — clicking the lead frame still opens the door, so
     * holding a dye doesn't stop you using it.
     *
     * <p>The two halves carry their own pane, but the {@link LeadedGlassDoorConfig} holding both is
     * mirrored on each half's block entity (the renderer reads whichever half it is drawing), so an
     * edit has to be written to both.</p>
     */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!LeadedGlassRecolor.isTool(stack)
                || !(level.getBlockEntity(pos) instanceof LeadedGlassDoorBlockEntity be)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        boolean upper = state.getValue(HALF) == DoubleBlockHalf.UPPER;
        LeadedGlassDoorConfig door = be.getConfig();
        LeadedGlassConfig pane = upper ? door.top() : door.bottom();

        int region = LeadedGlassPlacement.regionAt(state, state.getShape(level, pos).bounds(),
                pane.frame(), hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)));
        if (region < 0) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit); // missed the glass
        }
        if (!level.isClientSide) {
            List<Integer> colors = LeadedGlassRecolor.apply(
                    pane.colors(), pane.frame().regions(), region, LeadedGlassRecolor.target(stack));
            if (colors == null) {
                return InteractionResult.SUCCESS; // already that colour — eat the click, spend nothing
            }
            LeadedGlassConfig updated = new LeadedGlassConfig(pane.frame(), colors);
            LeadedGlassDoorConfig both = upper
                    ? new LeadedGlassDoorConfig(updated, door.bottom())
                    : new LeadedGlassDoorConfig(door.top(), updated);
            BlockPos lower = upper ? pos.below() : pos;
            applyConfig(level, lower, both);
            applyConfig(level, lower.above(), both);
            LeadedGlassRecolor.consume(stack, player, hand, level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LeadedGlassDoorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack); // also places the upper half
        LeadedGlassDoorConfig config = stack.get(ModDataComponents.LEADED_GLASS_DOOR_CONFIG.get());
        if (config != null) {
            applyConfig(level, pos, config);
            applyConfig(level, pos.above(), config);
        }
    }

    private static void applyConfig(Level level, BlockPos pos, LeadedGlassDoorConfig config) {
        if (level.getBlockEntity(pos) instanceof LeadedGlassDoorBlockEntity door) {
            door.setConfig(config);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        if (level.getBlockEntity(pos) instanceof LeadedGlassDoorBlockEntity door) {
            stack.set(ModDataComponents.LEADED_GLASS_DOOR_CONFIG.get(), door.getConfig());
        }
        return stack;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LeadedGlassDoorBlockEntity door) {
            for (ItemStack drop : drops) {
                if (drop.is(asItem())) {
                    drop.set(ModDataComponents.LEADED_GLASS_DOOR_CONFIG.get(), door.getConfig());
                }
            }
        }
        return drops;
    }
}

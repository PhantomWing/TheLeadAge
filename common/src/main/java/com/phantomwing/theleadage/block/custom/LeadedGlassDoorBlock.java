package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.ModBlockSetTypes;
import com.phantomwing.theleadage.block.entity.LeadedGlassDoorBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.BlockPos;
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
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
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

package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.ModBlockSetTypes;
import com.phantomwing.theleadage.block.entity.LeadedGlassTrapdoorBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A lead trapdoor whose flap is a configurable leaded glass pane. The glass design (frame +
 * colours) lives on a {@link LeadedGlassTrapdoorBlockEntity} and is drawn by a block-entity
 * renderer; otherwise it is an ordinary vanilla trapdoor.
 */
public class LeadedGlassTrapdoorBlock extends TrapDoorBlock implements EntityBlock {
    public static final MapCodec<LeadedGlassTrapdoorBlock> CODEC =
            simpleCodec(props -> new LeadedGlassTrapdoorBlock(ModBlockSetTypes.LEAD, props));

    public LeadedGlassTrapdoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public MapCodec<? extends TrapDoorBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LeadedGlassTrapdoorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config != null && level.getBlockEntity(pos) instanceof LeadedGlassTrapdoorBlockEntity trapdoor) {
            trapdoor.setConfig(config);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof LeadedGlassTrapdoorBlockEntity trapdoor) {
            stack.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), trapdoor.getConfig());
        }
        return stack;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LeadedGlassTrapdoorBlockEntity trapdoor) {
            for (ItemStack drop : drops) {
                if (drop.is(asItem())) {
                    drop.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), trapdoor.getConfig());
                }
            }
        }
        return drops;
    }
}

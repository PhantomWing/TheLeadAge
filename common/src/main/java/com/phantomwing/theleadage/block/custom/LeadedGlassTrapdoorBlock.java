package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.block.ModBlockSetTypes;
import com.phantomwing.theleadage.block.entity.LeadedGlassTrapdoorBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
            simpleCodec(props -> new LeadedGlassTrapdoorBlock(ModBlockSetTypes.LEADED_GLASS, props));

    public LeadedGlassTrapdoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public MapCodec<? extends TrapDoorBlock> codec() {
        return CODEC;
    }

    /**
     * Dye or shear one region of the flap's glass, exactly as on a placed pane. Only a click that
     * actually lands on the design does anything — clicking the lead frame still opens the trapdoor,
     * so holding a dye doesn't stop you using the door.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!LeadedGlassRecolor.isTool(stack)
                || !(level.getBlockEntity(pos) instanceof LeadedGlassTrapdoorBlockEntity be)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        LeadedGlassConfig config = be.getConfig();
        int region = LeadedGlassPlacement.regionAt(state, state.getShape(level, pos).bounds(),
                config.frame(), hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)));
        if (region < 0) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit); // missed the glass
        }
        if (!level.isClientSide) {
            List<Integer> colors = LeadedGlassRecolor.apply(
                    config.colors(), config.frame().regions(), region, LeadedGlassRecolor.target(stack));
            if (colors == null) {
                return ItemInteractionResult.SUCCESS; // already that colour — eat the click, spend nothing
            }
            be.setConfig(new LeadedGlassConfig(config.frame(), colors));
            LeadedGlassRecolor.consume(stack, player, hand, level, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
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

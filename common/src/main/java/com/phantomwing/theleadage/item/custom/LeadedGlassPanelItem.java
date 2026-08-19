package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.block.custom.LeadedGlassDoorBlock;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.block.custom.LeadedGlassTrapdoorBlock;
import com.phantomwing.theleadage.block.entity.LeadedGlassDoorBlockEntity;
import com.phantomwing.theleadage.block.entity.LeadedGlassTrapdoorBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.Collections;

/**
 * Item form of the configurable leaded glass panel. Carries the design in the
 * {@code leaded_glass_config} data component; a plain pane shows its colour in the name, every
 * other frame lists its colours per row in the tooltip (see {@link LeadedGlassTooltip}).
 *
 * <p>Sneak-right-clicking a leaded glass door or trapdoor with a pane in hand swaps that pane's
 * design: the held pane's design is installed and the old one handed back — the way to re-glaze a
 * door/trapdoor without uncrafting it. On a door the clicked half decides which pane is swapped.
 */
public class LeadedGlassPanelItem extends BlockItem {
    public LeadedGlassPanelItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult swap = trySwap(context);
        // PASS = not a swap (wrong target / not sneaking); fall through to normal pane placement.
        return swap != InteractionResult.PASS ? swap : super.useOn(context);
    }

    /** Re-glazes a clicked leaded glass door/trapdoor with the held pane, or PASS if not applicable. */
    private InteractionResult trySwap(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isSecondaryUseActive()) {
            return InteractionResult.PASS; // only on a sneak-click
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        LeadedGlassConfig held = heldPaneConfig(context.getItemInHand());

        if (state.getBlock() instanceof LeadedGlassTrapdoorBlock
                && level.getBlockEntity(pos) instanceof LeadedGlassTrapdoorBlockEntity be) {
            LeadedGlassConfig old = be.getConfig();
            if (!old.equals(held) && !level.isClientSide) {
                be.setConfig(held);
                handOver(player, context.getItemInHand(), old, level, pos);
            }
            return InteractionResult.SUCCESS;
        }

        if (state.getBlock() instanceof LeadedGlassDoorBlock
                && level.getBlockEntity(pos) instanceof LeadedGlassDoorBlockEntity be) {
            boolean upper = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
            LeadedGlassDoorConfig door = be.getConfig();
            LeadedGlassConfig old = upper ? door.top() : door.bottom();
            if (!old.equals(held) && !level.isClientSide) {
                LeadedGlassDoorConfig updated = upper
                        ? new LeadedGlassDoorConfig(held, door.bottom())
                        : new LeadedGlassDoorConfig(door.top(), held);
                BlockPos lower = upper ? pos.below() : pos;
                applyToDoor(level, lower, updated);
                applyToDoor(level, lower.above(), updated);
                handOver(player, context.getItemInHand(), old, level, pos);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static void applyToDoor(Level level, BlockPos pos, LeadedGlassDoorConfig config) {
        if (level.getBlockEntity(pos) instanceof LeadedGlassDoorBlockEntity door) {
            door.setConfig(config);
        }
    }

    /** Consumes one held pane and hands the swapped-out design back to the player as its pane item. */
    private static void handOver(Player player, ItemStack held, LeadedGlassConfig old, Level level, BlockPos pos) {
        ItemStack oldPane = new ItemStack(ModItems.paneItemFor(old.frame()));
        oldPane.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), old);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (!player.addItem(oldPane)) {
            player.drop(oldPane, false);
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    /** The held pane's design, or a clear pane of this item's frame when it carries no component. */
    private LeadedGlassConfig heldPaneConfig(ItemStack stack) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config != null) {
            return config;
        }
        LeadedGlassFrame frame = getBlock() instanceof LeadedGlassPaneBlock pane
                ? pane.cameType().frame(0) : LeadedGlassFrame.PLAIN;
        return new LeadedGlassConfig(frame, Collections.nCopies(frame.regions(), LeadedGlassConfig.CLEAR));
    }

    @Override
    public Component getName(ItemStack stack) {
        return LeadedGlassTooltip.name(stack, super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        LeadedGlassTooltip.append(stack, tooltip);
    }
}

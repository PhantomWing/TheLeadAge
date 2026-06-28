package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.entity.custom.LeadWeightEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block item for the Lead Weight — a drop-from-above weapon. {@code useOn} keeps normal block
 * placement (set it on the ground, or hang it under a block). Aiming at open air instead drops it
 * as a falling {@link LeadWeightEntity} with you as the owner (kill credit), which crushes whatever
 * it lands on — the further it falls, the harder it hits.
 *
 * <p>Because it's heavy, it spawns right next to you and falls onto what's below:</p>
 * <ul>
 *   <li>looking straight down with open space directly below you (e.g. sneaked out over an edge) drops
 *       it straight down your own column;</li>
 *   <li>otherwise into the adjacent cell (one of the 8 around you, incl. diagonals) in the direction
 *       you look — a near-vertical look uses your body facing;</li>
 *   <li>at a height that follows your pitch: eye level when level, one block higher when you look up,
 *       one block lower when you look down — so it stays in view (never more than a block off eye level);</li>
 *   <li>so position yourself above a target and aim its way — it drops in beside you and falls onto it.</li>
 * </ul>
 */
public class LeadWeightItem extends BlockItem {
    private static final double NEAR_VERTICAL = 0.1; // horizontal look below this → aim by body facing
    private static final int DROP_COOLDOWN = 10;     // ticks between throws

    public LeadWeightItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Combat-style tooltip: a guaranteed base hit, then a flat amount per block fallen (the smash
        // damage from LeadWeightEntity). Same on all three tiers — wear doesn't change the hit.
        tooltip.add(CommonComponents.EMPTY); // blank line before the section, like vanilla weapon tooltips
        tooltip.add(Component.translatable("tooltip.theleadage.lead_weight.when_dropped").withStyle(ChatFormatting.GRAY));
        tooltip.add(CommonComponents.space()
                .append(Component.translatable("tooltip.theleadage.lead_weight.base_damage", fmt(LeadWeightEntity.BASE_DAMAGE)))
                .withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(CommonComponents.space()
                .append(Component.translatable("tooltip.theleadage.lead_weight.per_block", fmt(LeadWeightEntity.DAMAGE_PER_BLOCK)))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    /** Trims a whole-number float to "6" rather than "6.0" for the tooltip. */
    private static String fmt(float value) {
        return value == (long) value ? Long.toString((long) value) : Float.toString(value);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos cell = targetCell(level, player, player.getViewVector(1.0f));
        if (cell == null) {
            return InteractionResultHolder.pass(stack); // no free adjacent cell at eye level / one above
        }

        if (level instanceof ServerLevel serverLevel) {
            if (FallingBlock.isFree(level.getBlockState(cell.below()))) {
                // Open space below → it actually falls. Centred at the bottom of the chosen cell, a
                // grid-aligned column, so where it lands matches where it was dropped.
                BlockState state = getBlock().defaultBlockState(); // HANGING = false; it falls
                LeadWeightEntity.inAir(serverLevel, cell.getX() + 0.5, cell.getY(), cell.getZ() + 0.5,
                        state, player);
            } else {
                // Sitting right on a block — just place it, skipping the zero-distance fall + landing fx.
                placeResting(serverLevel, cell);
            }
        }

        // The spawned weight plays its own "starting to fall" whoosh; a resting placement plays a block
        // place sound — so no throw sound here.
        player.swing(hand, true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, DROP_COOLDOWN);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Places a resting weight directly in {@code cell}, no fall (so no chip roll), with a block-place sound. */
    private void placeResting(ServerLevel level, BlockPos cell) {
        BlockState state = getBlock().defaultBlockState();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(cell).is(Fluids.WATER));
        }
        level.setBlock(cell, state, Block.UPDATE_ALL);
        SoundType sound = state.getSoundType();
        level.playSound(null, cell, sound.getPlaceSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f);
    }

    /**
     * The cell to drop into. Looking straight down with open space directly below you drops it into your
     * own column (the cell under your feet); otherwise it's the adjacent cell (one of the 8 around you,
     * incl. diagonals) in the look direction, at a pitch-following height ({@link #verticalOffset}) — or
     * one block higher if that cell is occupied. Null when blocked. Outside the straight-down case it's
     * always horizontally offset, so it never spawns inside the player.
     */
    @Nullable
    public static BlockPos targetCell(Level level, Player player, Vec3 look) {
        BlockPos feet = player.blockPosition();
        // Straight down with room directly below (e.g. sneaked out over an edge) → drop down your column.
        if (Math.sqrt(look.x * look.x + look.z * look.z) < NEAR_VERTICAL && look.y < 0
                && level.getBlockState(feet.below()).canBeReplaced()) {
            return feet.below();
        }
        int[] dir = aimDirection(player.getDirection(), look);
        BlockPos cell = feet.offset(dir[0], verticalOffset(look), dir[1]);
        if (level.getBlockState(cell).canBeReplaced()) {
            return cell;
        }
        BlockPos above = cell.above(); // blocked (a wall in the way) → try one higher
        return level.getBlockState(above).canBeReplaced() ? above : null;
    }

    /**
     * Vertical cell offset above the feet, following the look pitch so the weight stays in view: eye
     * level (1) when looking level, a block higher (2) when looking up, a block lower (0) when looking
     * down. Projects the look one block out (the cell is adjacent) and clamps to one block off eye level.
     */
    public static int verticalOffset(Vec3 look) {
        double horiz = Math.sqrt(look.x * look.x + look.z * look.z);
        if (horiz < NEAR_VERTICAL) {
            return look.y > 0 ? 2 : 0; // straight up / straight down
        }
        return Mth.clamp(1 + (int) Math.round(look.y / horiz), 0, 2);
    }

    /** The 8-way horizontal step nearest the look; a near-vertical look falls back to the body facing. */
    public static int[] aimDirection(Direction facing, Vec3 look) {
        if (Math.sqrt(look.x * look.x + look.z * look.z) < NEAR_VERTICAL) {
            return new int[]{facing.getStepX(), facing.getStepZ()};
        }
        int bestX = 0, bestZ = 0;
        double bestDot = -Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                double dot = (dx * look.x + dz * look.z) / Math.sqrt(dx * dx + dz * dz);
                if (dot > bestDot) {
                    bestDot = dot;
                    bestX = dx;
                    bestZ = dz;
                }
            }
        }
        return new int[]{bestX, bestZ};
    }
}

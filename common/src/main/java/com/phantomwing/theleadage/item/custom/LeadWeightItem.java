package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.entity.custom.LeadWeightEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Block item for the Lead Weight — a drop-from-above weapon. {@code useOn} keeps normal block
 * placement (set it on the ground, or hang it under a block). Aiming at open air instead drops it
 * as a falling {@link LeadWeightEntity} with you as the owner (kill credit), which crushes whatever
 * it lands on — the further it falls, the harder it hits.
 *
 * <p>Because it's heavy, it spawns right next to you and falls onto what's below:</p>
 * <ul>
 *   <li>in the adjacent cell (one of the 8 around you, incl. diagonals) in the direction you look —
 *       a near-vertical look uses your body facing;</li>
 *   <li>at a height that follows your pitch: eye level when level, one block higher when you look up,
 *       one block lower when you look down — so it stays in view (never more than a block off eye level);</li>
 *   <li>so position yourself above a target and aim its way — it drops in beside you and falls onto it.</li>
 * </ul>
 */
public class LeadWeightItem extends BlockItem {
    private static final double NEAR_VERTICAL = 0.1; // horizontal look below this → aim by body facing
    private static final int DROP_COOLDOWN = 10;     // ticks between throws

    /** Durability budget (~12 landings) and the wear per crushed entity / per ground impact. */
    public static final int MAX_DURABILITY = 120;
    public static final int WEAR_PER_ENTITY = 10;
    public static final int WEAR_PER_LANDING = 10;

    public LeadWeightItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos cell = targetCell(level, player, player.getViewVector(1.0f));
        if (cell == null) {
            return InteractionResultHolder.pass(stack); // no free adjacent cell at eye level / one above
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlock().defaultBlockState(); // HANGING = false; it falls
            // Centred at the bottom of the chosen cell: a grid-aligned column, so it drops straight down
            // to the floor there and the block it becomes lands exactly where it was dropped.
            LeadWeightEntity.inAir(serverLevel, cell.getX() + 0.5, cell.getY(), cell.getZ() + 0.5,
                    state, player, stack.getDamageValue());
        }

        // No throw sound here: the spawned weight already plays its "starting to fall" whoosh.
        player.swing(hand, true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, DROP_COOLDOWN);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * The cell to drop into: the adjacent cell (one of the 8 around the player, incl. diagonals) in the
     * look direction, at a pitch-following height ({@link #verticalOffset}) — or one block higher if that
     * cell is occupied. Null when both are blocked. Always horizontally offset, so it never spawns inside
     * the player.
     */
    @Nullable
    public static BlockPos targetCell(Level level, Player player, Vec3 look) {
        int[] dir = aimDirection(player.getDirection(), look);
        BlockPos cell = player.blockPosition().offset(dir[0], verticalOffset(look), dir[1]);
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

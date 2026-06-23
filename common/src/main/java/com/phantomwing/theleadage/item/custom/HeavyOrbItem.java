package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.entity.custom.HeavyOrbEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Block item for the Heavy Orb. {@code useOn} keeps the normal block placement (so
 * you can set it on the ground for decoration, or hang it under a block). Aiming at
 * open air instead drops it as a falling {@link HeavyOrbEntity} with you as the owner
 * (the weapon throw, which carries kill credit), placed one block away in the direction
 * you look:
 *
 * <ul>
 *   <li>the adjacent cell (of the 26 around you) nearest your look — horizontal
 *       directions, including flat diagonals, sit at head/eye level;</li>
 *   <li>downward directions, including down-diagonal corners, sit one block below
 *       your feet;</li>
 *   <li>looking (steeply) up is refused, so you can't drop it on your own head.</li>
 * </ul>
 */
public class HeavyOrbItem extends BlockItem {
    private static final double UP_THRESHOLD = 0.7;     // view.y above this = "looking up" → refuse
    private static final int DROP_COOLDOWN = 10;        // ticks between throws

    /** Durability budget (~12 landings) and the wear per crushed entity / per ground impact. */
    public static final int MAX_DURABILITY = 120;
    public static final int WEAR_PER_ENTITY = 10;
    public static final int WEAR_PER_LANDING = 10;

    public HeavyOrbItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 look = player.getViewVector(1.0f);
        if (look.y > UP_THRESHOLD) {
            // Looking up — refuse, so a dropped orb can't fall back onto the player.
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = targetCell(player, look);
        if (!level.getBlockState(pos).canBeReplaced()) {
            return InteractionResultHolder.pass(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlock().defaultBlockState(); // HANGING = false; it falls
            HeavyOrbEntity.inAir(serverLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    state, player, stack.getDamageValue());
        }

        // No throw sound here: the spawned orb already plays its "starting to fall" whoosh.
        player.swing(hand, true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, DROP_COOLDOWN);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * The block to drop into, one cell away in the direction {@code look}. The look vector is
     * snapped to the nearest of the 17 non-upward neighbour directions (8 horizontal incl. flat
     * diagonals + 9 with a downward component). Horizontal hits sit at head level (feet + 1);
     * downward hits sit one block below the feet. Upward directions are excluded — a steep-up
     * look is already refused by the caller, and a shallow-up look rounds to its level neighbour.
     */
    private static BlockPos targetCell(Player player, Vec3 look) {
        int[] offset = lookOffset(look);
        BlockPos feet = player.blockPosition();
        int y = offset[1] == 0 ? feet.getY() + 1 : feet.getY() - 1; // level ring at head, down ring below feet
        return new BlockPos(feet.getX() + offset[0], y, feet.getZ() + offset[2]);
    }

    /** The non-upward neighbour direction {@code (x, y, z)} nearest the look vector (y is 0 or -1). */
    public static int[] lookOffset(Vec3 look) {
        int[] best = {0, -1, 0};
        double bestDot = -Double.MAX_VALUE;
        for (int oy = 0; oy >= -1; oy--) {            // 0 = level ring, -1 = downward ring (no +1)
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    if (ox == 0 && oy == 0 && oz == 0) {
                        continue;
                    }
                    double dot = (ox * look.x + oy * look.y + oz * look.z)
                            / Math.sqrt(ox * ox + oy * oy + oz * oz);
                    if (dot > bestDot) {
                        bestDot = dot;
                        best[0] = ox;
                        best[1] = oy;
                        best[2] = oz;
                    }
                }
            }
        }
        return best;
    }
}

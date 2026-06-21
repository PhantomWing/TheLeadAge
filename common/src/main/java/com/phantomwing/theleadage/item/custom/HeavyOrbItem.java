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

/**
 * Block item for the Heavy Orb. {@code useOn} keeps the normal block placement (so
 * you can set it on the ground for decoration, or hang it under a block). Aiming at
 * open air instead drops it as a falling {@link HeavyOrbEntity} with you as the owner
 * (the weapon throw, which carries kill credit), placed one block away:
 *
 * <ul>
 *   <li>looking roughly level → one block in front;</li>
 *   <li>looking down → the block directly below you;</li>
 *   <li>looking (steeply) up → refused, so you can't drop it on your own head.</li>
 * </ul>
 */
public class HeavyOrbItem extends BlockItem {
    private static final double UP_THRESHOLD = 0.7;     // view.y above this = "looking up" → refuse
    private static final double DOWN_THRESHOLD = -0.7;  // view.y below this = "looking down" → drop below
    private static final int DROP_COOLDOWN = 10;        // ticks between throws

    public HeavyOrbItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        double lookY = player.getViewVector(1.0f).y;

        BlockPos pos;
        if (lookY > UP_THRESHOLD) {
            // Looking up — refuse, so a dropped orb can't fall back onto the player.
            return InteractionResultHolder.pass(stack);
        } else if (lookY < DOWN_THRESHOLD) {
            pos = player.blockPosition().below();
        } else {
            pos = player.blockPosition().relative(player.getDirection());
        }

        if (!level.getBlockState(pos).canBeReplaced()) {
            return InteractionResultHolder.pass(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlock().defaultBlockState(); // HANGING = false; it falls
            HeavyOrbEntity.inAir(serverLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state, player);
        }

        // No throw sound here: the spawned orb already plays its "starting to fall" whoosh.
        player.swing(hand, true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, DROP_COOLDOWN);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}

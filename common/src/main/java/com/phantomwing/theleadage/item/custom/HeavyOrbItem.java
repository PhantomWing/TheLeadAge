package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.entity.custom.HeavyOrbEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Block item for the Heavy Orb. {@code useOn} keeps the normal block placement (so
 * you can set it on the ground for decoration, or hang it under a block). Aiming at
 * open air instead drops it in front of you as a falling {@link HeavyOrbEntity} with
 * you as the owner — the intentional weapon throw, which carries kill credit.
 */
public class HeavyOrbItem extends BlockItem {
    private static final double AIR_DROP_REACH = 4.0;   // how far ahead we look for a drop spot
    private static final int AIR_DROP_COOLDOWN = 10;    // ticks between throws

    public HeavyOrbItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 end = eye.add(look.scale(AIR_DROP_REACH));
        BlockHitResult clip = level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        // Back off from a hit surface so the orb spawns in open space, not inside it.
        Vec3 spot = clip.getType() == HitResult.Type.BLOCK
                ? clip.getLocation().subtract(look.scale(0.51))
                : end;

        BlockPos pos = BlockPos.containing(spot);
        if (!level.getBlockState(pos).canBeReplaced()) {
            return InteractionResultHolder.pass(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlock().defaultBlockState(); // HANGING = false; it falls
            HeavyOrbEntity.inAir(serverLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state, player);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.6f, 0.8f);
        player.swing(hand, true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, AIR_DROP_COOLDOWN);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}

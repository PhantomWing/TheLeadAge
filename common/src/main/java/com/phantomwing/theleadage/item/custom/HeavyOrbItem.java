package com.phantomwing.theleadage.item.custom;

import com.phantomwing.theleadage.entity.custom.HeavyOrbEntity;
import net.minecraft.core.BlockPos;
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

/**
 * Block item for the Heavy Orb. {@code useOn} keeps the normal block placement (so
 * you can set it on the ground for decoration, or hang it under a block). Aiming at
 * open air instead drops it as a falling {@link HeavyOrbEntity} with you as the owner
 * (the weapon throw, which carries kill credit). It spawns a fixed reach ahead of your
 * eyes in the direction you look:
 *
 * <ul>
 *   <li>measured from your actual eye position, so it stays the same distance ahead
 *       whether you're centred on a block or right at its edge;</li>
 *   <li>biased to eye level — a level or only gently-downward look drops it at eye
 *       height in front of you; it descends only as your look tips further down;</li>
 *   <li>a near-vertical look drops it straight down at your feet, and looking
 *       (steeply) up is refused so you can't drop it on your own head.</li>
 * </ul>
 */
public class HeavyOrbItem extends BlockItem {
    private static final double UP_THRESHOLD = 0.7;     // view.y above this = "looking up" → refuse
    private static final double STRAIGHT_DOWN_HORIZ = 0.15; // horizontal look below this = near-vertical → drop at the feet
    private static final double REACH = 1.3;            // blocks ahead of the eyes the orb spawns (horizontal)
    private static final double EYE_LEVEL_BIAS = 0.5;   // looks shallower than ~27° down stay at eye level
    private static final double MAX_DROP = 2.0;         // never descends more than this far below eye level
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

        Vec3 spawn = dropPosition(player.getEyePosition(1.0f), player.getY(), look);
        if (!level.getBlockState(BlockPos.containing(spawn)).canBeReplaced()) {
            return InteractionResultHolder.pass(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlock().defaultBlockState(); // HANGING = false; it falls
            HeavyOrbEntity.inAir(serverLevel, spawn.x, spawn.y, spawn.z, state, player, stack.getDamageValue());
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
     * Where to spawn the orb: a point {@link #REACH} blocks ahead of the eyes along the look's
     * horizontal direction, descending with the look but lifted toward eye level so a level or
     * gently-downward glance keeps it at eye height. A near-vertical look ({@code horiz} below
     * {@link #STRAIGHT_DOWN_HORIZ}) drops it straight down just past the feet instead. Measuring
     * from {@code eye} (not a floored block) keeps the reach constant regardless of where in the
     * block the player stands.
     */
    public static Vec3 dropPosition(Vec3 eye, double feetY, Vec3 look) {
        double horiz = Math.sqrt(look.x * look.x + look.z * look.z);
        if (horiz < STRAIGHT_DOWN_HORIZ) {
            return new Vec3(eye.x, feetY - 0.5, eye.z); // near-vertical: straight down at the feet
        }
        double nx = look.x / horiz;
        double nz = look.z / horiz;
        double pitch = -look.y / horiz;                 // 0 = level, 1 = 45° down, larger = steeper
        double drop = Mth.clamp((pitch - EYE_LEVEL_BIAS) * REACH, 0.0, MAX_DROP);
        return new Vec3(eye.x + nx * REACH, eye.y - drop, eye.z + nz * REACH);
    }
}

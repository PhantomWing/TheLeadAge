package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.platform.CommonConfig;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Water a block of raw lead leaches into. Raw lead touching water on any side, at any depth, leaks into the water
 * around it a block at a time, as water wets farmland: each block of water waits a little after the one before it,
 * so the front spreads raggedly, out to {@value #MAX_RADIUS} blocks in a diamond, through water and open space but not
 * through walls. The lead's {@link RawLeadBlock#CONTAMINATION} counts the steps of its spreading. A player in that
 * water gets Lead Sickness, worse the longer they stay. The water is tinted for it, fading in over a few steps, and
 * grey specks drift in it, which rendering mods that draw water their own way still show.
 */
public final class LeadWater {
    /** How often the water is checked for players, and the lead's spreading takes a step. */
    public static final int INTERVAL = 10;
    /** How far raw lead reaches, counted along the blocks' sides: a diamond. */
    public static final int MAX_RADIUS = 4;
    /** The most steps a block of water waits after the one before it: how ragged the spreading is. */
    private static final int MOST_WAIT = 6;
    /** The steps a block of water takes from first touched to fully tinted. */
    private static final int FADE = 3;
    /** The steps raw lead takes to reach as far as it does and tint all of it. */
    public static final int MAX_STEP = MAX_RADIUS * MOST_WAIT + FADE - 1;

    private LeadWater() {
    }

    /** Whether raw lead at {@code pos} touches water on any side. */
    public static boolean touchesWater(BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /** Whether raw lead at {@code lead}, {@code step} steps into its spreading, contaminates the water at {@code water}. */
    public static boolean contaminates(BlockGetter level, BlockPos lead, int step, BlockPos water) {
        return since(level, lead, step, water) >= 0;
    }

    /**
     * How many steps ago raw lead at {@code lead}, {@code step} steps into its spreading, reached the water at
     * {@code water}, or -1 where it hasn't.
     */
    private static int since(BlockGetter level, BlockPos lead, int step, BlockPos water) {
        int dx = water.getX() - lead.getX();
        int dy = water.getY() - lead.getY();
        int dz = water.getZ() - lead.getZ();
        int distance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
        if (step <= 0 || distance == 0 || distance > MAX_RADIUS || !level.getFluidState(water).is(FluidTags.WATER)) {
            return -1;
        }
        int since = step - arrival(lead, dx, dy, dz);
        return since >= 0 && clearBetween(level, lead, water) ? since : -1;
    }

    /**
     * The step at which raw lead at {@code lead} reaches the block {@code dx, dy, dz} from it: the earliest of the
     * blocks one closer, plus this block's own wait. The waits are random but fixed for the place, so the client and
     * the server agree on them without being told.
     */
    private static int arrival(BlockPos lead, int dx, int dy, int dz) {
        if (dx == 0 && dy == 0 && dz == 0) {
            return 0;
        }
        int before = Integer.MAX_VALUE;
        if (dx != 0) {
            before = Math.min(before, arrival(lead, dx - Integer.signum(dx), dy, dz));
        }
        if (dy != 0) {
            before = Math.min(before, arrival(lead, dx, dy - Integer.signum(dy), dz));
        }
        if (dz != 0) {
            before = Math.min(before, arrival(lead, dx, dy, dz - Integer.signum(dz)));
        }
        long seed = HashCommon.murmurHash3(Mth.getSeed(lead.getX() + dx, lead.getY() + dy, lead.getZ() + dz) ^ Mth.getSeed(lead));
        return before + 1 + (int) Math.floorMod(seed, (long) MOST_WAIT);
    }

    /** Whether nothing solid stands between the lead and the water. */
    private static boolean clearBetween(BlockGetter level, BlockPos lead, BlockPos water) {
        Vec3 from = Vec3.atCenterOf(lead);
        Vec3 to = Vec3.atCenterOf(water);
        // From just outside the lead, which would otherwise be the first thing the ray hits.
        Vec3 start = from.add(to.subtract(from).normalize().scale(0.9));
        BlockHitResult hit = level.clip(new ClipContext(start, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                CollisionContext.empty()));
        return hit.getType() != HitResult.Type.BLOCK || hit.getBlockPos().equals(water);
    }

    /** Raw lead {@code step} steps into its spreading: the players in the water it contaminates breathe it. */
    public static void contaminate(ServerLevel level, BlockPos lead, int step) {
        if (!CommonConfig.leadWaterContamination()) {
            return;
        }
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(lead).inflate(MAX_RADIUS),
                player -> !player.isSpectator() && player.isAlive())) {
            if (contaminates(level, lead, step, player.blockPosition())
                    || contaminates(level, lead, step, BlockPos.containing(player.getEyePosition()))) {
                LeadFumes.touch(player, LeadFumes.FUMES_STAGE_TICKS);
            }
        }
    }

    /**
     * How strongly the water at {@code water} is contaminated, from 0 to 1: strongest beside the lead, fading to half
     * at the edge of its reach, and fading in over a few steps once reached, from the raw lead that tints it most.
     */
    public static float strength(BlockGetter level, BlockPos water) {
        float strongest = 0.0F;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int dx = -MAX_RADIUS; dx <= MAX_RADIUS; dx++) {
            int restY = MAX_RADIUS - Math.abs(dx);
            for (int dy = -restY; dy <= restY; dy++) {
                int restZ = restY - Math.abs(dy);
                for (int dz = -restZ; dz <= restZ; dz++) {
                    BlockState state = level.getBlockState(at.setWithOffset(water, dx, dy, dz));
                    if (!(state.getBlock() instanceof RawLeadBlock)) {
                        continue;
                    }
                    int since = since(level, at, state.getValue(RawLeadBlock.CONTAMINATION), water);
                    if (since >= 0) {
                        int distance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                        float near = 1.0F - 0.5F * (distance - 1) / (MAX_RADIUS - 1);
                        strongest = Math.max(strongest, near * Math.min(1.0F, (since + 1) / (float) FADE));
                    }
                }
            }
        }
        return strongest;
    }
}

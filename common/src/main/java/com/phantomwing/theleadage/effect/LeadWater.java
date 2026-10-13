package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
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
 * around it, its reach growing a block at a time from the water beside it to {@value #MAX_RADIUS} blocks
 * ({@link RawLeadBlock#CONTAMINATION}), through water and open space but not through walls. A player in that water
 * gets Lead Sickness, worse the longer they stay. The water is tinted for it, and grey specks drift in it, which
 * rendering mods that draw water their own way still show.
 */
public final class LeadWater {
    /** How often the water is checked for players, and the lead's reach for growing. */
    public static final int INTERVAL = 10;
    public static final int MAX_RADIUS = 4;

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

    /** Whether raw lead at {@code lead}, reaching {@code radius} blocks, contaminates the water at {@code water}. */
    public static boolean contaminates(BlockGetter level, BlockPos lead, int radius, BlockPos water) {
        if (radius <= 0 || lead.distSqr(water) > radius * radius || !level.getFluidState(water).is(FluidTags.WATER)) {
            return false;
        }
        Vec3 from = Vec3.atCenterOf(lead);
        Vec3 to = Vec3.atCenterOf(water);
        // From just outside the lead, which would otherwise be the first thing the ray hits.
        Vec3 start = from.add(to.subtract(from).normalize().scale(0.9));
        BlockHitResult hit = level.clip(new ClipContext(start, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                CollisionContext.empty()));
        return hit.getType() != HitResult.Type.BLOCK || hit.getBlockPos().equals(water);
    }

    /** Raw lead reaching {@code radius} blocks: the players in the water it contaminates breathe it. */
    public static void contaminate(ServerLevel level, BlockPos lead, int radius) {
        if (!CommonConfig.leadWaterContamination()) {
            return;
        }
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(lead).inflate(radius),
                player -> !player.isSpectator() && player.isAlive())) {
            if (contaminates(level, lead, radius, player.blockPosition())
                    || contaminates(level, lead, radius, BlockPos.containing(player.getEyePosition()))) {
                LeadFumes.touch(player, LeadFumes.FUMES_STAGE_TICKS);
            }
        }
    }

    /**
     * How strongly the water at {@code water} is contaminated, from 0 to 1: strongest beside the lead, fading towards
     * the edge of its reach, from the nearest raw lead that reaches it.
     */
    public static float strength(BlockGetter level, BlockPos water) {
        float strongest = 0.0F;
        for (BlockPos at : BlockPos.betweenClosed(water.offset(-MAX_RADIUS, -MAX_RADIUS, -MAX_RADIUS),
                water.offset(MAX_RADIUS, MAX_RADIUS, MAX_RADIUS))) {
            BlockState state = level.getBlockState(at);
            if (!(state.getBlock() instanceof RawLeadBlock)) {
                continue;
            }
            int radius = state.getValue(RawLeadBlock.CONTAMINATION);
            if (contaminates(level, at, radius, water)) {
                strongest = Math.max(strongest, 1.0F - (float) Math.sqrt(at.distSqr(water)) / (radius + 1));
            }
        }
        return strongest;
    }
}

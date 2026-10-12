package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Water a block of raw lead contaminates, as potent sulfur's does in 26.2, with Lead Sickness where sulfur gives
 * Nausea and only players breathing it. A water source on top makes the block wet ({@code RawLeadBlock}); its
 * fumes come out at the surface above, when the water is at most {@value #WATER_ABOVE} blocks deep, and reach
 * whoever is in that water within {@value #RANGE} blocks of the spot and in sight of it.
 */
public final class LeadWater {
    /** How often the fumes are breathed, as sulfur's. */
    public static final int INTERVAL = 10;
    public static final double RANGE = 3.0;
    private static final int WATER_ABOVE = 4;

    private LeadWater() {
    }

    /** Whether a block of raw lead at {@code pos} has a water source on top, so it contaminates the water. */
    public static boolean isWet(BlockGetter level, BlockPos pos) {
        return level.getFluidState(pos.above()).isSourceOfType(Fluids.WATER);
    }

    /**
     * Where the fumes come out: the first block over the water above the lead, or {@code null} where the water is
     * deeper than {@value #WATER_ABOVE} blocks or something solid caps it.
     */
    @Nullable
    public static BlockPos gasSpot(BlockGetter level, BlockPos lead) {
        int top = lead.getY() + WATER_ABOVE + 1;
        for (BlockPos.MutableBlockPos at = lead.above().mutable(); at.getY() <= top; at.move(Direction.UP)) {
            BlockState state = level.getBlockState(at);
            if (level.getFluidState(at).isSourceOfType(Fluids.WATER) && (state.is(Blocks.WATER) || passable(state, level, at))) {
                continue;
            }
            return state.isAir() || passable(state, level, at) ? at.immutable() : null;
        }
        return null;
    }

    /**
     * Whether the fumes from {@code spot} reach a point - a player's eyes, a puff's place: one in the open or in
     * the water, with water below it, within {@value #RANGE} blocks of the spot and in sight of the water over the
     * lead.
     */
    public static boolean reaches(Level level, BlockPos spot, Vec3 at) {
        BlockPos atPos = BlockPos.containing(at);
        if (!passable(level.getBlockState(atPos), level, atPos) || at.distanceToSqr(Vec3.atCenterOf(spot)) > RANGE * RANGE) {
            return false;
        }
        Vec3 below = at.with(Direction.Axis.Y, at.y - 1.0);
        return level.getFluidState(BlockPos.containing(below)).isSourceOfType(Fluids.WATER)
                && level.clip(new ClipContext(Vec3.atCenterOf(spot.below()), below, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, CollisionContext.empty())).getType() != HitResult.Type.BLOCK;
    }

    /** The fumes of the raw lead at {@code lead}: the players they reach breathe them. */
    public static void contaminate(ServerLevel level, BlockPos lead) {
        if (!CommonConfig.leadWaterContamination()) {
            return;
        }
        BlockPos spot = gasSpot(level, lead);
        if (spot == null) {
            return;
        }
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(spot).inflate(RANGE - 0.5),
                player -> !player.isSpectator() && player.isAlive())) {
            if (reaches(level, spot, player.getEyePosition())) {
                LeadFumes.touch(player);
            }
        }
    }

    /** Air, water, or a block with nothing to collide with: what the fumes pass through. */
    private static boolean passable(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isAir() || state.is(Blocks.WATER) || state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty();
    }
}

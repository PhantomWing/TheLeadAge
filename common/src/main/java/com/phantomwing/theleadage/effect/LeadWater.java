package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.platform.CommonConfig;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Water a block of raw lead leaches into. Raw lead touching water on any side, at any depth, leaks into the water
 * around it a block at a time, as water wets farmland: each block of water waits a little after the one it was reached
 * from, so the front spreads raggedly. It leaks through the water alone, around whatever stands in it, as far as a
 * route of {@value #MAX_RADIUS} blocks takes it, and tints a block further, faintly and harmlessly, so it fades into
 * the clean water. The lead's {@link RawLeadBlock#CONTAMINATION} counts the steps of its spreading. A player in the
 * contaminated water gets Lead Sickness, worse the longer they stay. The water is tinted for it, fading in over a few
 * steps, and dark specks drift in it, which shader packs that draw water their own way still show.
 */
public final class LeadWater {
    /** How often the water is checked for players, and the lead's spreading takes a step. */
    public static final int INTERVAL = 10;
    /** How far raw lead contaminates: the longest route through the water, in blocks. */
    public static final int MAX_RADIUS = 4;
    /** How far it tints: a block past what it contaminates, the edge it fades out over. */
    public static final int TINT_RADIUS = MAX_RADIUS + 1;
    /** How strongly the edge is tinted, beside the weakest contaminated water's. */
    private static final float EDGE = 0.5F;
    /** The most steps a block of water waits after the one it was reached from: how ragged the spreading is. */
    private static final int MOST_WAIT = 6;
    /** The steps a block of water takes from first reached to fully tinted. */
    private static final int FADE = 3;
    /** The steps raw lead takes to reach as far as it does and tint all of it. */
    public static final int MAX_STEP = TINT_RADIUS * MOST_WAIT + FADE - 1;

    private static final int SIZE = TINT_RADIUS * 2 + 1;
    private static final List<Direction> SIDES = List.of(Direction.values());

    private LeadWater() {
    }

    /** Whether raw lead at {@code pos} touches water on any side. */
    public static boolean touchesWater(BlockGetter level, BlockPos pos) {
        for (Direction direction : SIDES) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /** Where raw lead at {@code lead} leaks to as the water around it stands now, and when. */
    public static Leak leak(BlockGetter level, BlockPos lead) {
        return new Leak(level, lead.immutable());
    }

    /** Raw lead {@code step} steps into its spreading: the players in the water it contaminates breathe it. */
    public static void contaminate(ServerLevel level, BlockPos lead, int step) {
        if (!CommonConfig.leadWaterContamination()) {
            return;
        }
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(lead).inflate(MAX_RADIUS),
                player -> !player.isSpectator() && player.isAlive());
        if (players.isEmpty()) {
            return;
        }
        Leak leak = leak(level, lead);
        for (Player player : players) {
            if (leak.poisons(step, player.blockPosition()) || leak.poisons(step, BlockPos.containing(player.getEyePosition()))) {
                LeadFumes.touch(player, LeadFumes.FUMES_STAGE_TICKS);
            }
        }
    }

    /**
     * How strongly the water at {@code water} is tinted, from 0 to 1: strongest beside the lead, fading to half at the
     * end of what it contaminates and to a quarter over the edge past it, and fading in over a few steps once reached,
     * from the raw lead that tints it most.
     */
    public static float strength(BlockGetter level, BlockPos water) {
        return strength(level, water, lead -> leak(level, lead));
    }

    /** As {@link #strength(BlockGetter, BlockPos)}, with each lead's routes found by {@code leaks}. */
    public static float strength(BlockGetter level, BlockPos water, Function<BlockPos, Leak> leaks) {
        float strongest = 0.0F;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int dx = -TINT_RADIUS; dx <= TINT_RADIUS; dx++) {
            int restY = TINT_RADIUS - Math.abs(dx);
            for (int dy = -restY; dy <= restY; dy++) {
                int restZ = restY - Math.abs(dy);
                for (int dz = -restZ; dz <= restZ; dz++) {
                    BlockState state = level.getBlockState(at.setWithOffset(water, dx, dy, dz));
                    if (state.getBlock() instanceof RawLeadBlock && state.getValue(RawLeadBlock.CONTAMINATION) > 0) {
                        strongest = Math.max(strongest, leaks.apply(at).strength(state.getValue(RawLeadBlock.CONTAMINATION), water));
                    }
                }
            }
        }
        return strongest;
    }

    /**
     * Where one block of raw lead leaks to: each block of water a route of up to {@value #TINT_RADIUS} blocks through the
     * water reaches, the length of the shortest such route, and the step of the spreading that reaches it - the
     * earliest of the blocks one closer along a route, plus this block's own wait. The waits are random but fixed for
     * the place, so the client and the server agree on them without being told.
     */
    public static final class Leak {
        private static final byte DRY = -1;

        private final BlockPos lead;
        private final int[] arrival = new int[SIZE * SIZE * SIZE];
        /** The shortest route to each block, in blocks; 0 where nothing has looked yet, {@link #DRY} where no water is. */
        private final byte[] route = new byte[SIZE * SIZE * SIZE];
        /** Every block of water the lead contaminates, once its spreading reaches it: the edge left out. */
        private final IntArrayList reach = new IntArrayList();

        private Leak(BlockGetter level, BlockPos lead) {
            this.lead = lead;
            Arrays.fill(arrival, Integer.MAX_VALUE);
            int center = index(0, 0, 0);
            arrival[center] = 0;
            route[center] = DRY;
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
            IntArrayList frontier = IntArrayList.of(center);
            for (byte length = 1; length <= TINT_RADIUS && !frontier.isEmpty(); length++) {
                IntArrayList next = new IntArrayList();
                for (int i = 0; i < frontier.size(); i++) {
                    int from = frontier.getInt(i);
                    int fx = from / (SIZE * SIZE) - TINT_RADIUS;
                    int fy = from / SIZE % SIZE - TINT_RADIUS;
                    int fz = from % SIZE - TINT_RADIUS;
                    for (Direction side : SIDES) {
                        int x = fx + side.getStepX();
                        int y = fy + side.getStepY();
                        int z = fz + side.getStepZ();
                        int to = index(x, y, z);
                        if (route[to] == 0) {
                            if (!level.getFluidState(at.setWithOffset(lead, x, y, z)).is(FluidTags.WATER)) {
                                route[to] = DRY;
                                continue;
                            }
                            route[to] = length;
                            next.add(to);
                            if (length <= MAX_RADIUS) {
                                reach.add(to);
                            }
                        }
                        if (route[to] == length) {
                            arrival[to] = Math.min(arrival[to], arrival[from] + waitAt(x, y, z));
                        }
                    }
                }
                frontier = next;
            }
        }

        /** Whether the water at {@code water} is contaminated, {@code step} steps into the spreading: the edge isn't. */
        public boolean poisons(int step, BlockPos water) {
            return since(step, water) >= 0 && route[indexOf(water)] <= MAX_RADIUS;
        }

        /**
         * How many steps ago, {@code step} steps into its spreading, the lead reached the water at {@code water}, its
         * edge included, or -1 where it hasn't.
         */
        public int since(int step, BlockPos water) {
            int at = indexOf(water);
            if (step <= 0 || at < 0 || route[at] <= 0) {
                return -1;
            }
            int since = step - arrival[at];
            return since >= 0 ? since : -1;
        }

        /** How strongly the water at {@code water} is tinted, {@code step} steps into the spreading, from 0 to 1. */
        public float strength(int step, BlockPos water) {
            int since = since(step, water);
            if (since < 0) {
                return 0.0F;
            }
            int length = route[indexOf(water)];
            float near = 1.0F - 0.5F * (Math.min(length, MAX_RADIUS) - 1) / (MAX_RADIUS - 1);
            return (length > MAX_RADIUS ? near * EDGE : near) * Math.min(1.0F, (since + 1) / (float) FADE);
        }

        /** How many blocks of water the lead contaminates once it has spread as far as it does. */
        public int size() {
            return reach.size();
        }

        /** A block of water the lead contaminates, at random, if the spreading has reached it {@code step} steps in. */
        @Nullable
        public BlockPos pick(int step, RandomSource random) {
            if (reach.isEmpty()) {
                return null;
            }
            int at = reach.getInt(random.nextInt(reach.size()));
            if (step < arrival[at]) {
                return null;
            }
            return lead.offset(at / (SIZE * SIZE) - TINT_RADIUS, at / SIZE % SIZE - TINT_RADIUS, at % SIZE - TINT_RADIUS);
        }

        private int waitAt(int dx, int dy, int dz) {
            long seed = HashCommon.murmurHash3(Mth.getSeed(lead.getX() + dx, lead.getY() + dy, lead.getZ() + dz) ^ Mth.getSeed(lead));
            return 1 + (int) Math.floorMod(seed, (long) MOST_WAIT);
        }

        private int indexOf(BlockPos pos) {
            int dx = pos.getX() - lead.getX();
            int dy = pos.getY() - lead.getY();
            int dz = pos.getZ() - lead.getZ();
            if (Math.abs(dx) > TINT_RADIUS || Math.abs(dy) > TINT_RADIUS || Math.abs(dz) > TINT_RADIUS) {
                return -1;
            }
            return index(dx, dy, dz);
        }

        private static int index(int dx, int dy, int dz) {
            return ((dx + TINT_RADIUS) * SIZE + dy + TINT_RADIUS) * SIZE + dz + TINT_RADIUS;
        }
    }
}

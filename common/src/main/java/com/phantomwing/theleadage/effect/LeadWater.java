package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.platform.LeadSettings;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Water a block of raw lead leaches into. Raw lead touching water on any side, at any depth, leaks into the water
 * around it a block at a time, as water wets farmland: each block of water waits a little after the one it was reached
 * from, so the front spreads raggedly. It leaks through the water alone, around whatever stands in it, as far as a
 * route of the configured {@link #range()} takes it, and tints a block further, faintly and harmlessly, so it fades
 * into the clean water. The lead's {@link RawLeadBlock#CONTAMINATION} counts the steps of its spreading. A player in
 * the contaminated water gets Lead Sickness, worse the longer they stay. The water is tinted for it, fading in over a
 * few steps, murky from inside - the murk thickening over the last blocks before it - and dark specks drift in it,
 * which shader packs that draw water their own way still show.
 */
public final class LeadWater {
    /** How often the water is checked for players, and the lead's spreading takes a step. */
    public static final int INTERVAL = 10;
    /** The furthest the config lets raw lead contaminate. */
    public static final int MAX_RANGE = LeadSettings.MAX_WATER_RANGE;
    /** How many blocks before the contaminated water the murk seen from inside it begins. */
    public static final int MURK_AHEAD = 6;
    /** How strongly the edge is tinted, beside the weakest contaminated water's. */
    private static final float EDGE = 0.5F;
    /** The most steps a block of water waits after the one it was reached from: how ragged the spreading is. */
    private static final int MOST_WAIT = 6;
    /** The steps a block of water takes from first reached to fully tinted. */
    private static final int FADE = 3;
    /** The most steps raw lead takes to reach as far as the config lets it, and tint all of it. */
    public static final int MAX_STEP = lastStep(MAX_RANGE);

    private static final List<Direction> SIDES = List.of(Direction.values());

    private LeadWater() {
    }

    /**
     * How far raw lead contaminates: the longest route through the water, in blocks, 0 turning it off. The server's
     * setting, which a client has from the server it plays on ({@link LeadSettings}).
     */
    public static int range(boolean clientSide) {
        return LeadSettings.waterRange(clientSide);
    }

    /** The step at which raw lead reaching {@code range} blocks has spread as far as it does and tinted all of it. */
    public static int lastStep(int range) {
        return (range + 1) * MOST_WAIT + FADE - 1;
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

    /**
     * Where raw lead at {@code lead} leaks to as the water around it stands now, and when, reaching {@code range}:
     * the water it poisons, its edge and the murk ahead of it.
     */
    public static Leak leak(BlockGetter level, BlockPos lead, int range) {
        return new Leak(level, lead.immutable(), range, MURK_AHEAD);
    }

    /** As {@link #leak}, only as far as the water it poisons: all the server needs. */
    private static Leak poisonLeak(BlockGetter level, BlockPos lead) {
        return new Leak(level, lead.immutable(), range(false), 0);
    }

    /** Raw lead {@code step} steps into its spreading: the players in the water it contaminates breathe it. */
    public static void contaminate(ServerLevel level, BlockPos lead, int step) {
        int range = range(false);
        if (range == 0) {
            return;
        }
        // The routes are found only for a player a dose would reach now: in water a route could reach, and past the stage of
        // their last dose. Raw lead under a pond someone swims in looks for them a few times a minute, not every check.
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(lead).inflate(range),
                player -> !player.isSpectator() && player.isAlive() && LeadFumes.isDue(player, LeadFumes.FUMES_STAGE_TICKS)
                        && (mayReach(level, lead, player.blockPosition(), range)
                        || mayReach(level, lead, BlockPos.containing(player.getEyePosition()), range)));
        if (players.isEmpty()) {
            return;
        }
        Leak leak = poisonLeak(level, lead);
        for (Player player : players) {
            if (leak.poisons(step, player.blockPosition()) || leak.poisons(step, BlockPos.containing(player.getEyePosition()))) {
                LeadFumes.touch(player, LeadFumes.FUMES_STAGE_TICKS);
            }
        }
    }

    /** Whether a route from raw lead at {@code lead} could reach {@code at}: water no further away than the range. */
    private static boolean mayReach(BlockGetter level, BlockPos lead, BlockPos at, int range) {
        return at.distManhattan(lead) <= range && level.getFluidState(at).is(FluidTags.WATER);
    }

    /** Whether a block is raw lead leaking into water: dry raw lead, in storage or a lead vein, tints nothing. */
    public static boolean isLeaking(BlockState state) {
        return state.getBlock() instanceof RawLeadBlock && state.getValue(RawLeadBlock.CONTAMINATION) > 0;
    }

    /**
     * The raw lead leaking into water in the chunk section {@code section} and the 26 around it, each with how far it has
     * spread and its routes ({@code leaks}): all the lead that can tint or murk water within 16 blocks of the section. Only
     * the sections whose palettes may hold leaking lead are read block by block, so an ocean costs a look at 27 palettes.
     */
    public static Nearby nearby(LevelReader level, SectionPos section, Function<BlockPos, Leak> leaks) {
        LongArrayList leads = new LongArrayList();
        IntArrayList steps = new IntArrayList();
        for (int x = section.x() - 1; x <= section.x() + 1; x++) {
            for (int z = section.z() - 1; z <= section.z() + 1; z++) {
                ChunkAccess chunk = level.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }
                for (int y = section.y() - 1; y <= section.y() + 1; y++) {
                    int index = chunk.getSectionIndexFromSectionY(y);
                    if (index < 0 || index >= chunk.getSectionsCount()) {
                        continue;
                    }
                    LevelChunkSection blocks = chunk.getSection(index);
                    if (blocks.hasOnlyAir() || !blocks.maybeHas(LeadWater::isLeaking)) {
                        continue;
                    }
                    for (int bx = 0; bx < SectionPos.SECTION_SIZE; bx++) {
                        for (int by = 0; by < SectionPos.SECTION_SIZE; by++) {
                            for (int bz = 0; bz < SectionPos.SECTION_SIZE; bz++) {
                                BlockState state = blocks.getBlockState(bx, by, bz);
                                if (isLeaking(state)) {
                                    leads.add(BlockPos.asLong(SectionPos.sectionToBlockCoord(x, bx),
                                            SectionPos.sectionToBlockCoord(y, by), SectionPos.sectionToBlockCoord(z, bz)));
                                    steps.add(state.getValue(RawLeadBlock.CONTAMINATION));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (leads.isEmpty()) {
            return Nearby.NONE;
        }
        Leak[] found = new Leak[leads.size()];
        for (int i = 0; i < found.length; i++) {
            found[i] = leaks.apply(BlockPos.of(leads.getLong(i)));
        }
        return new Nearby(leads.toLongArray(), steps.toIntArray(), found);
    }

    /** The raw lead leaking into the water around a chunk section ({@link #nearby}): each lead, its step and its routes. */
    public record Nearby(long[] leads, int[] steps, Leak[] leaks) {
        public static final Nearby NONE = new Nearby(new long[0], new int[0], new Leak[0]);

        public boolean isEmpty() {
            return leads.length == 0;
        }

        /**
         * How strongly the water at {@code water} is tinted, from 0 to 1: strongest beside the lead, fading to half at the
         * end of what it contaminates and to a quarter over the edge past it, and fading in over a few steps once
         * reached, from the raw lead that tints it most, reaching {@code range}.
         */
        public float strength(BlockPos water, int range) {
            return strongest(water, range + 1, false);
        }

        /**
         * How thick the murk is seen from inside the water at {@code water}, from 0 to 1: at its thickest throughout the
         * contaminated water, thinning over the {@value #MURK_AHEAD} blocks before it, from the raw lead that murks it most.
         */
        public float murk(BlockPos water, int range) {
            return strongest(water, range + MURK_AHEAD, true);
        }

        private float strongest(BlockPos water, int reach, boolean murk) {
            float strongest = 0.0F;
            for (int i = 0; i < leads.length; i++) {
                long lead = leads[i];
                if (Math.abs(BlockPos.getX(lead) - water.getX()) + Math.abs(BlockPos.getY(lead) - water.getY())
                        + Math.abs(BlockPos.getZ(lead) - water.getZ()) > reach) {
                    continue;
                }
                strongest = Math.max(strongest, murk ? leaks[i].murk(steps[i], water) : leaks[i].strength(steps[i], water));
            }
            return strongest;
        }
    }

    /**
     * Where one block of raw lead leaks to: each block of water a route through the water reaches within its range and
     * the {@value #MURK_AHEAD} blocks of murk past it, the length of the shortest such route, and the step of the
     * spreading that reaches it - the earliest of the blocks one closer along a route, plus this block's own wait. The
     * waits are random but fixed for the place, so the client and the server agree on them without being told. The
     * murk past the tinted edge waits for nothing: it is there as soon as the water before it is.
     */
    public static final class Leak {
        private static final byte DRY = -1;

        private final BlockPos lead;
        private final int range;
        private final int extent;
        /** Whether every chunk the routes could reach was loaded when they were found. */
        private final boolean whole;
        private final int size;
        private final int[] arrival;
        /** The shortest route to each block, in blocks; 0 where nothing has looked yet, {@link #DRY} where no water is. */
        private final byte[] route;
        /** Every block of water the lead contaminates, once its spreading reaches it: the edge left out. */
        private final IntArrayList reach = new IntArrayList();

        private Leak(BlockGetter level, BlockPos lead, int range, int ahead) {
            this.lead = lead;
            this.range = range;
            this.extent = range == 0 ? 0 : range + ahead;
            this.whole = !(level instanceof LevelReader reader) || reader.hasChunksAt(lead.getX() - extent, lead.getZ() - extent,
                    lead.getX() + extent, lead.getZ() + extent);
            this.size = extent * 2 + 1;
            this.arrival = new int[size * size * size];
            this.route = new byte[size * size * size];
            Arrays.fill(arrival, Integer.MAX_VALUE);
            int center = index(0, 0, 0);
            arrival[center] = 0;
            route[center] = DRY;
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
            IntArrayList frontier = IntArrayList.of(center);
            for (byte length = 1; length <= extent && !frontier.isEmpty(); length++) {
                IntArrayList next = new IntArrayList();
                for (int i = 0; i < frontier.size(); i++) {
                    int from = frontier.getInt(i);
                    int fx = from / (size * size) - extent;
                    int fy = from / size % size - extent;
                    int fz = from % size - extent;
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
                            if (length <= range) {
                                reach.add(to);
                            }
                        }
                        if (route[to] == length) {
                            int wait = length <= range + 1 ? waitAt(x, y, z) : 0;
                            arrival[to] = Math.min(arrival[to], arrival[from] + wait);
                        }
                    }
                }
                frontier = next;
            }
        }

        /** The range this was found for. */
        public int range() {
            return range;
        }

        /** Whether every chunk its routes could reach was loaded: routes found otherwise end short at the gap. */
        public boolean whole() {
            return whole;
        }

        /** Whether the water at {@code water} is contaminated, {@code step} steps into the spreading: the edge isn't. */
        public boolean poisons(int step, BlockPos water) {
            return since(step, water) >= 0 && route[indexOf(water)] <= range;
        }

        /**
         * How many steps ago, {@code step} steps into its spreading, the lead reached the water at {@code water}, its
         * edge and murk included, or -1 where it hasn't.
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
            int length = since < 0 ? 0 : route[indexOf(water)];
            if (length == 0 || length > range + 1) {
                return 0.0F;
            }
            float near = range > 1 ? 1.0F - 0.5F * (Math.min(length, range) - 1) / (range - 1) : 1.0F;
            return (length > range ? near * EDGE : near) * fadeIn(since);
        }

        /** How thick the murk is seen from inside the water at {@code water}, {@code step} steps into the spreading. */
        public float murk(int step, BlockPos water) {
            int since = since(step, water);
            if (since < 0) {
                return 0.0F;
            }
            int beyond = route[indexOf(water)] - range;
            return (beyond <= 0 ? 1.0F : 1.0F - beyond / (float) (MURK_AHEAD + 1)) * fadeIn(since);
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
            return lead.offset(at / (size * size) - extent, at / size % size - extent, at % size - extent);
        }

        private static float fadeIn(int since) {
            return Math.min(1.0F, (since + 1) / (float) FADE);
        }

        private int waitAt(int dx, int dy, int dz) {
            long seed = HashCommon.murmurHash3(Mth.getSeed(lead.getX() + dx, lead.getY() + dy, lead.getZ() + dz) ^ Mth.getSeed(lead));
            return 1 + (int) Math.floorMod(seed, (long) MOST_WAIT);
        }

        private int indexOf(BlockPos pos) {
            int dx = pos.getX() - lead.getX();
            int dy = pos.getY() - lead.getY();
            int dz = pos.getZ() - lead.getZ();
            if (Math.abs(dx) > extent || Math.abs(dy) > extent || Math.abs(dz) > extent) {
                return -1;
            }
            return index(dx, dy, dz);
        }

        private int index(int dx, int dy, int dz) {
            return ((dx + extent) * size + dy + extent) * size + dz + extent;
        }
    }
}

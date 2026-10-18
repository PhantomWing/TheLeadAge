package com.phantomwing.theleadage.effect;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.platform.LeadSettings;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Water a block of raw lead leaches into. Raw lead touching water beside it or over it, at any depth, leaks into the
 * water around it a block at a time, through still water as slowly as lava flows, while a current carries it along
 * faster. The water carries it, so it
 * never goes upstream: up only through still water, never up a waterfall, and sideways only into water as high or lower.
 * It leaks through the water alone, around whatever stands in it, as far as a route of the configured {@link #range()}
 * takes it, and tints {@value #FADE_PAST} block further, fainter and harmless, so it fades into the clean water. The lead's {@link RawLeadBlock#CONTAMINATION} counts the steps of its spreading. A player or mob in
 * the contaminated water gets Lead Sickness, worse the longer they stay. The water is tinted for it, fading in over a
 * few steps but never more than a step of its fade darker than the water beside it - clean water flowing in counting
 * as clean - so the spreading darkens from its front inwards rather than leaving a hard edge; murky from inside - the murk thickening over the last blocks before
 * it - and dark specks drift in it,
 * which shader packs that draw water their own way still show.
 */
public final class LeadWater {
    /** How often the water is checked for players and mobs, and the lead's spreading takes a step. */
    public static final int INTERVAL = 10;
    /** The furthest the config lets raw lead contaminate. */
    public static final int MAX_RANGE = LeadSettings.MAX_WATER_RANGE;
    /** How many blocks before the contaminated water the murk seen from inside it begins. */
    public static final int MURK_AHEAD = 6;
    /**
     * How many blocks inside the end of what raw lead contaminates its water starts to look less fouled: from there to
     * {@value #FADE_PAST} blocks past it, the tint and the murky textures ease off into clean water.
     */
    private static final int FADE_INSIDE = 1;
    /** How many blocks past what it contaminates raw lead still tints the water, fading out: harmless water. */
    public static final int FADE_PAST = 1;
    /** The steps a block of still water waits after the one it was reached from: 30 ticks a block, as lava flows. */
    private static final int STILL_WAIT = 3;
    /** The steps a block of water takes from first reached to fully tinted. */
    private static final int FADE = 2;
    /**
     * How much more strongly a block of water may be tinted than the water beside it: a block's worth of the fade at the
     * end of the range, and a step of the murky textures.
     */
    public static final float TINT_STEP = 1.0F / (FADE_INSIDE + FADE_PAST + 1);
    /** The most steps raw lead takes to reach as far as the config lets it, and tint all of it. */
    public static final int MAX_STEP = lastStep(MAX_RANGE);
    /** How often what raw lead left recedes by a block once it stops leaking, in ticks: as fast as water flows. */
    public static final int RECEDE_TICKS = 5;
    /** No cut to the water the spreading reached: all of it is fouled ({@link Leak#strength(int, int, BlockPos)}). */
    public static final int UNCUT = Integer.MAX_VALUE;
    /** A block's water as {@link Leak} keeps it: no water, else its amount, and whether it falls. */
    private static final byte NOT_WATER = -1;
    private static final byte STILL = 8;
    private static final byte FALLING = 16;

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
        return (range + 1) * STILL_WAIT + FADE - 1;
    }

    /** Whether raw lead at {@code pos} leaks into water on any side ({@link #leaksInto}). */
    public static boolean touchesWater(BlockGetter level, BlockPos pos) {
        for (Direction side : SIDES) {
            if (leaksInto(level, pos, side)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether raw lead at {@code lead} leaks into the water on its {@code side}: water beside it or over it, but not water
     * under it, whose surface lies a little short of the block above, nor a waterfall landing on it, which only pours past.
     */
    public static boolean leaksInto(BlockGetter level, BlockPos lead, Direction side) {
        if (side == Direction.DOWN) {
            return false;
        }
        FluidState fluid = level.getFluidState(lead.relative(side));
        return fluid.is(FluidTags.WATER) && carries(STILL, flowOf(fluid), side);
    }

    /** {@code fluid}, water, as a {@link Leak} keeps it: its amount, a source's 8, and whether it falls. */
    private static byte flowOf(FluidState fluid) {
        boolean falling = !fluid.isSource() && fluid.hasProperty(FlowingFluid.FALLING) && fluid.getValue(FlowingFluid.FALLING);
        return (byte) (fluid.getAmount() | (falling ? FALLING : 0));
    }

    private static int amount(byte flow) {
        return flow & 15;
    }

    private static boolean isFalling(byte flow) {
        return (flow & FALLING) != 0;
    }

    /**
     * Whether water carries lead from water {@code from} into the water on its {@code side}, never upstream: up only out
     * of and into water that isn't falling, sideways only into water as high or lower, down always. Raw lead itself is as
     * still water, which nothing flows into.
     */
    private static boolean carries(byte from, byte to, Direction side) {
        return switch (side) {
            case UP -> !isFalling(from) && !isFalling(to);
            case DOWN -> true;
            default -> amount(to) <= amount(from);
        };
    }

    /** Whether that is along a current - into flowing water that runs on away - which carries the lead along at once. */
    private static boolean isCurrent(byte from, byte to, Direction side) {
        boolean flowing = isFalling(to) || amount(to) < STILL;
        return flowing && (side == Direction.DOWN || side.getAxis().isHorizontal() && amount(to) < amount(from));
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

    /** Raw lead {@code step} steps into its spreading: the players and mobs in the water it contaminates breathe it. */
    public static void contaminate(ServerLevel level, BlockPos lead, int step) {
        contaminate(level, lead, step, UNCUT);
    }

    /** As {@link #contaminate(ServerLevel, BlockPos, int)}, in what receding lead left: routes no longer than {@code cut}. */
    public static void contaminate(ServerLevel level, BlockPos lead, int step, int cut) {
        int range = range(false);
        if (range == 0) {
            return;
        }
        // The routes are found only for a player or mob a dose would reach now: in water a route could reach, and past the stage of
        // their last dose. Raw lead under a pond someone swims in looks for them a few times a minute, not every check.
        List<LivingEntity> swimmers = level.getEntitiesOfClass(LivingEntity.class, new AABB(lead).inflate(range),
                entity -> LeadFumes.canSicken(entity) && LeadFumes.isDue(entity, LeadFumes.FUMES_STAGE_TICKS)
                        && (mayReach(level, lead, entity.blockPosition(), range)
                        || mayReach(level, lead, BlockPos.containing(entity.getEyePosition()), range)));
        if (swimmers.isEmpty()) {
            return;
        }
        Leak leak = poisonLeak(level, lead);
        for (LivingEntity swimmer : swimmers) {
            if (leak.poisons(step, cut, swimmer.blockPosition())
                    || leak.poisons(step, cut, BlockPos.containing(swimmer.getEyePosition()))) {
                LeadFumes.touch(swimmer, LeadFumes.FUMES_STAGE_TICKS);
            }
        }
    }

    /**
     * Whether coral at {@code pos} touches water raw lead contaminates - its own, waterlogged, or beside it - which it
     * dies in as out of water: each loader's {@code CoralMixin} asks this once vanilla's own look found water.
     */
    public static boolean poisonsCoral(BlockGetter getter, BlockPos pos, @Nullable BlockState state) {
        if (!(getter instanceof ServerLevel level) || range(false) == 0) {
            return false;
        }
        Nearby nearby = nearby(level, SectionPos.of(pos), lead -> poisonLeak(level, lead));
        if (nearby.isEmpty()) {
            return false;
        }
        if (state != null && state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)
                && nearby.poisons(pos)) {
            return true;
        }
        for (Direction side : SIDES) {
            BlockPos beside = pos.relative(side);
            if (level.getFluidState(beside).is(FluidTags.WATER) && nearby.poisons(beside)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Raw lead at {@code lead} spread to {@code step}: the coral beside the water it reached this step looks for water
     * again, as a neighbour changing makes it, and dies there a few seconds later ({@link #poisonsCoral}).
     */
    public static void wakeCoral(ServerLevel level, BlockPos lead, int step) {
        Leak leak = poisonLeak(level, lead);
        BlockPos.MutableBlockPos coral = new BlockPos.MutableBlockPos();
        for (int i = 0; i < leak.reach.size(); i++) {
            BlockPos water = leak.at(leak.reach.getInt(i));
            if (leak.since(step, water) != 0) {
                continue;
            }
            for (Direction side : SIDES) {
                BlockState state = level.getBlockState(coral.setWithOffset(water, side));
                if (state.is(BlockTags.CORALS) || state.is(BlockTags.CORAL_BLOCKS) || state.is(BlockTags.WALL_CORALS)) {
                    level.scheduleTick(coral.immutable(), state.getBlock(), 60 + level.getRandom().nextInt(40));
                }
            }
        }
    }

    /** Whether a route from raw lead at {@code lead} could reach {@code at}: water no further away than the range. */
    private static boolean mayReach(BlockGetter level, BlockPos lead, BlockPos at, int range) {
        return at.distManhattan(lead) <= range && level.getFluidState(at).is(FluidTags.WATER);
    }

    /**
     * How far into its spreading the lead at {@code lead} is: a leaking block of raw lead's own step, else the step what it
     * left has receded to once it stopped ({@link RecedingLeads}), else 0.
     */
    public static int stepAt(LevelReader level, BlockPos lead) {
        BlockState state = level.getBlockState(lead);
        if (isLeaking(state)) {
            return state.getValue(RawLeadBlock.CONTAMINATION);
        }
        RecedingLeads.State receding = RecedingLeads.of(level).get(lead.asLong());
        return receding == null ? 0 : receding.step();
    }

    /** The longest route still fouled from the lead at {@code lead}: {@link #UNCUT} but where what it left recedes. */
    public static int cutAt(LevelReader level, BlockPos lead) {
        if (isLeaking(level.getBlockState(lead))) {
            return UNCUT;
        }
        RecedingLeads.State receding = RecedingLeads.of(level).get(lead.asLong());
        return receding == null ? UNCUT : receding.cut();
    }

    /**
     * What raw lead at {@code lead}, {@code step} steps into its spreading, leaves to recede once it stops leaking, null
     * where it leaves nothing: the water as it stands, its farthest block already clean so that it starts to clear at once.
     * The lead keeps counting a while after all its water is fouled; those steps change nothing, so they are dropped.
     */
    @Nullable
    public static RecedingLeads.State recedeFrom(BlockGetter level, BlockPos lead, int step, int range) {
        if (range == 0 || step <= 0) {
            return null;
        }
        Leak leak = leak(level, lead, range);
        int from = Math.min(step, leak.settled());
        int cut = leak.farthest(from) - 1;
        return from > 0 && cut > 0 ? new RecedingLeads.State(from, cut) : null;
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
        IntArrayList cuts = new IntArrayList();
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
                                    cuts.add(UNCUT);
                                }
                            }
                        }
                    }
                }
            }
        }
        // Where raw lead used to leak, what it left recedes (RecedingLeads): lead too, though its block has gone.
        for (Long2ObjectMap.Entry<RecedingLeads.State> receding : Long2ObjectMaps.fastIterable(RecedingLeads.of(level))) {
            long lead = receding.getLongKey();
            if (Math.abs(SectionPos.blockToSectionCoord(BlockPos.getX(lead)) - section.x()) <= 1
                    && Math.abs(SectionPos.blockToSectionCoord(BlockPos.getY(lead)) - section.y()) <= 1
                    && Math.abs(SectionPos.blockToSectionCoord(BlockPos.getZ(lead)) - section.z()) <= 1
                    && !leads.contains(lead)) {
                leads.add(lead);
                steps.add(receding.getValue().step());
                cuts.add(receding.getValue().cut());
            }
        }
        if (leads.isEmpty()) {
            return Nearby.NONE;
        }
        Leak[] found = new Leak[leads.size()];
        for (int i = 0; i < found.length; i++) {
            found[i] = leaks.apply(BlockPos.of(leads.getLong(i)));
        }
        return new Nearby(leads.toLongArray(), steps.toIntArray(), cuts.toIntArray(), found);
    }

    /**
     * The raw lead leaking into the water around a chunk section ({@link #nearby}): each lead, its step, the longest route
     * it still fouls ({@link #UNCUT} but where it recedes) and its routes.
     */
    public record Nearby(long[] leads, int[] steps, int[] cuts, Leak[] leaks) {
        public static final Nearby NONE = new Nearby(new long[0], new int[0], new int[0], new Leak[0]);

        public boolean isEmpty() {
            return leads.length == 0;
        }

        /**
         * How strongly the water at {@code water} is tinted, from 0 to 1: as strong as it gets but for the last few blocks
         * of what it contaminates, then easing off to nothing {@value #FADE_PAST} blocks past it, and fading in over a few steps once
         * reached, from the raw lead that tints it most, reaching {@code range}.
         */
        public float strength(BlockPos water, int range) {
            return strongest(water, range + FADE_PAST, false);
        }

        /**
         * How thick the murk is seen from inside the water at {@code water}, from 0 to 1: at its thickest throughout the
         * contaminated water, thinning over the {@value #MURK_AHEAD} blocks before it, from the raw lead that murks it most.
         */
        public float murk(BlockPos water, int range) {
            return strongest(water, range + MURK_AHEAD, true);
        }

        /** Whether any of the lead poisons the water at {@code water} now: as {@link Leak#poisons}, over every lead. */
        public boolean poisons(BlockPos water) {
            for (int i = 0; i < leads.length; i++) {
                if (leaks[i].poisons(steps[i], cuts[i], water)) {
                    return true;
                }
            }
            return false;
        }

        private float strongest(BlockPos water, int reach, boolean murk) {
            float strongest = 0.0F;
            for (int i = 0; i < leads.length; i++) {
                long lead = leads[i];
                if (Math.abs(BlockPos.getX(lead) - water.getX()) + Math.abs(BlockPos.getY(lead) - water.getY())
                        + Math.abs(BlockPos.getZ(lead) - water.getZ()) > reach) {
                    continue;
                }
                strongest = Math.max(strongest,
                        murk ? leaks[i].murk(steps[i], cuts[i], water) : leaks[i].strength(steps[i], cuts[i], water));
            }
            return strongest;
        }
    }

    /**
     * Where one block of raw lead leaks to: each block of water a route through the water reaches within its range and
     * the {@value #MURK_AHEAD} blocks of murk past it, the length of the shortest such route, and the step of the
     * spreading that reaches it - the earliest of the blocks one closer along a route, plus this block's own wait: a
     * step along a current, {@value #STILL_WAIT} through still water. The client and the server find the same routes
     * from the same water, so they agree without being told. Routes go only where the water carries the lead ({@link #carries}). The murk
     * past the tinted edge waits for nothing: it is there as soon as the water before it is.
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
        /** The shortest route to each block, in blocks; 0 where none goes, {@link #DRY} where no water is. */
        private final byte[] route;
        /** Each block's water, as {@link #flowOf} keeps it; 0 where nothing has looked yet. */
        private final byte[] flow;
        /** Every block of water the lead contaminates, once its spreading reaches it: the edge left out. */
        private final IntArrayList reach = new IntArrayList();
        /** The water it can tint and what is beside each, found the first time a tint is asked for. */
        @Nullable
        private volatile Tinting tinting;
        /** Every block of water's tint for the step last asked about: chunk-drawing threads share it. */
        @Nullable
        private volatile Tints tints;

        private Leak(BlockGetter level, BlockPos lead, int range, int ahead) {
            this.lead = lead;
            this.range = range;
            this.extent = range == 0 ? 0 : range + ahead;
            this.whole = !(level instanceof LevelReader reader) || reader.hasChunksAt(lead.getX() - extent, lead.getZ() - extent,
                    lead.getX() + extent, lead.getZ() + extent);
            this.size = extent * 2 + 1;
            this.arrival = new int[size * size * size];
            this.route = new byte[size * size * size];
            this.flow = new byte[size * size * size];
            Arrays.fill(arrival, Integer.MAX_VALUE);
            int center = index(0, 0, 0);
            arrival[center] = 0;
            // Where the lead used to be, receding, water may have flowed into its place: that water was the nearest to it.
            // It stays still water there, as the lead was, so its routes don't change with how that water settles.
            boolean wet = level.getFluidState(lead).is(FluidTags.WATER);
            route[center] = wet ? 1 : DRY;
            flow[center] = STILL;
            if (wet && range > 0) {
                reach.add(center);
            }
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
                        if (from == center && !wet && side == Direction.DOWN) {
                            continue;  // the lead doesn't touch the water under it (touchesWater)
                        }
                        int x = fx + side.getStepX();
                        int y = fy + side.getStepY();
                        int z = fz + side.getStepZ();
                        int to = index(x, y, z);
                        if (flow[to] == 0) {
                            FluidState fluid = level.getFluidState(at.setWithOffset(lead, x, y, z));
                            flow[to] = fluid.is(FluidTags.WATER) ? flowOf(fluid) : NOT_WATER;
                        }
                        if (flow[to] == NOT_WATER) {
                            route[to] = DRY;
                            continue;
                        }
                        if (!carries(flow[from], flow[to], side)) {
                            continue;
                        }
                        if (route[to] == 0) {
                            route[to] = length;
                            next.add(to);
                            if (length <= range) {
                                reach.add(to);
                            }
                        }
                        if (route[to] == length) {
                            int wait = length > range + 1 ? 0 : isCurrent(flow[from], flow[to], side) ? 1 : STILL_WAIT;
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
            return poisons(step, UNCUT, water);
        }

        /** As {@link #poisons(int, BlockPos)}, routes longer than {@code cut} left clean: what receding lead left. */
        public boolean poisons(int step, int cut, BlockPos water) {
            return since(step, water) >= 0 && route[indexOf(water)] <= Math.min(range, cut);
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

        /**
         * How strongly the water at {@code water} is tinted, {@code step} steps into the spreading, from 0 to 1: as its route
         * and how long ago the spreading reached it make it ({@link #ownStrength}), but never more than {@link #TINT_STEP}
         * above any water beside it. So no block is dark beside clean water: the front darkens a step a block inwards.
         */
        public float strength(int step, BlockPos water) {
            return strength(step, UNCUT, water);
        }

        /** As {@link #strength(int, BlockPos)}, routes longer than {@code cut} left clean: what receding lead left. */
        public float strength(int step, int cut, BlockPos water) {
            int at = indexOf(water);
            if (step <= 0 || at < 0 || route[at] <= 0 || route[at] > Math.min(range + FADE_PAST, cut)) {
                return 0.0F;
            }
            Tints kept = tints;
            if (kept == null || kept.step() != step || kept.cut() != cut) {
                kept = tintsAt(step, cut);
                tints = kept;
            }
            return kept.strength()[tinting().slotOf()[at]];
        }

        /**
         * How strongly the water at index {@code at} of the routes is tinted for itself, the water beside it aside: no more
         * than a step of the fade where clean water flows in beside it.
         */
        private float ownStrength(int step, int cut, int at, boolean inflow) {
            int since = step - arrival[at];
            if (since < 0 || route[at] > cut) {
                return 0.0F;
            }
            float own = falloff(route[at]) * fadeIn(since);
            return inflow ? Math.min(own, TINT_STEP) : own;
        }

        /**
         * Every block of water's tint {@code step} steps in: its own, lowered where water beside it is lighter. Each block is
         * settled lightest first, taking the lowest of its own and a step above each settled block beside it, which is
         * Dijkstra's walk with equal steps: those lowered come in the order they were, so a queue keeps them in order.
         */
        private Tints tintsAt(int step, int cut) {
            Tinting tinting = tinting();
            int count = tinting.at().length;
            float[] strength = new float[count];
            int[] order = new int[count];
            for (int slot = 0; slot < count; slot++) {
                strength[slot] = ownStrength(step, cut, tinting.at()[slot], tinting.inflow()[slot]);
                order[slot] = slot;
            }
            IntArrays.quickSort(order, (a, b) -> Float.compare(strength[a], strength[b]));
            boolean[] settled = new boolean[count];
            IntArrayFIFOQueue lowered = new IntArrayFIFOQueue();
            int next = 0;
            while (next < count || !lowered.isEmpty()) {
                int slot = lowered.isEmpty() || next < count && strength[order[next]] <= strength[lowered.firstInt()]
                        ? order[next++] : lowered.dequeueInt();
                if (settled[slot]) {
                    continue;
                }
                settled[slot] = true;
                float most = strength[slot] + TINT_STEP;
                for (int i = slot * SIDES.size(); i < (slot + 1) * SIDES.size(); i++) {
                    int beside = tinting.beside()[i];
                    if (beside >= 0 && !settled[beside] && strength[beside] > most) {
                        strength[beside] = most;
                        lowered.enqueue(beside);
                    }
                }
            }
            return new Tints(step, cut, strength);
        }

        /**
         * The water it can tint, the edge included, the water beside each block of it, and which have clean water flowing in
         * beside them: water no route goes into, since it flows this way.
         */
        private Tinting tinting() {
            Tinting kept = tinting;
            if (kept != null) {
                return kept;
            }
            int[] slotOf = new int[route.length];
            Arrays.fill(slotOf, -1);
            IntArrayList at = new IntArrayList();
            for (int i = 0; i < route.length; i++) {
                if (route[i] > 0 && route[i] <= range + FADE_PAST) {
                    slotOf[i] = at.size();
                    at.add(i);
                }
            }
            int[] beside = new int[at.size() * SIDES.size()];
            boolean[] inflow = new boolean[at.size()];
            for (int slot = 0; slot < at.size(); slot++) {
                int i = at.getInt(slot);
                int x = i / (size * size) - extent;
                int y = i / size % size - extent;
                int z = i % size - extent;
                for (int s = 0; s < SIDES.size(); s++) {
                    Direction side = SIDES.get(s);
                    int bx = x + side.getStepX();
                    int by = y + side.getStepY();
                    int bz = z + side.getStepZ();
                    boolean inside = Math.abs(bx) <= extent && Math.abs(by) <= extent && Math.abs(bz) <= extent;
                    int to = inside ? index(bx, by, bz) : -1;
                    beside[slot * SIDES.size() + s] = to < 0 ? -1 : slotOf[to];
                    if (to >= 0 && route[to] == 0 && flow[to] > 0) {
                        inflow[slot] = true;
                    }
                }
            }
            kept = new Tinting(at.toIntArray(), slotOf, beside, inflow);
            tinting = kept;
            return kept;
        }

        /**
         * How strongly water a route of {@code length} blocks reaches is tinted, all spreading done: fully up to
         * {@value #FADE_INSIDE} blocks inside the end of the range, then evenly down to nothing {@value #FADE_PAST} blocks
         * past it, a step of the murky textures a block. Spread over that many blocks, clean water and fouled meet without a seam.
         */
        private float falloff(int length) {
            return Mth.clamp((range + FADE_PAST + 1 - length) * TINT_STEP, 0.0F, 1.0F);
        }

        /** How thick the murk is seen from inside the water at {@code water}, {@code step} steps into the spreading. */
        public float murk(int step, BlockPos water) {
            return murk(step, UNCUT, water);
        }

        /** As {@link #murk(int, BlockPos)}, routes longer than {@code cut} left clean: what receding lead left. */
        public float murk(int step, int cut, BlockPos water) {
            int since = since(step, water);
            if (since < 0) {
                return 0.0F;
            }
            int beyond = route[indexOf(water)] - Math.min(range, cut);
            return Math.max(0.0F, beyond <= 0 ? 1.0F : 1.0F - beyond / (float) (MURK_AHEAD + 1)) * fadeIn(since);
        }

        /** The longest route to water the spreading has reached {@code step} steps in and tints, 0 for none. */
        public int farthest(int step) {
            int farthest = 0;
            for (int at = 0; at < route.length; at++) {
                if (route[at] > 0 && route[at] <= range + FADE_PAST && arrival[at] <= step) {
                    farthest = Math.max(farthest, route[at]);
                }
            }
            return farthest;
        }

        /** The first step at which the spreading has reached and fully tinted all the water it does, 0 for none. */
        public int settled() {
            int last = -1;
            for (int at = 0; at < route.length; at++) {
                if (route[at] > 0 && arrival[at] != Integer.MAX_VALUE) {
                    last = Math.max(last, arrival[at]);
                }
            }
            return last < 0 ? 0 : last + FADE - 1;
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

        /** The water at index {@code at} of the routes. */
        private BlockPos at(int at) {
            return lead.offset(at / (size * size) - extent, at / size % size - extent, at % size - extent);
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

        /**
         * The water a lead can tint, as indices of the routes, each one's place in that list, the six beside each, and
         * whether clean water flows in beside it.
         */
        private record Tinting(int[] at, int[] slotOf, int[] beside, boolean[] inflow) {
        }

        /** The tint of each block of {@link Tinting#at}, {@code step} steps into the spreading, routes past {@code cut} clean. */
        private record Tints(int step, int cut, float[] strength) {
        }
    }
}

package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.effect.RecedingLeads;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Water raw lead contaminates, tinted the lead's purple: strongest beside the lead, fading towards the edge of its reach
 * ({@link LeadWater.Nearby#strength}). Applied to the water colour every loader's water takes from the biome, by each
 * loader's {@code BiomeColorsMixin} - Sodium's and Embeddium's water too - and to the fog seen from inside that water, by
 * its {@code FogRendererMixin}. A shader pack may draw water in a colour of its own; the specks drifting in the water
 * ({@link LeadWaterSpecks}) show whatever it does.
 *
 * <p>The tint is read off the level itself rather than the copy of it a chunk is drawn from: Sodium's and Embeddium's
 * copies reach only two blocks past the chunk section, and the lead and the routes it leaks along reach further.</p>
 */
public final class LeadWaterTint {
    /**
     * The lead's purple ({@link LeadCampfireSmoke#COLOR}), more violet: darkness is the murky textures' to give
     * ({@link MurkyWater}), since the tint is only multiplied with them. Its green stays above the 0.39 Complementary's
     * default water ("Biome Based (Clamped)") raises it to, which turned a darker purple teal-grey under that shader pack.
     */
    private static final int LEAD_TINT = 0x7366B4;
    private static final float MOST = 0.90F;
    /** The murk seen from inside contaminated water, a dark purple, as a share of the water's own fog at its brightest. */
    private static final float MURK_RED = 0.19F;
    private static final float MURK_GREEN = 0.15F;
    private static final float MURK_BLUE = 0.26F;
    /** The most the murk takes over the water's own fog. */
    private static final float MURK_MOST = 0.92F;
    /** How long the murk takes to settle as the camera moves through the water, in milliseconds. */
    private static final float MURK_SETTLE = 400.0F;
    /**
     * How long a lead's routes through the water are kept at least, in ticks, should a change to the water go unseen:
     * each lead keeps its own for up to twice that, so a floor of raw lead doesn't find all its routes again in one frame.
     */
    private static final long LEAK_KEPT = 200;

    /** Each raw lead's routes through the water, by position: the tint asks for them for every block of water drawn. */
    private static final Map<Long, Kept> LEAKS = new ConcurrentHashMap<>();
    /** The level the routes were found in, held weakly so a world left behind isn't kept alive by them. */
    private static volatile WeakReference<ClientLevel> leaksOf = new WeakReference<>(null);
    /**
     * The leaking lead around the chunk section each thread last asked about, for the tick: a chunk is drawn block by
     * block, and finding it for every block of water cost every ocean drawn in a world a look at eight palettes a block.
     */
    private static final ThreadLocal<Near> NEAR = new ThreadLocal<>();
    /** Counts the changes to raw lead and the water it leaks into, so no thread's {@link #NEAR} outlives one. */
    private static final AtomicInteger CHANGES = new AtomicInteger();
    private static float murk;
    private static long murkAt;

    private LeadWaterTint() {
    }

    /**
     * The water colour at {@code pos}, tinted where raw lead contaminates it. Renderers that blend the colour of each
     * corner from the blocks around it ask for the shore and the lead too; those take the strongest water beside them,
     * or the tint would thin out where it is strongest.
     */
    public static int tint(BlockPos pos, int color) {
        try {
            return tinted(pos, color);
        } catch (RuntimeException e) {
            // Read off the live level from a chunk-drawing thread, which can catch a section mid-change: leave it plain.
            return color;
        }
    }

    /**
     * How strongly the water at {@code pos} is contaminated, from 0 to 1, as the tint takes it: which of the murkier
     * textures it is drawn with ({@link MurkyWater}). 0 where it isn't, or can't be told.
     */
    public static float strength(BlockPos pos) {
        try {
            ClientLevel level = Minecraft.getInstance().level;
            int range = LeadWater.range(true);
            if (level == null || range == 0) {
                return 0.0F;
            }
            Near near = near(level, pos);
            return near.nearby().isEmpty() ? 0.0F : near.strengthAt(level, pos, range);
        } catch (RuntimeException e) {
            // As tint(): a section caught mid-change from a chunk-drawing thread is drawn plain.
            return 0.0F;
        }
    }

    private static int tinted(BlockPos pos, int color) {
        ClientLevel level = Minecraft.getInstance().level;
        int range = LeadWater.range(true);
        if (level == null || range == 0) {
            return color;
        }
        Near near = near(level, pos);
        if (near.nearby().isEmpty()) {
            return color;
        }
        // Eased in a little, so the first steps of the spreading already read as fouled water rather than a faint wash.
        float strength = (float) Math.pow(near.strengthAt(level, pos, range), 0.75) * MOST;
        if (strength <= 0.0F) {
            return color;
        }
        return FastColor.ARGB32.color(FastColor.ARGB32.alpha(color),
                mix(FastColor.ARGB32.red(color), FastColor.ARGB32.red(LEAD_TINT), strength),
                mix(FastColor.ARGB32.green(color), FastColor.ARGB32.green(LEAD_TINT), strength),
                mix(FastColor.ARGB32.blue(color), FastColor.ARGB32.blue(LEAD_TINT), strength));
    }

    /**
     * The fog seen from inside contaminated water, as {@code red, green, blue}: the water's own fog, turned towards a
     * lead purple as bright as it - at its thickest all through the contaminated water, thinning over the few blocks
     * before it ({@link LeadWater.Nearby#murk}). Null outside it.
     */
    @Nullable
    public static float[] fog(Camera camera, ClientLevel level, float red, float green, float blue) {
        long now = Util.getMillis();
        float settle = Math.min(1.0F, (now - murkAt) / MURK_SETTLE);
        murkAt = now;
        if (camera.getFluidInCamera() != FogType.WATER) {
            murk = 0.0F;
            return null;
        }
        BlockPos at = BlockPos.containing(camera.getPosition());
        int range = LeadWater.range(true);
        float target;
        try {
            target = range > 0 ? near(level, at).nearby().murk(at, range) : 0.0F;
        } catch (RuntimeException e) {
            target = 0.0F;
        }
        murk += (target * MURK_MOST - murk) * settle;
        if (murk < 0.005F) {
            return null;
        }
        float bright = Math.max(red, Math.max(green, blue));
        return new float[] {Mth.lerp(murk, red, MURK_RED * bright), Mth.lerp(murk, green, MURK_GREEN * bright),
                Mth.lerp(murk, blue, MURK_BLUE * bright)};
    }

    /**
     * A block changed in the level: the water around raw lead whose spreading moved on, or whose routes through the
     * water it changes, is drawn again.
     */
    public static void blockChanged(LevelRenderer renderer, BlockPos pos, BlockState oldState, BlockState newState) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && LeadWater.isLeaking(oldState) && !LeadWater.isLeaking(newState)) {
            // Without it, water raw lead the player breaks shows clean until the server says what recedes there.
            RecedingLeads.predict(pos, LeadWater.recedeFrom(level, pos, oldState.getValue(RawLeadBlock.CONTAMINATION),
                    LeadWater.range(true)));
        }
        if (changesTint(oldState, newState)) {
            CHANGES.incrementAndGet();
            if (!(newState.getBlock() instanceof RawLeadBlock)) {
                LEAKS.remove(pos.asLong());
            }
            redraw(renderer, pos);
        }
        // The routes follow the water's currents as well as where it is, so any change to water near lead finds them again.
        if (!LEAKS.isEmpty() && oldState.getFluidState() != newState.getFluidState()
                && (oldState.getFluidState().is(FluidTags.WATER) || newState.getFluidState().is(FluidTags.WATER))) {
            LEAKS.keySet().removeIf(key -> {
                BlockPos lead = BlockPos.of(key);
                if (pos.distManhattan(lead) > LeadWater.range(true) + LeadWater.MURK_AHEAD) {
                    return false;
                }
                CHANGES.incrementAndGet();
                redraw(renderer, lead);
                return true;
            });
        }
    }

    /** Contamination receding from where raw lead used to leak took a step: the water it leaves is drawn again. */
    public static void leadChanged(BlockPos lead) {
        CHANGES.incrementAndGet();
        redraw(Minecraft.getInstance().levelRenderer, lead);
    }

    /** The leaking lead around the chunk section of {@code pos}, kept for the tick by the thread asking. */
    private static Near near(ClientLevel level, BlockPos pos) {
        keepFor(level);
        long section = SectionPos.asLong(pos);
        long time = level.getGameTime();
        int changes = CHANGES.get();
        Near near = NEAR.get();
        if (near != null && near.section == section && near.time == time && near.changes == changes) {
            return near;
        }
        near = new Near(section, time, changes, LeadWater.nearby(level, SectionPos.of(pos), lead -> leakOf(level, lead)));
        NEAR.set(near);
        return near;
    }

    /** The routes raw lead at {@code lead} leaks along, kept between draws. */
    public static LeadWater.Leak leakOf(ClientLevel level, BlockPos lead) {
        LeadWater.Leak kept = keptLeakOf(level, lead);
        if (kept != null) {
            return kept;
        }
        LeadWater.Leak leak = LeadWater.leak(level, lead, LeadWater.range(true));
        // Routes found beside a chunk that hasn't arrived end short there; the chunk's arrival redraws the water near it,
        // which should find them again rather than these.
        if (leak.whole()) {
            LEAKS.put(lead.asLong(), new Kept(leak, level.getGameTime()));
        }
        return leak;
    }

    /** The routes raw lead at {@code lead} leaks along, where they are kept, else null. */
    @Nullable
    public static LeadWater.Leak keptLeakOf(ClientLevel level, BlockPos lead) {
        keepFor(level);
        long key = lead.asLong();
        Kept kept = LEAKS.get(key);
        return kept != null && !isStale(key, kept, level.getGameTime()) && kept.leak().range() == LeadWater.range(true)
                ? kept.leak() : null;
    }

    /** The level's tick: routes kept for another level, or not asked for in a while, are let go. */
    public static void tick(ClientLevel level) {
        keepFor(level);
        long now = level.getGameTime();
        if (now % 20 == 0 && !LEAKS.isEmpty()) {
            LEAKS.entrySet().removeIf(entry -> isStale(entry.getKey(), entry.getValue(), now));
        }
    }

    private static boolean isStale(long lead, Kept kept, long now) {
        return now < kept.at() || now - kept.at() > LEAK_KEPT + Math.floorMod(HashCommon.mix(lead), LEAK_KEPT);
    }

    private static void keepFor(ClientLevel level) {
        if (leaksOf.get() != level) {
            LEAKS.clear();
            CHANGES.incrementAndGet();
            leaksOf = new WeakReference<>(level);
        }
    }

    /** Draws the water a lead at {@code pos} tints again, and a block more for renderers that blend each corner. */
    private static void redraw(LevelRenderer renderer, BlockPos pos) {
        int reach = LeadWater.range(true) + LeadWater.FADE_PAST + 1;
        renderer.setBlocksDirty(pos.getX() - reach, pos.getY() - reach, pos.getZ() - reach,
                pos.getX() + reach, pos.getY() + reach, pos.getZ() + reach);
    }

    /** Whether a change to a block changes the water's tint around it, so that water has to be drawn again. */
    private static boolean changesTint(BlockState oldState, BlockState newState) {
        boolean wasLead = oldState.getBlock() instanceof RawLeadBlock;
        boolean isLead = newState.getBlock() instanceof RawLeadBlock;
        return (wasLead || isLead) && (wasLead != isLead
                || !oldState.getValue(RawLeadBlock.CONTAMINATION).equals(newState.getValue(RawLeadBlock.CONTAMINATION)));
    }

    private static int mix(int from, int to, float amount) {
        return Math.round(from + (to - from) * amount);
    }

    /** A lead's routes, and the tick they were found at. */
    private record Kept(LeadWater.Leak leak, long at) {
    }

    /**
     * The leaking lead around a chunk section, as it stood at a tick and a count of changes, and the tint of each of the
     * section's blocks once asked for: renderers that blend each corner ask for one block's colour many times over.
     */
    private static final class Near {
        private final long section;
        private final long time;
        private final int changes;
        private final LeadWater.Nearby nearby;
        @Nullable
        private float[] strengths;

        private Near(long section, long time, int changes, LeadWater.Nearby nearby) {
            this.section = section;
            this.time = time;
            this.changes = changes;
            this.nearby = nearby;
        }

        LeadWater.Nearby nearby() {
            return nearby;
        }

        /** How strongly the block at {@code pos} is tinted: its water's, or the strongest water beside it. */
        float strengthAt(ClientLevel level, BlockPos pos, int range) {
            if (SectionPos.asLong(pos) != section) {
                return strengthOf(level, pos, range);
            }
            if (strengths == null) {
                strengths = new float[SectionPos.SECTION_SIZE * SectionPos.SECTION_SIZE * SectionPos.SECTION_SIZE];
                Arrays.fill(strengths, Float.NaN);
            }
            int index = (SectionPos.sectionRelative(pos.getY()) << 8) | (SectionPos.sectionRelative(pos.getZ()) << 4)
                    | SectionPos.sectionRelative(pos.getX());
            if (Float.isNaN(strengths[index])) {
                strengths[index] = strengthOf(level, pos, range);
            }
            return strengths[index];
        }

        private float strengthOf(ClientLevel level, BlockPos pos, int range) {
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                return nearby.strength(pos, range);
            }
            float strongest = 0.0F;
            for (Direction side : Direction.values()) {
                BlockPos beside = pos.relative(side);
                if (level.getFluidState(beside).is(FluidTags.WATER)) {
                    strongest = Math.max(strongest, nearby.strength(beside, range));
                }
            }
            return strongest;
        }
    }
}

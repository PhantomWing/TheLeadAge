package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.platform.CommonConfig;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Water raw lead contaminates, tinted lead grey: strongest beside the lead, fading towards the edge of its reach
 * ({@link LeadWater#strength}). Applied to the water colour every loader's water takes from the biome, by each loader's
 * {@code BiomeColorsMixin} - Sodium's and Embeddium's water too - and to the fog seen from inside that water, by its
 * {@code FogRendererMixin}. A shader pack may draw water in a colour of its own; the specks drifting in the water
 * ({@link LeadWaterSpecks}) show whatever it does.
 *
 * <p>The tint is read off the level itself rather than the copy of it a chunk is drawn from: Sodium's and Embeddium's
 * copies reach only two blocks past the chunk section, and the lead and the routes it leaks along reach further.</p>
 */
public final class LeadWaterTint {
    private static final int LEAD_GREY = 0x464B53;
    private static final float MOST = 0.85F;
    /** The murk seen from inside contaminated water, as a share of the water's own fog at its brightest. */
    private static final float MURK_RED = 0.20F;
    private static final float MURK_GREEN = 0.22F;
    private static final float MURK_BLUE = 0.25F;
    /** The most the murk takes over the water's own fog. */
    private static final float MURK_MOST = 0.92F;
    /** How long the murk takes to settle as the camera moves through the water, in milliseconds. */
    private static final float MURK_SETTLE = 400.0F;
    /** How long a lead's routes through the water are kept, in ticks, should a change to the water go unseen. */
    private static final long LEAK_KEPT = 200;
    /** How far past the lead's reach a change to it is drawn again: renderers that blend each corner read a block more. */
    private static final int REDRAW = LeadWater.TINT_RADIUS + 1;

    /** Each raw lead's routes through the water, by position: the tint asks for them for every block of water drawn. */
    private static final Map<Long, Kept> LEAKS = new ConcurrentHashMap<>();
    private static volatile ClientLevel leaksOf;
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
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !CommonConfig.leadWaterContamination() || !mayHaveRawLeadNear(level, pos)) {
            return color;
        }
        float strength = level.getFluidState(pos).is(FluidTags.WATER) ? strength(level, pos) : besideStrength(level, pos);
        strength *= MOST;
        if (strength <= 0.0F) {
            return color;
        }
        return FastColor.ARGB32.color(FastColor.ARGB32.alpha(color),
                mix(FastColor.ARGB32.red(color), FastColor.ARGB32.red(LEAD_GREY), strength),
                mix(FastColor.ARGB32.green(color), FastColor.ARGB32.green(LEAD_GREY), strength),
                mix(FastColor.ARGB32.blue(color), FastColor.ARGB32.blue(LEAD_GREY), strength));
    }

    /**
     * The fog seen from inside contaminated water, as {@code red, green, blue}: the water's own fog, turned towards a
     * lead grey as bright as it, by how strongly the water the camera is in is contaminated. Null outside it.
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
        float target = CommonConfig.leadWaterContamination() && mayHaveRawLeadNear(level, at) ? strength(level, at) : 0.0F;
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
        if (changesTint(oldState, newState)) {
            if (!(newState.getBlock() instanceof RawLeadBlock)) {
                LEAKS.remove(pos.asLong());
            }
            redraw(renderer, pos);
        }
        if (!LEAKS.isEmpty() && oldState.getFluidState().is(FluidTags.WATER) != newState.getFluidState().is(FluidTags.WATER)) {
            LEAKS.keySet().removeIf(key -> {
                BlockPos lead = BlockPos.of(key);
                if (pos.distManhattan(lead) > LeadWater.TINT_RADIUS + 1) {
                    return false;
                }
                redraw(renderer, lead);
                return true;
            });
        }
    }

    private static float strength(ClientLevel level, BlockPos water) {
        return LeadWater.strength(level, water, lead -> leakOf(level, lead));
    }

    private static float besideStrength(ClientLevel level, BlockPos pos) {
        float strongest = 0.0F;
        for (Direction side : Direction.values()) {
            BlockPos beside = pos.relative(side);
            if (level.getFluidState(beside).is(FluidTags.WATER)) {
                strongest = Math.max(strongest, strength(level, beside));
            }
        }
        return strongest;
    }

    /** The routes raw lead at {@code lead} leaks along, kept between draws. */
    public static LeadWater.Leak leakOf(ClientLevel level, BlockPos lead) {
        if (leaksOf != level) {
            LEAKS.clear();
            leaksOf = level;
        }
        long now = level.getGameTime();
        Kept kept = LEAKS.get(lead.asLong());
        if (kept == null || now < kept.at() || now - kept.at() > LEAK_KEPT) {
            kept = new Kept(LeadWater.leak(level, lead), now);
            LEAKS.put(lead.asLong(), kept);
        }
        return kept.leak();
    }

    private static void redraw(LevelRenderer renderer, BlockPos pos) {
        renderer.setBlocksDirty(pos.getX() - REDRAW, pos.getY() - REDRAW, pos.getZ() - REDRAW,
                pos.getX() + REDRAW, pos.getY() + REDRAW, pos.getZ() + REDRAW);
    }

    /** Whether a change to a block changes the water's tint around it, so that water has to be drawn again. */
    private static boolean changesTint(BlockState oldState, BlockState newState) {
        boolean wasLead = oldState.getBlock() instanceof RawLeadBlock;
        boolean isLead = newState.getBlock() instanceof RawLeadBlock;
        return (wasLead || isLead) && (wasLead != isLead
                || !oldState.getValue(RawLeadBlock.CONTAMINATION).equals(newState.getValue(RawLeadBlock.CONTAMINATION)));
    }

    /**
     * Whether any chunk section within reach of {@code pos} may hold raw lead, read off the sections' palettes, so
     * the oceans' water skips the search: the tint is asked for every water block a chunk draws.
     */
    private static boolean mayHaveRawLeadNear(ClientLevel level, BlockPos pos) {
        int reach = LeadWater.TINT_RADIUS + 1;
        for (int x = SectionPos.blockToSectionCoord(pos.getX() - reach); x <= SectionPos.blockToSectionCoord(pos.getX() + reach); x++) {
            for (int z = SectionPos.blockToSectionCoord(pos.getZ() - reach); z <= SectionPos.blockToSectionCoord(pos.getZ() + reach); z++) {
                LevelChunk chunk = level.getChunkSource().getChunk(x, z, false);
                if (chunk == null) {
                    continue;
                }
                for (int y = SectionPos.blockToSectionCoord(pos.getY() - reach); y <= SectionPos.blockToSectionCoord(pos.getY() + reach); y++) {
                    int index = chunk.getSectionIndexFromSectionY(y);
                    if (index < 0 || index >= chunk.getSectionsCount()) {
                        continue;
                    }
                    LevelChunkSection section = chunk.getSection(index);
                    if (!section.hasOnlyAir() && section.maybeHas(state -> state.getBlock() instanceof RawLeadBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int mix(int from, int to, float amount) {
        return Math.round(from + (to - from) * amount);
    }

    /** A lead's routes, and the tick they were found at. */
    private record Kept(LeadWater.Leak leak, long at) {
    }
}

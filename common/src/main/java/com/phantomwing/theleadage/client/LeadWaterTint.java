package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;

/**
 * Water raw lead contaminates, tinted lead grey: strongest beside the lead, fading towards the edge of its reach
 * ({@link LeadWater#strength}). Applied to the water colour every loader's water takes from the biome, by each loader's
 * {@code BiomeColorsMixin}, and to the fog seen from inside that water, by its {@code FogRendererMixin}; a rendering
 * mod that colours water its own way leaves the first out, which is why the water has specks too.
 */
public final class LeadWaterTint {
    private static final int LEAD_GREY = 0x464B53;
    private static final float MOST = 0.85F;
    /** The murk seen from inside contaminated water, as a share of the water's own fog at its brightest. */
    private static final float MURK_RED = 0.58F;
    private static final float MURK_GREEN = 0.61F;
    private static final float MURK_BLUE = 0.66F;
    /** How long the murk takes to settle as the camera moves through the water, in milliseconds. */
    private static final float MURK_SETTLE = 400.0F;
    private static float murk;
    private static long murkAt;

    private LeadWaterTint() {
    }

    /** The water colour at {@code pos}, tinted where raw lead contaminates it. */
    public static int tint(BlockAndTintGetter level, BlockPos pos, int color) {
        float strength = strength(level, pos) * MOST;
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
        murk += (strength(level, BlockPos.containing(camera.getPosition())) * MOST - murk) * settle;
        if (murk < 0.005F) {
            return null;
        }
        float bright = Math.max(red, Math.max(green, blue));
        return new float[] {Mth.lerp(murk, red, MURK_RED * bright), Mth.lerp(murk, green, MURK_GREEN * bright),
                Mth.lerp(murk, blue, MURK_BLUE * bright)};
    }

    private static float strength(BlockAndTintGetter level, BlockPos pos) {
        if (!CommonConfig.leadWaterContamination() || !mayHaveRawLeadNear(pos)) {
            return 0.0F;
        }
        return LeadWater.strength(level, pos);
    }

    /** Whether a change to a block changes the water's tint around it, so that water has to be drawn again. */
    public static boolean changesTint(BlockState oldState, BlockState newState) {
        boolean wasLead = oldState.getBlock() instanceof RawLeadBlock;
        boolean isLead = newState.getBlock() instanceof RawLeadBlock;
        return (wasLead || isLead) && (wasLead != isLead
                || !oldState.getValue(RawLeadBlock.CONTAMINATION).equals(newState.getValue(RawLeadBlock.CONTAMINATION)));
    }

    /**
     * Whether any chunk section within reach of {@code pos} may hold raw lead, read off the sections' palettes, so
     * the oceans' water skips the search: the tint is asked for every water block a chunk draws.
     */
    private static boolean mayHaveRawLeadNear(BlockPos pos) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }
        int reach = LeadWater.MAX_RADIUS;
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
}

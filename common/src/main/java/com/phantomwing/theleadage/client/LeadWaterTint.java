package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Water raw lead contaminates, tinted lead grey: strongest beside the lead, fading towards the edge of its reach
 * ({@link LeadWater#strength}). Applied to the water colour every loader's water takes from the biome, by each loader's
 * {@code BiomeColorsMixin}; a rendering mod that colours water its own way leaves it out, which is why the water has
 * specks too.
 */
public final class LeadWaterTint {
    private static final int LEAD_GREY = 0x5F656D;
    private static final float MOST = 0.7F;

    private LeadWaterTint() {
    }

    /** The water colour at {@code pos}, tinted where raw lead contaminates it. */
    public static int tint(BlockAndTintGetter level, BlockPos pos, int color) {
        if (!CommonConfig.leadWaterContamination() || !mayHaveRawLeadNear(pos)) {
            return color;
        }
        float strength = LeadWater.strength(level, pos) * MOST;
        if (strength <= 0.0F) {
            return color;
        }
        return FastColor.ARGB32.color(FastColor.ARGB32.alpha(color),
                mix(FastColor.ARGB32.red(color), FastColor.ARGB32.red(LEAD_GREY), strength),
                mix(FastColor.ARGB32.green(color), FastColor.ARGB32.green(LEAD_GREY), strength),
                mix(FastColor.ARGB32.blue(color), FastColor.ARGB32.blue(LEAD_GREY), strength));
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

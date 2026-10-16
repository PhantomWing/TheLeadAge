package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.RawLeadBlock;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.particle.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Lead specks drifting in contaminated water ({@link LeadSpeckParticle}), let loose every tick around each block of
 * raw lead near the camera, so they stream steadily rather than in the bursts a block's own animation ticks would give.
 * Particles are the one sign of the lead every shader pack shows. Ticked with the level's ambient particles by each
 * loader's {@code ClientLevelMixin}.
 */
public final class LeadWaterSpecks {
    /** How far from the player the lead is looked for: vanilla doesn't draw particles further out. */
    private static final int RANGE = 32;
    /** How often the lead around the player is looked for again, in ticks. */
    private static final int LOOK_EVERY = 20;
    /** Specks a tick for each block of water the lead contaminates, at full strength. */
    private static final float RATE = 0.05F;

    private static final List<BlockPos> LEADS = new ArrayList<>();
    private static ClientLevel lookedIn;
    private static long lookedAt;

    private LeadWaterSpecks() {
    }

    /** The level's ambient particle tick, around the player at {@code x, y, z}. */
    public static void tick(ClientLevel level, int x, int y, int z) {
        LeadWaterTint.tick(level);
        int range = LeadWater.range();
        if (range == 0) {
            return;
        }
        long now = level.getGameTime();
        if (level != lookedIn || now < lookedAt || now - lookedAt >= LOOK_EVERY) {
            look(level, x, y, z);
            lookedIn = level;
            lookedAt = now;
        }
        RandomSource random = level.getRandom();
        for (BlockPos lead : LEADS) {
            BlockState state = level.getBlockState(lead);
            if (!(state.getBlock() instanceof RawLeadBlock) || state.getValue(RawLeadBlock.CONTAMINATION) == 0) {
                continue;
            }
            int step = state.getValue(RawLeadBlock.CONTAMINATION);
            LeadWater.Leak leak = LeadWaterTint.leakOf(level, lead);
            // Lead whose water overlaps shares out the specks, or a floor of raw lead would fill the water many times over.
            float expected = leak.size() * RATE / sharing(lead, range);
            for (int i = (int) expected + (random.nextFloat() < expected % 1.0F ? 1 : 0); i > 0; i--) {
                BlockPos at = leak.pick(step, random);
                if (at != null && random.nextFloat() < leak.strength(step, at)) {
                    level.addParticle(ModParticles.LEAD_SPECK.get(), at.getX() + random.nextDouble(),
                            at.getY() + random.nextDouble(), at.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
                }
            }
        }
    }

    /** How many of the leaking raw lead around the player, this one included, reach water this one's does. */
    private static int sharing(BlockPos lead, int range) {
        int count = 0;
        for (BlockPos other : LEADS) {
            if (other.distManhattan(lead) <= range * 2) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    /** Finds the raw lead in water around the player, reading only the chunk sections whose palettes may hold it. */
    private static void look(ClientLevel level, int x, int y, int z) {
        LEADS.clear();
        for (int sx = SectionPos.blockToSectionCoord(x - RANGE); sx <= SectionPos.blockToSectionCoord(x + RANGE); sx++) {
            for (int sz = SectionPos.blockToSectionCoord(z - RANGE); sz <= SectionPos.blockToSectionCoord(z + RANGE); sz++) {
                LevelChunk chunk = level.getChunkSource().getChunk(sx, sz, false);
                if (chunk == null) {
                    continue;
                }
                for (int sy = SectionPos.blockToSectionCoord(y - RANGE); sy <= SectionPos.blockToSectionCoord(y + RANGE); sy++) {
                    int index = chunk.getSectionIndexFromSectionY(sy);
                    if (index < 0 || index >= chunk.getSectionsCount()) {
                        continue;
                    }
                    LevelChunkSection section = chunk.getSection(index);
                    if (section.hasOnlyAir() || !section.maybeHas(LeadWaterTint::isLeaking)) {
                        continue;
                    }
                    for (int bx = 0; bx < 16; bx++) {
                        for (int by = 0; by < 16; by++) {
                            for (int bz = 0; bz < 16; bz++) {
                                BlockState state = section.getBlockState(bx, by, bz);
                                if (LeadWaterTint.isLeaking(state)) {
                                    BlockPos lead = new BlockPos(SectionPos.sectionToBlockCoord(sx) + bx,
                                            SectionPos.sectionToBlockCoord(sy) + by, SectionPos.sectionToBlockCoord(sz) + bz);
                                    if (lead.distManhattan(new BlockPos(x, y, z)) <= RANGE * 2) {
                                        LEADS.add(lead);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

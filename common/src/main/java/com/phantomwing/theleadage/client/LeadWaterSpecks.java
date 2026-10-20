package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.effect.RecedingLeads;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.particle.ModParticles;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.lang.ref.WeakReference;
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
    private static final float RATE = 0.10F;
    /**
     * The most blocks of water a lead's specks are counted over: past it they spread thinner rather than more of them.
     * A speck lives about 90 ticks, so open water a lead contaminates by the hundreds kept thousands alive, which pushes
     * the other translucent particles - smoke, potion swirls - out at the game's cap.
     */
    private static final int SPECK_BLOCKS = 64;
    /**
     * How many leads' routes are found in a tick at most: a floor of raw lead coming into view would otherwise find all of
     * theirs in one frame. The rest drift their specks a tick or two later.
     */
    private static final int FIND_PER_TICK = 4;

    private static final List<BlockPos> LEADS = new ArrayList<>();
    /** How many of the leaking lead around the player reach water each one's does, found with {@link #LEADS}. */
    private static final IntArrayList SHARING = new IntArrayList();
    /** The level the lead was last looked for in, weakly: a world left behind isn't kept alive by it. */
    private static WeakReference<ClientLevel> lookedIn = new WeakReference<>(null);
    private static long lookedAt;

    private LeadWaterSpecks() {
    }

    /** The level's ambient particle tick, around the player at {@code x, y, z}. */
    public static void tick(ClientLevel level, int x, int y, int z) {
        LeadWaterTint.tick(level);
        int range = LeadWater.range(true);
        if (range == 0) {
            return;
        }
        long now = level.getGameTime();
        if (level != lookedIn.get() || now < lookedAt || now - lookedAt >= LOOK_EVERY) {
            look(level, x, y, z);
            countSharing(range);
            lookedIn = new WeakReference<>(level);
            lookedAt = now;
        }
        RandomSource random = level.getRandom();
        int found = 0;
        for (int i = 0; i < LEADS.size(); i++) {
            BlockPos lead = LEADS.get(i);
            int step = LeadWater.stepAt(level, lead);
            if (step <= 0) {
                continue;
            }
            int cut = LeadWater.cutAt(level, lead);
            LeadWater.Leak leak = LeadWaterTint.keptLeakOf(level, lead);
            if (leak == null) {
                if (found++ >= FIND_PER_TICK) {
                    continue;
                }
                leak = LeadWaterTint.leakOf(level, lead);
            }
            // Lead whose water overlaps shares out the specks, or a floor of raw lead would fill the water many times over.
            float expected = Math.min(leak.size(), SPECK_BLOCKS) * RATE / SHARING.getInt(i);
            for (int specks = (int) expected + (random.nextFloat() < expected % 1.0F ? 1 : 0); specks > 0; specks--) {
                BlockPos at = leak.pick(step, random);
                if (at != null && random.nextFloat() < leak.strength(step, cut, at)) {
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

    private static void countSharing(int range) {
        SHARING.clear();
        for (BlockPos lead : LEADS) {
            SHARING.add(sharing(lead, range));
        }
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
                    if (section.hasOnlyAir() || !section.maybeHas(LeadWater::isLeaking)) {
                        continue;
                    }
                    for (int bx = 0; bx < 16; bx++) {
                        for (int by = 0; by < 16; by++) {
                            for (int bz = 0; bz < 16; bz++) {
                                BlockState state = section.getBlockState(bx, by, bz);
                                if (LeadWater.isLeaking(state)) {
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
        // Where raw lead stopped leaking, what it left recedes: its specks with it.
        for (long receding : RecedingLeads.of(level).keySet()) {
            BlockPos lead = BlockPos.of(receding);
            if (lead.distManhattan(new BlockPos(x, y, z)) <= RANGE * 2 && !LEADS.contains(lead)) {
                LEADS.add(lead);
            }
        }
    }
}

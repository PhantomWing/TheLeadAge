package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.SequenceRule;

import java.util.List;
import java.util.Optional;

import static com.phantomwing.theleadage.gametest.TestCompat.fail;
import static com.phantomwing.theleadage.gametest.TestCompat.leadVeins;
import static com.phantomwing.theleadage.gametest.TestCompat.noiseSettings;
import static com.phantomwing.theleadage.gametest.TestCompat.randomState;
import static com.phantomwing.theleadage.gametest.TestCompat.topMaterial;
import static com.phantomwing.theleadage.gametest.TestCompat.vanillaVeinToggle;

/**
 * Large lead ore veins, sampled from the overworld's own noises for a fixed seed, so the run is the same every time:
 * the veins' data loads and binds, and what they place is ore and filler in their band, with raw lead now and then,
 * only ever on iron's side of vanilla's vein noise, so never where a copper vein can be.
 *
 * <p>From 26.3 vanilla's veins are rules of the noise settings' material rule, and so are these: the bodies here are
 * 26.3's own, where the lines before read a NoiseChunk's fillers.</p>
 */
public final class LeadVeinGameTest {
    private static final long SEED = 20261014L;
    /** The area sampled: wide enough to cross several veins, whose noise changes over a few hundred blocks. */
    private static final int SIZE = 1024;
    private static final int STEP = 4;

    private LeadVeinGameTest() {
    }

    /**
     * A chunk's terrain is given the lead veins right after vanilla's iron vein, which so comes first where they meet.
     * Game tests run in a flat world that never builds a chunk from noise, so this asks the overworld's material system
     * for the block at a lead vein, through the rule it is handed, which the mod adds the veins to.
     */
    public static void chunksGetLeadVeinsAfterVanillas(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        NoiseGeneratorSettings settings = noiseSettings(level, NoiseGeneratorSettings.OVERWORLD);
        RandomState random = randomState(level, settings, SEED);
        LeadVeins.bind(random, level.registryAccess());
        TestCompat.VeinSampler veins = leadVeins(level, random);
        if (veins == null) {
            fail(helper, "the lead vein rule isn't in the overworld's registries");
            return;
        }
        BlockPos vein = null;
        BlockState expected = null;
        for (int x = 0; x < SIZE && vein == null; x += STEP) {
            for (int z = 0; z < SIZE && vein == null; z += STEP) {
                for (int y = LeadVeins.MIN_Y; y < LeadVeins.MAX_Y && vein == null; y += 2) {
                    BlockState state = veins.at(x, y, z);
                    if (state != null && !state.is(Blocks.TUFF)) {
                        vein = new BlockPos(x, y, z);
                        expected = state;
                    }
                }
            }
        }
        if (vein == null) {
            fail(helper, "no lead ore in a lead vein within " + SIZE + " blocks");
            return;
        }

        MaterialRule overworld = settings.materialRule().value();
        MaterialRule iron = new MaterialRule.HolderHolder(level.registryAccess().lookupOrThrow(Registries.MATERIAL_RULE)
                .getOrThrow(LeadVeins.IRON_ORE_VEIN));
        Optional<BlockState> inOverworld = topMaterial(level, random, overworld, vein);
        Optional<BlockState> afterIron = topMaterial(level, random, new SequenceRule(List.of(iron)), vein);
        List<MaterialRule> rules = LeadVeins.withLeadVeins(overworld, random) instanceof SequenceRule(List<MaterialRule> sequence)
                ? sequence : List.of();
        int ironAt = rules.indexOf(iron);
        if (!inOverworld.equals(Optional.of(expected))) {
            fail(helper, "the overworld's material rule placed " + inOverworld + " at the lead vein at " + vein + ", not " + expected);
        } else if (!afterIron.equals(Optional.of(expected))) {
            fail(helper, "a rule of vanilla's iron vein alone placed " + afterIron + " at " + vein + ", not the lead vein's " + expected);
        } else if (ironAt < 1 || ironAt + 1 >= rules.size()
                || !(rules.get(ironAt + 1) instanceof MaterialRule.HolderHolder(var veinRule) && veinRule.is(LeadVeins.RULE))) {
            fail(helper, "expected the lead veins right after vanilla's iron vein in the overworld's rule, found " + rules);
        } else {
            helper.succeed();
        }
    }

    public static void leadVeinsFormInTheirBand(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        NoiseGeneratorSettings settings = noiseSettings(level, NoiseGeneratorSettings.OVERWORLD);
        RandomState random = randomState(level, settings, SEED);
        TestCompat.VeinSampler veins = leadVeins(level, random);
        if (veins == null) {
            fail(helper, "the lead vein rule isn't in the overworld's registries");
            return;
        }

        int ore = 0;
        int raw = 0;
        int filler = 0;
        int outside = 0;
        int onCoppersSide = 0;
        for (int x = 0; x < SIZE; x += STEP) {
            for (int z = 0; z < SIZE; z += STEP) {
                for (int y = LeadVeins.MIN_Y - 6; y < LeadVeins.MAX_Y + 6; y += 2) {
                    BlockState state = veins.at(x, y, z);
                    if (state == null) {
                        continue;
                    }
                    if (vanillaVeinToggle(level, random, x, y, z) > 0.0F) {
                        onCoppersSide++;
                    }
                    if (y < LeadVeins.MIN_Y || y >= LeadVeins.MAX_Y) {
                        outside++;
                    } else if (state.is(ModBlocks.LEAD_ORE.get())) {
                        ore++;
                    } else if (state.is(ModBlocks.RAW_LEAD_BLOCK.get())) {
                        raw++;
                    } else if (state.is(Blocks.TUFF)) {
                        filler++;
                    } else {
                        fail(helper, "a lead vein placed " + state);
                        return;
                    }
                }
            }
        }

        String counts = ore + " ore, " + raw + " raw lead, " + filler + " tuff";
        if (onCoppersSide > 0) {
            fail(helper, onCoppersSide + " vein blocks where vanilla's vein noise is copper's");
        } else if (outside > 0) {
            fail(helper, outside + " vein blocks outside Y " + LeadVeins.MIN_Y + " to " + LeadVeins.MAX_Y);
        } else if (ore == 0 || raw == 0 || filler <= ore) {
            // Mostly filler, as vanilla's veins are, with lead ore through it and raw lead in that.
            fail(helper, "expected veins of mostly tuff with ore and some raw lead, found " + counts);
        } else if (raw * 10 > ore + raw) {
            fail(helper, "raw lead should be about 2% of the ore, found " + counts);
        } else {
            helper.succeed();
        }
    }
}

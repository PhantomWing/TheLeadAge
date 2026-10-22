package com.phantomwing.theleadage.world.vein;

import com.phantomwing.theleadage.block.ModBlocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jetbrains.annotations.Nullable;

/**
 * Places a lead vein's blocks, as vanilla 26.3's ore vein rule places copper's and iron's (and as
 * {@code OreVeinifier} did before it): where the vein runs, each block is placed with the density's chance, and is
 * then ore with the richness's chance outside the gaps, 2% of that ore a block of raw lead, and filler otherwise.
 * Null leaves the block to the rules after it, stone.
 */
final class LeadVeinifier implements NoiseChunk.BlockStateFiller {
    private static final float RAW_ORE_CHANCE = 0.02F;

    private final DensityFunction density;
    private final DensityFunction richness;
    private final DensityFunction gap;
    private final PositionalRandomFactory random;
    private final BlockState ore = ModBlocks.LEAD_ORE.get().defaultBlockState();
    private final BlockState rawOre = ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState();
    private final BlockState filler = Blocks.TUFF.defaultBlockState();

    LeadVeinifier(DensityFunction density, DensityFunction richness, DensityFunction gap, PositionalRandomFactory random) {
        this.density = density;
        this.richness = richness;
        this.gap = gap;
        this.random = random;
    }

    @Nullable
    @Override
    public BlockState calculate(DensityFunction.FunctionContext context) {
        float solidness = (float) density.compute(context);
        if (solidness <= 0.0F) {
            return null;
        }
        RandomSource random = this.random.at(context.blockX(), context.blockY(), context.blockZ());
        if (random.nextFloat() > solidness) {
            return null;
        }
        if (random.nextFloat() < (float) richness.compute(context) && gap.compute(context) < 0.0) {
            return random.nextFloat() < RAW_ORE_CHANCE ? rawOre : ore;
        }
        return filler;
    }
}

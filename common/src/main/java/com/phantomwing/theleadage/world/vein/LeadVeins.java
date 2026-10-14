package com.phantomwing.theleadage.world.vein;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

/**
 * Large lead ore veins, made as vanilla's copper and iron ore veins are: long ribbons of lead ore in dripstone
 * winding through the stone between Y {@value #MIN_Y} and {@value #MAX_Y}, now and then a block of raw lead in
 * them. Where a vanilla vein crosses one, the vanilla vein wins.
 *
 * <p>Their shape is data, as it is from 26.3, where vanilla's veins became material rules: noises of their own,
 * so they wind their own way, and density functions under {@code theleadage:overworld/lead_vein/} built like
 * vanilla's {@code overworld/ore_vein/} ones. {@link LeadVeinifier} places the blocks as vanilla's ore vein rule
 * does. Before 26.3 the veins are hard-coded in {@code NoiseChunk}, so a mixin adds this one after them, with its
 * functions bound to the world's seed when the level's {@code RandomState} is made ({@link #bind}).</p>
 */
public final class LeadVeins {
    public static final int MIN_Y = 8;
    public static final int MAX_Y = 48;

    public static final ResourceKey<NormalNoise.NoiseParameters> VEININESS = noise("lead_veininess");
    public static final ResourceKey<NormalNoise.NoiseParameters> VEIN_A = noise("lead_vein_a");
    public static final ResourceKey<NormalNoise.NoiseParameters> VEIN_B = noise("lead_vein_b");
    public static final ResourceKey<NormalNoise.NoiseParameters> VEIN_GAP = noise("lead_vein_gap");

    /** Where the veins run: a vein lies where it is over 0.4, and its richness grows with it. */
    public static final ResourceKey<DensityFunction> TOGGLE = function("toggle");
    /** At or above 0 along the ribbons, which two crossing noises draw through the veins. */
    public static final ResourceKey<DensityFunction> MASK = function("mask");
    /** How solid the vein is where it runs, the chance each block of it is placed; 0 or less where there is none. */
    public static final ResourceKey<DensityFunction> DENSITY = function("density");
    /** The chance a placed block is ore rather than filler. */
    public static final ResourceKey<DensityFunction> RICHNESS = function("richness");
    /** Below 0 where ore can be; filler fills the gaps. */
    public static final ResourceKey<DensityFunction> GAP = function("gap");

    /** Vanilla's {@code minecraft:y}, the block's height. */
    private static final ResourceKey<DensityFunction> Y = ResourceKey.create(Registries.DENSITY_FUNCTION,
            ResourceLocation.withDefaultNamespace("y"));

    /** How far past the veins' band their noises reach, as vanilla's do past its veins', so the band's edges aren't cut off. */
    private static final int BELOW_BAND = 4;
    private static final int ABOVE_BAND = 7;

    private LeadVeins() {
    }

    /** A level's lead vein functions, bound to its seed: what {@link LeadVeinifier} reads. */
    public record Functions(DensityFunction density, DensityFunction richness, DensityFunction gap,
                            PositionalRandomFactory random) {
    }

    /** Vanilla's ore vein noises, each under a name of its own. */
    public static void bootstrapNoises(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(VEININESS, new NormalNoise.NoiseParameters(-8, 1.0));
        context.register(VEIN_A, new NormalNoise.NoiseParameters(-7, 1.0));
        context.register(VEIN_B, new NormalNoise.NoiseParameters(-7, 1.0));
        context.register(VEIN_GAP, new NormalNoise.NoiseParameters(-5, 1.0));
    }

    /**
     * The density functions, each as vanilla 26.3's of the same name under {@code overworld/ore_vein/}, with copper's
     * band moved to lead's. Like copper, a vein is where the toggle is over 0.4, so lead veins are as common in
     * their band as copper's are in theirs.
     */
    public static void bootstrapFunctions(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise.NoiseParameters> noises = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        DensityFunction y = new DensityFunctions.HolderHolder(functions.getOrThrow(Y));

        context.register(TOGGLE, DensityFunctions.cacheOnce(inBand(y,
                DensityFunctions.noise(noises.getOrThrow(VEININESS), 1.5, 1.5), 0.0)));

        DensityFunction toggle = new DensityFunctions.HolderHolder(functions.getOrThrow(TOGGLE));
        DensityFunction ridges = DensityFunctions.max(
                inBand(y, DensityFunctions.noise(noises.getOrThrow(VEIN_A), 4.0, 4.0), 1.0).abs(),
                inBand(y, DensityFunctions.noise(noises.getOrThrow(VEIN_B), 4.0, 4.0), 1.0).abs());
        context.register(MASK, DensityFunctions.cacheOnce(DensityFunctions.rangeChoice(toggle, -0.4, 0.4,
                DensityFunctions.constant(-1.0), minus(DensityFunctions.constant(0.08), ridges))));

        // Thins out over the last 20 blocks at either edge of the band.
        DensityFunction edge = DensityFunctions.add(DensityFunctions.mul(DensityFunctions.min(
                        minus(DensityFunctions.constant(MAX_Y), y), DensityFunctions.add(y, DensityFunctions.constant(-MIN_Y))).clamp(0.0, 20.0),
                DensityFunctions.constant(0.01)), DensityFunctions.constant(-0.2));
        DensityFunction mask = new DensityFunctions.HolderHolder(functions.getOrThrow(MASK));
        DensityFunction never = DensityFunctions.constant(-1.0);
        context.register(DENSITY, DensityFunctions.rangeChoice(y, MIN_Y, MAX_Y,
                DensityFunctions.rangeChoice(mask, 0.0, 1000000.0,
                        DensityFunctions.rangeChoice(DensityFunctions.add(DensityFunctions.add(toggle, DensityFunctions.constant(-0.4)), edge),
                                0.0, 1000000.0, DensityFunctions.constant(0.7), never),
                        never),
                never));

        context.register(RICHNESS, DensityFunctions.add(toggle.abs().clamp(0.4, 0.6), DensityFunctions.constant(-0.3)));
        context.register(GAP, minus(DensityFunctions.constant(-0.3), DensityFunctions.noise(noises.getOrThrow(VEIN_GAP))));
    }

    /**
     * Binds the level's lead vein functions to its seed, when its registries have them: a datapack that takes them
     * out takes the veins with them.
     */
    public static void bind(RandomState random, RegistryAccess registries) {
        Registry<DensityFunction> functions = registries.registry(Registries.DENSITY_FUNCTION).orElse(null);
        if (functions == null) {
            return;
        }
        DensityFunction density = functions.get(DENSITY);
        DensityFunction richness = functions.get(RICHNESS);
        DensityFunction gap = functions.get(GAP);
        if (density == null || richness == null || gap == null) {
            return;
        }
        // What RandomState does for its own router: each noise made from the seed.
        DensityFunction.Visitor seeded = new DensityFunction.Visitor() {
            @Override
            public DensityFunction apply(DensityFunction function) {
                return function;
            }

            @Override
            public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noise) {
                return new DensityFunction.NoiseHolder(noise.noiseData(),
                        random.getOrCreateNoise(noise.noiseData().unwrapKey().orElseThrow()));
            }
        };
        ((LeadVeinHolder) (Object) random).theleadage$setLeadVeins(new Functions(
                density.mapAll(seeded), richness.mapAll(seeded), gap.mapAll(seeded), random.oreRandom()));
    }

    /**
     * The lead veins for one {@code NoiseChunk}, or null where there are none: where the level's noise settings turn
     * vanilla's veins off, the config turns these off, or nothing bound them. {@code chunk} is the chunk's own
     * {@code wrap}, which turns the functions' markers into its caches, as it does vanilla's router.
     */
    @Nullable
    public static NoiseChunk.BlockStateFiller filler(RandomState random, NoiseGeneratorSettings settings,
                                                     DensityFunction.Visitor chunk) {
        if (!settings.oreVeinsEnabled() || !CommonConfig.generateLeadVeins()) {
            return null;
        }
        Functions functions = ((LeadVeinHolder) (Object) random).theleadage$leadVeins();
        if (functions == null) {
            return null;
        }
        return new LeadVeinifier(functions.density().mapAll(chunk), functions.richness().mapAll(chunk),
                functions.gap().mapAll(chunk), functions.random());
    }

    /** {@code inRange} within the veins' band, and a little beyond it; {@code outside} elsewhere. Interpolated, as vanilla's are. */
    private static DensityFunction inBand(DensityFunction y, DensityFunction inRange, double outside) {
        return DensityFunctions.interpolated(DensityFunctions.rangeChoice(y, MIN_Y - BELOW_BAND, MAX_Y + ABOVE_BAND,
                inRange, DensityFunctions.constant(outside)));
    }

    private static DensityFunction minus(DensityFunction left, DensityFunction right) {
        return DensityFunctions.add(left, DensityFunctions.mul(DensityFunctions.constant(-1.0), right));
    }

    private static ResourceKey<NormalNoise.NoiseParameters> noise(String name) {
        return ResourceKey.create(Registries.NOISE, TheLeadAge.resourceLocation(name));
    }

    private static ResourceKey<DensityFunction> function(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, TheLeadAge.resourceLocation("overworld/lead_vein/" + name));
    }
}

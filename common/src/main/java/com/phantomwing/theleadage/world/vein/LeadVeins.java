package com.phantomwing.theleadage.world.vein;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;
import net.minecraft.world.level.levelgen.material.rule.SequenceRule;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Large lead ore veins, made as vanilla's copper and iron ore veins are: long ribbons of lead ore in tuff
 * winding through the stone between Y {@value #MIN_Y} and {@value #MAX_Y}, now and then a block of raw lead in
 * them.
 *
 * <p>They come from vanilla's own vein noises. Its {@code ore_veininess} decides where a vein lies and which: copper
 * where it is over 0.4, between Y 0 and 50, iron where it is under -0.4, between Y -60 and -8. Iron's side is
 * unused above that, and lead takes it, where the noise is under -{@value #THRESHOLD}: so lead never crosses a
 * copper vein, vanilla's veins are as they were, and lead veins are rarer than theirs, only the noise's strongest
 * reach being lead's. Its ribbons and gaps are vanilla's {@code ore_vein_a}, {@code ore_vein_b} and
 * {@code ore_gap}.</p>
 *
 * <p>Their shape is data, as vanilla's veins are from 26.3: density functions under
 * {@code theleadage:overworld/lead_vein/}, built like vanilla's {@code overworld/ore_vein/} ones, and the material
 * rule {@code theleadage:overworld/lead_ore_vein}, vanilla's own {@code ore_vein} rule over them, which places the
 * blocks as it places copper's and iron's. A chunk's material rule is its noise settings', so the rule is added to it
 * right after vanilla's iron vein as the chunk's surface is built ({@link #withLeadVeins}), once it is bound to the
 * level when the level's {@code RandomState} is made ({@link #bind}).</p>
 */
public final class LeadVeins {
    public static final int MIN_Y = 8;
    public static final int MAX_Y = 58;
    /**
     * How far under 0 vanilla's vein noise must reach for a lead vein, where copper and iron need 0.4: at 0.6 about a
     * quarter as many chunks have a lead vein as have a copper one. Vanilla's richness is at its most from 0.6, so
     * every lead vein is as rich as its richest.
     */
    public static final float THRESHOLD = 0.6F;

    /** Vanilla's vein noise, over its range of heights, within the veins' band: a lead vein lies where it is under -{@value #THRESHOLD}. */
    public static final ResourceKey<DensityFunction> TOGGLE = function("toggle");
    /** At or above 0 along the ribbons, which two crossing noises draw through the veins. */
    public static final ResourceKey<DensityFunction> MASK = function("mask");
    /** How solid the vein is where it runs, the chance each block of it is placed; 0 or less where there is none. */
    public static final ResourceKey<DensityFunction> DENSITY = function("density");
    /** The chance a placed block is ore rather than filler. */
    public static final ResourceKey<DensityFunction> RICHNESS = function("richness");
    /** Below 0 where ore can be; filler fills the gaps. */
    public static final ResourceKey<DensityFunction> GAP = function("gap");

    /** The rule that places the veins' blocks: vanilla's ore vein rule over the functions above. */
    public static final ResourceKey<MaterialRule> RULE = ResourceKey.create(Registries.MATERIAL_RULE,
            TheLeadAge.resourceLocation("overworld/lead_ore_vein"));
    /** Vanilla's iron vein, which the lead veins follow among a chunk's rules. */
    public static final ResourceKey<MaterialRule> IRON_ORE_VEIN = ResourceKey.create(Registries.MATERIAL_RULE,
            Identifier.withDefaultNamespace("overworld/iron_ore_vein"));

    /** Vanilla's {@code minecraft:y}, the block's height. */
    private static final ResourceKey<DensityFunction> Y = ResourceKey.create(Registries.DENSITY_FUNCTION,
            Identifier.withDefaultNamespace("y"));

    /** How far past the veins' band their noises reach, as vanilla's do past its veins', so the band's edges aren't cut off. */
    private static final int BELOW_BAND = 4;
    private static final int ABOVE_BAND = 7;
    /** The overworld's noise cell, which vanilla's vein noises are interpolated over; 26.3 names it in the function. */
    private static final int CELL_SIZE_XZ = 4;
    private static final int CELL_SIZE_Y = 8;
    private static final float RAW_ORE_CHANCE = 0.02F;

    private LeadVeins() {
    }

    /**
     * The density functions, each as vanilla 26.3's of the same name under {@code overworld/ore_vein/}, over vanilla's
     * noises: iron's density, moved up to lead's band and needing the noise under -{@value #THRESHOLD}.
     */
    public static void bootstrapFunctions(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        DensityFunction y = new DensityFunctions.HolderHolder(functions.getOrThrow(Y));

        context.register(TOGGLE, DensityFunctions.cache(inBand(y,
                DensityFunctions.noise(noises.getOrThrow(Noises.ORE_VEININESS), 1.5, 1.5), 0.0F)));

        DensityFunction toggle = new DensityFunctions.HolderHolder(functions.getOrThrow(TOGGLE));
        DensityFunction ridges = DensityFunctions.max(
                inBand(y, DensityFunctions.noise(noises.getOrThrow(Noises.ORE_VEIN_A), 4.0, 4.0), 1.0F).abs(),
                inBand(y, DensityFunctions.noise(noises.getOrThrow(Noises.ORE_VEIN_B), 4.0, 4.0), 1.0F).abs());
        context.register(MASK, DensityFunctions.cache(DensityFunctions.rangeChoice(toggle, -0.4F, 0.4F,
                DensityFunctions.constant(-1.0F), minus(DensityFunctions.constant(0.08F), ridges))));

        // Thins out over the last 20 blocks at either edge of the band.
        DensityFunction edge = DensityFunctions.add(DensityFunctions.mul(DensityFunctions.min(
                        minus(DensityFunctions.constant(MAX_Y), y), DensityFunctions.add(y, DensityFunctions.constant(-MIN_Y))).clamp(0.0F, 20.0F),
                DensityFunctions.constant(0.01F)), DensityFunctions.constant(-0.2F));
        DensityFunction mask = new DensityFunctions.HolderHolder(functions.getOrThrow(MASK));
        DensityFunction never = DensityFunctions.constant(-1.0F);
        context.register(DENSITY, DensityFunctions.rangeChoice(y, MIN_Y, MAX_Y,
                DensityFunctions.rangeChoice(mask, 0.0F, 1000000.0F,
                        DensityFunctions.rangeChoice(DensityFunctions.add(minus(DensityFunctions.constant(-THRESHOLD), toggle), edge),
                                0.0F, 1000000.0F, DensityFunctions.constant(0.7F), never),
                        never),
                never));

        context.register(RICHNESS, DensityFunctions.add(toggle.abs().clamp(0.4F, 0.6F), DensityFunctions.constant(-0.3F)));
        context.register(GAP, minus(DensityFunctions.constant(-0.3F), DensityFunctions.noise(noises.getOrThrow(Noises.ORE_GAP))));
    }

    /** The rule placing the veins: lead ore in tuff, 2% of the ore a block of raw lead, as vanilla's veins hold theirs. */
    public static void bootstrapRules(BootstrapContext<MaterialRule> context) {
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        context.register(RULE, new OreVeinRule(ModBlocks.LEAD_ORE.get().defaultBlockState(),
                ModBlocks.RAW_LEAD_BLOCK.get().defaultBlockState(), Blocks.TUFF.defaultBlockState(), RAW_ORE_CHANCE,
                new DensityFunctions.HolderHolder(functions.getOrThrow(DENSITY)),
                new DensityFunctions.HolderHolder(functions.getOrThrow(RICHNESS)),
                new DensityFunctions.HolderHolder(functions.getOrThrow(GAP))));
    }

    /**
     * Binds the level's lead vein rule to its {@code RandomState}, when its registries have it: a datapack that takes it
     * out takes the veins with it.
     */
    public static void bind(RandomState random, RegistryAccess registries) {
        registries.lookup(Registries.MATERIAL_RULE).flatMap(rules -> rules.get(RULE))
                .ifPresent(rule -> ((LeadVeinHolder) (Object) random).theleadage$setLeadVeins(new Bound(new MaterialRule.HolderHolder(rule))));
    }

    /**
     * {@code rule}, a chunk's material rule, with the lead veins tried right after vanilla's iron vein, which so comes
     * first where they meet; {@code rule} itself where it has no iron vein of vanilla's (the nether, the end, a world
     * of a mod's own), the config turns the veins off, or nothing bound them.
     */
    public static MaterialRule withLeadVeins(MaterialRule rule, RandomState random) {
        if (!CommonConfig.generateLeadVeins()) {
            return rule;
        }
        Bound bound = ((LeadVeinHolder) (Object) random).theleadage$leadVeins();
        return bound == null ? rule : bound.withLeadVeins(rule);
    }

    /** The lead vein rule bound to a level, and the last chunk rule given it, which every chunk of the level shares. */
    public static final class Bound {
        private final MaterialRule veins;
        @Nullable
        private volatile Added last;

        Bound(MaterialRule veins) {
            this.veins = veins;
        }

        /** The bound rule, vanilla's {@code ore_vein} rule as the level's registries hold it. */
        public MaterialRule veins() {
            return veins;
        }

        MaterialRule withLeadVeins(MaterialRule rule) {
            Added added = last;
            if (added == null || added.to() != rule) {
                added = new Added(rule, afterIronVein(rule, veins));
                last = added;
            }
            return added.with();
        }

        private record Added(MaterialRule to, MaterialRule with) {
        }
    }

    private static MaterialRule afterIronVein(MaterialRule rule, MaterialRule veins) {
        if (!(rule instanceof SequenceRule(List<MaterialRule> sequence))) {
            return rule;
        }
        for (int i = 0; i < sequence.size(); i++) {
            if (sequence.get(i) instanceof MaterialRule.HolderHolder(Holder<MaterialRule> holder) && holder.is(IRON_ORE_VEIN)) {
                List<MaterialRule> withLead = new ArrayList<>(sequence);
                withLead.add(i + 1, veins);
                return new SequenceRule(List.copyOf(withLead));
            }
        }
        return rule;
    }

    /** {@code inRange} within the veins' band, and a little beyond it; {@code outside} elsewhere. Interpolated, as vanilla's are. */
    private static DensityFunction inBand(DensityFunction y, DensityFunction inRange, float outside) {
        return DensityFunctions.interpolated(DensityFunctions.rangeChoice(y, MIN_Y - BELOW_BAND, MAX_Y + ABOVE_BAND,
                inRange, DensityFunctions.constant(outside)), CELL_SIZE_XZ, CELL_SIZE_Y);
    }

    private static DensityFunction minus(DensityFunction left, DensityFunction right) {
        return DensityFunctions.add(left, DensityFunctions.mul(DensityFunctions.constant(-1.0F), right));
    }

    private static ResourceKey<DensityFunction> function(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, TheLeadAge.resourceLocation("overworld/lead_vein/" + name));
    }
}

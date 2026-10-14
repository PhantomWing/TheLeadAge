package com.phantomwing.theleadage.neoforge.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * {@code /leadvein}, for looking at lead veins in a dev run; the test source set never ships.
 *
 * <ul>
 *   <li>{@code /leadvein <pos> [section|xray]} finds the richest lead vein world generation made under the column
 *   at {@code pos}, within {@value #SEARCH} blocks, and copies the {@value #SIZE}-block cube around it, as generated,
 *   caves and all, to stand on {@code pos}, with its fluids and loose blocks left out. {@code section} copies the
 *   half beyond the vein's middle, so its face to the north is cut through the vein; {@code xray} copies the vein
 *   alone.</li>
 *   <li>{@code /leadvein survey} says how many chunks around have a lead vein, beside how many have a copper one.</li>
 * </ul>
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID)
public final class LeadVeinCommand {
    private static final int SIZE = 32;
    private static final int SEARCH = 256;
    /** How many of the nearest veins found are weighed up before the richest is copied. */
    private static final int CANDIDATES = 32;
    /** Wide enough to cross dozens of either vein's noise, which changes over a few hundred blocks. */
    private static final int SURVEY_CHUNKS = 64;
    private static final SimpleCommandExceptionType NONE_FOUND =
            new SimpleCommandExceptionType(Component.literal("No lead vein within " + SEARCH + " blocks"));

    private LeadVeinCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("leadvein").requires(source -> source.hasPermission(2))
                .then(Commands.literal("survey").executes(context -> survey(context.getSource())))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> copy(context.getSource(), BlockPosArgument.getBlockPos(context, "pos"), View.WHOLE))
                        .then(Commands.literal("section")
                                .executes(context -> copy(context.getSource(), BlockPosArgument.getBlockPos(context, "pos"), View.SECTION)))
                        .then(Commands.literal("xray")
                                .executes(context -> copy(context.getSource(), BlockPosArgument.getBlockPos(context, "pos"), View.XRAY)))));
    }

    private enum View { WHOLE, SECTION, XRAY }

    private static int copy(CommandSourceStack source, BlockPos target, View view) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        BlockPos vein = richestVein(veins(level), target.getX(), target.getZ());
        BlockPos from = vein.offset(-SIZE / 2, -SIZE / 2, -SIZE / 2);
        BlockPos to = target.offset(-SIZE / 2, 0, -SIZE / 2);
        int ore = 0;
        int raw = 0;
        int tuff = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                for (int y = 0; y < SIZE; y++) {
                    if (view == View.SECTION && z < SIZE / 2) {
                        level.setBlock(to.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        continue;
                    }
                    BlockState state = level.getBlockState(from.offset(x, y, z));
                    boolean isVein = true;
                    if (state.is(ModBlocks.LEAD_ORE.get())) {
                        ore++;
                    } else if (state.is(ModBlocks.RAW_LEAD_BLOCK.get())) {
                        raw++;
                    } else if (state.is(Blocks.TUFF)) {
                        tuff++;
                    } else {
                        isVein = false;
                    }
                    if (!isVein && (view == View.XRAY || !state.getFluidState().isEmpty() || state.hasBlockEntity()
                            || state.getBlock() instanceof FallingBlock)) {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    level.setBlock(to.offset(x, y, z), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
        String biome = level.getBiome(vein).unwrapKey().map(key -> key.location().toString()).orElse("?");
        String said = String.format("Copied the lead vein at %d %d %d, in %s: %d lead ore, %d raw lead, %d tuff",
                vein.getX(), vein.getY(), vein.getZ(), biome, ore, raw, tuff);
        source.sendSuccess(() -> Component.literal(said), true);
        return ore + raw;
    }

    /**
     * The middle of the richest of the nearest veins under the column at x, z, read from the vein functions, which is
     * quick, rather than the world, which would have to generate every chunk looked in.
     */
    private static BlockPos richestVein(NoiseChunk.BlockStateFiller veins, int x, int z) throws CommandSyntaxException {
        List<BlockPos> found = new ArrayList<>();
        for (int dx = -SEARCH; dx <= SEARCH; dx += 8) {
            for (int dz = -SEARCH; dz <= SEARCH; dz += 8) {
                for (int y = LeadVeins.MIN_Y + 4; y < LeadVeins.MAX_Y - 4; y += 4) {
                    if (veins.calculate(new DensityFunction.SinglePointContext(x + dx, y, z + dz)) != null) {
                        found.add(new BlockPos(x + dx, y, z + dz));
                    }
                }
            }
        }
        found.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(x, pos.getY(), z)));
        List<BlockPos> weighed = new ArrayList<>();
        BlockPos richest = null;
        int most = 0;
        for (BlockPos pos : found) {
            if (weighed.size() == CANDIDATES) {
                break;
            }
            if (weighed.stream().anyMatch(other -> other.distManhattan(pos) < SIZE / 2)) {
                continue;
            }
            weighed.add(pos);
            int blocks = 0;
            for (int ax = -SIZE / 2; ax < SIZE / 2; ax += 2) {
                for (int ay = -SIZE / 2; ay < SIZE / 2; ay += 2) {
                    for (int az = -SIZE / 2; az < SIZE / 2; az += 2) {
                        if (veins.calculate(new DensityFunction.SinglePointContext(pos.getX() + ax, pos.getY() + ay, pos.getZ() + az)) != null) {
                            blocks++;
                        }
                    }
                }
            }
            if (blocks > most) {
                most = blocks;
                richest = pos;
            }
        }
        if (richest == null) {
            throw NONE_FOUND.create();
        }
        return richest;
    }

    private static int survey(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        RandomState random = overworldNoises(level);
        LeadVeins.bind(random, level.registryAccess());
        NoiseChunk.BlockStateFiller lead = LeadVeins.filler(random, overworld(level), function -> function);
        NoiseRouter router = random.router();
        BlockPos at = BlockPos.containing(source.getPosition());
        int centreX = at.getX() >> 4;
        int centreZ = at.getZ() >> 4;
        int chunks = 0;
        int withLead = 0;
        int withCopper = 0;
        for (int cx = centreX - SURVEY_CHUNKS; cx <= centreX + SURVEY_CHUNKS; cx++) {
            for (int cz = centreZ - SURVEY_CHUNKS; cz <= centreZ + SURVEY_CHUNKS; cz++) {
                chunks++;
                if (lead != null && hasLead(lead, cx << 4, cz << 4)) {
                    withLead++;
                }
                if (hasCopper(random, router, cx << 4, cz << 4)) {
                    withCopper++;
                }
            }
        }
        int seen = chunks;
        int leadChunks = withLead;
        int copperChunks = withCopper;
        source.sendSuccess(() -> Component.literal(String.format(
                "Of %d chunks within %d blocks: %d have a lead vein (%.1f%%), %d a copper vein (%.1f%%)",
                seen, SURVEY_CHUNKS * 16, leadChunks, 100.0 * leadChunks / seen, copperChunks, 100.0 * copperChunks / seen)), false);
        return leadChunks;
    }

    private static boolean hasLead(NoiseChunk.BlockStateFiller lead, int x0, int z0) {
        for (int x = x0; x < x0 + 16; x += 4) {
            for (int z = z0; z < z0 + 16; z += 4) {
                for (int y = LeadVeins.MIN_Y; y < LeadVeins.MAX_Y; y += 2) {
                    if (lead.calculate(new DensityFunction.SinglePointContext(x, y, z)) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Whether vanilla's copper vein places a block in the chunk, read as {@code OreVeinifier} reads it. */
    private static boolean hasCopper(RandomState random, NoiseRouter router, int x0, int z0) {
        for (int x = x0; x < x0 + 16; x += 4) {
            for (int z = z0; z < z0 + 16; z += 4) {
                for (int y = 0; y <= 50; y += 2) {
                    DensityFunction.FunctionContext at = new DensityFunction.SinglePointContext(x, y, z);
                    double toggle = router.veinToggle().compute(at);
                    if (toggle <= 0.0) {
                        continue;
                    }
                    double edge = Mth.clampedMap(Math.min(50 - y, y), 0.0, 20.0, -0.2, 0.0);
                    if (toggle + edge < 0.4 || random.oreRandom().at(x, y, z).nextFloat() > 0.7F
                            || router.veinRidged().compute(at) >= 0.0) {
                        continue;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static NoiseChunk.BlockStateFiller veins(ServerLevel level) throws CommandSyntaxException {
        RandomState random = overworldNoises(level);
        LeadVeins.bind(random, level.registryAccess());
        // Outside a chunk there are no caches to fill, so the functions are read as they are.
        NoiseChunk.BlockStateFiller veins = LeadVeins.filler(random, overworld(level), function -> function);
        if (veins == null) {
            throw NONE_FOUND.create();
        }
        return veins;
    }

    private static RandomState overworldNoises(ServerLevel level) {
        return RandomState.create(overworld(level), level.registryAccess().lookupOrThrow(Registries.NOISE), level.getSeed());
    }

    private static NoiseGeneratorSettings overworld(ServerLevel level) {
        return level.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD);
    }
}

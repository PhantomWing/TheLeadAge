package com.phantomwing.theleadage.block.custom;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data-driven surface transformations applied when a {@link LeadWeightBlock} lands on a block
 * hard enough (bricks crack, grass is pounded to dirt, ...). Entries are loaded from datapacks,
 * so any mod or pack can extend them — drop JSON files under {@code data/<namespace>/lead_weight_transforms/}
 * (nested folders allowed). Each file is an array (or a single object) of transforms:
 *
 * <pre>{@code
 * [
 *   { "input": "minecraft:stone_bricks", "output": "minecraft:cracked_stone_bricks" },
 *   { "input": "#minecraft:dirt",        "output": "minecraft:dirt" }
 * ]
 * }</pre>
 *
 * {@code input} is a block id, or a block tag when prefixed with {@code #}; {@code output} is a
 * block id (its default state). Direct-block entries win over tag entries; among tags, the first
 * match (in load order) is used.
 *
 * <p>1.21.2 made {@link SimpleJsonResourceReloadListener} generic over a {@link Codec} rather than
 * handing subclasses raw Gson, so parsing happens through {@link #FILE_CODEC} and {@code apply}
 * receives already-decoded records.</p>
 */
public final class LeadWeightTransforms extends SimpleJsonResourceReloadListener<List<LeadWeightTransforms.Transform>> {
    public static final String DIRECTORY = "lead_weight_transforms";
    private static final Logger LOGGER = LogUtils.getLogger();

    /** One datapack entry, still stringly-typed so a "#tag" input stays expressible. */
    protected record Transform(String input, String output) {
        static final Codec<Transform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("input").forGetter(Transform::input),
                Codec.STRING.fieldOf("output").forGetter(Transform::output)
        ).apply(instance, Transform::new));
    }

    /** A file is either an array of transforms or a single bare object. */
    private static final Codec<List<Transform>> FILE_CODEC = Codec.withAlternative(
            Transform.CODEC.listOf(),
            Transform.CODEC.xmap(List::of, single -> single.get(0)));

    private record TagRule(TagKey<Block> tag, BlockState output) {
    }

    // Rebuilt on every datapack reload. volatile: the reload runs off-thread, transforms read on the server thread.
    private static volatile Map<Block, BlockState> byBlock = Map.of();
    private static volatile List<TagRule> byTag = List.of();

    public LeadWeightTransforms() {
        // 1.21.4: the ctor takes a FileToIdConverter instead of a bare directory string.
        super(FILE_CODEC, net.minecraft.resources.FileToIdConverter.json(DIRECTORY));
    }

    /** Wire the datapack loader on both loaders. Call once from common init. */
    public static void register() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new LeadWeightTransforms(),
                ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, DIRECTORY));
    }

    /** The state {@code state} becomes on a hard impact, or {@code null} if it doesn't transform. */
    @Nullable
    public static BlockState transform(BlockState state) {
        BlockState direct = byBlock.get(state.getBlock());
        if (direct != null) {
            return direct;
        }
        for (TagRule rule : byTag) {
            if (state.is(rule.tag())) {
                return rule.output();
            }
        }
        return null;
    }

    @Override
    protected void apply(Map<ResourceLocation, List<Transform>> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<Block, BlockState> blocks = new HashMap<>();
        List<TagRule> tags = new ArrayList<>();
        for (Map.Entry<ResourceLocation, List<Transform>> file : files.entrySet()) {
            try {
                for (Transform entry : file.getValue()) {
                    parse(entry, blocks, tags);
                }
            } catch (Exception e) {
                LOGGER.error("Skipping invalid lead weight transform file {}: {}", file.getKey(), e.getMessage());
            }
        }
        byBlock = Map.copyOf(blocks);
        byTag = List.copyOf(tags);
        LOGGER.debug("Loaded {} lead weight block transforms + {} tag rules from {} file(s)", blocks.size(), tags.size(), files.size());
    }

    private static void parse(Transform entry, Map<Block, BlockState> blocks, List<TagRule> tags) {
        BlockState output = blockOrThrow(entry.output()).defaultBlockState();
        String input = entry.input();
        if (input.startsWith("#")) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(input.substring(1)));
            tags.add(new TagRule(tag, output));
        } else {
            blocks.put(blockOrThrow(input), output);
        }
    }

    private static Block blockOrThrow(String id) {
        // 1.21.2: Registry#get returns an Optional<Holder.Reference<T>> rather than the value.
        ResourceLocation key = ResourceLocation.parse(id);
        return BuiltInRegistries.BLOCK.get(key)
                .map(Holder::value)
                .orElseThrow(() -> new IllegalArgumentException("Unknown block '" + id + "'"));
    }
}

package com.phantomwing.theleadage.block.custom;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
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
 * Data-driven surface transformations applied when a {@link HeavyOrbBlock} lands on a block
 * hard enough (bricks crack, grass is pounded to dirt, ...). Entries are loaded from datapacks,
 * so any mod or pack can extend them — drop JSON files under {@code data/<namespace>/heavy_orb_transforms/}
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
 */
public final class HeavyOrbTransforms extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "heavy_orb_transforms";
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    private record TagRule(TagKey<Block> tag, BlockState output) {
    }

    // Rebuilt on every datapack reload. volatile: the reload runs off-thread, transforms read on the server thread.
    private static volatile Map<Block, BlockState> byBlock = Map.of();
    private static volatile List<TagRule> byTag = List.of();

    public HeavyOrbTransforms() {
        super(GSON, DIRECTORY);
    }

    /** Wire the datapack loader on both loaders. Call once from common init. */
    public static void register() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new HeavyOrbTransforms(),
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
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<Block, BlockState> blocks = new HashMap<>();
        List<TagRule> tags = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> file : files.entrySet()) {
            try {
                JsonElement root = file.getValue();
                if (root.isJsonArray()) {
                    for (JsonElement element : root.getAsJsonArray()) {
                        parse(GsonHelper.convertToJsonObject(element, "transform"), blocks, tags);
                    }
                } else {
                    parse(GsonHelper.convertToJsonObject(root, "transform"), blocks, tags);
                }
            } catch (Exception e) {
                LOGGER.error("Skipping invalid heavy orb transform file {}: {}", file.getKey(), e.getMessage());
            }
        }
        byBlock = Map.copyOf(blocks);
        byTag = List.copyOf(tags);
        LOGGER.debug("Loaded {} heavy orb block transforms + {} tag rules from {} file(s)", blocks.size(), tags.size(), files.size());
    }

    private static void parse(JsonObject obj, Map<Block, BlockState> blocks, List<TagRule> tags) {
        String input = GsonHelper.getAsString(obj, "input");
        BlockState output = blockOrThrow(GsonHelper.getAsString(obj, "output")).defaultBlockState();
        if (input.startsWith("#")) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(input.substring(1)));
            tags.add(new TagRule(tag, output));
        } else {
            blocks.put(blockOrThrow(input), output);
        }
    }

    private static Block blockOrThrow(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        if (!BuiltInRegistries.BLOCK.containsKey(key)) {
            throw new JsonSyntaxException("Unknown block '" + id + "'");
        }
        return BuiltInRegistries.BLOCK.get(key);
    }
}

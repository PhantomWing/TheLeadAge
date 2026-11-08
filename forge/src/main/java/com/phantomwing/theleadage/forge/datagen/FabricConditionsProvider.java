package com.phantomwing.theleadage.forge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Cross-loader condition bridge for the single shared generated tree. The recipe
 * providers gate the conditional recipes and their unlocking advancements with Forge's
 * top-level {@code "conditions"}, which Fabric cannot parse. This provider runs LAST
 * (after every recipe JSON is written) and, for each recipe or advancement carrying a
 * {@code conditions} array, additionally writes a translated
 * {@code "fabric:load_conditions"} array into the same file. One file, both
 * dialects, identical gating on both loaders. An unmapped condition type fails the
 * build so the parity contract can't drift unnoticed.
 *
 * <p>Only {@code data/<namespace>/recipes} and {@code data/<namespace>/advancements}: on 1.20.1 a loot
 * modifier's {@code conditions} are loot conditions.</p>
 */
public class FabricConditionsProvider implements DataProvider {
    private static final String FORGE_KEY = "conditions";
    private static final String FABRIC_KEY = "fabric:load_conditions";
    private static final String FABRIC_CONDITION = "condition";
    private static final String FORGE_TYPE = "type";

    private final PackOutput packOutput;

    public FabricConditionsProvider(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        Path root = packOutput.getOutputFolder();
        List<Path> jsonFiles = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .filter(p -> root.relativize(p).toString().replace(java.io.File.separatorChar, '/')
                            .matches("data/[^/]+/(recipes|advancements)/.*"))
                    .forEach(jsonFiles::add);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Path path : jsonFiles) {
            JsonObject json;
            try {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                JsonElement parsed = JsonParser.parseString(content);
                if (!parsed.isJsonObject()) {
                    continue;
                }
                json = parsed.getAsJsonObject();
            } catch (IOException e) {
                return CompletableFuture.failedFuture(e);
            }

            if (!json.has(FORGE_KEY) || !json.get(FORGE_KEY).isJsonArray()) {
                continue;
            }

            JsonArray forgeConditions = json.getAsJsonArray(FORGE_KEY);
            JsonArray fabricConditions = new JsonArray();
            for (JsonElement element : forgeConditions) {
                fabricConditions.add(translate(element.getAsJsonObject(), path));
            }

            json.add(FABRIC_KEY, fabricConditions);
            futures.add(DataProvider.saveStable(cache, json, path));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static JsonObject translate(JsonObject forge, Path path) {
        String type = forge.get(FORGE_TYPE).getAsString();
        JsonObject fabric = new JsonObject();

        switch (type) {
            case "forge:mod_loaded" -> {
                fabric.addProperty(FABRIC_CONDITION, "fabric:all_mods_loaded");
                JsonArray values = new JsonArray();
                values.add(forge.get("modid").getAsString());
                fabric.add("values", values);
            }
            case "forge:not" -> {
                fabric.addProperty(FABRIC_CONDITION, "fabric:not");
                fabric.add("value", translate(forge.getAsJsonObject("value"), path));
            }
            case "forge:and", "forge:or" -> {
                fabric.addProperty(FABRIC_CONDITION, type.equals("forge:and") ? "fabric:and" : "fabric:or");
                JsonArray values = new JsonArray();
                for (JsonElement value : forge.getAsJsonArray("values")) {
                    values.add(translate(value.getAsJsonObject(), path));
                }
                fabric.add("values", values);
            }
            case "theleadage:config_boolean" -> {
                // Same condition id + field; only the dispatch key differs
                // (Forge "type" -> Fabric "condition").
                fabric.addProperty(FABRIC_CONDITION, "theleadage:config_boolean");
                fabric.addProperty("settingId", forge.get("settingId").getAsString());
            }
            default -> throw new IllegalStateException(
                    "FabricConditionsProvider: unmapped Forge condition type '" + type
                            + "' in " + path + ". Add a translation to keep loader parity.");
        }

        return fabric;
    }

    @Override
    public @NotNull String getName() {
        return "The Lead Age Fabric Load Conditions";
    }
}

package com.phantomwing.theleadage.neoforge.datagen;

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
 * providers gate the conditional vanilla-recipe overrides with NeoForge-only
 * {@code "neoforge:conditions"} blocks, which Fabric cannot parse. This provider
 * runs LAST (after every recipe JSON is written) and, for each file carrying a
 * {@code neoforge:conditions} array, additionally writes a translated
 * {@code "fabric:load_conditions"} array into the same file. One file, both
 * dialects, identical gating on both loaders. An unmapped condition type fails the
 * build so the parity contract can't drift unnoticed.
 */
public class FabricConditionsProvider implements DataProvider {
    private static final String NEOFORGE_KEY = "neoforge:conditions";
    private static final String FABRIC_KEY = "fabric:load_conditions";
    private static final String FABRIC_CONDITION = "condition";
    private static final String NEOFORGE_TYPE = "type";

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

            if (!json.has(NEOFORGE_KEY) || !json.get(NEOFORGE_KEY).isJsonArray()) {
                continue;
            }

            JsonArray neoforgeConditions = json.getAsJsonArray(NEOFORGE_KEY);
            JsonArray fabricConditions = new JsonArray();
            for (JsonElement element : neoforgeConditions) {
                fabricConditions.add(translate(element.getAsJsonObject(), path));
            }

            json.add(FABRIC_KEY, fabricConditions);
            futures.add(DataProvider.saveStable(cache, json, path));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static JsonObject translate(JsonObject neoforge, Path path) {
        String type = neoforge.get(NEOFORGE_TYPE).getAsString();
        JsonObject fabric = new JsonObject();

        switch (type) {
            case "neoforge:mod_loaded" -> {
                fabric.addProperty(FABRIC_CONDITION, "fabric:all_mods_loaded");
                JsonArray values = new JsonArray();
                values.add(neoforge.get("modid").getAsString());
                fabric.add("values", values);
            }
            case "neoforge:not" -> {
                fabric.addProperty(FABRIC_CONDITION, "fabric:not");
                fabric.add("value", translate(neoforge.getAsJsonObject("value"), path));
            }
            case "neoforge:and" -> {
                fabric.addProperty(FABRIC_CONDITION, "fabric:and");
                JsonArray values = new JsonArray();
                for (JsonElement value : neoforge.getAsJsonArray("values")) {
                    values.add(translate(value.getAsJsonObject(), path));
                }
                fabric.add("values", values);
            }
            case "theleadage:config_boolean" -> {
                // Same condition id + field; only the dispatch key differs
                // (NeoForge "type" -> Fabric "condition").
                fabric.addProperty(FABRIC_CONDITION, "theleadage:config_boolean");
                fabric.addProperty("settingId", neoforge.get("settingId").getAsString());
            }
            default -> throw new IllegalStateException(
                    "FabricConditionsProvider: unmapped NeoForge condition type '" + type
                            + "' in " + path + ". Add a translation to keep loader parity.");
        }

        return fabric;
    }

    @Override
    public @NotNull String getName() {
        return "The Lead Age Fabric Load Conditions";
    }
}

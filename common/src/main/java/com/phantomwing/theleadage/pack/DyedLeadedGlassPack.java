package com.phantomwing.theleadage.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadedGlassDyes;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The files of the dyed leaded glass in the colours a dye mod adds: blockstates and item models for the
 * client, loot tables and tag entries for the server. The jar has them for vanilla's sixteen only, from
 * datagen; for any other colour they are written here, as datagen writes vanilla's, and served from memory
 * as a pack that is always on. It is offered only while a dye mod adds a colour, so without one nothing
 * changes. Each loader adds it to its pack repositories: NeoForge through {@code AddPackFindersEvent}, Fabric
 * through a mixin.
 *
 * <p>Written rather than copied from white glass's files: in a NeoForge dev run the generated resources are
 * a mod of their own, so ours can't be found in the mod's files there.</p>
 */
public final class DyedLeadedGlassPack extends AbstractPackResources {
    private static final String ID = TheLeadAge.MOD_ID + "/dyed_leaded_glass";
    private static final String MODEL = TheLeadAge.MOD_ID + ":block/stained_leaded_glass";

    private final PackType type;
    private final Map<ResourceLocation, byte[]> files;

    private DyedLeadedGlassPack(PackLocationInfo location, PackType type, Map<ResourceLocation, byte[]> files) {
        super(location);
        this.type = type;
        this.files = files;
    }

    /** The pack of this type, or {@code null} when no dye mod adds a colour. */
    @Nullable
    public static Pack create(PackType type) {
        List<String> colors = LeadedGlassDyes.extraColors().stream().map(DyeColor::getName).toList();
        return colors.isEmpty() ? null : create(type, colors);
    }

    /** The pack of this type for these colours, whatever mods are loaded: the game tests open one of white glass. */
    @Nullable
    public static Pack create(PackType type, List<String> colors) {
        Map<ResourceLocation, byte[]> made = new HashMap<>();
        files(type, colors).forEach((file, json) -> made.put(file, json.toString().getBytes(StandardCharsets.UTF_8)));
        PackLocationInfo location = new PackLocationInfo(ID, Component.literal("The Lead Age: dyed leaded glass"),
                PackSource.BUILT_IN, Optional.empty());
        return Pack.readMetaAndCreate(location, new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo info) {
                return new DyedLeadedGlassPack(info, type, made);
            }

            @Override
            public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                return new DyedLeadedGlassPack(info, type, made);
            }
        }, type, new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        if (!String.join("/", elements).equals(PACK_META)) {
            return null;
        }
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "Leaded glass in the colours of dye mods");
        pack.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(type));
        JsonObject meta = new JsonObject();
        meta.add("pack", pack);
        byte[] bytes = meta.toString().getBytes(StandardCharsets.UTF_8);
        return () -> new ByteArrayInputStream(bytes);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation id) {
        byte[] bytes = packType == type ? files.get(id) : null;
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput output) {
        if (packType != type) {
            return;
        }
        String folder = path.endsWith("/") ? path : path + "/";
        files.forEach((id, bytes) -> {
            if (id.getNamespace().equals(namespace) && id.getPath().startsWith(folder)) {
                output.accept(id, () -> new ByteArrayInputStream(bytes));
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return packType == type
                ? files.keySet().stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet())
                : Set.of();
    }

    @Override
    public void close() {
    }

    /**
     * The files of dyed leaded glass in these colours, as datagen writes vanilla's: the game tests hold
     * white glass's to its datagen'd loot table and tags.
     */
    public static Map<ResourceLocation, JsonObject> files(PackType type, List<String> colors) {
        Map<ResourceLocation, JsonObject> files = new HashMap<>();
        if (type == PackType.CLIENT_RESOURCES) {
            for (String color : colors) {
                String name = color + "_leaded_glass";
                files.put(id("blockstates/" + name + ".json"), blockState());
                files.put(id("models/item/" + name + ".json"), object("parent", MODEL));
            }
            return files;
        }
        JsonArray ids = new JsonArray();
        for (String color : colors) {
            String name = color + "_leaded_glass";
            files.put(id("loot_table/blocks/" + name + ".json"), lootTable(TheLeadAge.MOD_ID + ":" + name));
            ids.add(TheLeadAge.MOD_ID + ":" + name);
        }
        files.put(tagFile("block", ModTags.Blocks.LEADED_GLASS_BLOCKS.location()), tag(ids));
        files.put(tagFile("block", BlockTags.MINEABLE_WITH_PICKAXE.location()), tag(ids));
        files.put(tagFile("item", ModTags.Items.LEADED_GLASS_BLOCKS.location()), tag(ids));
        return files;
    }

    private static JsonObject blockState() {
        JsonObject variants = new JsonObject();
        variants.add("", object("model", MODEL));
        JsonObject state = new JsonObject();
        state.add("variants", variants);
        return state;
    }

    /** Drops itself when it survives the blast, as {@code dropSelf} writes it. */
    private static JsonObject lootTable(String item) {
        JsonObject entry = object("type", "minecraft:item");
        entry.addProperty("name", item);
        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonArray conditions = new JsonArray();
        conditions.add(object("condition", "minecraft:survives_explosion"));
        JsonObject pool = new JsonObject();
        pool.addProperty("bonus_rolls", 0.0);
        pool.add("conditions", conditions);
        pool.add("entries", entries);
        pool.addProperty("rolls", 1.0);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        JsonObject table = object("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", item.replace(":", ":blocks/"));
        return table;
    }

    private static JsonObject tag(JsonArray values) {
        JsonObject tag = new JsonObject();
        tag.addProperty("replace", false);
        tag.add("values", values);
        return tag;
    }

    private static ResourceLocation tagFile(String registry, ResourceLocation tag) {
        return ResourceLocation.fromNamespaceAndPath(tag.getNamespace(), "tags/" + registry + "/" + tag.getPath() + ".json");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, path);
    }

    private static JsonObject object(String key, String value) {
        JsonObject object = new JsonObject();
        object.addProperty(key, value);
        return object;
    }
}

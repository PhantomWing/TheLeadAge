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
import net.minecraft.server.packs.PackResources;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The files of the dyed leaded glass in the colours a dye mod adds: blockstates and item models for the
 * client, loot tables and tag entries for the server. The jar has them for vanilla's sixteen only, from
 * datagen; for any other colour they are written here, as datagen writes vanilla's, and served from memory
 * as a pack that is always on. It is offered only while a dye mod adds a colour, so without one nothing
 * changes. Each loader adds it to its pack repositories: Forge through {@code AddPackFindersEvent}, Fabric
 * through a mixin.
 *
 * <p>Written rather than copied from white glass's files: in a Forge dev run the generated resources are
 * a mod of their own, so ours can't be found in the mod's files there.</p>
 */
public final class DyedLeadedGlassPack extends AbstractPackResources {
    private static final String ID = TheLeadAge.MOD_ID + "/dyed_leaded_glass";
    private static final String MODEL = TheLeadAge.MOD_ID + ":block/stained_leaded_glass";

    private final PackType type;
    private final Map<ResourceLocation, byte[]> files;

    private DyedLeadedGlassPack(String id, PackType type, Map<ResourceLocation, byte[]> files) {
        super(id, true);
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
        Pack.ResourcesSupplier resources = id -> new DyedLeadedGlassPack(id, type, made);
        return Pack.readMetaAndCreate(ID, Component.literal("The Lead Age: dyed leaded glass"), true, resources, type,
                Pack.Position.BOTTOM, PackSource.BUILT_IN);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        if (!String.join("/", elements).equals(PACK_META)) {
            return null;
        }
        JsonObject pack = new JsonObject();
        // Fabric lists the pack where Forge hides it, and can't turn it off: it says it is The Lead Age's own.
        pack.addProperty("description", "Added by The Lead Age for your dye mods' colours");
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
            // 1.20.1's folder names: loot_tables, tags/blocks and tags/items (1.21 dropped the plurals). It has no
            // c:dyed tags either (1.21's), so there is none to put the colour in.
            files.put(id("loot_tables/blocks/" + name + ".json"), lootTable(TheLeadAge.MOD_ID + ":" + name));
            ids.add(TheLeadAge.MOD_ID + ":" + name);
        }
        files.put(tagFile("blocks", ModTags.Blocks.LEADED_GLASS_BLOCKS.location()), tag(ids));
        files.put(tagFile("blocks", BlockTags.MINEABLE_WITH_PICKAXE.location()), tag(ids));
        files.put(tagFile("items", ModTags.Items.LEADED_GLASS_BLOCKS.location()), tag(ids));
        // Forge's stained glass, which 1.20.1's datagen puts vanilla's sixteen in as 1.21's puts them in c:dyed.
        files.put(tagFile("blocks", new ResourceLocation("forge", "stained_glass")), tag(ids));
        files.put(tagFile("items", new ResourceLocation("forge", "stained_glass")), tag(ids));
        // Grappling Hooks' 1.20.1 build lists the glass its hooks break by id, where 1.21's takes the glass tags.
        files.put(tagFile("blocks", new ResourceLocation("yo_hooks", "fragile")), tag(ids));
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
        return new ResourceLocation(tag.getNamespace(), "tags/" + registry + "/" + tag.getPath() + ".json");
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(TheLeadAge.MOD_ID, path);
    }

    private static JsonObject object(String key, String value) {
        JsonObject object = new JsonObject();
        object.addProperty(key, value);
        return object;
    }
}

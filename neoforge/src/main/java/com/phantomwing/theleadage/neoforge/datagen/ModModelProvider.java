package com.phantomwing.theleadage.neoforge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.client.LeadedGlassItemModels;
import com.phantomwing.theleadage.client.LeadedGlassPaneItemSpecialRenderer;
import com.phantomwing.theleadage.client.LeadedGlassTrapdoorSpecialRenderer;
import com.phantomwing.theleadage.item.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified block + item model datagen for 1.21.4+ (vanilla {@link ModelProvider}).
 *
 * <p>Standard shapes (cubes, slab/stairs/wall, chiseled, pillar, doors, trapdoors) use the vanilla
 * generators. The mod-specific shapes (torch/lantern/chain/bars, lead weight tiers, the leaded
 * glass pane families and their dynamic multiparts) are emitted as raw JSON mirroring the output
 * this mod shipped on 1.21.1/1.21.3, since their state layouts exceed what PropertyDispatch can
 * express. Generated model JSON no longer carries render_type: since 1.21.4 render layers are
 * registered in code (see LeadedGlassItemModels#registerRenderLayers).</p>
 *
 * <p>Item models are data-driven definitions ({@code items/*.json}, since 1.21.4): every pane renders via
 * the {@code theleadage:leaded_glass_pane} special renderer (config-driven frame, tints and
 * clear-sprite swap), the trapdoor via {@code theleadage:leaded_glass_trapdoor}.</p>
 */
public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, TheLeadAge.MOD_ID);
        // This datagen JVM must know the custom ids to serialize the items/ definitions.
        LeadedGlassItemModels.registerTypes();
    }

    @Override
    protected void registerModels(BlockModelGenerators bmg, ItemModelGenerators img) {
        // ---------- Simple cubes (item model = the block model) ----------
        bmg.createTrivialCube(ModBlocks.LEAD_ORE.get());
        bmg.createTrivialCube(ModBlocks.DEEPSLATE_LEAD_ORE.get());
        bmg.createTrivialCube(ModBlocks.RAW_LEAD_BLOCK.get());
        bmg.createTrivialCube(ModBlocks.LEAD_BLOCK.get());
        bmg.createTrivialCube(ModBlocks.LEADED_GLASS.get());
        bmg.createTrivialCube(ModBlocks.LEAD_GRATE.get());
        for (DyeColor color : DyeColor.values()) {
            bmg.createTrivialCube(ModBlocks.STAINED_LEADED_GLASS.get(color).get());
        }

        // ---------- Families ----------
        bmg.new BlockFamilyProvider(TextureMapping.cube(ModBlocks.CUT_LEAD.get()))
                .fullBlock(ModBlocks.CUT_LEAD.get(), ModelTemplates.CUBE_ALL)
                .slab(ModBlocks.CUT_LEAD_SLAB.get())
                .stairs(ModBlocks.CUT_LEAD_STAIRS.get());
        bmg.new BlockFamilyProvider(TextureMapping.cube(ModBlocks.LEAD_BRICKS.get()))
                .fullBlock(ModBlocks.LEAD_BRICKS.get(), ModelTemplates.CUBE_ALL)
                .slab(ModBlocks.LEAD_BRICK_SLAB.get())
                .stairs(ModBlocks.LEAD_BRICK_STAIRS.get())
                .wall(ModBlocks.LEAD_BRICK_WALL.get());

        // ---------- Chiseled (horizontal-facing) + pillar ----------
        bmg.createHorizontallyRotatedBlock(ModBlocks.CHISELED_LEAD.get(), TexturedModel.CUBE);
        pillar(bmg, ModBlocks.LEAD_PILLAR.get());

        // ---------- Doors / trapdoors ----------
        bmg.createDoor(ModBlocks.LEAD_DOOR.get());
        // Explicitly orientable (bmg.createTrapdoor emitted the non-orientable variant, losing the
        // facing rotations this trapdoor shipped with).
        orientableTrapdoor(bmg, ModBlocks.LEAD_TRAPDOOR.get());
        // Leaded glass door/trapdoor: hand-authored block models; only the blockstate is generated.
        // Their item models are handled in the items section below.
        bmg.blockStateOutput.accept(BlockModelGenerators.createDoor(ModBlocks.LEADED_GLASS_DOOR.get(),
                pv("leaded_glass_door_bottom_left"),
                pv("leaded_glass_door_bottom_left_open"),
                pv("leaded_glass_door_bottom_right"),
                pv("leaded_glass_door_bottom_right_open"),
                pv("leaded_glass_door_top_left"),
                pv("leaded_glass_door_top_left_open"),
                pv("leaded_glass_door_top_right"),
                pv("leaded_glass_door_top_right_open")));
        bmg.blockStateOutput.accept(BlockModelGenerators.createOrientableTrapdoor(ModBlocks.LEADED_GLASS_TRAPDOOR.get(),
                pv("leaded_glass_trapdoor_top"),
                pv("leaded_glass_trapdoor_bottom"),
                pv("leaded_glass_trapdoor_open")));

        // ---------- Torch / lantern / chain / bars / bulb / weights (raw JSON) ----------
        leadTorchAndLantern(bmg);
        leadChain(bmg);
        leadBars(bmg);
        leadBulb(bmg);
        leadWeights(bmg);

        // ---------- Leaded glass panes ----------
        leadedGlassPanes(bmg);

        // ============================ ITEMS ============================
        img.generateFlatItem(ModItems.RAW_LEAD.get(), ModelTemplates.FLAT_ITEM);
        img.generateFlatItem(ModItems.LEAD_INGOT.get(), ModelTemplates.FLAT_ITEM);
        img.generateFlatItem(ModItems.LEAD_NUGGET.get(), ModelTemplates.FLAT_ITEM);
        img.generateFlatItem(ModItems.LEAD_SHEET.get(), ModelTemplates.FLAT_ITEM); // Create compat (dormant)
        img.generateFlatItem(ModItems.LEAD_HELMET.get(), ModelTemplates.FLAT_ITEM);
        img.generateFlatItem(ModItems.LEAD_CHESTPLATE.get(), ModelTemplates.FLAT_ITEM);
        img.generateFlatItem(ModItems.LEAD_LEGGINGS.get(), ModelTemplates.FLAT_ITEM);
        img.generateFlatItem(ModItems.LEAD_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        img.generateFlatItem(ModItems.LEAD_HORSE_ARMOR.get(), ModelTemplates.FLAT_ITEM);

        img.generateFlatItem(ModItems.LEAD_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        img.generateFlatItem(ModItems.LEAD_KNIFE.get(), ModelTemplates.FLAT_HANDHELD_ITEM); // FD compat (dormant)
        img.generateFlatItem(ModItems.LEAD_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        img.generateFlatItem(ModItems.LEAD_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        img.generateFlatItem(ModItems.LEAD_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        img.generateFlatItem(ModItems.LEAD_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);

        // Block items whose vanilla generator didn't register one (BlockFamilyProvider
        // slab/stairs/wall and createTrapdoor auto-register theirs — bytecode-verified).
        bmg.registerSimpleItemModel(ModBlocks.CHISELED_LEAD.get().asItem(), modBlock("chiseled_lead"));
        bmg.registerSimpleItemModel(ModBlocks.LEAD_PILLAR.get().asItem(), modBlock("lead_pillar"));
        bmg.registerSimpleItemModel(ModBlocks.LEAD_BULB.get().asItem(), modBlock("lead_bulb"));
        bmg.registerSimpleItemModel(ModBlocks.LEAD_WEIGHT.get().asItem(), modBlock("lead_weight"));
        bmg.registerSimpleItemModel(ModBlocks.CHIPPED_LEAD_WEIGHT.get().asItem(), modBlock("chipped_lead_weight"));
        bmg.registerSimpleItemModel(ModBlocks.DAMAGED_LEAD_WEIGHT.get().asItem(), modBlock("damaged_lead_weight"));

        // Flat sprites (cutout-alpha textures render fine as generated item layers).
        flatFromTexture(img, ModItems.LEAD_CHAIN.get(), modItem("lead_chain"));
        flatFromTexture(img, ModItems.LEAD_TORCH.get(), modBlockTexture("lead_torch"));
        flatFromTexture(img, ModItems.LEAD_LANTERN.get(), modItem("lead_lantern"));
        flatFromTexture(img, ModItems.LEAD_BARS.get(), modBlockTexture("lead_bars"));
        // (The plain lead door's flat item model comes from createDoor above.)

        // Leaded glass door: flat sprite (hand-authored model with the item texture).
        img.itemModelOutput.accept(ModItems.LEADED_GLASS_DOOR.get(),
                ItemModelUtils.plainModel(modItem("leaded_glass_door")));
        // Leaded glass trapdoor: the special renderer draws frame + configured glass.
        img.itemModelOutput.accept(ModItems.LEADED_GLASS_TRAPDOOR.get(),
                ItemModelUtils.specialModel(modItem("leaded_glass_trapdoor"),
                        new LeadedGlassTrapdoorSpecialRenderer.Unbaked()));

        // Panes: every type renders through the pane special renderer — the stack's config drives
        // the frame, region tints AND the clear-sprite swap. A static tinted model can't show
        // partially-dyed panes correctly (clear regions would draw as the untinted white texture);
        // the base model supplies only display transforms + particle.
        paneItem(img, ModItems.LEADED_GLASS_PANEL, "leaded_glass_pane");
        paneItem(img, ModItems.LEADED_GLASS_PANE_SPLIT, "leaded_glass_pane_split");
        paneItem(img, ModItems.LEADED_GLASS_PANE_PLUS, "leaded_glass_pane_plus");
        paneItem(img, ModItems.LEADED_GLASS_PANE_GRID, "leaded_glass_pane_grid");
        paneItem(img, ModItems.LEADED_GLASS_PANE_DIAGONAL, "leaded_glass_pane_diagonal");
        paneItem(img, ModItems.LEADED_GLASS_PANE_CROSS, "leaded_glass_pane_cross");
        paneItem(img, ModItems.LEADED_GLASS_PANE_DIAMOND, "leaded_glass_pane_diamond");
        paneItem(img, ModItems.LEADED_GLASS_PANE_LATTICE, "leaded_glass_pane_lattice");
        paneItem(img, ModItems.LEADED_GLASS_PANE_BARS, "leaded_glass_pane_bars");
        paneItem(img, ModItems.LEADED_GLASS_PANE_DIAGONAL_BARS, "leaded_glass_pane_diagonal_bars");
    }

    // ---------------- pane items ----------------

    private static void paneItem(ItemModelGenerators img, RegistrySupplier<Item> item, String baseModel) {
        img.itemModelOutput.accept(item.get(),
                ItemModelUtils.specialModel(modItem(baseModel), new LeadedGlassPaneItemSpecialRenderer.Unbaked()));
    }

    // ---------------- simple helpers ----------------

    /** plainVariant of a mod block model: the 1.21.5 statics take MultiVariant, not raw ids. */
    private static MultiVariant pv(String path) {
        return BlockModelGenerators.plainVariant(modBlock(path));
    }

    private static ResourceLocation modBlock(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "block/" + path);
    }

    private static ResourceLocation modItem(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "item/" + path);
    }

    private static ResourceLocation modBlockTexture(String path) {
        return modBlock(path);
    }

    private static void flatFromTexture(ItemModelGenerators img, Item item, ResourceLocation texture) {
        ResourceLocation model = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(texture), img.modelOutput);
        img.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private static void pillar(BlockModelGenerators bmg, Block block) {
        // Side texture is block/<name> (no "_side" suffix), end is _top — off the vanilla convention,
        // so the mapping is built explicitly. A dedicated horizontal model (not a rotated upright
        // one) keeps the lying pillar's texture orientation identical to pre-1.21.4 output.
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block))
                .put(TextureSlot.END, TextureMapping.getBlockTexture(block, "_top"));
        ResourceLocation model = ModelTemplates.CUBE_COLUMN.create(block, mapping, bmg.modelOutput);
        ResourceLocation horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(block, mapping, bmg.modelOutput);
        bmg.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block,
                BlockModelGenerators.plainVariant(model), BlockModelGenerators.plainVariant(horizontal)));
    }

    private static void orientableTrapdoor(BlockModelGenerators bmg, Block block) {
        TextureMapping mapping = TextureMapping.defaultTexture(block);
        ResourceLocation top = ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(block, mapping, bmg.modelOutput);
        ResourceLocation bottom = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(block, mapping, bmg.modelOutput);
        ResourceLocation open = ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(block, mapping, bmg.modelOutput);
        bmg.blockStateOutput.accept(BlockModelGenerators.createOrientableTrapdoor(block,
                BlockModelGenerators.plainVariant(top), BlockModelGenerators.plainVariant(bottom),
                BlockModelGenerators.plainVariant(open)));
        bmg.registerSimpleItemModel(block.asItem(), bottom);
    }

    // ---------------- raw JSON emission (shapes beyond PropertyDispatch) ----------------

    /** A blockstate emitted as raw JSON (parsed through the vanilla codec), mirroring committed output. */
    private record RawBlockState(Block block, JsonElement json) implements BlockModelDefinitionGenerator {
        @Override
        public BlockModelDefinition create() {
            return BlockModelDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        }
    }

    private static void rawModel(BlockModelGenerators bmg, ResourceLocation id, JsonObject json) {
        bmg.modelOutput.accept(id, () -> json);
    }

    /** A child model: {"parent": ..., "textures": {...}} (the old datagen's withExistingParent + textures). */
    private static JsonObject childModel(ResourceLocation parent, String[][] textures) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent.toString());
        JsonObject tex = new JsonObject();
        for (String[] entry : textures) {
            tex.addProperty(entry[0], entry[1]);
        }
        json.add("textures", tex);
        return json;
    }

    private static JsonObject variant(String model, Integer x, Integer y) {
        JsonObject v = new JsonObject();
        v.addProperty("model", model);
        if (x != null && x != 0) v.addProperty("x", x);
        if (y != null && y != 0) v.addProperty("y", y);
        return v;
    }

    private static void variants(BlockModelGenerators bmg, Block block, JsonObject variants) {
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        bmg.blockStateOutput.accept(new RawBlockState(block, root));
    }

    private void leadTorchAndLantern(BlockModelGenerators bmg) {
        String torchTex = "theleadage:block/lead_torch";
        rawModel(bmg, modBlock("lead_torch"), childModel(ResourceLocation.withDefaultNamespace("block/template_torch"),
                new String[][]{{"torch", torchTex}}));
        rawModel(bmg, modBlock("lead_wall_torch"), childModel(ResourceLocation.withDefaultNamespace("block/template_torch_wall"),
                new String[][]{{"torch", torchTex}}));
        JsonObject torch = new JsonObject();
        torch.add("", variant("theleadage:block/lead_torch", 0, 0));
        variants(bmg, ModBlocks.LEAD_TORCH.get(), torch);
        JsonObject wallTorch = new JsonObject();
        wallTorch.add("facing=east", variant("theleadage:block/lead_wall_torch", 0, 0));
        wallTorch.add("facing=north", variant("theleadage:block/lead_wall_torch", 0, 270));
        wallTorch.add("facing=south", variant("theleadage:block/lead_wall_torch", 0, 90));
        wallTorch.add("facing=west", variant("theleadage:block/lead_wall_torch", 0, 180));
        variants(bmg, ModBlocks.LEAD_WALL_TORCH.get(), wallTorch);

        String lanternTex = "theleadage:block/lead_lantern";
        rawModel(bmg, modBlock("lead_lantern"), childModel(ResourceLocation.withDefaultNamespace("block/template_lantern"),
                new String[][]{{"lantern", lanternTex}}));
        rawModel(bmg, modBlock("lead_lantern_hanging"), childModel(ResourceLocation.withDefaultNamespace("block/template_hanging_lantern"),
                new String[][]{{"lantern", lanternTex}}));
        JsonObject lantern = new JsonObject();
        lantern.add("hanging=false", variant("theleadage:block/lead_lantern", 0, 0));
        lantern.add("hanging=true", variant("theleadage:block/lead_lantern_hanging", 0, 0));
        variants(bmg, ModBlocks.LEAD_LANTERN.get(), lantern);
    }

    private void leadChain(BlockModelGenerators bmg) {
        String tex = "theleadage:block/lead_chain";
        rawModel(bmg, modBlock("lead_chain"), childModel(ResourceLocation.withDefaultNamespace("block/chain"),
                new String[][]{{"particle", tex}, {"all", tex}}));
        JsonObject chain = new JsonObject();
        chain.add("axis=x", variant("theleadage:block/lead_chain", 90, 90));
        chain.add("axis=y", variant("theleadage:block/lead_chain", 0, 0));
        chain.add("axis=z", variant("theleadage:block/lead_chain", 90, 0));
        variants(bmg, ModBlocks.LEAD_CHAIN.get(), chain);
    }

    private void leadBars(BlockModelGenerators bmg) {
        String tex = "theleadage:block/lead_bars";
        String[] parts = {"post_ends", "post", "cap", "cap_alt", "side", "side_alt"};
        for (String part : parts) {
            rawModel(bmg, modBlock("lead_bars_" + part),
                    childModel(ResourceLocation.withDefaultNamespace("block/iron_bars_" + part),
                            new String[][]{{"particle", tex}, {"bars", tex}, {"edge", tex}}));
        }
        JsonArray multipart = new JsonArray();
        multipart.add(barsPart("lead_bars_post_ends", null, null));
        multipart.add(barsPart("lead_bars_post", null, "north=false,east=false,south=false,west=false"));
        multipart.add(barsPart("lead_bars_cap", null, "north=true,east=false,south=false,west=false"));
        multipart.add(barsPart("lead_bars_cap", 90, "north=false,east=true,south=false,west=false"));
        multipart.add(barsPart("lead_bars_cap_alt", null, "north=false,east=false,south=true,west=false"));
        multipart.add(barsPart("lead_bars_cap_alt", 90, "north=false,east=false,south=false,west=true"));
        multipart.add(barsPart("lead_bars_side", null, "north=true"));
        multipart.add(barsPart("lead_bars_side", 90, "east=true"));
        multipart.add(barsPart("lead_bars_side_alt", null, "south=true"));
        multipart.add(barsPart("lead_bars_side_alt", 90, "west=true"));
        JsonObject root = new JsonObject();
        root.add("multipart", multipart);
        bmg.blockStateOutput.accept(new RawBlockState(ModBlocks.LEAD_BARS.get(), root));
    }

    private static JsonObject barsPart(String model, Integer yRot, String when) {
        JsonObject part = new JsonObject();
        part.add("apply", variant("theleadage:block/" + model, 0, yRot));
        if (when != null) {
            JsonObject conditions = new JsonObject();
            for (String term : when.split(",")) {
                String[] kv = term.split("=");
                conditions.addProperty(kv[0], kv[1]);
            }
            part.add("when", conditions);
        }
        return part;
    }

    private void leadBulb(BlockModelGenerators bmg) {
        JsonObject bulb = new JsonObject();
        for (boolean lit : new boolean[]{false, true}) {
            for (boolean powered : new boolean[]{false, true}) {
                String suffix = lit && powered ? "_lit_powered" : lit ? "_lit" : powered ? "_powered" : "";
                String name = "lead_bulb" + suffix;
                rawModel(bmg, modBlock(name), childModel(ResourceLocation.withDefaultNamespace("block/cube_all"),
                        new String[][]{{"all", "theleadage:block/" + name}}));
                bulb.add("lit=" + lit + ",powered=" + powered, variant("theleadage:block/" + name, 0, 0));
            }
        }
        variants(bmg, ModBlocks.LEAD_BULB.get(), bulb);
    }

    private void leadWeights(BlockModelGenerators bmg) {
        // Base tier: hand-authored models. Chipped/damaged: generated re-textures of the base parents.
        weightTier(bmg, ModBlocks.LEAD_WEIGHT.get(), "lead_weight", "lead_weight_hanging");
        retexturedWeight(bmg, "chipped_lead_weight");
        weightTier(bmg, ModBlocks.CHIPPED_LEAD_WEIGHT.get(), "chipped_lead_weight", "chipped_lead_weight_hanging");
        retexturedWeight(bmg, "damaged_lead_weight");
        weightTier(bmg, ModBlocks.DAMAGED_LEAD_WEIGHT.get(), "damaged_lead_weight", "damaged_lead_weight_hanging");
    }

    private void retexturedWeight(BlockModelGenerators bmg, String tier) {
        rawModel(bmg, modBlock(tier), childModel(modBlock("lead_weight"),
                new String[][]{{"particle", "theleadage:block/" + tier}, {"all", "theleadage:block/" + tier}}));
        rawModel(bmg, modBlock(tier + "_hanging"), childModel(modBlock("lead_weight_hanging"),
                new String[][]{{"particle", "theleadage:block/" + tier}, {"all", "theleadage:block/" + tier}}));
    }

    private void weightTier(BlockModelGenerators bmg, Block block, String model, String hangingModel) {
        JsonObject weight = new JsonObject();
        weight.add("hanging=false", variant("theleadage:block/" + model, 0, 0));
        weight.add("hanging=true", variant("theleadage:block/" + hangingModel, 0, 0));
        variants(bmg, block, weight);
    }

    // ---------------- leaded glass panes ----------------

    private void leadedGlassPanes(BlockModelGenerators bmg) {
        paneVariants(bmg, ModBlocks.LEADED_GLASS_PANEL,
                paneFamily(bmg, "leaded_glass_pane_plain", "leaded_glass",
                        new String[]{"glass"}, new String[]{null, ""}));
        paneVariants(bmg, ModBlocks.LEADED_GLASS_PANE_SPLIT,
                paneFamily(bmg, "leaded_glass_pane_split_h", "leaded_glass_split_h",
                        new String[]{"glass_left", "glass_right"}, new String[]{null, "_left", "_right", "_both"}),
                paneFamily(bmg, "leaded_glass_pane_split_v", "leaded_glass_split_v",
                        new String[]{"glass_top", "glass_bottom"}, new String[]{null, "_top", "_bottom", "_both"}));
        paneVariants(bmg, ModBlocks.LEADED_GLASS_PANE_PLUS,
                paneFamily(bmg, "leaded_glass_pane_plus", "leaded_glass_plus", regionKeys(4), numericClearNames(4)));
        paneVariants(bmg, ModBlocks.LEADED_GLASS_PANE_CROSS,
                paneFamily(bmg, "leaded_glass_pane_cross", "leaded_glass_cross", regionKeys(4), numericClearNames(4)));
        paneVariants(bmg, ModBlocks.LEADED_GLASS_PANE_DIAMOND,
                paneFamily(bmg, "leaded_glass_pane_diamond", "leaded_glass_diamond", regionKeys(5), numericClearNames(5)));
        paneVariants(bmg, ModBlocks.LEADED_GLASS_PANE_BARS,
                paneFamily(bmg, "leaded_glass_pane_bars_h", "leaded_glass_bars_h", regionKeys(3), numericClearNames(3)),
                paneFamily(bmg, "leaded_glass_pane_bars_v", "leaded_glass_bars_v", regionKeys(3), numericClearNames(3)));
        paneVariants(bmg, ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS,
                paneFamily(bmg, "leaded_glass_pane_diagonal_bars_a", "leaded_glass_diagonal_bars_a", regionKeys(4), numericClearNames(4)),
                paneFamily(bmg, "leaded_glass_pane_diagonal_bars_b", "leaded_glass_diagonal_bars_b", regionKeys(4), numericClearNames(4)));
        paneVariants(bmg, ModBlocks.LEADED_GLASS_PANE_DIAGONAL,
                paneFamily(bmg, "leaded_glass_pane_diagonal_a", "leaded_glass_diagonal_a", regionKeys(2), numericClearNames(2)),
                paneFamily(bmg, "leaded_glass_pane_diagonal_b", "leaded_glass_diagonal_b", regionKeys(2), numericClearNames(2)));
        paneMultipart(bmg, ModBlocks.LEADED_GLASS_PANE_GRID, "leaded_glass_pane_grid", 9);
        paneMultipart(bmg, ModBlocks.LEADED_GLASS_PANE_LATTICE, "leaded_glass_pane_lattice", 12);
    }

    /**
     * The models for one pane pattern (one orientation), indexed by the bitmask of clear regions:
     * index 0 is the hand-authored base model; every other subset gets a generated child model that
     * re-textures just those regions to {@code clearTexture}.
     */
    private String[] paneFamily(BlockModelGenerators bmg, String base, String clearTexture,
                                String[] regionKeys, String[] clearNames) {
        String[] byMask = new String[1 << regionKeys.length];
        byMask[0] = "theleadage:block/" + base;
        for (int mask = 1; mask < byMask.length; mask++) {
            String name = base + "_clear" + clearNames[mask];
            List<String[]> textures = new ArrayList<>();
            for (int i = 0; i < regionKeys.length; i++) {
                if ((mask & (1 << i)) != 0) {
                    textures.add(new String[]{regionKeys[i], "theleadage:block/" + clearTexture});
                }
            }
            rawModel(bmg, modBlock(name), childModel(modBlock(base), textures.toArray(String[][]::new)));
            byMask[mask] = "theleadage:block/" + name;
        }
        return byMask;
    }

    private static String[] regionKeys(int regions) {
        String[] keys = new String[regions];
        for (int i = 0; i < regions; i++) {
            keys[i] = "glass_" + i;
        }
        return keys;
    }

    /** "_013"-style model-name suffixes (the clear region indices, ascending), indexed by clear bitmask. */
    private static String[] numericClearNames(int regions) {
        String[] names = new String[1 << regions];
        for (int mask = 1; mask < names.length; mask++) {
            StringBuilder name = new StringBuilder("_");
            for (int i = 0; i < regions; i++) {
                if ((mask & (1 << i)) != 0) {
                    name.append(i);
                }
            }
            names[mask] = name.toString();
        }
        return names;
    }

    /**
     * Variants for a pane block: orientation picks the model family, the clear bitmask the model,
     * face/facing the rotation. Keys enumerate every state (alphabetical property order, waterlogged
     * included), matching the output this mod shipped before 1.21.4.
     */
    private void paneVariants(BlockModelGenerators bmg, RegistrySupplier<Block> blockSupplier, String[]... familiesByOrientation) {
        LeadedGlassPaneBlock block = (LeadedGlassPaneBlock) blockSupplier.get();
        int regions = block.cameType().regions;
        int orientations = familiesByOrientation.length;
        JsonObject variants = new JsonObject();
        for (int mask = 0; mask < (1 << regions); mask++) {
            for (String face : new String[]{"ceiling", "floor", "wall"}) {
                for (String facing : new String[]{"east", "north", "south", "west"}) {
                    for (int orientation = 0; orientation < orientations; orientation++) {
                        for (boolean waterlogged : new boolean[]{false, true}) {
                            StringBuilder key = new StringBuilder();
                            for (int i = 0; i < regions; i++) {
                                key.append("clear_").append(i).append('=').append((mask & (1 << i)) != 0).append(',');
                            }
                            key.append("face=").append(face).append(",facing=").append(facing);
                            if (block.cameType().orientation != null) {
                                key.append(",orientation=").append(orientation);
                            }
                            key.append(",waterlogged=").append(waterlogged);
                            variants.add(key.toString(), variant(familiesByOrientation[orientation][mask],
                                    paneXRot(face), paneYRot(facing)));
                        }
                    }
                }
            }
        }
        variants(bmg, block, variants);
    }

    /** Dynamic (cell-based) panes: came + every cell per face/facing; clear cells swap at draw time. */
    private void paneMultipart(BlockModelGenerators bmg, RegistrySupplier<Block> block, String base, int cellCount) {
        JsonArray multipart = new JsonArray();
        for (String face : new String[]{"floor", "wall", "ceiling"}) {
            for (String facing : new String[]{"north", "east", "south", "west"}) {
                int xRot = paneXRot(face);
                int yRot = paneYRot(facing);
                multipart.add(panePart(base + "_came", xRot, yRot, face, facing));
                for (int cell = 0; cell < cellCount; cell++) {
                    multipart.add(panePart(base + "_cell_" + cell, xRot, yRot, face, facing));
                }
            }
        }
        JsonObject root = new JsonObject();
        root.add("multipart", multipart);
        bmg.blockStateOutput.accept(new RawBlockState(block.get(), root));
    }

    private static JsonObject panePart(String model, int xRot, int yRot, String face, String facing) {
        JsonObject part = new JsonObject();
        part.add("apply", variant("theleadage:block/" + model, xRot, yRot));
        JsonObject when = new JsonObject();
        when.addProperty("face", face);
        when.addProperty("facing", facing);
        part.add("when", when);
        return part;
    }

    /**
     * The pane sheet is authored 1px off-centre on its thin axis so it hugs the face it is attached
     * to, which means these two rotations also decide WHICH side of the sheet the player looks at.
     * A flat pane must present its FRONT face (model +Z), or the design reads mirrored; floor 90 /
     * ceiling 270 do that, and the collision boxes in LeadedGlassPaneBlock follow the sheet there.
     */
    private static int paneXRot(String face) {
        return switch (face) {
            case "floor" -> 90;
            case "ceiling" -> 270;
            default -> 0; // wall
        };
    }

    /** With the flat faces the right way up, the design's axes line up with the placer's view. */
    private static int paneYRot(String facing) {
        return switch (facing) {
            case "east" -> 90;
            case "south" -> 180;
            case "west" -> 270;
            default -> 0; // north
        };
    }
}

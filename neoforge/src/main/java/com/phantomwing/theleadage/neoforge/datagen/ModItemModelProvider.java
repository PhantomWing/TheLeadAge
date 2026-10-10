package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import com.phantomwing.theleadage.armor.ModTrimMaterials;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TheLeadAge.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Plain items use a generated (flat) model from their sprite.
        basicItem(ModItems.RAW_LEAD.get());
        basicItem(ModItems.LEAD_INGOT.get());
        basicItem(ModItems.LEAD_NUGGET.get());
        basicItem(ModItems.LEAD_SHEET.get()); // Create compat

        // Tools use the held (3D) parent; armor uses the flat generated model.
        handheldItem("lead_sword");
        handheldItem("lead_knife"); // Farmer's Delight compat
        handheldItem("lead_pickaxe");
        handheldItem("lead_axe");
        handheldItem("lead_shovel");
        handheldItem("lead_hoe");
        // Trimmable armor: flat model plus a trim_type override per material, lead included, so a
        // trim shows on the inventory icon. Vanilla armor is left alone.
        trimmableArmor(ModItems.LEAD_HELMET.get(), "helmet");
        trimmableArmor(ModItems.LEAD_CHESTPLATE.get(), "chestplate");
        trimmableArmor(ModItems.LEAD_LEGGINGS.get(), "leggings");
        trimmableArmor(ModItems.LEAD_BOOTS.get(), "boots");
        basicItem(ModItems.LEAD_HORSE_ARMOR.get());

        // Block items reuse the block's cube model (created by the BlockStateProvider).
        blockItem("lead_ore");
        blockItem("deepslate_lead_ore");
        blockItem("raw_lead_block");
        blockItem("lead_block");

        // Cut Lead + Lead Bricks get their item models from simpleBlockWithItem.
        // The rest need an explicit item model pointing at the right block model.
        blockItem("lead_brick_slab");
        blockItem("lead_brick_stairs");
        // Walls have no full block model; the item shows the vanilla wall inventory shape.
        wallInventory("lead_brick_wall", modLoc("block/lead_bricks"));
        blockItem("cut_lead_slab");
        blockItem("cut_lead_stairs");
        blockItem("chiseled_lead");
        blockItem("lead_pillar");
        blockItem("lead_grate");
        withExistingParent("lead_trapdoor", modLoc("block/lead_trapdoor_bottom"));
        // Door is a flat (generated) item sprite, not the 3D block model.
        basicItem(ModItems.LEAD_DOOR.get());
        // leaded_glass_door item model is hand-authored (a per-came-pattern texture via overrides).

        // Bulb: the unlit, unpowered block model.
        blockItem("lead_bulb");

        blockItem("leaded_glass");
        // Lead Weight (+ chipped/damaged tiers): 3D item from each hand-authored block model.
        blockItem("lead_weight");
        blockItem("chipped_lead_weight");
        blockItem("damaged_lead_weight");
        // Chain: a dedicated flat inventory sprite (item/lead_chain), like vanilla chain.
        withExistingParent("lead_chain", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/lead_chain"))
                .renderType(RenderType.cutout().name);
        // Bars: flat sprite of the block texture (cutout for the transparency).
        paneItem("lead_bars", "lead_bars", RenderType.cutout().name);
        // Torch: flat sprite of the block texture (like vanilla); lantern has a dedicated item sprite.
        paneItem("lead_torch", "lead_torch", RenderType.cutout().name);
        withExistingParent("lead_lantern", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/lead_lantern"))
                .renderType(RenderType.cutout().name);
        // Leaded glass pane item models come from scripts/genpanes.js, NOT datagen: their GUI
        // display must be an explicit identity transform (flat, front-facing, filling the 16x16
        // slot) to shadow block/block's isometric view, and NeoForge's transforms builder
        // silently drops identity transforms from the generated JSON.
        for (DyeColor color : DyeColor.values()) {
            blockItem(color.getName() + "_leaded_glass");
        }
    }

    // Panes use a flat (generated) item sprite of the glass body texture, like vanilla.
    // The render type is set explicitly so the alpha shows in the inventory (matching the
    // block items): plain = cutout (binary alpha), dyed = translucent (semi-transparent tint).
    private void paneItem(String name, String bodyTexture, String renderType) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("block/" + bodyTexture))
                .renderType(renderType);
    }

    private void handheldItem(String name) {
        withExistingParent(name, mcLoc("item/handheld")).texture("layer0", modLoc("item/" + name));
    }

    private void blockItem(String name) {
        withExistingParent(name, modLoc("block/" + name));
    }

    private static final ResourceLocation TRIM_TYPE = ResourceLocation.withDefaultNamespace("trim_type");
    /** Atlas permutations, not files, so the existing-file check has to be told they will exist. */
    private static final ExistingFileHelper.ResourceType TEXTURE =
            new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");

    /** Vanilla's trim materials and their item_model_index, the trim_type value each override matches. */
    private record VanillaTrim(String name, float index) {}
    private static final List<VanillaTrim> VANILLA_TRIMS = List.of(
            new VanillaTrim("quartz", 0.1f), new VanillaTrim("iron", 0.2f), new VanillaTrim("netherite", 0.3f),
            new VanillaTrim("redstone", 0.4f), new VanillaTrim("copper", 0.5f), new VanillaTrim("gold", 0.6f),
            new VanillaTrim("emerald", 0.7f), new VanillaTrim("diamond", 0.8f), new VanillaTrim("lapis", 0.9f),
            new VanillaTrim("amethyst", 1.0f));

    /**
     * The flat armor model plus one trim_type override per material, the way vanilla generates its
     * own armor, but with the lead material included. Without it the piece renders untrimmed in the
     * inventory for every material, vanilla ones included. Overrides are emitted in ascending
     * index order because the last override whose threshold the value reaches wins.
     */
    private void trimmableArmor(Item item, String slot) {
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        ResourceLocation texture = modLoc("item/" + name);
        ItemModelBuilder base = withExistingParent(name, "item/generated").texture("layer0", texture);

        record Entry(String suffix, float index) {}
        List<Entry> entries = new ArrayList<>();
        // Lead on lead needs the darker palette or it vanishes against the armour.
        entries.add(new Entry("lead_darker", ModTrimMaterials.LEAD_INDEX));
        for (VanillaTrim vanilla : VANILLA_TRIMS) {
            entries.add(new Entry(vanilla.name(), vanilla.index()));
        }
        entries.sort(Comparator.comparing(Entry::index));

        for (Entry entry : entries) {
            ResourceLocation sprite = mcLoc("trims/items/" + slot + "_trim_" + entry.suffix());
            existingFileHelper.trackGenerated(sprite, TEXTURE);
            ModelFile trim = withExistingParent(name + "_" + entry.suffix() + "_trim", "item/generated")
                    .texture("layer0", texture)
                    .texture("layer1", sprite);
            base.override().predicate(TRIM_TYPE, entry.index()).model(trim).end();
        }
    }

}

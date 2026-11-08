package com.phantomwing.theleadage.forge.datagen;

import com.phantomwing.theleadage.tags.BackportTags;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.tags.CommonTags;
import com.phantomwing.theleadage.tags.FabricTags;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    /** Farmer's Delight's knife tag — the Cutting Board's accepted tool. Inert when FD is absent. */
    private static final TagKey<Item> FARMERS_DELIGHT_KNIVES =
            TagKey.create(Registries.ITEM, new ResourceLocation("farmersdelight", "tools/knives"));
    /** Fabric API's glass blocks on 1.20.1. */
    private static final TagKey<Item> FABRIC_GLASS_BLOCKS =
            TagKey.create(Registries.ITEM, new ResourceLocation("c", "glass_blocks"));

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, TheLeadAge.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // The shared material tags: Forge's (forge:ingots/lead), which the recipes take, each taking in its
        // Fabric twin (c:lead_ingots), which lists the mod's own items too, so other mods' lead counts on both
        // loaders. Create's and Mekanism's crushing and enriching read forge:ores/lead, forge:raw_materials/lead and
        // forge:storage_blocks/raw_lead.
        Item[] ores = {ModItems.LEAD_ORE.get(), ModItems.DEEPSLATE_LEAD_ORE.get()};
        tag(CommonTags.Items.ORES_LEAD).add(ores).addOptionalTag(FabricTags.Items.LEAD_ORES.location());
        tag(FabricTags.Items.LEAD_ORES).add(ores);
        tag(Tags.Items.ORES).addTag(CommonTags.Items.ORES_LEAD);
        tag(Tags.Items.ORES_IN_GROUND_STONE).add(ModItems.LEAD_ORE.get());
        tag(Tags.Items.ORES_IN_GROUND_DEEPSLATE).add(ModItems.DEEPSLATE_LEAD_ORE.get());
        tag(Tags.Items.ORE_RATES_SINGULAR).add(ores);

        tag(CommonTags.Items.RAW_MATERIALS_LEAD).add(ModItems.RAW_LEAD.get())
                .addOptionalTag(FabricTags.Items.RAW_LEAD_ORES.location());
        tag(FabricTags.Items.RAW_LEAD_ORES).add(ModItems.RAW_LEAD.get());
        tag(Tags.Items.RAW_MATERIALS).addTag(CommonTags.Items.RAW_MATERIALS_LEAD);

        tag(CommonTags.Items.INGOTS_LEAD).add(ModItems.LEAD_INGOT.get())
                .addOptionalTag(FabricTags.Items.LEAD_INGOTS.location());
        tag(FabricTags.Items.LEAD_INGOTS).add(ModItems.LEAD_INGOT.get());
        tag(Tags.Items.INGOTS).addTag(CommonTags.Items.INGOTS_LEAD);

        tag(CommonTags.Items.NUGGETS_LEAD).add(ModItems.LEAD_NUGGET.get())
                .addOptionalTag(FabricTags.Items.LEAD_NUGGETS.location());
        tag(FabricTags.Items.LEAD_NUGGETS).add(ModItems.LEAD_NUGGET.get());
        tag(Tags.Items.NUGGETS).addTag(CommonTags.Items.NUGGETS_LEAD);

        tag(CommonTags.Items.STORAGE_BLOCKS_LEAD).add(ModItems.LEAD_BLOCK.get())
                .addOptionalTag(FabricTags.Items.LEAD_BLOCKS.location());
        tag(FabricTags.Items.LEAD_BLOCKS).add(ModItems.LEAD_BLOCK.get());
        tag(CommonTags.Items.STORAGE_BLOCKS_RAW_LEAD).add(ModItems.RAW_LEAD_BLOCK.get())
                .addOptionalTag(FabricTags.Items.RAW_LEAD_BLOCKS.location());
        tag(FabricTags.Items.RAW_LEAD_BLOCKS).add(ModItems.RAW_LEAD_BLOCK.get());
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTag(CommonTags.Items.STORAGE_BLOCKS_LEAD)
                .addTag(CommonTags.Items.STORAGE_BLOCKS_RAW_LEAD);

        // Create compat: the pressed Lead Sheet, under Create's plates convention.
        tag(CommonTags.Items.PLATES_LEAD).add(ModItems.LEAD_SHEET.get())
                .addOptionalTag(FabricTags.Items.LEAD_PLATES.location());
        tag(FabricTags.Items.LEAD_PLATES).add(ModItems.LEAD_SHEET.get());
        tag(CommonTags.Items.PLATES).addTag(CommonTags.Items.PLATES_LEAD);

        // Farmer's Delight compat: the Lead Knife, in FD's own knife tag (what the Cutting Board
        // accepts) and the c: convention tag. Both unconditional — a tag nobody consults is inert.
        tag(FARMERS_DELIGHT_KNIVES).add(ModItems.LEAD_KNIFE.get());
        tag(CommonTags.Items.TOOLS_KNIFE).add(ModItems.LEAD_KNIFE.get());
        tag(FabricTags.Items.KNIVES).add(ModItems.LEAD_KNIFE.get());

        // Beacon payment, like other metal blocks.
        tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.LEAD_INGOT.get());

        // Tools — vanilla type tags (gameplay hooks: weapon/tool behaviour, mob AI).
        tag(ItemTags.SWORDS).add(ModItems.LEAD_SWORD.get());
        tag(ItemTags.PICKAXES).add(ModItems.LEAD_PICKAXE.get());
        tag(ItemTags.AXES).add(ModItems.LEAD_AXE.get());
        tag(ItemTags.SHOVELS).add(ModItems.LEAD_SHOVEL.get());
        tag(ItemTags.HOES).add(ModItems.LEAD_HOE.get());

        // Armor — trim support. 1.21's enchantable armour tags (head_armor and the rest) aren't on 1.20.1, where an
        // armour item is enchanted by its slot.
        tag(ItemTags.TRIMMABLE_ARMOR).add(
                ModItems.LEAD_HELMET.get(), ModItems.LEAD_CHESTPLATE.get(),
                ModItems.LEAD_LEGGINGS.get(), ModItems.LEAD_BOOTS.get());
        // Lets the smithing table accept a lead ingot as a trim material.
        tag(ItemTags.TRIM_MATERIALS).add(ModItems.LEAD_INGOT.get());

        // Every pickaxe gets an amethyst cluster's full four shards.
        tag(ItemTags.CLUSTER_MAX_HARVESTABLES).add(ModItems.LEAD_PICKAXE.get());

        // Decorative block items.
        tag(ItemTags.SLABS).add(ModItems.LEAD_BRICK_SLAB.get(), ModItems.CUT_LEAD_SLAB.get());
        tag(ItemTags.STAIRS).add(ModItems.LEAD_BRICK_STAIRS.get(), ModItems.CUT_LEAD_STAIRS.get());
        tag(ItemTags.WALLS).add(ModItems.LEAD_BRICK_WALL.get());
        tag(ItemTags.DOORS).add(ModItems.LEAD_DOOR.get());
        tag(ItemTags.TRAPDOORS).add(ModItems.LEAD_TRAPDOOR.get());

        // Mirror the leaded-glass block tag onto items (used by the "Leaded Lights" advancement).
        copy(ModTags.Blocks.LEADED_GLASS_BLOCKS, ModTags.Items.LEADED_GLASS_BLOCKS);

        // 1.20.1 has no conventional chains tag; minecraft:chains (BackportTags) has them.
        // The 1.21.9 tags Vanilla Backport and Copper Age Backport fill on this version, as copper's are.
        tag(BackportTags.Items.BARS).add(ModItems.LEAD_BARS.get());
        tag(BackportTags.Items.CHAINS).add(ModItems.LEAD_CHAIN.get());
        tag(BackportTags.Items.LANTERNS).add(ModItems.LEAD_LANTERN.get());
        // Both spear backports put minecraft:spears into the enchantable tags, so the Lead Spear takes Lunge and
        // the melee enchantments through it.
        tag(BackportTags.Items.SPEARS).add(ModItems.LEAD_SPEAR.get());
        tag(CommonTags.Items.ARMORS_NAUTILUS).add(ModItems.LEAD_NAUTILUS_ARMOR.get());
        // What a Sulfur Cube swallowing it turns into, as vanilla's metal blocks, ores and bulbs do (26.2, and Vanilla
        // Backport here).
        tag(BackportTags.Items.SULFUR_CUBE_SLOW_FLAT).add(ModItems.LEAD_ORE.get(), ModItems.DEEPSLATE_LEAD_ORE.get(),
                ModItems.RAW_LEAD_BLOCK.get(), ModItems.LEAD_BLOCK.get(), ModItems.CUT_LEAD.get(),
                ModItems.CHISELED_LEAD.get(), ModItems.LEAD_BULB.get());
        // Glass, as on the blocks (see ModBlockTagsProvider).
        tag(Tags.Items.GLASS).addTag(ModTags.Items.LEADED_GLASS_BLOCKS);
        tag(Tags.Items.GLASS_COLORLESS).add(ModItems.LEADED_GLASS.get());
        tag(FABRIC_GLASS_BLOCKS).addTag(ModTags.Items.LEADED_GLASS_BLOCKS);
        for (DyeColor color : DyeColor.values()) {
            tag(Tags.Items.STAINED_GLASS).add(ModBlocks.STAINED_LEADED_GLASS.get(color).get().asItem());
        }

        // Item forms of the repellent tags, mirroring vanilla's #minecraft:piglin_repellents item tag.
        // NOT a copy() of the block tags: the wall torch has no item, and one item (LEAD_TORCH) places
        // both the standing and wall variants — so, exactly as in vanilla, this is torch + lantern.
        Item[] burningLeadItems = {ModItems.LEAD_TORCH.get(), ModItems.LEAD_LANTERN.get()};
        tag(ModTags.Items.CREEPER_REPELLENTS).add(burningLeadItems);
        tag(ModTags.Items.PILLAGER_REPELLENTS).add(burningLeadItems);

        // Gear made of solid lead: it gasses everyone nearby when it finally wears out. Unconditional
        // like the knife's other tags — the FD-only knife is simply inert when FD is absent.
        tag(ModTags.Items.LEAD_EQUIPMENT).add(
                ModItems.LEAD_SWORD.get(), ModItems.LEAD_PICKAXE.get(), ModItems.LEAD_AXE.get(),
                ModItems.LEAD_SHOVEL.get(), ModItems.LEAD_HOE.get(), ModItems.LEAD_KNIFE.get(),
                ModItems.LEAD_SPEAR.get(), ModItems.LEAD_HELMET.get(), ModItems.LEAD_CHESTPLATE.get(),
                ModItems.LEAD_LEGGINGS.get(), ModItems.LEAD_BOOTS.get());
    }

}

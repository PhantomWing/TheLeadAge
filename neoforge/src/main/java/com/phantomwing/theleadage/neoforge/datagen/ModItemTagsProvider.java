package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, TheLeadAge.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Conventional `c:` tags so other mods recognise lead as a standard metal.
        tag(c("ores")).add(ModItems.LEAD_ORE.get(), ModItems.DEEPSLATE_LEAD_ORE.get());
        tag(c("ores/lead")).add(ModItems.LEAD_ORE.get(), ModItems.DEEPSLATE_LEAD_ORE.get());

        tag(c("raw_materials")).add(ModItems.RAW_LEAD.get());
        tag(c("raw_materials/lead")).add(ModItems.RAW_LEAD.get());

        tag(c("ingots")).add(ModItems.LEAD_INGOT.get());
        tag(c("ingots/lead")).add(ModItems.LEAD_INGOT.get());

        tag(c("nuggets")).add(ModItems.LEAD_NUGGET.get());
        tag(c("nuggets/lead")).add(ModItems.LEAD_NUGGET.get());

        tag(c("storage_blocks")).add(ModItems.LEAD_BLOCK.get(), ModItems.RAW_LEAD_BLOCK.get());
        tag(c("storage_blocks/lead")).add(ModItems.LEAD_BLOCK.get());
        tag(c("storage_blocks/raw_lead")).add(ModItems.RAW_LEAD_BLOCK.get());

        // Beacon payment, like other metal blocks.
        tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.LEAD_INGOT.get());

        // Tools — vanilla type tags (gameplay hooks: weapon/tool behaviour, mob AI).
        tag(ItemTags.SWORDS).add(ModItems.LEAD_SWORD.get());
        tag(ItemTags.PICKAXES).add(ModItems.LEAD_PICKAXE.get());
        tag(ItemTags.AXES).add(ModItems.LEAD_AXE.get());
        tag(ItemTags.SHOVELS).add(ModItems.LEAD_SHOVEL.get());
        tag(ItemTags.HOES).add(ModItems.LEAD_HOE.get());

        // Armor — slot tags + trim support.
        tag(ItemTags.HEAD_ARMOR).add(ModItems.LEAD_HELMET.get());
        tag(ItemTags.CHEST_ARMOR).add(ModItems.LEAD_CHESTPLATE.get());
        tag(ItemTags.LEG_ARMOR).add(ModItems.LEAD_LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR).add(ModItems.LEAD_BOOTS.get());
        tag(ItemTags.TRIMMABLE_ARMOR).add(
                ModItems.LEAD_HELMET.get(), ModItems.LEAD_CHESTPLATE.get(),
                ModItems.LEAD_LEGGINGS.get(), ModItems.LEAD_BOOTS.get());
        // Lets the smithing table accept a lead ingot as a trim material.
        tag(ItemTags.TRIM_MATERIALS).add(ModItems.LEAD_INGOT.get());

        // Decorative block items.
        tag(ItemTags.SLABS).add(ModItems.LEAD_BRICK_SLAB.get(), ModItems.CUT_LEAD_SLAB.get());
        tag(ItemTags.STAIRS).add(ModItems.LEAD_BRICK_STAIRS.get(), ModItems.CUT_LEAD_STAIRS.get());
        tag(ItemTags.WALLS).add(ModItems.LEAD_BRICK_WALL.get());
        tag(ItemTags.DOORS).add(ModItems.LEAD_DOOR.get());
        tag(ItemTags.TRAPDOORS).add(ModItems.LEAD_TRAPDOOR.get());

        // Mirror the leaded-glass block tag onto items (used by the "Leaded Lights" advancement).
        copy(ModTags.Blocks.LEADED_GLASS_BLOCKS, ModTags.Items.LEADED_GLASS_BLOCKS);
    }

    private static TagKey<Item> c(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}

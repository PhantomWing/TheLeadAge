package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

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

        // Tools use the held (3D) parent; armor uses the flat generated model.
        handheldItem("lead_sword");
        handheldItem("lead_pickaxe");
        handheldItem("lead_axe");
        handheldItem("lead_shovel");
        handheldItem("lead_hoe");
        basicItem(ModItems.LEAD_HELMET.get());
        basicItem(ModItems.LEAD_CHESTPLATE.get());
        basicItem(ModItems.LEAD_LEGGINGS.get());
        basicItem(ModItems.LEAD_BOOTS.get());

        // Block items reuse the block's cube model (created by the BlockStateProvider).
        blockItem("lead_ore");
        blockItem("deepslate_lead_ore");
        blockItem("raw_lead_block");
        blockItem("lead_block");
    }

    private void handheldItem(String name) {
        withExistingParent(name, mcLoc("item/handheld")).texture("layer0", modLoc("item/" + name));
    }

    private void blockItem(String name) {
        withExistingParent(name, modLoc("block/" + name));
    }
}

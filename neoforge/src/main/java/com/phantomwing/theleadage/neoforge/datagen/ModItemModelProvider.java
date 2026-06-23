package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
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
        blockItem("cut_lead_slab");
        blockItem("cut_lead_stairs");
        blockItem("chiseled_lead");
        blockItem("lead_pillar");
        blockItem("lead_grate");
        withExistingParent("lead_trapdoor", modLoc("block/lead_trapdoor_bottom"));
        // Door is a flat (generated) item sprite, not the 3D block model.
        basicItem(ModItems.LEAD_DOOR.get());
        // leaded_glass_door item model is hand-authored (a per-came-pattern texture via overrides).

        blockItem("leaded_glass");
        // Heavy Orb: 3D item from its hand-authored block model.
        blockItem("heavy_orb");
        // Chain + bars: flat sprites of their block texture (cutout for the transparency).
        paneItem("lead_chain", "lead_chain", RenderType.cutout().name);
        paneItem("lead_bars", "lead_bars", RenderType.cutout().name);
        // leaded_glass_pane (the configurable pane) item model is hand-authored.
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
}

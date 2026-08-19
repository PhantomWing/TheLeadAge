package com.phantomwing.theleadage.armor;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

import java.util.Map;

/**
 * Registers lead as a smithing-table armor trim material (datapack registry,
 * loaded on both loaders). The {@code lead} colour palette is added to the
 * {@code minecraft:armor_trims} atlas via a merging {@code "replace": false}
 * source, and {@link com.phantomwing.theleadage.item.ModItems#LEAD_INGOT} carries the
 * PROVIDES_TRIM_MATERIAL component (1.21.5 moved the ingredient link onto the item)
 * so the smithing UI accepts it — together that makes lead trims apply and render
 * on <i>worn</i> armor.
 *
 * <p>Unlike The Silver Age we deliberately do <b>not</b> regenerate the vanilla
 * armor item models to add a per-material trim override: those models live in the
 * {@code minecraft:} namespace and are last-pack-wins (they don't merge), so doing
 * so would clobber any other trim mod's overrides. The only cost is that a
 * lead-trimmed piece shows no trim overlay on its <i>inventory icon</i>; the worn
 * trim is unaffected.</p>
 */
public class ModTrimMaterials {
    public static final ResourceKey<TrimMaterial> LEAD = trimMaterialKey("lead");

    /** The lead armor equipment-asset key (matches ModArmorMaterials' asset id). */
    private static final ResourceKey<EquipmentAsset> LEAD_EQUIPMENT_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, TheLeadAge.resourceLocation("lead"));

    /**
     * The lead trim palette. Use the darker palette when lead trim is applied to lead
     * armor, else it nearly disappears against the same colour (vanilla does the same,
     * e.g. iron trim → iron_darker on iron armor).
     */
    private static final MaterialAssetGroup LEAD_ASSETS =
            MaterialAssetGroup.create("lead", Map.of(LEAD_EQUIPMENT_ASSET, "lead_darker"));

    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        // 1.21.5: TrimMaterial is a record(MaterialAssetGroup assets, Component description). The
        // ingredient item moved onto the item's PROVIDES_TRIM_MATERIAL component, and the per-asset
        // override map lives on the MaterialAssetGroup.
        context.register(LEAD, new TrimMaterial(LEAD_ASSETS,
                Component.translatable(Util.makeDescriptionId("trim_material", LEAD.location()))
                        .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#6E737D").getOrThrow()))));
    }

    private static ResourceKey<TrimMaterial> trimMaterialKey(String name) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, TheLeadAge.resourceLocation(name));
    }
}

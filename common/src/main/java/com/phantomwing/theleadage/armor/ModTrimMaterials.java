package com.phantomwing.theleadage.armor;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.trim.TrimMaterial;


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
     * The lead trim palette, resolved under {@code textures/palettes/}. 26.3 replaced
     * MaterialAssetGroup with a single palette id: the darker palette for lead trim on lead armor
     * (else it nearly disappears against the same colour) now lives in the equipment asset's
     * {@code trim_overrides} instead.
     */
    public static final Identifier LEAD_PALETTE = TheLeadAge.resourceLocation("trim/lead");

    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        // 1.21.5 moved the ingredient item onto the item's PROVIDES_TRIM_MATERIAL component;
        // 26.3 reduced TrimMaterial to (palette id, description).
        context.register(LEAD, new TrimMaterial(LEAD_PALETTE,
                Component.translatable(Util.makeDescriptionId("trim_material", LEAD.identifier()))
                        .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#6E737D").getOrThrow()))));
    }

    private static ResourceKey<TrimMaterial> trimMaterialKey(String name) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, TheLeadAge.resourceLocation(name));
    }
}

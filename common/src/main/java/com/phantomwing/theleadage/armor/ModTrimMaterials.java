package com.phantomwing.theleadage.armor;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

import java.util.Map;

/**
 * Registers lead as a smithing-table armor trim material (datapack registry,
 * loaded on both loaders). The {@code lead} colour palette is added to the
 * {@code minecraft:armor_trims} atlas via a merging {@code "replace": false}
 * source, and {@link ModItems#LEAD_INGOT} is tagged {@code minecraft:trim_materials}
 * so the smithing UI accepts it — together that makes lead trims apply and render
 * on <i>worn</i> armor.
 *
 * <p>Unlike The Silver Age we deliberately do <b>not</b> regenerate the vanilla
 * armor item models to add a per-material {@code trim_type} override: those models
 * live in the {@code minecraft:} namespace and are last-pack-wins (they don't
 * merge), so doing so would clobber any other trim mod's overrides. The only cost
 * is that a lead-trimmed piece shows no trim overlay on its <i>inventory icon</i>;
 * the worn trim is unaffected.</p>
 */
public class ModTrimMaterials {
    // trim_type model-predicate index. Kept distinct from other mods' values, but
    // currently cosmetic-only since we don't regenerate the vanilla armor item models.
    public static final float LEAD_INDEX = 0.0019604F;
    public static final ResourceKey<TrimMaterial> LEAD = trimMaterialKey("lead");

    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        // 1.21.2 removed Registries.ARMOR_MATERIAL: the override map is now keyed by the equipment
        // asset id (the ArmorMaterial's modelId), so no registry lookup is needed at all.
        ResourceLocation leadArmor = TheLeadAge.resourceLocation("lead");

        registerMaterial(context, LEAD,
                ModItems.LEAD_INGOT.get(),
                Style.EMPTY.withColor(TextColor.parseColor("#6E737D").getOrThrow()),
                LEAD_INDEX,
                // Use the darker palette when lead trim is applied to lead armor, else
                // it nearly disappears against the same colour (vanilla does the same,
                // e.g. iron trim → iron_darker on iron armor).
                Map.of(leadArmor, "lead_darker"));
    }

    private static ResourceKey<TrimMaterial> trimMaterialKey(String name) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, TheLeadAge.resourceLocation(name));
    }

    private static void registerMaterial(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> trimKey, Item item, Style style, float itemModelIndex, Map<ResourceLocation, String> overrideArmorMaterials) {
        TrimMaterial trimMaterial = TrimMaterial.create(trimKey.location().getPath(), item, itemModelIndex,
                Component.translatable(Util.makeDescriptionId("trim_material", trimKey.location())).withStyle(style), overrideArmorMaterials);
        context.register(trimKey, trimMaterial);
    }
}

package com.phantomwing.theleadage.armor;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;

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

    // On 1.20.1 a trim material's darker palettes are keyed by vanilla's armour materials alone, so lead armour can't
    // take lead_darker as it does from 1.21: a lead trim on it draws in the plain lead palette.
    public static void bootstrap(BootstapContext<TrimMaterial> context) {
        registerMaterial(context, LEAD,
                ModItems.LEAD_INGOT.get(),
                Style.EMPTY.withColor(TextColor.parseColor("#6E737D")),
                LEAD_INDEX);
    }

    private static ResourceKey<TrimMaterial> trimMaterialKey(String name) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, TheLeadAge.resourceLocation(name));
    }

    private static void registerMaterial(BootstapContext<TrimMaterial> context, ResourceKey<TrimMaterial> trimKey, Item item, Style style, float itemModelIndex) {
        TrimMaterial trimMaterial = TrimMaterial.create(trimKey.location().getPath(), item, itemModelIndex,
                Component.translatable(Util.makeDescriptionId("trim_material", trimKey.location())).withStyle(style), Map.of());
        context.register(trimKey, trimMaterial);
    }
}

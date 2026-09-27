package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.armor.ModTrimMaterials;
import com.phantomwing.theleadage.damage.ModDamageTypes;
import com.phantomwing.theleadage.neoforge.world.ModBiomeModifiers;
import com.phantomwing.theleadage.world.ModFeatures;
import com.phantomwing.theleadage.world.ModPlacedFeatures;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * World-layer datapack registry entries; wired in {@link DataGenerators}. 26.3 moved the
 * DatapackBuiltinEntriesProvider construction into GatherDataEvent#createWorldRegistryObjects,
 * so this class is just the builder now.
 */
public final class ModDatapackProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap)
            .add(Registries.TRIM_MATERIAL, ModTrimMaterials::bootstrap)
            .add(Registries.FEATURE, ModFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

    private ModDatapackProvider() {
    }
}

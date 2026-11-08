package com.phantomwing.theleadage.forge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.armor.ModTrimMaterials;
import com.phantomwing.theleadage.damage.ModDamageTypes;
import com.phantomwing.theleadage.forge.world.ModBiomeModifiers;
import com.phantomwing.theleadage.world.ModConfiguredFeatures;
import com.phantomwing.theleadage.world.ModPlacedFeatures;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDatapackProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap)
            .add(Registries.DENSITY_FUNCTION, LeadVeins::bootstrapFunctions)
            .add(Registries.TRIM_MATERIAL, ModTrimMaterials::bootstrap)
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

    public ModDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(TheLeadAge.MOD_ID));
    }
}

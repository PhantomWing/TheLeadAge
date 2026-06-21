package com.phantomwing.theleadage.component;

import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

/**
 * Custom item data components. {@code leaded_glass_config} carries a panel's design
 * ({@link LeadedGlassConfig}) on the item so it survives crafting, placing and breaking.
 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<LeadedGlassConfig>> LEADED_GLASS_CONFIG =
            DATA_COMPONENTS.register("leaded_glass_config", () -> DataComponentType.<LeadedGlassConfig>builder()
                    .persistent(LeadedGlassConfig.CODEC)
                    .networkSynchronized(LeadedGlassConfig.STREAM_CODEC)
                    .build());

    public static void register() {
        DATA_COMPONENTS.register();
    }
}

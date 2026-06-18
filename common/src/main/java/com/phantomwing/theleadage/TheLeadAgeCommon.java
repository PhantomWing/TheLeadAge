package com.phantomwing.theleadage;

import com.phantomwing.theleadage.armor.ModArmorMaterials;
import com.phantomwing.theleadage.armor.MonsterArmorHandler;
import com.phantomwing.theleadage.attribute.ModAttributes;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.ui.ModCreativeModeTab;

/**
 * Common (loader-agnostic) entrypoint for The Lead Age.
 *
 * <p>Registers the Architectury {@code DeferredRegister}s (blocks, items, creative
 * tab). Loader-specific bootstrap (NeoForge config + datagen, Fabric AutoConfig,
 * biome injection) is performed by the per-loader entrypoints.</p>
 */
public final class TheLeadAgeCommon {
    public static final String MOD_ID = TheLeadAge.MOD_ID;

    private TheLeadAgeCommon() {
    }

    public static void init() {
        // Blocks MUST register before items: ModItems' BlockItem factories resolve
        // their Block via RegistrySupplier#get(), which Architectury invokes eagerly
        // during ITEMS.register() on Fabric.
        ModBlocks.register();
        ModAttributes.register();
        ModArmorMaterials.register();
        ModItems.register();
        ModCreativeModeTab.register();

        // Swap naturally-spawned iron armor on certain mobs for lead armor (low chance).
        MonsterArmorHandler.register();
    }
}

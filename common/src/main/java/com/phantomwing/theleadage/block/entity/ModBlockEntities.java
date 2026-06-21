package com.phantomwing.theleadage.block.entity;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<LeadedGlassPanelBlockEntity>> LEADED_GLASS_PANEL =
            BLOCK_ENTITIES.register("leaded_glass_pane", () -> BlockEntityType.Builder.of(
                    LeadedGlassPanelBlockEntity::new, ModBlocks.LEADED_GLASS_PANEL.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}

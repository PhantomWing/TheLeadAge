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
                    LeadedGlassPanelBlockEntity::new, ModBlocks.LEADED_GLASS_PANEL.get(),
                    ModBlocks.LEADED_GLASS_PANE_SPLIT.get(), ModBlocks.LEADED_GLASS_PANE_GRID.get(),
                    ModBlocks.LEADED_GLASS_PANE_GRID_3.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(),
                    ModBlocks.LEADED_GLASS_PANE_CROSS.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<LeadedGlassDoorBlockEntity>> LEADED_GLASS_DOOR =
            BLOCK_ENTITIES.register("leaded_glass_door", () -> BlockEntityType.Builder.of(
                    LeadedGlassDoorBlockEntity::new, ModBlocks.LEADED_GLASS_DOOR.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<LeadedGlassTrapdoorBlockEntity>> LEADED_GLASS_TRAPDOOR =
            BLOCK_ENTITIES.register("leaded_glass_trapdoor", () -> BlockEntityType.Builder.of(
                    LeadedGlassTrapdoorBlockEntity::new, ModBlocks.LEADED_GLASS_TRAPDOOR.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}

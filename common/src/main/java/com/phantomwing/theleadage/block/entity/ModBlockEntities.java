package com.phantomwing.theleadage.block.entity;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    // 1.21.2 removed BlockEntityType.Builder; construct via the (now widened — see
    // theleadage.accesswidener / accesstransformer.cfg) constructor directly.
    public static final RegistrySupplier<BlockEntityType<LeadedGlassPanelBlockEntity>> LEADED_GLASS_PANEL =
            BLOCK_ENTITIES.register("leaded_glass_pane", () -> new BlockEntityType<>(
                    LeadedGlassPanelBlockEntity::new, Set.of(ModBlocks.LEADED_GLASS_PANEL.get(),
                    ModBlocks.LEADED_GLASS_PANE_SPLIT.get(), ModBlocks.LEADED_GLASS_PANE_PLUS.get(),
                    ModBlocks.LEADED_GLASS_PANE_GRID.get(), ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(),
                    ModBlocks.LEADED_GLASS_PANE_CROSS.get(), ModBlocks.LEADED_GLASS_PANE_DIAMOND.get(),
                    ModBlocks.LEADED_GLASS_PANE_LATTICE.get(),
                    ModBlocks.LEADED_GLASS_PANE_BARS.get(),
                    ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS.get())));

    public static final RegistrySupplier<BlockEntityType<LeadedGlassDoorBlockEntity>> LEADED_GLASS_DOOR =
            BLOCK_ENTITIES.register("leaded_glass_door", () -> new BlockEntityType<>(
                    LeadedGlassDoorBlockEntity::new, Set.of(ModBlocks.LEADED_GLASS_DOOR.get())));

    // Lead torch toxicity ticker; one type shared by the standing and wall variants.
    public static final RegistrySupplier<BlockEntityType<LeadTorchBlockEntity>> LEAD_TORCH =
            BLOCK_ENTITIES.register("lead_torch", () -> new BlockEntityType<>(
                    LeadTorchBlockEntity::new, Set.of(ModBlocks.LEAD_TORCH.get(),
                    ModBlocks.LEAD_WALL_TORCH.get())));

    public static final RegistrySupplier<BlockEntityType<LeadedGlassTrapdoorBlockEntity>> LEADED_GLASS_TRAPDOOR =
            BLOCK_ENTITIES.register("leaded_glass_trapdoor", () -> new BlockEntityType<>(
                    LeadedGlassTrapdoorBlockEntity::new, Set.of(ModBlocks.LEADED_GLASS_TRAPDOOR.get())));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}

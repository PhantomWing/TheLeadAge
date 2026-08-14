package com.phantomwing.theleadage.entity;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.entity.custom.LeadWeightEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.ENTITY_TYPE);

    // The falling Lead Weight. Reuses the vanilla FallingBlockRenderer (registered per
    // loader), so no custom model/renderer is needed — only the entity behaviour.
    public static final RegistrySupplier<EntityType<LeadWeightEntity>> LEAD_WEIGHT =
            ENTITIES.register("lead_weight", () -> EntityType.Builder.<LeadWeightEntity>of(LeadWeightEntity::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, TheLeadAge.resourceLocation("lead_weight"))));

    public static void register() {
        ENTITIES.register();
    }
}

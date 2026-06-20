package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.entity.ModEntities;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Registers the Heavy Orb entity's renderer on NeoForge. It reuses the vanilla
 * {@link FallingBlockRenderer} (the orb is a {@code FallingBlockEntity}), so the
 * falling orb draws its block model automatically.
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID, value = Dist.CLIENT)
public final class ModEntityRenderers {
    private ModEntityRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HEAVY_ORB.get(), FallingBlockRenderer::new);
    }
}

package com.phantomwing.theleadage.forge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.entity.ModEntities;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * Registers the Lead Weight entity's renderer on Forge. It reuses the vanilla
 * {@link FallingBlockRenderer} (the weight is a {@code FallingBlockEntity}), so the
 * falling weight draws its block model automatically.
 */
@Mod.EventBusSubscriber(modid = TheLeadAge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntityRenderers {
    private ModEntityRenderers() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LEAD_WEIGHT.get(), FallingBlockRenderer::new);
    }
}

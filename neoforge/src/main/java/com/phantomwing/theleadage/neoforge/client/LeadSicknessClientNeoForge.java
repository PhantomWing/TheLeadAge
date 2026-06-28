package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.client.LeadSicknessBlur;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Runs the Lead Sickness radial-blur post effect on NeoForge, just before the HUD renders (so the
 * world blurs but the HUD stays sharp). Fabric uses WorldRenderEvents.END for the same point.
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID, value = Dist.CLIENT)
public final class LeadSicknessClientNeoForge {
    private LeadSicknessClientNeoForge() {
    }

    @SubscribeEvent
    static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        LeadSicknessBlur.render();
    }
}

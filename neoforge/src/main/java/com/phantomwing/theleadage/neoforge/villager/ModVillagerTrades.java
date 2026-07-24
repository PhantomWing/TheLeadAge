package com.phantomwing.theleadage.neoforge.villager;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.neoforge.Configuration;
import com.phantomwing.theleadage.villager.LeadVillagerTrades;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/**
 * NeoForge trade registrant. Trade content lives in the shared
 * {@link LeadVillagerTrades}; this only applies it through the NeoForge village
 * events, which re-fire whenever trades are (re)built so the config checks are
 * live. The Fabric twin is {@code com.phantomwing.theleadage.fabric.villager.ModVillagerTrades}.
 *
 * <p>Auto-registered to the game event bus via {@link EventBusSubscriber} — no wiring needed.</p>
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID)
public class ModVillagerTrades {
    /** Kept for source compatibility; the value is owned by the shared spec. */
    public static final float PRICE_MULTIPLIER = LeadVillagerTrades.PRICE_MULTIPLIER;

    @SubscribeEvent
    public static void addVillagerTrades(VillagerTradesEvent event) {
        if (!Configuration.ENABLE_VILLAGER_TRADES.get()) {
            return;
        }
        // Mirror vanilla: Armorer, Toolsmith and Weaponsmith all buy 4 lead ingots for
        // an emerald at apprentice (level 2), exactly as they do iron ingots.
        VillagerProfession type = event.getType();
        if (type == VillagerProfession.ARMORER
                || type == VillagerProfession.TOOLSMITH
                || type == VillagerProfession.WEAPONSMITH) {
            event.getTrades().get(2).add(LeadVillagerTrades.smithBuysLeadIngots());
        }
    }
}

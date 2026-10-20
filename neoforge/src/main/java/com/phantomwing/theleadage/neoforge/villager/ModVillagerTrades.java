package com.phantomwing.theleadage.neoforge.villager;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.neoforge.Configuration;
import com.phantomwing.theleadage.villager.LeadVillagerTrades;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.Arrays;

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
    /**
     * The trade as it goes into vanilla's rebalanced pools, the one listing for every reload, so it is added once.
     * The config is read as the offer is made, since those pools are built once, not on every reload.
     */
    private static final VillagerTrades.ItemListing REBALANCED = (trader, random) ->
            Configuration.ENABLE_VILLAGER_TRADES.get() ? LeadVillagerTrades.smithBuysLeadIngots().getOffer(trader, random) : null;

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

    /**
     * The trade in the world with the Villager Trade Rebalance experiment on too, which gives the armorer a pool of its
     * own: NeoForge's event writes back to the normal pools alone.
     */
    @SubscribeEvent
    public static void addRebalancedTrades(VillagerTradesEvent event) {
        VillagerProfession type = event.getType();
        if (type != VillagerProfession.ARMORER && type != VillagerProfession.TOOLSMITH && type != VillagerProfession.WEAPONSMITH) {
            return;
        }
        Int2ObjectMap<VillagerTrades.ItemListing[]> rebalanced = VillagerTrades.EXPERIMENTAL_TRADES.get(type);
        if (rebalanced == null) {
            return; // it trades from the normal pool there too
        }
        VillagerTrades.ItemListing[] apprentice = rebalanced.getOrDefault(2, new VillagerTrades.ItemListing[0]);
        if (!Arrays.asList(apprentice).contains(REBALANCED)) {
            VillagerTrades.ItemListing[] added = Arrays.copyOf(apprentice, apprentice.length + 1);
            added[apprentice.length] = REBALANCED;
            rebalanced.put(2, added);
        }
    }
}

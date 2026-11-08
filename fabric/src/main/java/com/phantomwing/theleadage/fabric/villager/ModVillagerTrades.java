package com.phantomwing.theleadage.fabric.villager;

import com.phantomwing.theleadage.fabric.config.TheLeadAgeFabricConfig;
import com.phantomwing.theleadage.villager.LeadVillagerTrades;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;

/**
 * Fabric parity for the Forge {@code ModVillagerTrades}. Forge adds trades from
 * {@code VillagerTradesEvent}, which re-fires on every rebuild, so its config checks are live;
 * Fabric's {@code TradeOfferHelper} registers a factory <b>once</b> at mod-init, so the config gate
 * has to be pushed into the listing instead — when the option is off the factory returns
 * {@code null}, which vanilla skips, giving the same live-toggleable behaviour.
 *
 * <p>1.20.1 has no Villager Trade Rebalance (1.20.2's experiment), so Fabric API 0.92 keeps one pool per
 * profession and level.</p>
 */
public final class ModVillagerTrades {
    private ModVillagerTrades() {
    }

    public static void register() {
        VillagerTrades.ItemListing smithTrade = LeadVillagerTrades.smithBuysLeadIngots();

        // Mirror vanilla: Armorer, Toolsmith and Weaponsmith all buy lead ingots at
        // level 2, exactly like iron ingots. Parity with the Forge event branch;
        // the config gate is pushed into the listing (null = no offer, vanilla skips it).
        VillagerProfession[] smiths = {
                VillagerProfession.ARMORER,
                VillagerProfession.TOOLSMITH,
                VillagerProfession.WEAPONSMITH
        };
        for (VillagerProfession smith : smiths) {
            registerGated(smith, 2, smithTrade);
        }
    }

    /** Adds {@code listing} to {@code profession}'s pool for {@code level}, offered while the config allows it. */
    private static void registerGated(VillagerProfession profession, int level,
                                      VillagerTrades.ItemListing listing) {
        TradeOfferHelper.registerVillagerOffers(profession, level, factories ->
                factories.add((trader, random) ->
                        TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                                TheLeadAgeFabricConfig.ENABLE_VILLAGER_TRADES_ID)
                                ? listing.getOffer(trader, random)
                                : null));
    }
}

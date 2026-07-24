package com.phantomwing.theleadage.fabric.villager;

import com.phantomwing.theleadage.fabric.config.TheLeadAgeFabricConfig;
import com.phantomwing.theleadage.villager.LeadVillagerTrades;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;

/**
 * Fabric parity for the NeoForge {@code ModVillagerTrades}. NeoForge adds trades from
 * {@code VillagerTradesEvent}, which re-fires on every rebuild, so its config checks are live;
 * Fabric's {@code TradeOfferHelper} registers a factory <b>once</b> at mod-init, so the config gate
 * has to be pushed into the listing instead — when the option is off the factory returns
 * {@code null}, which vanilla skips, giving the same live-toggleable behaviour.
 *
 * <p>Called from {@code TheLeadAgeFabric#onInitialize}. Keep the trades here in lockstep with the
 * NeoForge side; the content itself lives in the shared {@link LeadVillagerTrades}.</p>
 */
public final class ModVillagerTrades {
    private ModVillagerTrades() {
    }

    public static void register() {
        VillagerTrades.ItemListing smithTrade = LeadVillagerTrades.smithBuysLeadIngots();

        // Mirror vanilla: Armorer, Toolsmith and Weaponsmith all buy lead ingots at
        // level 2, exactly like iron ingots. Parity with the NeoForge event branch;
        // the config gate is pushed into the listing (null = no offer, vanilla skips it).
        VillagerProfession[] smiths = {
                VillagerProfession.ARMORER,
                VillagerProfession.TOOLSMITH,
                VillagerProfession.WEAPONSMITH
        };
        for (VillagerProfession smith : smiths) {
            TradeOfferHelper.registerVillagerOffers(smith, 2, factories ->
                    factories.add((trader, random) ->
                            TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                                    TheLeadAgeFabricConfig.ENABLE_VILLAGER_TRADES_ID)
                                    ? smithTrade.getOffer(trader, random)
                                    : null));
        }
    }
}

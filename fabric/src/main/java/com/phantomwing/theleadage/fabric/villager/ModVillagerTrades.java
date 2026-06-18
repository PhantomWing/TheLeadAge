package com.phantomwing.theleadage.fabric.villager;

/**
 * Fabric parity for the NeoForge {@code ModVillagerTrades}. NeoForge adds trades
 * from {@code VillagerTradesEvent}/{@code WandererTradesEvent} (re-fired on every
 * rebuild, so its config checks are live); Fabric's {@code TradeOfferHelper}
 * registers a factory <b>once</b> at mod-init, so the config gate is pushed into
 * the listing — when the option is off the factory returns {@code null}, which
 * vanilla skips, giving the same live-toggleable behaviour.
 *
 * <p>Called from {@code TheLeadAgeFabric#onInitialize}. No trades are registered
 * yet; add them here in lockstep with the NeoForge side. Example:</p>
 *
 * <pre>{@code
 * TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 2, factories ->
 *         factories.add((trader, random) ->
 *                 TheLeadAgeFabricConfig.getBooleanConfigurationValue(
 *                         TheLeadAgeFabricConfig.ENABLE_VILLAGER_TRADES_ID)
 *                         ? LeadVillagerTrades.toolsmithBuysRawLead().getOffer(trader, random)
 *                         : null));
 * }</pre>
 */
public final class ModVillagerTrades {
    private ModVillagerTrades() {
    }

    public static void register() {
        // TODO: register villager + wandering-trader offers via TradeOfferHelper,
        //       each gated on the config, in lockstep with the NeoForge side.
    }
}

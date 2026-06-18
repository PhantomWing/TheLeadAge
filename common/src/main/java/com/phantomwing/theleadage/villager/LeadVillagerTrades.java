package com.phantomwing.theleadage.villager;

/**
 * Loader-agnostic source of truth for the lead villager trades — the "shared spec,
 * per-loader apply" model already used for loot. Trade content (items, counts,
 * uses, xp, price) is defined here once as {@code VillagerTrades.ItemListing}
 * factories; each loader registers them through its own API (NeoForge from
 * {@code VillagerTradesEvent}/{@code WandererTradesEvent}, Fabric via
 * {@code TradeOfferHelper}). Config gating is applied per loader, not here.
 *
 * <p>No trades are defined yet. To add one: write a static
 * {@code VillagerTrades.ItemListing factoryMethod()} here, then wire it into BOTH
 * {@code com.phantomwing.theleadage.neoforge.villager.ModVillagerTrades} and
 * {@code com.phantomwing.theleadage.fabric.villager.ModVillagerTrades}. Example:</p>
 *
 * <pre>{@code
 * public static VillagerTrades.ItemListing toolsmithBuysRawLead() {
 *     return (trader, random) -> new MerchantOffer(
 *             new ItemCost(ModItems.RAW_LEAD.get(), 6),   // cost
 *             new ItemStack(Items.EMERALD, 1),            // result
 *             12, 4, PRICE_MULTIPLIER);                   // maxUses, villagerXp, priceMultiplier
 * }
 * }</pre>
 */
public final class LeadVillagerTrades {
    /** Standard villager price multiplier (how much demand shifts the price). */
    public static final float PRICE_MULTIPLIER = 0.05f;

    private LeadVillagerTrades() {
    }
}

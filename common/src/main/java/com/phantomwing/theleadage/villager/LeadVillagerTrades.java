package com.phantomwing.theleadage.villager;

import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * Loader-agnostic source of truth for the lead villager trades — the "shared spec,
 * per-loader apply" model already used for loot. Trade content (items, counts,
 * uses, xp, price) is defined here once as {@code VillagerTrades.ItemListing}
 * factories; each loader registers them through its own API (NeoForge from
 * {@code VillagerTradesEvent}/{@code WandererTradesEvent}, Fabric via
 * {@code TradeOfferHelper}). Config gating is applied per loader, not here.
 *
 * <p>To add a trade: write a static {@code VillagerTrades.ItemListing factoryMethod()}
 * here, then wire it into BOTH {@code com.phantomwing.theleadage.neoforge.villager.ModVillagerTrades}
 * and {@code com.phantomwing.theleadage.fabric.villager.ModVillagerTrades}.</p>
 */
public final class LeadVillagerTrades {
    /** Standard villager price multiplier (how much demand shifts the price). */
    public static final float PRICE_MULTIPLIER = 0.05f;

    private LeadVillagerTrades() {
    }

    /**
     * Smith (Armorer / Toolsmith / Weaponsmith), profession level 2: buy 4 Lead
     * Ingot, sell 1 Emerald (maxUses 12, villagerXp 10). Mirrors vanilla's identical
     * iron-ingot buy trade, which all three smith professions share at apprentice.
     */
    public static VillagerTrades.ItemListing smithBuysLeadIngots() {
        return (level, trader, random) -> new MerchantOffer(
                new ItemCost(ModItems.LEAD_INGOT.get(), 4),
                new ItemStack(Items.EMERALD, 1),
                12,
                10,
                PRICE_MULTIPLIER
        );
    }
}

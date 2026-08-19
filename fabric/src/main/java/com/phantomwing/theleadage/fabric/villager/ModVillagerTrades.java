package com.phantomwing.theleadage.fabric.villager;

import com.phantomwing.theleadage.fabric.config.TheLeadAgeFabricConfig;
import com.phantomwing.theleadage.villager.LeadVillagerTrades;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.List;

/**
 * Fabric parity for the NeoForge {@code ModVillagerTrades}. NeoForge adds trades from
 * {@code VillagerTradesEvent}, which re-fires on every rebuild, so its config checks are live;
 * Fabric's {@code TradeOfferHelper} registers a factory <b>once</b> at mod-init, so the config gate
 * has to be pushed into the listing instead — when the option is off the factory returns
 * {@code null}, which vanilla skips, giving the same live-toggleable behaviour.
 *
 * <p>Called from {@code TheLeadAgeFabric#onInitialize}. Keep the trades here in lockstep with the
 * NeoForge side; the content itself lives in the shared {@link LeadVillagerTrades}.</p>
 *
 * <p><b>Why the two-argument adder.</b> The {@code Consumer}-based overload of
 * {@code registerVillagerOffers} documents that it "adds the same trade offers to current and
 * rebalanced trades": it runs the callback twice, once against {@link VillagerTrades#TRADES} and
 * once against {@code VillagerTrades.EXPERIMENTAL_TRADES}. Vanilla builds the experimental map by
 * copying the normal one and replacing only the professions it actually rebalanced, so for a
 * profession it left alone both maps hold the <em>same</em> per-profession object. Both passes then
 * mutated a single array, leaving two copies of the listing and letting one villager roll the lead
 * trade twice.</p>
 *
 * <p>This mod is the case that makes the identity check necessary rather than optional. Vanilla
 * rebalances the <b>armorer</b> but not the toolsmith or weaponsmith, so the three smiths here do
 * not agree: an unconditional {@code if (rebalanced) return;} would fix the two duplicates and
 * silently drop the armorer trade in rebalanced worlds. {@code sharesRebalancedPool} compares the
 * two pools by identity and skips only when they are literally the same object, which is right for
 * all three.</p>
 */
public final class ModVillagerTrades {
    private ModVillagerTrades() {
    }

    public static void register() {
        VillagerTrades.ItemListing smithTrade = LeadVillagerTrades.smithBuysLeadIngots();

        // Mirror vanilla: Armorer, Toolsmith and Weaponsmith all buy lead ingots at
        // level 2, exactly like iron ingots. Parity with the NeoForge event branch;
        // the config gate is pushed into the listing (null = no offer, vanilla skips it).
        // 1.21.5: professions are addressed by ResourceKey (the constants are keys now).
        List<ResourceKey<VillagerProfession>> smiths = List.of(
                VillagerProfession.ARMORER,
                VillagerProfession.TOOLSMITH,
                VillagerProfession.WEAPONSMITH);
        for (ResourceKey<VillagerProfession> smith : smiths) {
            registerGated(smith, 2, smithTrade);
        }
    }

    /**
     * Adds {@code listing} to {@code profession}'s pool for {@code level} exactly once, for both
     * normal and rebalanced worlds.
     *
     * <p>The rebalanced pass is skipped only when that profession's two pools are literally the
     * same object (writing to it twice would duplicate). When vanilla gives the profession its own
     * rebalanced pool — as it does for the armorer — the listing is added to both, so the trade is
     * never missing from a rebalanced world.</p>
     */
    private static void registerGated(ResourceKey<VillagerProfession> profession, int level,
                                      VillagerTrades.ItemListing listing) {
        TradeOfferHelper.registerVillagerOffers(profession, level, (factories, rebalanced) -> {
            // Checked INSIDE the callback: TradeOfferHelper only sets up its trade maps when
            // registerVillagerOffers is first called, so testing beforehand would read an
            // uninitialised state and wrongly report the pools as distinct.
            if (rebalanced && sharesRebalancedPool(profession)) {
                return;
            }
            factories.add((trader, random) ->
                    TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                            TheLeadAgeFabricConfig.ENABLE_VILLAGER_TRADES_ID)
                            ? listing.getOffer(trader, random)
                            : null);
        });
    }

    /** True when the profession's normal and rebalanced trade pools are the same object. */
    private static boolean sharesRebalancedPool(ResourceKey<VillagerProfession> profession) {
        var normal = VillagerTrades.TRADES.get(profession);
        var rebalanced = VillagerTrades.EXPERIMENTAL_TRADES.get(profession);
        // A null rebalanced entry means Fabric will create a fresh map for it, so the
        // two are distinct and the listing must be added on both passes.
        return normal != null && normal == rebalanced;
    }
}

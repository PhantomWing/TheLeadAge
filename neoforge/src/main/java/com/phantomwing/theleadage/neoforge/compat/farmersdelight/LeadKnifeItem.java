package com.phantomwing.theleadage.neoforge.compat.farmersdelight;

import net.minecraft.world.item.Item;
import vectorwing.farmersdelight.common.item.KnifeItem;

/**
 * Lead Knife when Farmer's Delight is present: a real FD {@link KnifeItem}, so it gains the Cutting
 * Board {@code KNIFE_DIG}/{@code KNIFE_HARVEST} item abilities and the {@code MINEABLE_WITH_KNIFE}
 * mining tag for free.
 *
 * <p>This class references FD types, so it is only ever classloaded when FD is installed —
 * {@code KnifePlatformImpl} guards construction behind {@code isModLoaded("farmersdelight")} and
 * otherwise builds a plain SwordItem.</p>
 */
public class LeadKnifeItem extends KnifeItem {
    public LeadKnifeItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * Factory used by {@code KnifePlatformImpl} behind the {@code isModLoaded} guard. The
     * {@code new LeadKnifeItem} MUST live here, not inline in the caller: the JVM verifier loads the
     * classes named by a {@code new} instruction when it verifies the <em>enclosing method</em>, so an
     * inline {@code new LeadKnifeItem} would force-load the FD-only {@code KnifeItem} superclass — and
     * crash — even when FD is absent and the branch never runs. Reached via {@code invokestatic}, this
     * class only loads when the call actually executes. Returns {@link Item} so the caller's method
     * descriptor never names this class either.
     */
    public static Item create(Item.Properties properties) {
        return new LeadKnifeItem(properties);
    }
}

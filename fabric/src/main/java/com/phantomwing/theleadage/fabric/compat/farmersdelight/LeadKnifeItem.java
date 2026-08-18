package com.phantomwing.theleadage.fabric.compat.farmersdelight;

import net.minecraft.world.item.Item;
import vectorwing.farmersdelight.common.item.KnifeItem;

/**
 * Lead Knife when Farmer's Delight Refabricated is present: a real FD {@link KnifeItem}. FDR keeps the
 * {@code vectorwing.farmersdelight.common} package and the same {@code (Item.Properties)}
 * constructor as the NeoForge build, so this is the same shape as its NeoForge counterpart.
 *
 * <p>Only classloaded when FDR is installed — {@code KnifePlatformImpl} guards construction behind
 * {@code isModLoaded("farmersdelight")}. At compile time {@code KnifeItem} resolves to the local stub
 * (which is stripped from the shipped jar); at runtime it binds to the real FDR class.</p>
 */
public class LeadKnifeItem extends KnifeItem {
    public LeadKnifeItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * Factory used by {@code KnifePlatformImpl} behind the {@code isModLoaded} guard. The
     * {@code new LeadKnifeItem} MUST live here, not inline in the caller: the JVM verifier loads the
     * classes named by a {@code new} instruction when it verifies the enclosing method, so an inline
     * {@code new LeadKnifeItem} would force-load the FDR-only {@code KnifeItem} superclass and crash
     * when FDR is absent (the compile-time stub is stripped from the jar). Reached via
     * {@code invokestatic}, this only loads when the call actually executes.
     */
    public static Item create(Item.Properties properties) {
        return new LeadKnifeItem(properties);
    }
}

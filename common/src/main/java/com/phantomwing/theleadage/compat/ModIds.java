package com.phantomwing.theleadage.compat;

/** Mod IDs for optional dependencies used in compat checks and recipe conditions. */
public final class ModIds {
    public static final String CREATE = "create";
    /** Farmer's Delight — the mod id of its Forge build (vectorwing). */
    public static final String FARMERS_DELIGHT = "farmersdelight";
    /** Vanilla Backport: newer vanilla content on 1.20.1 under vanilla's own ids, the nautilus and its armour among it. */
    public static final String VANILLA_BACKPORT = "vanillabackport";
    /** Mekanism, Forge only here, whose own lead processing takes the mod's ore and raw lead. */
    public static final String MEKANISM = "mekanism";
    /** Yori3o's Grappling Hooks, whose hook tiers are read from every jar's {@code yo_hooks/hooks.json}. */
    public static final String GRAPPLING_HOOKS = "yo_hooks";

    private ModIds() {
    }
}

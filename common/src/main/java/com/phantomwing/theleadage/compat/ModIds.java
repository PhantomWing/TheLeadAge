package com.phantomwing.theleadage.compat;

/** Mod IDs for optional dependencies used in compat checks and recipe conditions. */
public final class ModIds {
    public static final String CREATE = "create";
    /** Farmer's Delight — same mod id on NeoForge (vectorwing) and Fabric (Refabricated). */
    public static final String FARMERS_DELIGHT = "farmersdelight";
    /** Yori3o's Grappling Hooks, whose hook tiers are read from every jar's {@code yo_hooks/hooks.json}. */
    public static final String GRAPPLING_HOOKS = "yo_hooks";

    private ModIds() {
    }
}

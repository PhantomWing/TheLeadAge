package com.phantomwing.theleadage.fabric.config;

import com.phantomwing.theleadage.TheLeadAge;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

/**
 * Fabric config, backed by Cloth Config's AutoConfig. Persists to
 * {@code config/theleadage.json}. Option ids and {@code true} defaults are kept
 * 1:1 with the NeoForge {@code Configuration}. Cross-loader code reaches these
 * gates through the {@code @ExpectPlatform CommonConfig} bridge.
 */
@Config(name = TheLeadAge.MOD_ID)
public class TheLeadAgeFabricConfig implements ConfigData {
    public static final String GENERATE_LEAD_ORE_ID = "generate_lead_ore";
    public boolean generate_lead_ore = true;

    public static final String ENABLE_LEAD_ORE_SICKNESS_ID = "enable_lead_ore_sickness";
    public boolean enable_lead_ore_sickness = true;

    public static final String ENABLE_RECIPE_OVERRIDES_ID = "enable_recipe_overrides";
    public boolean enable_recipe_overrides = true;

    public static final String OVERRIDE_FISHING_ROD_RECIPE_ID = "override_fishing_rod_recipe";
    public boolean override_fishing_rod_recipe = true;

    public static final String OVERRIDE_HEAVY_CORE_RECIPE_ID = "override_heavy_core_recipe";
    /** Off by default: crafting the Heavy Core makes a Trial-Chamber-exclusive item renewable, so it is opt-in. */
    public boolean override_heavy_core_recipe = false;

    public static final String ENABLE_STRUCTURE_LOOT_ID = "enable_structure_loot";
    public boolean enable_structure_loot = true;

    public static final String ENABLE_VILLAGER_TRADES_ID = "enable_villager_trades";
    public boolean enable_villager_trades = true;

    public static final String ENABLE_WANDERING_TRADER_TRADES_ID = "enable_wandering_trader_trades";
    public boolean enable_wandering_trader_trades = true;

    public static TheLeadAgeFabricConfig get() {
        return AutoConfig.getConfigHolder(TheLeadAgeFabricConfig.class).getConfig();
    }

    /** Registers the config holder + serializer. MUST be called before the first {@link #get()}. */
    public static void register() {
        AutoConfig.register(TheLeadAgeFabricConfig.class, GsonConfigSerializer::new);
    }

    public static boolean getBooleanConfigurationValue(String id) {
        TheLeadAgeFabricConfig config = get();
        return switch (id) {
            case GENERATE_LEAD_ORE_ID -> config.generate_lead_ore;
            case ENABLE_LEAD_ORE_SICKNESS_ID -> config.enable_lead_ore_sickness;
            case ENABLE_RECIPE_OVERRIDES_ID -> config.enable_recipe_overrides;
            case OVERRIDE_FISHING_ROD_RECIPE_ID -> config.override_fishing_rod_recipe;
            case OVERRIDE_HEAVY_CORE_RECIPE_ID -> config.override_heavy_core_recipe;
            case ENABLE_STRUCTURE_LOOT_ID -> config.enable_structure_loot;
            case ENABLE_VILLAGER_TRADES_ID -> config.enable_villager_trades;
            case ENABLE_WANDERING_TRADER_TRADES_ID -> config.enable_wandering_trader_trades;
            default -> throw new Error("Invalid setting ID: " + id);
        };
    }
}

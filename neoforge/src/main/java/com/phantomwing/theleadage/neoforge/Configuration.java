package com.phantomwing.theleadage.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * NeoForge config ({@code ModConfigSpec}), persisted to
 * {@code config/theleadage-common.toml}. Option ids and {@code true} defaults are
 * kept 1:1 with the Fabric {@code TheLeadAgeFabricConfig}.
 */
public class Configuration {
    public static final ModConfigSpec COMMON_CONFIG;

    public static final String GENERATE_LEAD_ORE_ID = "generate_lead_ore";
    public static final ModConfigSpec.BooleanValue GENERATE_LEAD_ORE;

    public static final String ENABLE_LEAD_ORE_NAUSEA_ID = "enable_lead_ore_nausea";
    public static final ModConfigSpec.BooleanValue ENABLE_LEAD_ORE_NAUSEA;

    public static final String ENABLE_RECIPE_OVERRIDES_ID = "enable_recipe_overrides";
    public static final ModConfigSpec.BooleanValue ENABLE_RECIPE_OVERRIDES;

    public static final String OVERRIDE_FISHING_ROD_RECIPE_ID = "override_fishing_rod_recipe";
    public static final ModConfigSpec.BooleanValue OVERRIDE_FISHING_ROD_RECIPE;

    public static final String OVERRIDE_HEAVY_CORE_RECIPE_ID = "override_heavy_core_recipe";
    public static final ModConfigSpec.BooleanValue OVERRIDE_HEAVY_CORE_RECIPE;

    public static final String ENABLE_STRUCTURE_LOOT_ID = "enable_structure_loot";
    public static final ModConfigSpec.BooleanValue ENABLE_STRUCTURE_LOOT;

    public static final String ENABLE_VILLAGER_TRADES_ID = "enable_villager_trades";
    public static final ModConfigSpec.BooleanValue ENABLE_VILLAGER_TRADES;

    public static final String ENABLE_WANDERING_TRADER_TRADES_ID = "enable_wandering_trader_trades";
    public static final ModConfigSpec.BooleanValue ENABLE_WANDERING_TRADER_TRADES;

    public static boolean getBooleanConfigurationValue(String id) {
        return switch (id) {
            case GENERATE_LEAD_ORE_ID -> GENERATE_LEAD_ORE.get();
            case ENABLE_LEAD_ORE_NAUSEA_ID -> ENABLE_LEAD_ORE_NAUSEA.get();
            case ENABLE_RECIPE_OVERRIDES_ID -> ENABLE_RECIPE_OVERRIDES.get();
            case OVERRIDE_FISHING_ROD_RECIPE_ID -> OVERRIDE_FISHING_ROD_RECIPE.get();
            case OVERRIDE_HEAVY_CORE_RECIPE_ID -> OVERRIDE_HEAVY_CORE_RECIPE.get();
            case ENABLE_STRUCTURE_LOOT_ID -> ENABLE_STRUCTURE_LOOT.get();
            case ENABLE_VILLAGER_TRADES_ID -> ENABLE_VILLAGER_TRADES.get();
            case ENABLE_WANDERING_TRADER_TRADES_ID -> ENABLE_WANDERING_TRADER_TRADES.get();
            default -> throw new Error("Invalid setting ID: " + id);
        };
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        GENERATE_LEAD_ORE = builder
                .comment("Should lead ore generate in the world? (Applies to newly generated chunks.)")
                .translation("text.autoconfig.theleadage.option.generate_lead_ore")
                .define(GENERATE_LEAD_ORE_ID, true);
        ENABLE_LEAD_ORE_NAUSEA = builder
                .comment("Should mining lead ore sometimes release fumes (a brief Nausea effect + smoke particles)?")
                .translation("text.autoconfig.theleadage.option.enable_lead_ore_nausea")
                .define(ENABLE_LEAD_ORE_NAUSEA_ID, true);
        ENABLE_RECIPE_OVERRIDES = builder
                .comment("Master toggle for the lead recipe overrides below. When off, none of them apply and vanilla recipes are kept.")
                .translation("text.autoconfig.theleadage.option.enable_recipe_overrides")
                .define(ENABLE_RECIPE_OVERRIDES_ID, true);
        OVERRIDE_FISHING_ROD_RECIPE = builder
                .comment("Craft the Fishing Rod with a lead nugget instead of the bottom string. Requires the master toggle.")
                .translation("text.autoconfig.theleadage.option.override_fishing_rod_recipe")
                .define(OVERRIDE_FISHING_ROD_RECIPE_ID, true);
        OVERRIDE_HEAVY_CORE_RECIPE = builder
                .comment("Allow crafting the otherwise-uncraftable Heavy Core from lead blocks + a netherite ingot. Requires the master toggle.")
                .translation("text.autoconfig.theleadage.option.override_heavy_core_recipe")
                .define(OVERRIDE_HEAVY_CORE_RECIPE_ID, true);
        ENABLE_STRUCTURE_LOOT = builder
                .comment("Should lead items appear in structure/chest loot? (Lead horse armor occasionally replaces iron horse armor.)")
                .translation("text.autoconfig.theleadage.option.enable_structure_loot")
                .define(ENABLE_STRUCTURE_LOOT_ID, true);
        ENABLE_VILLAGER_TRADES = builder
                .comment("Should villagers offer lead trades?")
                .translation("text.autoconfig.theleadage.option.enable_villager_trades")
                .define(ENABLE_VILLAGER_TRADES_ID, true);
        ENABLE_WANDERING_TRADER_TRADES = builder
                .comment("Should the wandering trader offer lead trades?")
                .translation("text.autoconfig.theleadage.option.enable_wandering_trader_trades")
                .define(ENABLE_WANDERING_TRADER_TRADES_ID, true);

        COMMON_CONFIG = builder.build();
    }
}

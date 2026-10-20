package com.phantomwing.theleadage.neoforge;

import com.phantomwing.theleadage.TheLeadAge;
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

    public static final String GENERATE_LEAD_VEINS_ID = "generate_lead_veins";
    public static final ModConfigSpec.BooleanValue GENERATE_LEAD_VEINS;

    public static final String ENABLE_LEAD_ORE_SICKNESS_ID = "enable_lead_ore_sickness";
    public static final ModConfigSpec.BooleanValue ENABLE_LEAD_ORE_SICKNESS;

    public static final String CREEPERS_AVOID_LEAD_FUMES_ID = "creepers_avoid_lead_fumes";
    public static final ModConfigSpec.BooleanValue CREEPERS_AVOID_LEAD_FUMES;

    public static final String PILLAGERS_AVOID_LEAD_FUMES_ID = "pillagers_avoid_lead_fumes";
    public static final ModConfigSpec.BooleanValue PILLAGERS_AVOID_LEAD_FUMES;

    public static final String ENABLE_CAMPFIRE_LEAD_FUMES_ID = "enable_campfire_lead_fumes";
    public static final ModConfigSpec.BooleanValue ENABLE_CAMPFIRE_LEAD_FUMES;

    public static final String ENABLE_RAW_LEAD_CONTACT_ID = "enable_raw_lead_contact";
    public static final ModConfigSpec.BooleanValue ENABLE_RAW_LEAD_CONTACT;

    public static final String LEAD_WATER_RANGE_ID = "lead_water_range";
    public static final ModConfigSpec.IntValue LEAD_WATER_RANGE;

    public static final String MOBS_AVOID_LEAD_WATER_ID = "mobs_avoid_lead_water";
    public static final ModConfigSpec.BooleanValue MOBS_AVOID_LEAD_WATER;

    public static final String ENABLE_RECIPE_OVERRIDES_ID = "enable_recipe_overrides";
    public static final ModConfigSpec.BooleanValue ENABLE_RECIPE_OVERRIDES;

    public static final String OVERRIDE_FISHING_ROD_RECIPE_ID = "override_fishing_rod_recipe";
    public static final ModConfigSpec.BooleanValue OVERRIDE_FISHING_ROD_RECIPE;

    public static final String OVERRIDE_HEAVY_CORE_RECIPE_ID = "override_heavy_core_recipe";
    public static final ModConfigSpec.BooleanValue OVERRIDE_HEAVY_CORE_RECIPE;

    public static final String ENABLE_STRUCTURE_LOOT_ID = "enable_structure_loot";
    public static final ModConfigSpec.BooleanValue ENABLE_STRUCTURE_LOOT;

    public static final String ENABLE_MOB_LEAD_EQUIPMENT_ID = "enable_mob_lead_equipment";
    public static final ModConfigSpec.BooleanValue ENABLE_MOB_LEAD_EQUIPMENT;

    public static final String ENABLE_VILLAGER_TRADES_ID = "enable_villager_trades";
    public static final ModConfigSpec.BooleanValue ENABLE_VILLAGER_TRADES;

    public static boolean getBooleanConfigurationValue(String id) {
        return switch (id) {
            case GENERATE_LEAD_ORE_ID -> GENERATE_LEAD_ORE.get();
            case GENERATE_LEAD_VEINS_ID -> GENERATE_LEAD_VEINS.get();
            case ENABLE_LEAD_ORE_SICKNESS_ID -> ENABLE_LEAD_ORE_SICKNESS.get();
            case CREEPERS_AVOID_LEAD_FUMES_ID -> CREEPERS_AVOID_LEAD_FUMES.get();
            case PILLAGERS_AVOID_LEAD_FUMES_ID -> PILLAGERS_AVOID_LEAD_FUMES.get();
            case ENABLE_CAMPFIRE_LEAD_FUMES_ID -> ENABLE_CAMPFIRE_LEAD_FUMES.get();
            case ENABLE_RAW_LEAD_CONTACT_ID -> ENABLE_RAW_LEAD_CONTACT.get();
            case MOBS_AVOID_LEAD_WATER_ID -> MOBS_AVOID_LEAD_WATER.get();
            case ENABLE_RECIPE_OVERRIDES_ID -> ENABLE_RECIPE_OVERRIDES.get();
            case OVERRIDE_FISHING_ROD_RECIPE_ID -> OVERRIDE_FISHING_ROD_RECIPE.get();
            case OVERRIDE_HEAVY_CORE_RECIPE_ID -> OVERRIDE_HEAVY_CORE_RECIPE.get();
            case ENABLE_STRUCTURE_LOOT_ID -> ENABLE_STRUCTURE_LOOT.get();
            case ENABLE_MOB_LEAD_EQUIPMENT_ID -> ENABLE_MOB_LEAD_EQUIPMENT.get();
            case ENABLE_VILLAGER_TRADES_ID -> ENABLE_VILLAGER_TRADES.get();
            default -> {
                // A datapack's config_boolean condition can name anything: one that names no setting is false.
                TheLeadAge.LOGGER.warn("theleadage:config_boolean names no setting: {}", id);
                yield false;
            }
        };
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        GENERATE_LEAD_ORE = builder
                .comment("Should lead ore generate in the world? (Applies to newly generated chunks.)")
                .translation("text.autoconfig.theleadage.option.generate_lead_ore")
                .define(GENERATE_LEAD_ORE_ID, true);
        GENERATE_LEAD_VEINS = builder
                .comment("Should large lead ore veins generate, as copper and iron ones do: lead ore in tuff between Y 8 and 58, with the odd block of raw lead? (Applies to newly generated chunks.)")
                .translation("text.autoconfig.theleadage.option.generate_lead_veins")
                .define(GENERATE_LEAD_VEINS_ID, true);
        ENABLE_LEAD_ORE_SICKNESS = builder
                .comment("Should mining lead ore or a Block of Raw Lead release fumes (the Lead Sickness effect + particles)?")
                .translation("text.autoconfig.theleadage.option.enable_lead_ore_sickness")
                .define(ENABLE_LEAD_ORE_SICKNESS_ID, true);
        CREEPERS_AVOID_LEAD_FUMES = builder
                .comment("Should creepers keep away from lit lead torches and campfires burning over raw lead? They back off rather than close in, but a creeper cornered next to you can still explode.")
                .translation("text.autoconfig.theleadage.option.creepers_avoid_lead_fumes")
                .define(CREEPERS_AVOID_LEAD_FUMES_ID, true);
        PILLAGERS_AVOID_LEAD_FUMES = builder
                .comment("Should pillagers keep away from lit lead torches and campfires burning over raw lead? Only while wandering — raids, patrols and pillagers already shooting at you are unaffected.")
                .translation("text.autoconfig.theleadage.option.pillagers_avoid_lead_fumes")
                .define(PILLAGERS_AVOID_LEAD_FUMES_ID, true);
        ENABLE_CAMPFIRE_LEAD_FUMES = builder
                .comment("Should a lit campfire or soul campfire on a block of raw lead, or on a hay bale on one, give off a stream of lead fumes? Players and mobs touching it get Lead Sickness, worse the longer they stay, and creepers and pillagers keep away from it when their options above are on.")
                .translation("text.autoconfig.theleadage.option.enable_campfire_lead_fumes")
                .define(ENABLE_CAMPFIRE_LEAD_FUMES_ID, true);
        ENABLE_RAW_LEAD_CONTACT = builder
                .comment("Should standing on a block of raw lead for a few seconds give players and mobs Lead Sickness, worse the longer they stay? Sneaking doesn't help.")
                .translation("text.autoconfig.theleadage.option.enable_raw_lead_contact")
                .define(ENABLE_RAW_LEAD_CONTACT_ID, true);
        LEAD_WATER_RANGE = builder
                .comment("How far a block of raw lead leaches into the water it touches, in blocks through the water and around what stands in it. The contaminated water spreads a block at a time, is tinted purple and murky, and gives players and mobs in it Lead Sickness, worse the longer they stay. 0 turns it off.")
                .translation("text.autoconfig.theleadage.option.lead_water_range")
                .defineInRange(LEAD_WATER_RANGE_ID, 6, 0, 8);
        MOBS_AVOID_LEAD_WATER = builder
                .comment("Should mobs keep out of water raw lead contaminates, as they keep out of fire? Fish, squid, axolotls and turtles swim round it where they can and out of it when it spreads over them, and animals and villagers walk round it. The undead, which it can't hurt, take no notice.")
                .translation("text.autoconfig.theleadage.option.mobs_avoid_lead_water")
                .define(MOBS_AVOID_LEAD_WATER_ID, true);
        ENABLE_RECIPE_OVERRIDES = builder
                .comment("Master toggle for the lead recipe overrides below. When off, none of them apply and vanilla recipes are kept.")
                .translation("text.autoconfig.theleadage.option.enable_recipe_overrides")
                .define(ENABLE_RECIPE_OVERRIDES_ID, true);
        OVERRIDE_FISHING_ROD_RECIPE = builder
                .comment("Craft the Fishing Rod with a lead nugget instead of the bottom string. Requires the master toggle.")
                .translation("text.autoconfig.theleadage.option.override_fishing_rod_recipe")
                .define(OVERRIDE_FISHING_ROD_RECIPE_ID, true);
        OVERRIDE_HEAVY_CORE_RECIPE = builder
                .comment("Allow crafting the otherwise-uncraftable Heavy Core from lead blocks + a netherite ingot. Off by default: it makes a Trial-Chamber-exclusive item renewable, so it is opt-in. Requires the master toggle.")
                .translation("text.autoconfig.theleadage.option.override_heavy_core_recipe")
                .define(OVERRIDE_HEAVY_CORE_RECIPE_ID, false);
        ENABLE_STRUCTURE_LOOT = builder
                .comment("Should lead items appear in structure/chest loot? (Lead horse armor occasionally replaces iron horse armor.)")
                .translation("text.autoconfig.theleadage.option.enable_structure_loot")
                .define(ENABLE_STRUCTURE_LOOT_ID, true);
        ENABLE_MOB_LEAD_EQUIPMENT = builder
                .comment("Should mobs sometimes spawn with lead armor, swords, axes and shovels where they would have had iron ones? (Zombies, skeletons, vindicators and the like.)")
                .translation("text.autoconfig.theleadage.option.enable_mob_lead_equipment")
                .define(ENABLE_MOB_LEAD_EQUIPMENT_ID, true);
        ENABLE_VILLAGER_TRADES = builder
                .comment("Should villagers offer lead trades?")
                .translation("text.autoconfig.theleadage.option.enable_villager_trades")
                .define(ENABLE_VILLAGER_TRADES_ID, true);

        COMMON_CONFIG = builder.build();
    }
}

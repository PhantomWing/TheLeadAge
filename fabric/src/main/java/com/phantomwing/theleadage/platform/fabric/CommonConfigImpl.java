package com.phantomwing.theleadage.platform.fabric;

import com.phantomwing.theleadage.fabric.config.TheLeadAgeFabricConfig;

/**
 * Fabric implementation of {@link com.phantomwing.theleadage.platform.CommonConfig}
 * (resolved by Architectury's {@code @ExpectPlatform} transformer).
 */
public final class CommonConfigImpl {
    private CommonConfigImpl() {
    }

    public static boolean generateLeadOre() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.GENERATE_LEAD_ORE_ID);
    }

    public static boolean leadOreSickness() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.ENABLE_LEAD_ORE_SICKNESS_ID);
    }

    public static boolean creepersAvoidLeadFumes() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.CREEPERS_AVOID_LEAD_FUMES_ID);
    }

    public static boolean pillagersAvoidLeadFumes() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.PILLAGERS_AVOID_LEAD_FUMES_ID);
    }

    public static boolean campfireLeadFumes() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.ENABLE_CAMPFIRE_LEAD_FUMES_ID);
    }

    public static boolean rawLeadContact() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.ENABLE_RAW_LEAD_CONTACT_ID);
    }

    public static int leadWaterRange() {
        return TheLeadAgeFabricConfig.get().lead_water_range;
    }

    public static boolean enableStructureLoot() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.ENABLE_STRUCTURE_LOOT_ID);
    }

    public static boolean mobLeadEquipment() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.ENABLE_MOB_LEAD_EQUIPMENT_ID);
    }
}

package com.phantomwing.theleadage.platform.neoforge;

import com.phantomwing.theleadage.neoforge.Configuration;

/**
 * NeoForge implementation of {@link com.phantomwing.theleadage.platform.CommonConfig}
 * (resolved by Architectury's {@code @ExpectPlatform} transformer).
 */
public final class CommonConfigImpl {
    private CommonConfigImpl() {
    }

    public static boolean generateLeadOre() {
        return Configuration.GENERATE_LEAD_ORE.get();
    }

    public static boolean leadOreSickness() {
        return Configuration.ENABLE_LEAD_ORE_SICKNESS.get();
    }

    public static boolean creepersAvoidLeadFumes() {
        return Configuration.CREEPERS_AVOID_LEAD_FUMES.get();
    }

    public static boolean pillagersAvoidLeadFumes() {
        return Configuration.PILLAGERS_AVOID_LEAD_FUMES.get();
    }

    public static boolean campfireLeadFumes() {
        return Configuration.ENABLE_CAMPFIRE_LEAD_FUMES.get();
    }

    public static boolean rawLeadContact() {
        return Configuration.ENABLE_RAW_LEAD_CONTACT.get();
    }

    public static int leadWaterRange() {
        return Configuration.LEAD_WATER_RANGE.get();
    }

    public static boolean enableStructureLoot() {
        return Configuration.ENABLE_STRUCTURE_LOOT.get();
    }
}

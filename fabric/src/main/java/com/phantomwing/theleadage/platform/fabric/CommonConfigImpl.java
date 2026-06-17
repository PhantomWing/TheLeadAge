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

    public static boolean leadOreNausea() {
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(
                TheLeadAgeFabricConfig.ENABLE_LEAD_ORE_NAUSEA_ID);
    }
}

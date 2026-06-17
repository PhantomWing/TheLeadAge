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

    public static boolean leadOreNausea() {
        return Configuration.ENABLE_LEAD_ORE_NAUSEA.get();
    }
}

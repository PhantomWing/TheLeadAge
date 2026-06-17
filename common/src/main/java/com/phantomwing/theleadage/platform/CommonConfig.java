package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * {@code @ExpectPlatform} bridge exposing the config booleans the loader-agnostic
 * code needs. The config itself stays loader-specific: NeoForge {@code ModConfigSpec},
 * Fabric Cloth {@code AutoConfig}, with 1:1 option ids and {@code true} defaults.
 * Implemented per loader at
 * {@code com.phantomwing.theleadage.platform.<loader>.CommonConfigImpl}.
 */
public final class CommonConfig {
    private CommonConfig() {
    }

    /** Gate for lead-ore world generation. */
    @ExpectPlatform
    public static boolean generateLeadOre() {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }

    /** Gate for the "lead fumes" Nausea + particle effect when mining lead ore. */
    @ExpectPlatform
    public static boolean leadOreNausea() {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }

    // Recipe-override toggles are read at datapack-load time directly by the
    // conditional recipes (NeoForge ConfigBooleanCondition / Fabric config_boolean
    // resource condition), so they don't need a CommonConfig bridge here.
}

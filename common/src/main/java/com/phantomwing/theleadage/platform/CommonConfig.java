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

    /**
     * Gate for structure/chest loot injection (lead horse armor replacing iron
     * horse armor). Read at loot-roll time by the NeoForge Global Loot Modifiers
     * and the Fabric loot mixin.
     */
    @ExpectPlatform
    public static boolean enableStructureLoot() {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }

    /** Gate for the falling Heavy Orb's crush damage + knockback (read by HeavyOrbEntity). */
    @ExpectPlatform
    public static boolean heavyOrbDamage() {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }

    // Recipe-override toggles are read at datapack-load time directly by the
    // conditional recipes (NeoForge ConfigBooleanCondition / Fabric config_boolean
    // resource condition), so they don't need a CommonConfig bridge here.
}

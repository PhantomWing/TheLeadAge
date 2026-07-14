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

    /** Gate for the "lead fumes" Lead Sickness + particle effect when mining lead ore. */
    @ExpectPlatform
    public static boolean leadOreSickness() {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }

    /**
     * Gate for creepers fleeing lead torches. Read live, every time the goal considers running, so
     * flipping it takes effect without a restart (see {@code AvoidRepellentBlockGoal}).
     */
    @ExpectPlatform
    public static boolean creepersAvoidLeadFumes() {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }

    /** Gate for pillagers fleeing lead torches. Read live, exactly like the creeper toggle. */
    @ExpectPlatform
    public static boolean pillagersAvoidLeadFumes() {
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

    // Recipe-override toggles are read at datapack-load time directly by the
    // conditional recipes (NeoForge ConfigBooleanCondition / Fabric config_boolean
    // resource condition), so they don't need a CommonConfig bridge here.
}

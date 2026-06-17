package com.phantomwing.theleadage.fabric;

import com.phantomwing.theleadage.TheLeadAgeCommon;
import com.phantomwing.theleadage.fabric.condition.ConfigBooleanResourceCondition;
import com.phantomwing.theleadage.fabric.config.TheLeadAgeFabricConfig;
import com.phantomwing.theleadage.fabric.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

/**
 * Fabric entrypoint for The Lead Age. Registers the AutoConfig holder first (the
 * world-gen biome injection reads config at init), then the loader-agnostic
 * bootstrap. Fabric biome injection is added in the world-gen phase.
 */
public final class TheLeadAgeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TheLeadAgeFabricConfig.register();

        // Parity twin of the NeoForge theleadage:config_boolean recipe condition,
        // so the shared conditional/fallback recipes gate identically on Fabric.
        ResourceConditions.register(ConfigBooleanResourceCondition.TYPE);

        TheLeadAgeCommon.init();

        // Attach lead ore to overworld biomes (gated by config).
        ModWorldGen.register();
    }
}

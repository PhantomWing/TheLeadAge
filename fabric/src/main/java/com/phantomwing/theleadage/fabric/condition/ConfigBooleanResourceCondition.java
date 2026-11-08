package com.phantomwing.theleadage.fabric.condition;

import com.google.gson.JsonObject;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.fabric.config.TheLeadAgeFabricConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/**
 * Fabric parity twin of the Forge
 * {@link com.phantomwing.theleadage.forge.condition.ConfigBooleanCondition}.
 * The shared generated recipe JSON carries BOTH dialects (Forge's top-level
 * {@code conditions} + Fabric {@code fabric:load_conditions}, the latter
 * emitted by the Forge {@code FabricConditionsProvider}); this is the runtime
 * handler Fabric uses. Id + field are byte-identical to the Forge side.
 */
public final class ConfigBooleanResourceCondition {
    public static final ResourceLocation ID =
            new ResourceLocation(TheLeadAge.MOD_ID, "config_boolean");

    private ConfigBooleanResourceCondition() {
    }

    /** Registers the condition with Fabric API's 1.20.1 resource conditions, which read the condition's JSON as it is. */
    public static void register() {
        ResourceConditions.register(ID, ConfigBooleanResourceCondition::test);
    }

    private static boolean test(JsonObject json) {
        // Resolve straight off the Fabric config; setting ids are kept 1:1 with the
        // Forge side and only generated (known) ids ever reach here.
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(GsonHelper.getAsString(json, "settingId"));
    }
}

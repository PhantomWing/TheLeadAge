package com.phantomwing.theleadage.fabric.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.fabric.config.TheLeadAgeFabricConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric parity twin of the NeoForge
 * {@link com.phantomwing.theleadage.neoforge.condition.ConfigBooleanCondition}.
 * The shared generated recipe JSON carries BOTH dialects (NeoForge
 * {@code neoforge:conditions} + Fabric {@code fabric:load_conditions}, the latter
 * emitted by the NeoForge {@code FabricConditionsProvider}); this is the runtime
 * handler Fabric uses. Id + field are byte-identical to the NeoForge side.
 */
public record ConfigBooleanResourceCondition(String settingId) implements ResourceCondition {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(TheLeadAge.MOD_ID, "config_boolean");

    public static final MapCodec<ConfigBooleanResourceCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("settingId").forGetter(ConfigBooleanResourceCondition::settingId)
    ).apply(inst, ConfigBooleanResourceCondition::new));

    public static final ResourceConditionType<ConfigBooleanResourceCondition> TYPE =
            ResourceConditionType.create(ID, CODEC);

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable HolderLookup.Provider registryLookup) {
        // Resolve straight off the Fabric config; setting ids are kept 1:1 with the
        // NeoForge side and only generated (known) ids ever reach here.
        return TheLeadAgeFabricConfig.getBooleanConfigurationValue(settingId);
    }
}

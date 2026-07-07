package com.phantomwing.theleadage.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * The design of a leaded glass door: two independent {@link LeadedGlassConfig} panes, one shown in
 * each half ({@code top} in the upper half, {@code bottom} in the lower). Carried on the door item
 * as a data component and copied onto its block entity when placed.
 */
public record LeadedGlassDoorConfig(LeadedGlassConfig top, LeadedGlassConfig bottom) {
    private static final LeadedGlassConfig CLEAR_PANE =
            new LeadedGlassConfig(LeadedGlassFrame.PLAIN, List.of(LeadedGlassConfig.CLEAR));

    /** A door with both halves clear plain glass (the fallback for an unconfigured door). */
    public static final LeadedGlassDoorConfig DEFAULT = new LeadedGlassDoorConfig(CLEAR_PANE, CLEAR_PANE);

    public static final Codec<LeadedGlassDoorConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LeadedGlassConfig.CODEC.fieldOf("top").forGetter(LeadedGlassDoorConfig::top),
            LeadedGlassConfig.CODEC.fieldOf("bottom").forGetter(LeadedGlassDoorConfig::bottom)
    ).apply(instance, LeadedGlassDoorConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LeadedGlassDoorConfig> STREAM_CODEC = StreamCodec.composite(
            LeadedGlassConfig.STREAM_CODEC, LeadedGlassDoorConfig::top,
            LeadedGlassConfig.STREAM_CODEC, LeadedGlassDoorConfig::bottom,
            LeadedGlassDoorConfig::new);
}

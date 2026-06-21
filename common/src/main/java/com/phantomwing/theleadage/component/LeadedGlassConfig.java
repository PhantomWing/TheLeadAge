package com.phantomwing.theleadage.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The design of a configured leaded glass panel, carried on the item as a data component
 * and copied onto its block entity when placed: the {@link LeadedGlassFrame frame} plus a
 * per-region colour (a dye id, or {@code -1} for clear/uncoloured glass).
 */
public record LeadedGlassConfig(LeadedGlassFrame frame, List<Integer> colors) {
    public static final int CLEAR = -1;

    public static final Codec<LeadedGlassConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LeadedGlassFrame.CODEC.fieldOf("frame").forGetter(LeadedGlassConfig::frame),
            Codec.INT.listOf().fieldOf("colors").forGetter(LeadedGlassConfig::colors)
    ).apply(instance, LeadedGlassConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LeadedGlassConfig> STREAM_CODEC = StreamCodec.composite(
            LeadedGlassFrame.STREAM_CODEC, LeadedGlassConfig::frame,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), LeadedGlassConfig::colors,
            LeadedGlassConfig::new);

    /** The dye for {@code region}, or {@code null} when that region is clear / out of range. */
    @Nullable
    public DyeColor colorAt(int region) {
        if (region < 0 || region >= colors.size()) {
            return null;
        }
        int id = colors.get(region);
        return id < 0 ? null : DyeColor.byId(id);
    }
}

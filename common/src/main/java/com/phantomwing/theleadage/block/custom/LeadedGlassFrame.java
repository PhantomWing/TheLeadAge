package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The came layout of a leaded glass panel — a small, curated set (the geometry axis).
 * Colours are NOT part of this; they're tinted per region from the block entity, so the
 * frame count stays tiny while colours stay unlimited.
 */
public enum LeadedGlassFrame implements StringRepresentable {
    /** A single full pane (one region). */
    PLAIN("plain", 1),
    /** Split left/right by a vertical came — a horizontal split (two regions: 0 = left, 1 = right). */
    SPLIT_H("split_h", 2);

    public static final Codec<LeadedGlassFrame> CODEC = StringRepresentable.fromEnum(LeadedGlassFrame::values);
    public static final StreamCodec<ByteBuf, LeadedGlassFrame> STREAM_CODEC =
            ByteBufCodecs.idMapper(id -> values()[id], LeadedGlassFrame::ordinal);

    private final String name;
    private final int regions;

    LeadedGlassFrame(String name, int regions) {
        this.name = name;
        this.regions = regions;
    }

    /** Number of colourable regions (= the tint indices the model uses). */
    public int regions() {
        return regions;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

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
    SPLIT_H("split_h", 2),
    /** Split top/bottom by a horizontal came — a vertical split (two regions: 0 = top, 1 = bottom). */
    SPLIT_V("split_v", 2),
    /** A plus of came dividing the pane 2×2 (four regions: 0 = top-left, 1 = top-right, 2 = bottom-left, 3 = bottom-right). */
    PLUS("plus", 4),
    /** A 3×3 grid of came (nine regions, row-major from the top-left: 0..2 top, 3..5 middle, 6..8 bottom). */
    GRID("grid", 9),
    /** Diagonal came "/" (two regions: 0 = upper-left, 1 = lower-right). */
    DIAGONAL_A("diagonal_a", 2),
    /** Diagonal came "\" (two regions: 0 = upper-right, 1 = lower-left). */
    DIAGONAL_B("diagonal_b", 2),
    /** An X of came (four regions: 0 = top, 1 = right, 2 = bottom, 3 = left). */
    CROSS("cross", 4),
    /** A rhombus of came joining the edge midpoints (five regions: corners 0 = top-left,
     * 1 = top-right, 3 = bottom-left, 4 = bottom-right, and 2 = the centre diamond). */
    DIAMOND("diamond", 5),
    /** The diamond rhombus overlaid with both full diagonals — a diamond lattice. Twelve regions,
     * roughly row-major: corner triangles hugging the top border (0, 1), the left/right-hugging
     * upper triangles flanking the north rhombus (2, 3 = north, 4), the west/east rhombi (5, 6),
     * the lower flanks with the south rhombus (7, 8 = south, 9), and the bottom-hugging pair (10, 11). */
    LATTICE("lattice", 12),
    /** Three columns behind two vertical cames (three regions: 0 = left, 1 = middle, 2 = right). */
    BARS_H("bars_h", 3),
    /** Three rows behind two horizontal cames (three regions: 0 = top, 1 = middle, 2 = bottom). */
    BARS_V("bars_v", 3),
    /** Three parallel "/" cames — four diagonal strips (0 = top-left … 3 = bottom-right). */
    DIAGONAL_BARS_A("diagonal_bars_a", 4),
    /** Three parallel "\" cames — four diagonal strips (0 = top-right … 3 = bottom-left). */
    DIAGONAL_BARS_B("diagonal_bars_b", 4);

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

    /**
     * Region indices grouped per display row (top → bottom), so the tooltip can lay colours out the
     * way the pane reads rather than on one long line.
     */
    public int[][] rows() {
        return switch (this) {
            case PLAIN -> new int[][]{{0}};
            case SPLIT_H -> new int[][]{{0, 1}};                       // left · right
            case SPLIT_V -> new int[][]{{0}, {1}};                     // top / bottom
            case PLUS -> new int[][]{{0, 1}, {2, 3}};
            case GRID -> new int[][]{{0, 1, 2}, {3, 4, 5}, {6, 7, 8}};
            case DIAGONAL_A, DIAGONAL_B -> new int[][]{{0}, {1}};
            case CROSS -> new int[][]{{0}, {3, 1}, {2}};               // top / left · right / bottom
            case DIAMOND -> new int[][]{{0, 1}, {2}, {3, 4}};          // top corners / centre / bottom corners
            case LATTICE -> new int[][]{{0, 1}, {2, 3, 4}, {5, 6}, {7, 8, 9}, {10, 11}};
            case BARS_H -> new int[][]{{0, 1, 2}};                     // left · middle · right
            case BARS_V -> new int[][]{{0}, {1}, {2}};                 // top / middle / bottom
            case DIAGONAL_BARS_A, DIAGONAL_BARS_B -> new int[][]{{0}, {1}, {2}, {3}};
        };
    }

    /**
     * The region a normalized hit on the pane face falls in, matching the model geometry.
     * {@code u}: 0 = came-left → 1 = came-right; {@code v}: 0 = bottom → 1 = top.
     */
    public int regionAt(double u, double v) {
        return switch (this) {
            case PLAIN -> 0;
            case SPLIT_H -> u < 0.5 ? 0 : 1;                       // left | right
            case SPLIT_V -> v > 0.5 ? 0 : 1;                       // top / bottom
            case PLUS -> (v > 0.5 ? 0 : 2) + (u < 0.5 ? 0 : 1);    // TL, TR, BL, BR
            case GRID -> {                                       // 3×3, row-major from top-left
                int col = u < 1.0 / 3 ? 0 : (u < 2.0 / 3 ? 1 : 2);
                int row = v > 2.0 / 3 ? 0 : (v > 1.0 / 3 ? 1 : 2);
                yield row * 3 + col;
            }
            case DIAGONAL_A -> v > u ? 0 : 1;                      // "/" upper-left | lower-right
            case DIAGONAL_B -> v > 1 - u ? 0 : 1;                  // "\" upper-right | lower-left
            case CROSS -> {                                        // X: the triangle by both diagonals
                boolean aboveSlash = v > u;
                boolean aboveBackslash = v > 1 - u;
                if (aboveSlash && aboveBackslash) yield 0;         // top
                if (!aboveSlash && aboveBackslash) yield 1;        // right
                if (!aboveSlash) yield 2;                          // bottom
                yield 3;                                           // left
            }
            case DIAMOND -> {                                      // inside the rhombus, or a corner
                if (Math.abs(u - 0.5) + Math.abs(v - 0.5) < 0.5) yield 2;
                yield (v > 0.5 ? 0 : 3) + (u < 0.5 ? 0 : 1);       // TL, TR / BL, BR
            }
            case BARS_H -> u < 1.0 / 3 ? 0 : (u < 2.0 / 3 ? 1 : 2); // left | middle | right
            case BARS_V -> v > 2.0 / 3 ? 0 : (v > 1.0 / 3 ? 1 : 2); // top / middle / bottom
            case DIAGONAL_BARS_A -> {                              // "/" cames at v-u = ±7/16, 0
                double d = v - u;
                if (d > 7.0 / 16) yield 0;                         // top-left triangle
                if (d > 0) yield 1;
                if (d > -7.0 / 16) yield 2;
                yield 3;                                           // bottom-right triangle
            }
            case DIAGONAL_BARS_B -> {                              // "\" cames at u+v = 9/16, 1, 23/16
                double s = u + v;
                if (s > 23.0 / 16) yield 0;                        // top-right triangle
                if (s > 1) yield 1;
                if (s > 9.0 / 16) yield 2;
                yield 3;                                           // bottom-left triangle
            }
            case LATTICE -> {                                // diamond lattice: "/" bands × "\" bands
                double p = u - v;                                  // "/" lines at -0.5, 0, +0.5
                double q = u + v;                                  // "\" lines at 0.5, 1, 1.5
                if (p < -0.5) yield q > 1 ? 0 : 2;                 // top-left corner: top / left triangle
                if (p > 0.5) yield q > 1 ? 9 : 11;                 // bottom-right: right / bottom triangle
                if (q > 1.5) yield p < 0 ? 1 : 4;                  // top-right: top / right triangle
                if (q < 0.5) yield p < 0 ? 7 : 10;                 // bottom-left: left / bottom triangle
                if (p < 0) yield q > 1 ? 3 : 5;                    // rhombus: north / west
                yield q > 1 ? 6 : 8;                               // rhombus: east / south
            }
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

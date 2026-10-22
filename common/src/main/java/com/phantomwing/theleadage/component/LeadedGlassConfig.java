package com.phantomwing.theleadage.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The design of a configured leaded glass panel, carried on the item as a data component
 * and copied onto its block entity when placed: the {@link LeadedGlassFrame frame} plus a
 * per-region colour (a dye id, or {@code -1} for clear/uncoloured glass). The ids are this game's own: what is saved
 * or sent is each dye's name, as vanilla saves its dyes, since a dye mod's colours are numbered by which dye mods are
 * installed and in what order. A colour no installed mod has any more reads as clear.
 */
public record LeadedGlassConfig(LeadedGlassFrame frame, List<Integer> colors) {
    public static final int CLEAR = -1;
    private static final String CLEAR_NAME = "clear";

    /** One region's colour: a dye's name or {@value #CLEAR_NAME}, or the number a world saved before 1.2 holds. */
    public static final Codec<Integer> COLOR_CODEC = Codec.either(Codec.STRING, Codec.INT).xmap(
            saved -> saved.map(LeadedGlassConfig::idOf, LeadedGlassConfig::known),
            id -> Either.left(nameOf(id)));
    private static final StreamCodec<ByteBuf, Integer> COLOR_STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(LeadedGlassConfig::idOf, LeadedGlassConfig::nameOf);

    public static final Codec<LeadedGlassConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LeadedGlassFrame.CODEC.fieldOf("frame").forGetter(LeadedGlassConfig::frame),
            COLOR_CODEC.listOf().fieldOf("colors").forGetter(LeadedGlassConfig::colors)
    ).apply(instance, LeadedGlassConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LeadedGlassConfig> STREAM_CODEC = StreamCodec.composite(
            LeadedGlassFrame.STREAM_CODEC, LeadedGlassConfig::frame,
            COLOR_STREAM_CODEC.apply(ByteBufCodecs.list()), LeadedGlassConfig::colors,
            LeadedGlassConfig::new);

    /** The name a colour is saved by. */
    public static String nameOf(int id) {
        return id < 0 ? CLEAR_NAME : DyeColor.byId(id).getSerializedName();
    }

    /** The colour saved by {@code name}, clear where no installed mod has that dye. */
    public static int idOf(String name) {
        DyeColor color = CLEAR_NAME.equals(name) ? null : DyeColor.byName(name, null);
        return color == null ? CLEAR : color.getId();
    }

    /**
     * A colour saved by number, clear where no installed mod has a dye with it, as a name none has reads: the game's
     * own lookup gives white for any number past its dyes, which saving would then have written as white.
     */
    private static int known(int id) {
        return id >= 0 && DyeColor.byId(id).getId() == id ? id : CLEAR;
    }

    /**
     * The colours as a block entity saves them: a list of names, as {@link #COLOR_CODEC} writes them, read back from that
     * or from the array of numbers 1.1.2 saved, whose numbers the codec takes one by one.
     */
    private static final Codec<List<Integer>> SAVED_COLORS = COLOR_CODEC.listOf();

    /** Writes {@code colors} into a block entity's data, by name. */
    public static void putColors(ValueOutput output, List<Integer> colors) {
        output.store("Colors", SAVED_COLORS, colors);
    }

    /** The colours in a block entity's data, saved by name or, before 1.2, by number; one clear region where none are. */
    public static List<Integer> getColors(ValueInput input) {
        List<Integer> colors = input.read("Colors", SAVED_COLORS).orElse(List.of());
        return colors.isEmpty() ? List.of(CLEAR) : List.copyOf(colors);
    }

    /** {@link #putColors(ValueOutput, List)} on a tag of its own, as a block entity writes it. */
    public static void putColors(CompoundTag tag, List<Integer> colors) {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        putColors(output, colors);
        tag.merge(output.buildResult());
    }

    /** {@link #getColors(ValueInput)} from a tag of its own, as a block entity reads it. */
    public static List<Integer> getColors(CompoundTag tag) {
        return getColors(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, tag));
    }

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

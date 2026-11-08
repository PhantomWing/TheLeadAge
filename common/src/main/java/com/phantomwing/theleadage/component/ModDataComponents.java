package com.phantomwing.theleadage.component;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The item data the mod keeps on a stack. {@code leaded_glass_config} carries a panel's design
 * ({@link LeadedGlassConfig}) on the item so it survives crafting, placing and breaking.
 *
 * <p>1.20.1 has no data components (1.20.5's), so each is a key of the stack's tag, written with the design's codec, as
 * 1.21's component is: the same JSON a recipe's result gives it under {@code nbt}. A stack's tag goes to clients with
 * it, so nothing is synchronised apart.</p>
 */
public final class ModDataComponents {
    private ModDataComponents() {
    }

    public static final StackData<LeadedGlassConfig> LEADED_GLASS_CONFIG =
            new StackData<>("theleadage:leaded_glass_config", LeadedGlassConfig.CODEC);

    // The leaded glass door's design: a pane per half (top + bottom).
    public static final StackData<LeadedGlassDoorConfig> LEADED_GLASS_DOOR_CONFIG =
            new StackData<>("theleadage:leaded_glass_door_config", LeadedGlassDoorConfig.CODEC);

    /** One kind of item data, saved under {@code key} in the stack's tag. */
    public record StackData<T>(String key, Codec<T> codec) {
        @Nullable
        public T get(ItemStack stack) {
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains(key)) {
                return null;
            }
            return codec.parse(NbtOps.INSTANCE, tag.get(key)).result().orElse(null);
        }

        public boolean has(ItemStack stack) {
            return get(stack) != null;
        }

        public void set(ItemStack stack, @Nullable T value) {
            if (value == null) {
                stack.removeTagKey(key);
                return;
            }
            Tag saved = codec.encodeStart(NbtOps.INSTANCE, value).result().orElse(null);
            if (saved != null) {
                stack.getOrCreateTag().put(key, saved);
            }
        }

        /** This data as it is saved on a stack, for a recipe result's {@code nbt}. */
        public CompoundTag tagOf(T value) {
            CompoundTag tag = new CompoundTag();
            codec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(saved -> tag.put(key, saved));
            return tag;
        }
    }
}

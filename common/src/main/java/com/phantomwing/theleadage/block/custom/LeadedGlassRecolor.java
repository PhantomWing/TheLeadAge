package com.phantomwing.theleadage.block.custom;

import com.phantomwing.theleadage.component.LeadedGlassConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The shared half of "recolour one region of a leaded glass design": deciding what a held item wants
 * to do, applying it to a colour list, and spending the item.
 *
 * <p>Used by the pane, the door and the trapdoor, which differ only in how a click maps to a region
 * and where the resulting colours are stored — so only that part lives in each block.</p>
 */
public final class LeadedGlassRecolor {
    private LeadedGlassRecolor() {
    }

    /** Whether this item recolours glass at all (a dye, or shears to clear it). */
    public static boolean isTool(ItemStack stack) {
        return stack.getItem() instanceof DyeItem || stack.getItem() instanceof ShearsItem;
    }

    /** Shears clear a region rather than colouring it. */
    public static boolean isShears(ItemStack stack) {
        return stack.getItem() instanceof ShearsItem;
    }

    /** The colour this item would paint, or {@code null} for "make it clear" (shears). */
    @Nullable
    public static DyeColor target(ItemStack stack) {
        return stack.getItem() instanceof DyeItem dye ? dye.getDyeColor() : null;
    }

    /**
     * {@code colors} with {@code region} set to {@code target}, padded to {@code regions} entries.
     * Returns {@code null} when it already has that colour, so callers can consume the click without
     * spending the item.
     */
    @Nullable
    public static List<Integer> apply(List<Integer> colors, int regions, int region, @Nullable DyeColor target) {
        int want = target == null ? LeadedGlassConfig.CLEAR : target.getId();
        List<Integer> out = new ArrayList<>(colors);
        while (out.size() < regions) {
            out.add(LeadedGlassConfig.CLEAR);
        }
        if (region >= out.size() || out.get(region) == want) {
            return null;
        }
        out.set(region, want);
        return out;
    }

    /** Spend the dye (or a point of shear durability) and play the matching sound. */
    public static void consume(ItemStack stack, Player player, InteractionHand hand, Level level, BlockPos pos) {
        if (isShears(stack)) {
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
            stack.hurtAndBreak(1, player,
                    hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        } else {
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
    }
}

package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Shared tint logic for the leaded glass panel — this is what turns one white-glass texture
 * into any colour. Each glass region carries a {@code tintindex}; these methods return the
 * region's dye colour from the block entity (in-world) or the item's component (inventory),
 * or white for a clear region. Registration is per loader (NeoForge
 * {@code RegisterColorHandlersEvent}, Fabric {@code ColorProviderRegistry}) at the proper
 * client hook — Architectury's wrapper registers too eagerly (it needs a live Minecraft).
 */
public final class ModColorHandlers {
    /** No tint — a clear (uncoloured) region shows the plain glass texture; also the came layer. */
    public static final int NO_TINT = -1;

    private ModColorHandlers() {
    }

    public static int blockTint(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (level != null && pos != null && level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity panel) {
            return tintOf(panel.colorAt(tintIndex));
        }
        return NO_TINT;
    }

    public static int itemTint(ItemStack stack, int tintIndex) {
        if (tintIndex >= 2) {
            return NO_TINT; // the came-frame layer is never tinted
        }
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null) {
            return NO_TINT; // a default (unconfigured) pane = clear glass
        }
        // The 2D item has two glass halves (tintindex 0 = left, 1 = right). For a plain pane
        // (one colour) clamp so both halves show it; a split shows its two colours.
        int region = Math.min(tintIndex, config.colors().size() - 1);
        return tintOf(config.colorAt(region));
    }

    private static int tintOf(@Nullable DyeColor dye) {
        return dye == null ? NO_TINT : 0xFF000000 | dye.getTextureDiffuseColor();
    }

    /**
     * Item-model predicate ({@code theleadage:frame}): 1 for a split pane, 0 for plain — drives
     * the item-model override that swaps in the came-divided icon. Registered per loader.
     */
    public static float frameProperty(ItemStack stack) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        return config != null && config.frame() != LeadedGlassFrame.PLAIN ? 1.0F : 0.0F;
    }

    /**
     * Item-model predicate ({@code theleadage:clear}): 1 for a plain, uncoloured pane — swaps in
     * the clear-glass icon. Only the plain frame, so it never collides with the split override.
     */
    public static float clearProperty(ItemStack stack) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config != null && config.frame() != LeadedGlassFrame.PLAIN) {
            return 0.0F; // splits keep their own icon
        }
        boolean clear = config == null || config.colors().isEmpty()
                || config.colors().get(0) == LeadedGlassConfig.CLEAR;
        return clear ? 1.0F : 0.0F;
    }
}

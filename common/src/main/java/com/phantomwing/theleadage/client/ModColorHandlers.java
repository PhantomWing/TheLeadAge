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
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null) {
            return NO_TINT; // a default (unconfigured) pane = clear glass
        }
        // Each item model lays out one glass layer per region (tintindex 0..regions-1) then the
        // came-frame layer last (tintindex == regions), which is never tinted.
        if (tintIndex >= config.frame().regions()) {
            return NO_TINT;
        }
        return tintOf(config.colorAt(tintIndex));
    }

    private static int tintOf(@Nullable DyeColor dye) {
        return dye == null ? NO_TINT : 0xFF000000 | dye.getTextureDiffuseColor();
    }

    /**
     * Item-model predicate: 1 when the pane has exactly this frame, else 0. One boolean property
     * per non-plain frame (split_h/split_v/grid) drives the icon override. Booleans (not an
     * ordinal) because {@code ItemProperties.register} clamps the value to [0, 1].
     */
    public static float framePredicate(ItemStack stack, LeadedGlassFrame frame) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        return config != null && config.frame() == frame ? 1.0F : 0.0F;
    }

    /**
     * Item-model predicate ({@code theleadage:clear}): 1 when every region is uncoloured — swaps
     * in the clear-glass icon (the white tintable texture reads wrong for a clear pane). Missing
     * colour entries count as clear; any dyed region keeps the tinted white-glass icon.
     */
    public static float clearProperty(ItemStack stack) {
        LeadedGlassConfig config = stack.get(ModDataComponents.LEADED_GLASS_CONFIG.get());
        if (config == null) {
            return 1.0F; // a default (unconfigured) pane = clear glass
        }
        for (int color : config.colors()) {
            if (color != LeadedGlassConfig.CLEAR) {
                return 0.0F;
            }
        }
        return 1.0F;
    }
}

package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Shared block-tint logic for the leaded glass panel — this is what turns one white-glass texture
 * into any colour in-world. Each glass region carries a {@code tintindex}; the provider returns
 * the region's dye colour from the block entity, or white for a clear region. Registration is per
 * loader at the proper client hook — Architectury's wrapper registers too eagerly (it needs a live
 * Minecraft). Item icons don't tint at all: they render through the pane special renderer, which
 * reads the stack's config directly.
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

    private static int tintOf(@Nullable DyeColor dye) {
        return dye == null ? NO_TINT : 0xFF000000 | dye.getTextureDiffuseColor();
    }
}

package com.phantomwing.theleadage.client;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import com.phantomwing.theleadage.block.entity.LeadedGlassPanelBlockEntity;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Shared block-tint logic for the leaded glass panes - this is what turns one white-glass texture
 * into any colour in-world. Each glass region carries a {@code tintindex}; the tint source for that
 * index returns the region's dye colour from the block entity, or white for a clear region.
 * Registration is per loader at the proper client hook - Architectury's wrapper registers too
 * eagerly (it needs a live Minecraft). Item icons don't tint at all: they render through the pane
 * special renderer, which reads the stack's config directly.
 *
 * <p>26.1 replaced the per-block tint <i>function</i> (called once per tint index) with a
 * {@link BlockTintSource} <i>list</i>, one entry per tint index, so each pane block registers as
 * many sources as its came type has regions.</p>
 */
public final class ModColorHandlers {
    /** No tint - a clear (uncoloured) region shows the plain glass texture; also the came layer. */
    public static final int NO_TINT = -1;

    private ModColorHandlers() {
    }

    /**
     * Feeds every tintable pane block and its per-region tint sources to {@code sink}, so each
     * loader only has to call its own registration API. The list length is the came type's region
     * count: a tint index outside it belongs to the came, which is never tinted.
     */
    public static void forEachPane(BiConsumer<Block, List<BlockTintSource>> sink) {
        Stream.of(ModBlocks.LEADED_GLASS_PANEL, ModBlocks.LEADED_GLASS_PANE_SPLIT,
                        ModBlocks.LEADED_GLASS_PANE_PLUS, ModBlocks.LEADED_GLASS_PANE_GRID,
                        ModBlocks.LEADED_GLASS_PANE_DIAGONAL, ModBlocks.LEADED_GLASS_PANE_CROSS,
                        ModBlocks.LEADED_GLASS_PANE_DIAMOND, ModBlocks.LEADED_GLASS_PANE_LATTICE,
                        ModBlocks.LEADED_GLASS_PANE_BARS, ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS)
                .map(supplier -> (Block) supplier.get())
                .forEach(block -> {
                    int regions = block instanceof LeadedGlassPaneBlock pane ? pane.cameType().regions : 1;
                    sink.accept(block, regionTints(regions));
                });
    }

    /** One tint source per region, in tint-index order. */
    private static List<BlockTintSource> regionTints(int regions) {
        List<BlockTintSource> sources = new ArrayList<>(regions);
        for (int region = 0; region < regions; region++) {
            sources.add(new RegionTint(region));
        }
        return sources;
    }

    private static int tintOf(@Nullable DyeColor dye) {
        return dye == null ? NO_TINT : 0xFF000000 | dye.getTextureDiffuseColor();
    }

    /**
     * The colour of one glass region, read from the pane's block entity. Outside a level (item
     * frames of reference, particle colours) there is no block entity, so it stays untinted.
     */
    private record RegionTint(int region) implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return NO_TINT;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof LeadedGlassPanelBlockEntity panel) {
                return tintOf(panel.colorAt(region));
            }
            return NO_TINT;
        }
    }
}

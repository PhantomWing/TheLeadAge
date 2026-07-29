package com.phantomwing.theleadage.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Leaded glass breaks two ways. Pried out with a pickaxe it comes away whole — the heavy lead
 * {@code break} sound. Knocked out with anything else it is simply destroyed, and glass that is not
 * recovered has shattered, so it gets the glass sound instead.
 *
 * <p>Vanilla bundles the two: {@link LevelEvent#PARTICLES_DESTROY_BLOCK} carries a single block-state
 * id, and the client reads <em>both</em> the break sound and the destroy particles off it. So the
 * shatter case reports the equivalent vanilla glass instead of the leaded glass — which also swaps the
 * particles to plain glass shards. That reads correctly (it shattered into glass), and it is the only
 * way to split the sound without a client-side mixin.</p>
 *
 * <p>Called from {@code spawnDestroyParticles}, which runs on the server for onlookers and again on the
 * breaking player's own client for its prediction — both with that player, so both pick the same
 * sound.</p>
 */
public final class LeadedGlassShatter {
    private LeadedGlassShatter() {
    }

    /**
     * Fire the destroy effect, choosing the flavour by whether this break will actually yield an item.
     * Mirrors the engine's own drop test in {@code ServerPlayerGameMode#destroyBlock}: creative mode
     * drops nothing, and the block needs the correct tool.
     */
    public static void spawnDestroyEffect(Level level, Player player, BlockPos pos, BlockState state,
                                          BlockState shattered) {
        boolean recovered = !player.isCreative() && player.hasCorrectToolForDrops(state);
        level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos,
                Block.getId(recovered ? state : shattered));
    }

    /** The vanilla glass that stands in when leaded glass shatters — colour-matched, clear if uncoloured. */
    public static BlockState shatteredGlass(@Nullable DyeColor color) {
        if (color == null) {
            return Blocks.GLASS.defaultBlockState();
        }
        return switch (color) {
            case WHITE -> Blocks.WHITE_STAINED_GLASS.defaultBlockState();
            case ORANGE -> Blocks.ORANGE_STAINED_GLASS.defaultBlockState();
            case MAGENTA -> Blocks.MAGENTA_STAINED_GLASS.defaultBlockState();
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
            case YELLOW -> Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
            case LIME -> Blocks.LIME_STAINED_GLASS.defaultBlockState();
            case PINK -> Blocks.PINK_STAINED_GLASS.defaultBlockState();
            case GRAY -> Blocks.GRAY_STAINED_GLASS.defaultBlockState();
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState();
            case CYAN -> Blocks.CYAN_STAINED_GLASS.defaultBlockState();
            case PURPLE -> Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
            case BLUE -> Blocks.BLUE_STAINED_GLASS.defaultBlockState();
            case BROWN -> Blocks.BROWN_STAINED_GLASS.defaultBlockState();
            case GREEN -> Blocks.GREEN_STAINED_GLASS.defaultBlockState();
            case RED -> Blocks.RED_STAINED_GLASS.defaultBlockState();
            case BLACK -> Blocks.BLACK_STAINED_GLASS.defaultBlockState();
        };
    }
}

package com.phantomwing.theleadage.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
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
 * {@code break} sound. Knocked out by a player with anything else it is simply destroyed, and glass that
 * is not recovered has shattered, so it gets the glass sound instead.
 *
 * <p>Vanilla bundles the two: {@link LevelEvent#PARTICLES_AND_SOUND_DESTROY_BLOCK} carries a single
 * block-state id, and the client reads <em>both</em> the break sound and the destroy particles off it
 * (26.3's {@code PARTICLES_DESTROY_BLOCK} draws the particles alone, silently). So the
 * shatter case reports the equivalent stained glass instead of the leaded glass — which also swaps the
 * particles to plain glass shards. That reads correctly (it shattered into glass), and it is the only
 * way to split the sound without a client-side mixin.</p>
 *
 * <p>Called from {@code spawnDestroyByEntityParticles}, which {@code playerWillDestroy} runs on the server
 * for onlookers and again on the breaking player's own client for its prediction — both with that player,
 * so both pick the same sound. {@code Level#destroyBlock} runs it with no breaker.</p>
 */
public final class LeadedGlassShatter {
    private LeadedGlassShatter() {
    }

    /**
     * Fire the destroy effect, choosing the flavour by whether this break will actually yield an item.
     * Mirrors the engine's own drop test in {@code ServerPlayerGameMode#destroyBlock}: creative mode
     * drops nothing, and the block needs the correct tool.
     */
    public static void spawnDestroyEffect(Level level, @Nullable Entity breaker, BlockPos pos,
                                          BlockState state, BlockState shattered) {
        level.levelEvent(breaker, LevelEvent.PARTICLES_AND_SOUND_DESTROY_BLOCK, pos,
                Block.getId(shownAs(breaker, state, shattered)));
    }

    /**
     * The block a break shows and sounds as: the leaded glass itself, or the glass it shattered into. Only a player
     * shatters it: 26.3's {@code Level#destroyBlock} hands this hook no breaker (/setblock and /fill with destroy, a
     * Wither), and before 26.3 that path never came here and showed the leaded glass.
     */
    public static BlockState shownAs(@Nullable Entity breaker, BlockState state, BlockState shattered) {
        boolean shatters = breaker instanceof Player player
                && (player.isCreative() || !player.hasCorrectToolForDrops(state));
        return shatters ? shattered : state;
    }

    /**
     * The glass that stands in when leaded glass shatters: the stained glass of its colour, clear if
     * uncoloured. A dye mod's colour takes that mod's stained glass, else plain glass.
     */
    public static BlockState shatteredGlass(@Nullable DyeColor color) {
        if (color == null) {
            return Blocks.GLASS.defaultBlockState();
        }
        Block glass = LeadedGlassDyes.stainedGlassOf(color);
        return (glass == null ? Blocks.GLASS : glass).defaultBlockState();
    }
}

package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dyed leaded glass. Still a {@link StainedGlassBlock}, so it keeps the beacon-beam tint; it only
 * differs in the break sound (see {@link LeadedGlassShatter}), whose shatter is colour-matched to the
 * equivalent stained glass. One white texture tinted by the dye, as the panes are, so a colour a dye mod
 * adds looks right with no texture of its own.
 */
public class StainedLeadedGlassBlock extends StainedGlassBlock {
    /** Typed as the parent's codec type: {@code StainedGlassBlock#codec} is declared concretely, so an override must match. */
    public static final MapCodec<StainedGlassBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    DyeColor.CODEC.fieldOf("color").forGetter(StainedGlassBlock::getColor),
                    propertiesCodec()
            ).apply(instance, StainedLeadedGlassBlock::new));

    public StainedLeadedGlassBlock(DyeColor color, Properties properties) {
        super(color, properties);
    }

    @Override
    public MapCodec<StainedGlassBlock> codec() {
        return CODEC;
    }

    /** Vanilla's sixteen have their own translations; a dye mod's colour is named by pattern. */
    @Override
    public MutableComponent getName() {
        return LeadedGlassDyes.isVanilla(getColor())
                ? super.getName()
                : LeadedGlassDyes.name("leaded_glass", getColor()).copy();
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        LeadedGlassShatter.spawnDestroyEffect(level, player, pos, state,
                LeadedGlassShatter.shatteredGlass(getColor()));
    }
}

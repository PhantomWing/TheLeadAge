package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.effect.LeadFumes;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Lead ore. Drops no experience (like iron/copper), and releases toxic "lead fumes" when broken for
 * its raw drops: a pale, light-gray smoke plume at the block (the colour lead burns), then the fumes
 * dose nearby living entities with {@link LeadFumes Lead Sickness}. A big release ({@link #FUMES_CHANCE}) adds a
 * hiss and guarantees the dose up close; otherwise a faint wisp gives a small chance. The delay is the
 * window to back away; undead are immune (they don't breathe).
 *
 * <p>{@link #playerDestroy} is only invoked by the engine on an actual harvest — survival mode with a
 * correct tool that drops items (creative and wrong-tool breaks never call it) — so the only extra
 * check needed is Silk Touch, which yields the ore block itself and so never produces fumes.</p>
 */
public class LeadOreBlock extends Block {
    public static final MapCodec<LeadOreBlock> CODEC = simpleCodec(LeadOreBlock::new);

    /** Chance a (non-silk-touch) harvest is a BIG release (hiss + guaranteed dose) rather than a small wisp. */
    private static final float FUMES_CHANCE = 0.30f;
    private static final double BIG_DOSE_CHANCE = 1.0;    // big release: guaranteed dose at point-blank
    private static final double SMALL_DOSE_CHANCE = 0.25; // small wisp: low chance even up close

    public LeadOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // Whole mechanic (Lead Sickness + particles) is toggleable.
        if (!CommonConfig.leadOreSickness()) {
            return;
        }
        // Silk Touch yields the ore block (no raw ore) -> never fumes.
        Holder<Enchantment> silkTouch = serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0) {
            return;
        }
        // Every non-silk harvest fumes; the roll decides a big release vs a faint wisp. The fumes
        // immediately expose nearby living entities (LeadFumes) — keep clear of the block to dodge them.
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.6, cz = pos.getZ() + 0.5;
        if (serverLevel.getRandom().nextFloat() < FUMES_CHANCE) {
            // Big release: a pale lead-oxide plume, guaranteed dose up close. The hiss plays when the
            // fumes actually take hold (see LeadFumes), not here.
            LeadFumes.plume(serverLevel, cx, cy, cz);
            LeadFumes.expose(serverLevel, pos, BIG_DOSE_CHANCE);
        } else {
            // Small wisp: raw lead is still toxic, but only a small chance to dose anyone close.
            LeadFumes.wisp(serverLevel, cx, cy - 0.05, cz);
            LeadFumes.expose(serverLevel, pos, SMALL_DOSE_CHANCE);
        }
    }
}

package com.phantomwing.theleadage.block.custom;

import com.phantomwing.theleadage.effect.LeadFumes;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Lead ore. Drops no experience (like iron/copper), and releases toxic "lead fumes" when broken for
 * its raw drops: a pale, light-gray smoke plume at the block (the colour lead burns), then the fumes
 * dose nearby living entities with {@link LeadFumes Lead Sickness}. A big release adds a hiss and guarantees the
 * dose up close; otherwise a faint wisp gives a small chance ({@link LeadFumes#mined}). The delay is the
 * window to back away; undead are immune (they don't breathe).
 *
 * <p>{@link #playerDestroy} is only invoked by the engine on an actual harvest — survival mode with a
 * correct tool that drops items (creative and wrong-tool breaks never call it) — so the only extra
 * check needed is Silk Touch, which yields the ore block itself and so never produces fumes.</p>
 */
public class LeadOreBlock extends Block {
    public LeadOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroy(ServerLevel serverLevel, ServerPlayer player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        // 26.3 narrowed this hook to ServerLevel/ServerPlayer, so the old client-side guard is gone.
        super.playerDestroy(serverLevel, player, pos, state, blockEntity, tool);
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
        LeadFumes.mined(serverLevel, pos);
    }
}

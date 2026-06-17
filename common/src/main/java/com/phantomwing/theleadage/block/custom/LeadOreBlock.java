package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * Lead ore. Drops no experience (like iron/copper), and sometimes releases "lead
 * fumes" when broken for its raw drops — a brief Nausea effect plus a puff of
 * smoke at the block.
 *
 * <p>{@link #playerDestroy} is only invoked by the engine on an actual harvest —
 * survival mode with a correct tool that drops items (creative and wrong-tool
 * breaks never call it) — so the only extra check needed is Silk Touch, which
 * yields the ore block itself rather than raw lead and so never produces fumes.
 * Non-silk-touch harvests then release fumes with {@link #FUMES_CHANCE} probability.</p>
 */
public class LeadOreBlock extends Block {
    public static final MapCodec<LeadOreBlock> CODEC = simpleCodec(LeadOreBlock::new);

    /** Chance a (non-silk-touch) harvest releases fumes. */
    private static final float FUMES_CHANCE = 0.30f;
    /** Subtle, short Nausea ("fumes") — tweak here. */
    private static final int NAUSEA_TICKS = 140; // 7 seconds

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
        // Whole mechanic (Nausea + particles) is toggleable.
        if (!CommonConfig.leadOreNausea()) {
            return;
        }
        // Silk Touch yields the ore block (no raw ore) -> never fumes.
        Holder<Enchantment> silkTouch = serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0) {
            return;
        }
        // Fumes only happen sometimes.
        if (serverLevel.getRandom().nextFloat() >= FUMES_CHANCE) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS, 0));
        serverLevel.sendParticles(ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                12, 0.25, 0.25, 0.25, 0.01);
    }
}

package com.phantomwing.theleadage.block.custom;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.effect.LeadFumes;
import com.phantomwing.theleadage.effect.LeadWater;
import com.phantomwing.theleadage.effect.PoisonedWaterAvoidance;
import com.phantomwing.theleadage.effect.RecedingLeads;
import com.phantomwing.theleadage.particle.ModParticles;
import com.phantomwing.theleadage.platform.CommonConfig;
import com.phantomwing.theleadage.platform.LeadSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/**
 * The block of raw lead: unrefined lead, poisonous to handle. A player who stands on it a few seconds gets Lead
 * Sickness, worse the longer they stay, sneaking or not, and lead dust lifts off it. In water it leaks
 * ({@link LeadWater}). Only a block in water ticks often, every {@value LeadWater#INTERVAL} ticks rather than with
 * a block entity, so the blocks in a storage room cost next to nothing.
 */
public class RawLeadBlock extends Block {
    public static final MapCodec<RawLeadBlock> CODEC = simpleCodec(RawLeadBlock::new);
    /** How far it has leaked into the water it touches: 0 out of water, else the steps its spreading has taken. */
    public static final IntegerProperty CONTAMINATION = IntegerProperty.create("contamination", 0, LeadWater.MAX_STEP);
    /** A step of the spreading is the client's to redraw and nothing else's: no shape updates, no observer pulses. */
    private static final int STEP_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    public RawLeadBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CONTAMINATION, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTAMINATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(CONTAMINATION,
                LeadWater.range(context.getLevel().isClientSide()) > 0
                        && LeadWater.touchesWater(context.getLevel(), context.getClickedPos()) ? 1 : 0);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        if (LeadWater.range(level.isClientSide()) == 0 || !LeadWater.touchesWater(level, pos)) {
            return state.setValue(CONTAMINATION, 0);
        }
        if (state.getValue(CONTAMINATION) == 0) {
            // onPlace isn't called while the world generates, so the first wetting asks for its tick itself.
            level.scheduleTick(pos, this, LeadWater.INTERVAL);
            return state.setValue(CONTAMINATION, 1);
        }
        return state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            PoisonedWaterAvoidance.leadChanged();
        }
        if (state.getValue(CONTAMINATION) > 1 && oldState.is(Blocks.MOVING_PISTON)) {
            // Pushed into new water, it starts leaking there afresh rather than at once as far as it had got.
            level.setBlock(pos, state.setValue(CONTAMINATION, 1), STEP_FLAGS);
            level.scheduleTick(pos, this, LeadWater.INTERVAL);
            return;
        }
        if (state.getValue(CONTAMINATION) > 0 && !(oldState.is(this) && oldState.getValue(CONTAMINATION) > 0)) {
            level.scheduleTick(pos, this, LeadWater.INTERVAL);
        }
    }

    /**
     * The block changed: where it stops leaking - mined, blown up, pushed away or dried out - what it left in the water
     * recedes rather than clearing at once ({@link RecedingLeads}). A step further into its spreading is no stop.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        PoisonedWaterAvoidance.leadChanged();
        int step = state.getValue(CONTAMINATION);
        boolean stillLeaking = newState.is(this) && newState.getValue(CONTAMINATION) > 0;
        if (step > 0 && !stillLeaking && level instanceof ServerLevel server) {
            RecedingLeads.start(server, pos, step);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Every block of raw lead looks now and then, by random tick, whether it should be leaking: what the world generated
     * beside water - a lead vein by an aquifer - never had a neighbour change to wake it, nor did lead lying dry or
     * waiting when the range was switched back on. A few lookups a minute a block, so a storage room costs next to nothing.
     */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean wet = LeadWater.range(false) > 0 && LeadWater.touchesWater(level, pos);
        int step = state.getValue(CONTAMINATION);
        if (wet && step == 0) {
            level.setBlock(pos, state.setValue(CONTAMINATION, 1), STEP_FLAGS);
            level.scheduleTick(pos, this, LeadWater.INTERVAL);
        } else if (!wet && step > 0) {
            level.setBlock(pos, state.setValue(CONTAMINATION, 0), STEP_FLAGS);
        } else if (step > 0 && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, LeadWater.INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int step = state.getValue(CONTAMINATION);
        if (step == 0) {
            return;
        }
        int range = LeadWater.range(false);
        if (range == 0) {
            level.setBlock(pos, state.setValue(CONTAMINATION, 0), STEP_FLAGS);
            return;
        }
        LeadWater.contaminate(level, pos, step);
        if (step < Math.min(LeadWater.lastStep(range), LeadWater.MAX_STEP)) {
            level.setBlock(pos, state.setValue(CONTAMINATION, step + 1), STEP_FLAGS);
            LeadWater.wakeCoral(level, pos, step + 1);
        }
        level.scheduleTick(pos, this, LeadWater.INTERVAL);
    }

    /**
     * Mining it releases lead fumes, as mining lead ore does, unless it is mined with Silk Touch: lifted out whole,
     * as lead ore is. Only an actual harvest gets here (see {@link LeadOreBlock}).
     */
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!(level instanceof ServerLevel serverLevel) || !CommonConfig.leadOreSickness()) {
            return;
        }
        Holder<Enchantment> silkTouch = serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) == 0) {
            LeadFumes.mined(serverLevel, pos);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && LeadFumes.canSicken(living)
                && LeadSettings.rawLeadContact(false)) {
            LeadFumes.stand(living, LeadFumes.CONTACT_STAGE_TICKS);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (LeadSettings.rawLeadContact(true) && level.getBlockState(pos.above()).isAir()) {
            for (int i = random.nextInt(3); i > 0; i--) {
                level.addParticle(ModParticles.LEAD_DUST.get(), pos.getX() + 0.1 + random.nextDouble() * 0.8,
                        pos.getY() + 1.02 + random.nextDouble() * 0.15, pos.getZ() + 0.1 + random.nextDouble() * 0.8, 0.0, 0.0, 0.0);
            }
        }
    }
}

package com.phantomwing.theleadage.entity.custom;

import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.block.entity.LeadWeightBlockEntity;
import com.phantomwing.theleadage.damage.ModDamageTypes;
import com.phantomwing.theleadage.entity.ModEntities;
import com.phantomwing.theleadage.item.custom.LeadWeightItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The falling Lead Weight. Subclasses {@link FallingBlockEntity} (so it reuses the
 * vanilla falling/render/landing logic), but moves all combat behaviour here:
 *
 * <ul>
 *   <li><b>Mace-style impact + mid-fall damage</b> — each tick it hits every living
 *       entity it overlaps (once each), scaled by fall height with the Mace smash
 *       formula. Impact is just the final tick of this.</li>
 *   <li><b>Knockback</b> — hit entities are shoved outward + down.</li>
 *   <li><b>Kill credit</b> — when thrown by a player, damage names them as the
 *       attacker.</li>
 * </ul>
 */
public class LeadWeightEntity extends FallingBlockEntity {
    private static final float SMASH_THRESHOLD = 1.5f;   // min fall (blocks) before it crushes
    private static final float BASE_DAMAGE = 6.0f;       // flat impact damage on top of the fall bonus
    private static final float MAX_DAMAGE = 50.0f;       // safety cap
    private static final double KNOCKBACK_POWER = 0.6;   // horizontal shove
    private static final double KNOCKBACK_DOWN = 0.3;    // extra downward smash
    private static final float MOMENTUM_LOSS_PER_HIT = 0.8f; // each crushed entity leaves the weight 80% as deadly
    private static final float MIN_MOMENTUM = 0.1f;      // below this the weight is spent and passes through harmlessly
    private static final double SPEED_KEPT_PER_HIT = 0.85; // each crush also shaves the weight's downward speed

    @Nullable
    private UUID ownerUUID;
    /** Entities already damaged by this weight, so a single fall hits each at most once. */
    private final Set<Integer> hitEntityIds = new HashSet<>();
    /** Drops as the weight crushes entities, scaling down damage to later ones (it loses momentum). */
    private float momentum = 1.0f;
    /** Durability damage carried by this weight (entity hits + landings), reapplied to the item it becomes. */
    private int durabilityDamage;
    /** Tracks water state so the splash only fires on the tick the weight enters water. */
    private boolean wasInWater;

    public LeadWeightEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
    }

    /** Spawn from a block that is starting to fall (removes the source block, carrying its wear). */
    public static LeadWeightEntity fromBlock(Level level, BlockPos pos, BlockState state, @Nullable Player owner) {
        int durability = level.getBlockEntity(pos) instanceof LeadWeightBlockEntity orbBe ? orbBe.getDamage() : 0;
        // The falling weight carries a dry state; the source position keeps its fluid (so a
        // waterlogged weight leaves water behind, matching vanilla falling blocks).
        BlockState fallingState = state.hasProperty(BlockStateProperties.WATERLOGGED)
                ? state.setValue(BlockStateProperties.WATERLOGGED, false) : state;
        LeadWeightEntity weight = create(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, fallingState, owner, durability);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(weight);
        return weight;
    }

    /** Spawn directly in mid-air (the thrown weapon use); no source block to remove. */
    public static LeadWeightEntity inAir(Level level, double x, double y, double z, BlockState state,
                                       @Nullable Player owner, int durability) {
        LeadWeightEntity weight = create(level, x, y, z, state, owner, durability);
        level.addFreshEntity(weight);
        return weight;
    }

    private static LeadWeightEntity create(Level level, double x, double y, double z, BlockState state,
                                         @Nullable Player owner, int durability) {
        LeadWeightEntity weight = new LeadWeightEntity(ModEntities.LEAD_WEIGHT.get(), level);
        weight.blockState = state; // AccessWidener-exposed field on FallingBlockEntity
        weight.durabilityDamage = durability;
        weight.setPos(x, y, z);
        weight.setDeltaMovement(Vec3.ZERO);
        weight.xo = x;
        weight.yo = y;
        weight.zo = z;
        weight.setStartPos(weight.blockPosition());
        if (owner != null) {
            weight.ownerUUID = owner.getUUID();
        }
        // A deep "starting to fall" whoosh so the weight is felt the moment it drops.
        level.playSound(null, x, y, z, SoundEvents.ANVIL_FALL, SoundSource.BLOCKS, 0.9f, 0.6f);
        return weight;
    }

    @Override
    public void tick() {
        // Damage entities the weight currently overlaps (before this tick's move/landing
        // discards it). Successive ticks cover the whole fall path, including impact.
        if (!level().isClientSide() && isAlive()) {
            crushEntitiesInPath();
            handleWater();
        }
        super.tick();
        // If that landing just placed the weight as a block on top of a hopper, hand it over as an item
        // the hopper collects — so only a *falling* weight is sucked in (manual placement is untouched).
        if (!level().isClientSide() && isRemoved()) {
            convertHopperLanding();
        }
    }

    /**
     * Replace a freshly-placed weight block sitting on a hopper with the worn item the hopper then
     * sucks in. The thud and wear/shatter already ran in LeadWeightBlock#onLand.
     */
    private void convertHopperLanding() {
        BlockPos pos = blockPosition();
        if (!level().getBlockState(pos).is(ModBlocks.LEAD_WEIGHT.get())) {
            return; // didn't place a block here (dropped as an item, or landed elsewhere)
        }
        if (!(level().getBlockState(pos.below()).getBlock() instanceof HopperBlock)) {
            return;
        }
        level().removeBlock(pos, false);
        spawnAtLocation(getBlockState().getBlock()); // worn weight item, or nothing if it was spent
    }

    /**
     * A splash sound + droplet/bubble burst the moment the weight plunges into water. Water is
     * detected directly from the level because {@link FallingBlockEntity} never updates
     * {@code isInWater()}.
     */
    private void handleWater() {
        boolean inWater = waterInFallPath();
        if (inWater && !wasInWater && level() instanceof ServerLevel server) {
            double x = getX(), y = getY(), z = getZ();
            server.playSound(null, x, y, z, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0f, 0.8f);
            server.sendParticles(ParticleTypes.SPLASH, x, y + 0.1, z, 40, 0.3, 0.1, 0.3, 0.2);
            server.sendParticles(ParticleTypes.BUBBLE, x, y, z, 20, 0.25, 0.1, 0.25, 0.1);
        }
        wasInWater = inWater;
    }

    /** True if the weight is in, or will fall into this tick, a water block (its centre column). */
    private boolean waterInFallPath() {
        double startY = getY();
        double endY = startY + Math.min(0.0, getDeltaMovement().y) - 0.04; // include this tick's fall
        int x = Mth.floor(getX());
        int z = Mth.floor(getZ());
        for (int y = Mth.floor(startY); y >= Mth.floor(endY); y--) {
            if (level().getFluidState(new BlockPos(x, y, z)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    private void crushEntitiesInPath() {
        double fall = getStartPos().getY() - getY();
        if (fall < SMASH_THRESHOLD) {
            return;
        }
        float baseDamage = smashDamage((float) fall);
        if (baseDamage <= 0.0f || momentum < MIN_MOMENTUM) {
            return;
        }

        Player owner = resolveOwner();
        DamageSource source = orbDamageSource(owner);
        AABB box = getBoundingBox().inflate(0.1);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, box);
        for (LivingEntity target : targets) {
            if (target == owner) {
                continue;
            }
            if (!hitEntityIds.add(target.getId())) {
                continue; // already crushed by this weight
            }
            if (momentum < MIN_MOMENTUM) {
                break; // spent: the weight has shed too much momentum to keep hurting things
            }
            if (target.hurt(source, baseDamage * momentum)) {
                applyKnockback(target);
                // The crush bleeds the weight's momentum, so the next entity takes less — and it
                // physically slows, lengthening the fall and easing off the fall-height bonus too.
                momentum *= MOMENTUM_LOSS_PER_HIT;
                setDeltaMovement(getDeltaMovement().multiply(1.0, SPEED_KEPT_PER_HIT, 1.0));
                durabilityDamage += LeadWeightItem.WEAR_PER_ENTITY; // each crush wears the weight
            }
        }
    }

    /** The Mace smash bonus: 4/block for the first 3, 2/block to 8, 1/block beyond, plus a flat base. */
    private static float smashDamage(float fall) {
        float bonus;
        if (fall <= 3.0f) {
            bonus = 4.0f * fall;
        } else if (fall <= 8.0f) {
            bonus = 12.0f + 2.0f * (fall - 3.0f);
        } else {
            bonus = 22.0f + (fall - 8.0f);
        }
        return Math.min(BASE_DAMAGE + bonus, MAX_DAMAGE);
    }

    private void applyKnockback(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0e-4) {
            // Directly beneath the weight: scatter in a random horizontal direction.
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            dx = Math.cos(angle);
            dz = Math.sin(angle);
        }
        // knockback(power, x, z) pushes the target AWAY from (x, z) = the weight's column.
        target.knockback(KNOCKBACK_POWER, this.getX() - target.getX(), this.getZ() - target.getZ());
        target.setDeltaMovement(target.getDeltaMovement().add(0.0, -KNOCKBACK_DOWN, 0.0));
        target.hurtMarked = true;
    }

    private DamageSource orbDamageSource(@Nullable Player owner) {
        Holder<DamageType> type = level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageTypes.LEAD_WEIGHT);
        return owner != null ? new DamageSource(type, this, owner) : new DamageSource(type, this);
    }

    @Nullable
    private Player resolveOwner() {
        if (ownerUUID == null) {
            return null;
        }
        Entity entity = level().getPlayerByUUID(ownerUUID);
        return entity instanceof Player player ? player : null;
    }

    /** Total durability damage this weight has accrued so far (entity crushes), for the block/item it becomes. */
    public int getDurabilityDamage() {
        return durabilityDamage;
    }

    /** Whether a landing would spend the weight (it shatters instead of returning a block/item). */
    public boolean isSpentOnLanding() {
        return durabilityDamage + LeadWeightItem.WEAR_PER_LANDING >= LeadWeightItem.MAX_DURABILITY;
    }

    /**
     * The weight's only drop is FallingBlockEntity's "couldn't place" item drop — replace its plain
     * weight with one carrying this weight's wear (landing included), or nothing if the weight is spent.
     */
    @Override
    public ItemEntity spawnAtLocation(ItemLike item) {
        if (isSpentOnLanding()) {
            return null; // shattered — the fx is played in LeadWeightBlock#onBrokenAfterFall
        }
        ItemStack worn = new ItemStack(item);
        worn.setDamageValue(durabilityDamage + LeadWeightItem.WEAR_PER_LANDING);
        return spawnAtLocation(worn);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        tag.putFloat("Momentum", momentum);
        tag.putInt("Durability", durabilityDamage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("Momentum")) {
            momentum = tag.getFloat("Momentum");
        }
        durabilityDamage = tag.getInt("Durability");
    }
}

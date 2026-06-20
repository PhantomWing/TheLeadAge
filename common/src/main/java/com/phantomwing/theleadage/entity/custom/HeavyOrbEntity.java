package com.phantomwing.theleadage.entity.custom;

import com.phantomwing.theleadage.damage.ModDamageTypes;
import com.phantomwing.theleadage.entity.ModEntities;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The falling Heavy Orb. Subclasses {@link FallingBlockEntity} (so it reuses the
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
public class HeavyOrbEntity extends FallingBlockEntity {
    private static final float SMASH_THRESHOLD = 1.5f;   // min fall (blocks) before it crushes
    private static final float BASE_DAMAGE = 6.0f;       // flat impact damage on top of the fall bonus
    private static final float MAX_DAMAGE = 50.0f;       // safety cap
    private static final double KNOCKBACK_POWER = 0.6;   // horizontal shove
    private static final double KNOCKBACK_DOWN = 0.3;    // extra downward smash

    @Nullable
    private UUID ownerUUID;
    /** Entities already damaged by this orb, so a single fall hits each at most once. */
    private final Set<Integer> hitEntityIds = new HashSet<>();

    public HeavyOrbEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
    }

    /** Spawn from a block that is starting to fall (removes the source block). */
    public static HeavyOrbEntity fromBlock(Level level, BlockPos pos, BlockState state, @Nullable Player owner) {
        HeavyOrbEntity orb = create(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state, owner);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(orb);
        return orb;
    }

    /** Spawn directly in mid-air (the thrown weapon use); no source block to remove. */
    public static HeavyOrbEntity inAir(Level level, double x, double y, double z, BlockState state, @Nullable Player owner) {
        HeavyOrbEntity orb = create(level, x, y, z, state, owner);
        level.addFreshEntity(orb);
        return orb;
    }

    private static HeavyOrbEntity create(Level level, double x, double y, double z, BlockState state, @Nullable Player owner) {
        HeavyOrbEntity orb = new HeavyOrbEntity(ModEntities.HEAVY_ORB.get(), level);
        orb.blockState = state; // AccessWidener-exposed field on FallingBlockEntity
        orb.setPos(x, y, z);
        orb.setDeltaMovement(Vec3.ZERO);
        orb.xo = x;
        orb.yo = y;
        orb.zo = z;
        orb.setStartPos(orb.blockPosition());
        if (owner != null) {
            orb.ownerUUID = owner.getUUID();
        }
        return orb;
    }

    @Override
    public void tick() {
        // Damage entities the orb currently overlaps (before this tick's move/landing
        // discards it). Successive ticks cover the whole fall path, including impact.
        if (!level().isClientSide() && isAlive()) {
            crushEntitiesInPath();
        }
        super.tick();
    }

    private void crushEntitiesInPath() {
        if (!CommonConfig.heavyOrbDamage()) {
            return;
        }
        double fall = getStartPos().getY() - getY();
        if (fall < SMASH_THRESHOLD) {
            return;
        }
        float damage = smashDamage((float) fall);
        if (damage <= 0.0f) {
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
                continue; // already crushed by this orb
            }
            if (target.hurt(source, damage)) {
                applyKnockback(target);
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
            // Directly beneath the orb: scatter in a random horizontal direction.
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            dx = Math.cos(angle);
            dz = Math.sin(angle);
        }
        // knockback(power, x, z) pushes the target AWAY from (x, z) = the orb's column.
        target.knockback(KNOCKBACK_POWER, this.getX() - target.getX(), this.getZ() - target.getZ());
        target.setDeltaMovement(target.getDeltaMovement().add(0.0, -KNOCKBACK_DOWN, 0.0));
        target.hurtMarked = true;
    }

    private DamageSource orbDamageSource(@Nullable Player owner) {
        Holder<DamageType> type = level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageTypes.HEAVY_ORB);
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

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
    }
}

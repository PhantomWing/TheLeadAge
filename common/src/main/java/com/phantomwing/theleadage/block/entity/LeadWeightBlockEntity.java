package com.phantomwing.theleadage.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds a placed Lead Weight's accumulated durability damage, so its wear survives while it is in
 * block form (it falls, lands, and is mined back into an item carrying the same damage value).
 */
public class LeadWeightBlockEntity extends BlockEntity {
    private int damage;

    public LeadWeightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEAD_WEIGHT.get(), pos, state);
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        damage = tag.getInt("Damage");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Damage", damage);
    }
}

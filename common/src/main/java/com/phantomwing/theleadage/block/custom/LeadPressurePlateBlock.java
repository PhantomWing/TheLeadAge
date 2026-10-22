package com.phantomwing.theleadage.block.custom;

import com.phantomwing.theleadage.block.ModBlockSetTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;

/**
 * A weighted pressure plate that only living things can press: dropped items are too light to push
 * lead down, as on a stone plate. Each mob or player on it adds one to the signal, up to 15, the light
 * weighted plate's scale, so it counts mobs where gold counts everything.
 */
public class LeadPressurePlateBlock extends WeightedPressurePlateBlock {
    private static final int MAX_MOBS = 15;

    public LeadPressurePlateBlock(Properties properties) {
        super(MAX_MOBS, ModBlockSetTypes.LEAD, properties);
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        return Math.min(getEntityCount(level, TOUCH_AABB.move(pos), LivingEntity.class), MAX_MOBS);
    }
}

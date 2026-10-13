package com.phantomwing.theleadage.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.phantomwing.theleadage.block.custom.LeadedGlassPaneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Tints a beacon's beam through a dyed leaded glass pane, which NeoForge does through
 * {@code getBeaconColorMultiplier}. Vanilla takes a beam's colour from the block alone, and a
 * pane's colours are in its block entity, so the pane is read where the beacon reads each block
 * on the beam's way up, and that colour stands in for the one the block gives.
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {
    // The beacon ticks on the client's thread and the server's.
    @Unique
    private static final ThreadLocal<Integer> theleadage$paneColor = new ThreadLocal<>();

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState theleadage$readPane(Level level, BlockPos pos, Operation<BlockState> original) {
        BlockState state = original.call(level, pos);
        theleadage$paneColor.set(state.getBlock() instanceof LeadedGlassPaneBlock pane
                ? pane.beaconColor(state, level, pos) : null);
        return state;
    }

    // A clear pane leaves the beam as clear glass does. The class constant also matches the cast that
    // follows the check, which can't be wrapped, so the check is the first.
    @WrapOperation(method = "tick", constant = @Constant(classValue = BeaconBeamBlock.class, ordinal = 0))
    private static boolean theleadage$clearPanePasses(Object block, Operation<Boolean> original) {
        return block instanceof LeadedGlassPaneBlock ? theleadage$paneColor.get() != null : original.call(block);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColor()I"))
    private static int theleadage$paneTint(DyeColor color, Operation<Integer> original) {
        Integer pane = theleadage$paneColor.get();
        return pane != null ? pane : original.call(color);
    }
}

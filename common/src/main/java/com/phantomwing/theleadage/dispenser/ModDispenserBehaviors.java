package com.phantomwing.theleadage.dispenser;

import com.phantomwing.theleadage.item.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.List;

/**
 * Dispenser special-cases for the mod's items.
 *
 * <p>A dispenser normally spits an item out as an entity; a handful of items instead <em>do</em>
 * something (TNT primes, shulker boxes place themselves). The Lead Weight joins that second group: a
 * dispenser sets one down in the cell it faces. Because a weight with nothing beneath it falls, a
 * dispenser aimed into open air becomes a drop trap.</p>
 */
public final class ModDispenserBehaviors {
    private ModDispenserBehaviors() {
    }

    /**
     * Hooks each weight up as it is registered. Deliberately not a setup-phase callback:
     * {@link DispenserBlock}'s behaviour map is a plain static map, and registry callbacks run on the
     * main thread, whereas NeoForge dispatches common setup in parallel.
     */
    public static void register() {
        List<RegistrySupplier<Item>> weights = List.of(
                ModItems.LEAD_WEIGHT, ModItems.CHIPPED_LEAD_WEIGHT, ModItems.DAMAGED_LEAD_WEIGHT);
        for (RegistrySupplier<Item> weight : weights) {
            weight.listen(item -> DispenserBlock.registerBehavior(item, PLACE_WEIGHT));
        }
    }

    /**
     * Places the weight in the block the dispenser faces, falling back to the default "spit it out"
     * behaviour when that cell is occupied. Mirrors vanilla's shulker box: the placement is treated as
     * coming from above unless the cell below is empty, which keeps the block's own placement rules
     * (the weight hangs from a chain when there is one overhead) working as if a player had placed it.
     */
    private static final DispenseItemBehavior PLACE_WEIGHT = new OptionalDispenseItemBehavior() {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            setSuccess(false);
            if (stack.getItem() instanceof BlockItem blockItem) {
                Direction facing = source.state().getValue(DispenserBlock.FACING);
                BlockPos target = source.pos().relative(facing);
                Direction placedAgainst = source.level().isEmptyBlock(target.below()) ? facing : Direction.UP;
                setSuccess(blockItem.place(
                                new DirectionalPlaceContext(source.level(), target, facing, stack, placedAgainst))
                        .consumesAction());
            }
            return stack;
        }
    };
}

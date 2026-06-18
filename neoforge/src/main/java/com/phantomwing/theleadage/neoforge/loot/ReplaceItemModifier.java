package com.phantomwing.theleadage.neoforge.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phantomwing.theleadage.loot.LeadLootAlgorithms;
import com.phantomwing.theleadage.platform.CommonConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Global Loot Modifier that replaces stacks of one or more items with another item
 * (keeping count, carrying durability/enchantments). Delegates the post-roll
 * mutation to the shared {@link LeadLootAlgorithms} so it stays byte-identical to
 * the Fabric loot mixin. Gated on the {@code enable_structure_loot} config.
 */
public class ReplaceItemModifier extends LootModifier {
    public static final Supplier<MapCodec<ReplaceItemModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(
                    inst.group(
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter((m) -> m.item),
                            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("removed_item").forGetter((m) -> m.removedItems),
                            Codec.INT.fieldOf("in_stacks").forGetter((m) -> m.minStacks),
                            Codec.INT.fieldOf("max_stacks").forGetter((m) -> m.maxStacks)
                    )
            ).apply(inst, ReplaceItemModifier::new)));

    private final Item item;
    private final List<Item> removedItems;
    private final int minStacks;
    private final int maxStacks;

    public ReplaceItemModifier(LootItemCondition[] conditions, ItemLike itemToAdd, List<Item> itemToReplace, int minStacks, int maxStacks) {
        super(conditions);
        this.removedItems = itemToReplace;
        this.item = itemToAdd.asItem();
        this.minStacks = minStacks;
        this.maxStacks = maxStacks;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext lootContext) {
        if (!CommonConfig.enableStructureLoot()) {
            return generatedLoot;
        }
        LeadLootAlgorithms.applyReplaceItem(generatedLoot, lootContext, this.item, this.removedItems, this.minStacks, this.maxStacks);
        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}

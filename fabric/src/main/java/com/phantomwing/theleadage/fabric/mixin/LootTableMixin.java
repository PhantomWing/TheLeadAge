package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.fabric.loot.LeadLootTableId;
import com.phantomwing.theleadage.loot.LeadLootAlgorithms;
import com.phantomwing.theleadage.loot.LeadLootSpec;
import com.phantomwing.theleadage.platform.CommonConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Fabric parity for the NeoForge Global Loot Modifiers. Fabric has no post-roll
 * loot API, so this mixes into {@code LootTable.getRandomItems(LootContext)} (the
 * list every chest/structure roll funnels through) and mutates the rolled list
 * exactly like the NeoForge {@code ReplaceItemModifier}. The rolled table's id is
 * read back from the {@code @Unique} field stamped at load time (see
 * {@code TheLeadAgeFabric}), mirroring the GLM {@code loot_table_id} condition;
 * the {@code random_chance} draw and the config gate match the GLM too.
 */
@Mixin(LootTable.class)
public abstract class LootTableMixin implements LeadLootTableId {
    @Unique
    @Nullable
    private Identifier theleadage$lootTableId;

    @Override
    @Nullable
    public Identifier theleadage$getLootTableId() {
        return this.theleadage$lootTableId;
    }

    @Override
    public void theleadage$setLootTableId(Identifier id) {
        this.theleadage$lootTableId = id;
    }

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN")
    )
    private void theleadage$applyLeadLoot(LootContext context,
                                          CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        Identifier tableId = this.theleadage$lootTableId;
        ObjectArrayList<ItemStack> generatedLoot = cir.getReturnValue();
        if (tableId == null || generatedLoot == null || !CommonConfig.enableStructureLoot()) {
            return;
        }

        RandomSource random = context.getRandom();
        for (LeadLootSpec.Entry entry : LeadLootSpec.entries()) {
            // GLM condition: only entries for this table, then the random_chance draw
            // (random.nextFloat() < chance, identical to LootItemRandomChanceCondition).
            if (!entry.targetLootTable().equals(tableId) || random.nextFloat() >= entry.chance()) {
                continue;
            }
            Item item = entry.item().get();
            List<Item> removed = entry.removedItems().stream().map(Supplier::get).toList();
            LeadLootAlgorithms.applyReplaceItem(generatedLoot, context, item, removed, entry.min(), entry.max());
        }
    }
}

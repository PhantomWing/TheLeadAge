package com.phantomwing.theleadage.fabric.loot;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Duck interface mixed onto {@code LootTable} so a rolled table can report its own
 * id. Vanilla 1.21.1 {@code LootTable} carries no id (NeoForge adds one via a patch;
 * Mojmap does not), so the {@code LootTableMixin} needs this to know which
 * {@link com.phantomwing.theleadage.loot.LeadLootSpec} entries apply — the Fabric
 * equivalent of the NeoForge GLM {@code neoforge:loot_table_id} condition. The id is
 * stamped once after all loot tables load (see {@code TheLeadAgeFabric}).
 */
public interface LeadLootTableId {
    @Nullable
    Identifier theleadage$getLootTableId();

    void theleadage$setLootTableId(Identifier id);
}

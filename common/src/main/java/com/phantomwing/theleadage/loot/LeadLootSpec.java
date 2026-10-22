package com.phantomwing.theleadage.loot;

import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Single, loader-agnostic source of truth for the lead loot injections. Both the
 * NeoForge Global Loot Modifier datagen provider and the Fabric loot mixin iterate
 * this list, so the two loaders behave identically. (Mirrors The Silver Age's
 * {@code SilverLootSpec}; lead currently does REPLACE only.)
 *
 * <p>Lead horse armor occasionally replaces iron horse armor in the vanilla loot
 * tables that contain it, and lead nautilus armor iron nautilus armor. {@code ModItems}
 * suppliers are lazy so this class can be loaded before item registration completes.</p>
 */
public final class LeadLootSpec {
    private LeadLootSpec() {
    }

    /**
     * One injection: in {@code targetLootTable}, with probability {@code chance},
     * replace up to {@code [min, max]} matched stacks (0 max ⇒ all) of any
     * {@code removedItems} with {@code item}.
     */
    public record Entry(String id, Identifier targetLootTable, float chance,
                        Supplier<Item> item, int min, int max, List<Supplier<Item>> removedItems) {
    }

    /** Low chance for a given iron horse armor, nautilus armor or iron gear roll to become lead instead. */
    private static final float CHANCE = 0.10f;
    /** Chance for a rolled iron-ingot stack to become lead ingots instead. */
    private static final float INGOT_CHANCE = 0.20f;

    private static Identifier mc(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    private static Supplier<Item> vanilla(Item item) {
        return () -> item;
    }

    /** The entries for {@code table}, none for most: Fabric asks on every roll of every table, block drops included. */
    public static List<Entry> entriesFor(Identifier table) {
        return ByTable.ENTRIES.getOrDefault(table, List.of());
    }

    private static final class ByTable {
        private static final Map<Identifier, List<Entry>> ENTRIES = entries().stream()
                .collect(Collectors.groupingBy(Entry::targetLootTable, Collectors.toUnmodifiableList()));
    }

    public static List<Entry> entries() {
        List<Supplier<Item>> ironHorseArmor = List.of(vanilla(Items.IRON_HORSE_ARMOR));
        List<Supplier<Item>> ironNautilusArmor = List.of(vanilla(Items.IRON_NAUTILUS_ARMOR));
        List<Supplier<Item>> ironIngot = List.of(vanilla(Items.IRON_INGOT));
        List<Entry> entries = new ArrayList<>(List.of(
                // Lead horse armor: occasionally replaces iron horse armor.
                replaceIronHorseArmor("lead_horse_armor_from_desert_pyramid", "chests/desert_pyramid", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_jungle_temple", "chests/jungle_temple", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_simple_dungeon", "chests/simple_dungeon", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_stronghold_corridor", "chests/stronghold_corridor", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_village_weaponsmith", "chests/village/village_weaponsmith", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_end_city", "chests/end_city_treasure", ironHorseArmor),

                // Lead nautilus armor: occasionally replaces iron nautilus armor, in the ocean chests
                // that hold it.
                replaceIronNautilusArmor("lead_nautilus_armor_from_buried_treasure", "chests/buried_treasure", ironNautilusArmor),
                replaceIronNautilusArmor("lead_nautilus_armor_from_shipwreck_map", "chests/shipwreck_map", ironNautilusArmor),
                replaceIronNautilusArmor("lead_nautilus_armor_from_shipwreck_supply", "chests/shipwreck_supply", ironNautilusArmor),
                replaceIronNautilusArmor("lead_nautilus_armor_from_shipwreck_treasure", "chests/shipwreck_treasure", ironNautilusArmor),
                replaceIronNautilusArmor("lead_nautilus_armor_from_underwater_ruin_big", "chests/underwater_ruin_big", ironNautilusArmor),
                replaceIronNautilusArmor("lead_nautilus_armor_from_underwater_ruin_small", "chests/underwater_ruin_small", ironNautilusArmor),

                // Lead ingots: a rolled iron-ingot stack occasionally comes up lead instead. Curated to
                // old ruins, mines, shipwrecks/treasure and metalworkers — where a mundane heavy metal fits.
                replaceIronIngot("lead_ingot_from_abandoned_mineshaft", "chests/abandoned_mineshaft", ironIngot),
                replaceIronIngot("lead_ingot_from_simple_dungeon", "chests/simple_dungeon", ironIngot),
                replaceIronIngot("lead_ingot_from_desert_pyramid", "chests/desert_pyramid", ironIngot),
                replaceIronIngot("lead_ingot_from_jungle_temple", "chests/jungle_temple", ironIngot),
                replaceIronIngot("lead_ingot_from_stronghold_corridor", "chests/stronghold_corridor", ironIngot),
                replaceIronIngot("lead_ingot_from_stronghold_crossing", "chests/stronghold_crossing", ironIngot),
                replaceIronIngot("lead_ingot_from_shipwreck_treasure", "chests/shipwreck_treasure", ironIngot),
                replaceIronIngot("lead_ingot_from_buried_treasure", "chests/buried_treasure", ironIngot),
                replaceIronIngot("lead_ingot_from_woodland_mansion", "chests/woodland_mansion", ironIngot),
                replaceIronIngot("lead_ingot_from_pillager_outpost", "chests/pillager_outpost", ironIngot),
                replaceIronIngot("lead_ingot_from_village_weaponsmith", "chests/village/village_weaponsmith", ironIngot),
                replaceIronIngot("lead_ingot_from_village_toolsmith", "chests/village/village_toolsmith", ironIngot),
                replaceIronIngot("lead_ingot_from_village_armorer", "chests/village/village_armorer", ironIngot),
                // The vault rolls chests/trial_chambers/reward, which reaches the iron in reward_common as a
                // nested table; loot modifiers only ever see the table that was rolled.
                replaceIronIngot("lead_ingot_from_trial_chambers_reward", "chests/trial_chambers/reward", ironIngot)
        ));

        // Lead gear: an iron tool or piece of armor occasionally comes up lead instead, in every chest
        // vanilla puts iron gear in, keeping its wear and enchantments.
        replaceIronGear(entries, "village_armorer", "chests/village/village_armorer", Items.IRON_HELMET);
        replaceIronGear(entries, "village_toolsmith", "chests/village/village_toolsmith",
                Items.IRON_PICKAXE, Items.IRON_SHOVEL);
        replaceIronGear(entries, "village_weaponsmith", "chests/village/village_weaponsmith",
                Items.IRON_SWORD, Items.IRON_SPEAR, Items.IRON_PICKAXE,
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        replaceIronGear(entries, "stronghold_corridor", "chests/stronghold_corridor",
                Items.IRON_SWORD, Items.IRON_PICKAXE,
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        replaceIronGear(entries, "stronghold_crossing", "chests/stronghold_crossing", Items.IRON_PICKAXE);
        replaceIronGear(entries, "end_city", "chests/end_city_treasure",
                Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_SHOVEL,
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        replaceIronGear(entries, "abandoned_mineshaft", "chests/abandoned_mineshaft", Items.IRON_PICKAXE);
        replaceIronGear(entries, "buried_treasure", "chests/buried_treasure", Items.IRON_SWORD, Items.IRON_SPEAR);
        replaceIronGear(entries, "ancient_city", "chests/ancient_city", Items.IRON_LEGGINGS);
        replaceIronGear(entries, "bastion_other", "chests/bastion_other", Items.IRON_SWORD);
        replaceIronGear(entries, "trial_chambers_corridor", "chests/trial_chambers/corridor", Items.IRON_AXE);
        // reward_rare, nested in the vault's table like reward_common's iron ingots.
        replaceIronGear(entries, "trial_chambers_reward", "chests/trial_chambers/reward",
                Items.IRON_AXE, Items.IRON_CHESTPLATE);
        return List.copyOf(entries);
    }

    private static Entry replaceIronHorseArmor(String id, String table, List<Supplier<Item>> removed) {
        return new Entry(id, mc(table), CHANCE, ModItems.LEAD_HORSE_ARMOR::get, 1, 1, removed);
    }

    private static Entry replaceIronNautilusArmor(String id, String table, List<Supplier<Item>> removed) {
        return new Entry(id, mc(table), CHANCE, ModItems.LEAD_NAUTILUS_ARMOR::get, 1, 1, removed);
    }

    private static Entry replaceIronIngot(String id, String table, List<Supplier<Item>> removed) {
        return new Entry(id, mc(table), INGOT_CHANCE, ModItems.LEAD_INGOT::get, 1, 1, removed);
    }

    /** One entry per piece, named lead_&lt;piece&gt;_from_&lt;name&gt;. */
    private static void replaceIronGear(List<Entry> entries, String name, String table, Item... iron) {
        for (Item piece : iron) {
            String id = BuiltInRegistries.ITEM.getKey(piece).getPath().replace("iron_", "lead_") + "_from_" + name;
            entries.add(new Entry(id, mc(table), CHANCE, leadGearFor(piece), 1, 1, List.of(vanilla(piece))));
        }
    }

    private static Supplier<Item> leadGearFor(Item iron) {
        if (iron == Items.IRON_SWORD) return ModItems.LEAD_SWORD::get;
        if (iron == Items.IRON_AXE) return ModItems.LEAD_AXE::get;
        if (iron == Items.IRON_PICKAXE) return ModItems.LEAD_PICKAXE::get;
        if (iron == Items.IRON_SHOVEL) return ModItems.LEAD_SHOVEL::get;
        if (iron == Items.IRON_SPEAR) return ModItems.LEAD_SPEAR::get;
        if (iron == Items.IRON_HELMET) return ModItems.LEAD_HELMET::get;
        if (iron == Items.IRON_CHESTPLATE) return ModItems.LEAD_CHESTPLATE::get;
        if (iron == Items.IRON_LEGGINGS) return ModItems.LEAD_LEGGINGS::get;
        if (iron == Items.IRON_BOOTS) return ModItems.LEAD_BOOTS::get;
        throw new IllegalArgumentException("No lead counterpart for " + iron);
    }
}

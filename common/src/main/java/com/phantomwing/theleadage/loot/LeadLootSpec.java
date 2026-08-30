package com.phantomwing.theleadage.loot;

import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Supplier;

/**
 * Single, loader-agnostic source of truth for the lead loot injections. Both the
 * NeoForge Global Loot Modifier datagen provider and the Fabric loot mixin iterate
 * this list, so the two loaders behave identically. (Mirrors The Silver Age's
 * {@code SilverLootSpec}; lead currently does REPLACE only.)
 *
 * <p>Lead horse armor occasionally replaces iron horse armor in the vanilla loot
 * tables that contain it. {@code ModItems} suppliers are lazy so this class can be
 * loaded before item registration completes.</p>
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

    /** Low chance for a given iron-horse-armor roll to become lead instead. */
    private static final float CHANCE = 0.10f;
    /** Chance for a rolled iron-ingot stack to become lead ingots instead. */
    private static final float INGOT_CHANCE = 0.20f;

    private static Identifier mc(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    private static Supplier<Item> vanilla(Item item) {
        return () -> item;
    }

    public static List<Entry> entries() {
        List<Supplier<Item>> ironHorseArmor = List.of(vanilla(Items.IRON_HORSE_ARMOR));
        List<Supplier<Item>> ironIngot = List.of(vanilla(Items.IRON_INGOT));
        return List.of(
                // Lead horse armor: occasionally replaces iron horse armor.
                replaceIronHorseArmor("lead_horse_armor_from_desert_pyramid", "chests/desert_pyramid", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_jungle_temple", "chests/jungle_temple", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_simple_dungeon", "chests/simple_dungeon", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_stronghold_corridor", "chests/stronghold_corridor", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_village_weaponsmith", "chests/village/village_weaponsmith", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_end_city", "chests/end_city_treasure", ironHorseArmor),

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
                replaceIronIngot("lead_ingot_from_village_armorer", "chests/village/village_armorer", ironIngot)
        );
    }

    private static Entry replaceIronHorseArmor(String id, String table, List<Supplier<Item>> removed) {
        return new Entry(id, mc(table), CHANCE, ModItems.LEAD_HORSE_ARMOR::get, 1, 1, removed);
    }

    private static Entry replaceIronIngot(String id, String table, List<Supplier<Item>> removed) {
        return new Entry(id, mc(table), INGOT_CHANCE, ModItems.LEAD_INGOT::get, 1, 1, removed);
    }
}

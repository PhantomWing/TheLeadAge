package com.phantomwing.theleadage.loot;

import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.resources.ResourceLocation;
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
    public record Entry(String id, ResourceLocation targetLootTable, float chance,
                        Supplier<Item> item, int min, int max, List<Supplier<Item>> removedItems) {
    }

    /** Low chance for a given iron-horse-armor roll to become lead instead. */
    private static final float CHANCE = 0.10f;

    private static ResourceLocation mc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private static Supplier<Item> vanilla(Item item) {
        return () -> item;
    }

    public static List<Entry> entries() {
        List<Supplier<Item>> ironHorseArmor = List.of(vanilla(Items.IRON_HORSE_ARMOR));
        return List.of(
                replaceIronHorseArmor("lead_horse_armor_from_desert_pyramid", "chests/desert_pyramid", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_jungle_temple", "chests/jungle_temple", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_simple_dungeon", "chests/simple_dungeon", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_stronghold_corridor", "chests/stronghold_corridor", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_village_weaponsmith", "chests/village/village_weaponsmith", ironHorseArmor),
                replaceIronHorseArmor("lead_horse_armor_from_end_city", "chests/end_city_treasure", ironHorseArmor)
        );
    }

    private static Entry replaceIronHorseArmor(String id, String table, List<Supplier<Item>> removed) {
        return new Entry(id, mc(table), CHANCE, ModItems.LEAD_HORSE_ARMOR::get, 1, 1, removed);
    }
}

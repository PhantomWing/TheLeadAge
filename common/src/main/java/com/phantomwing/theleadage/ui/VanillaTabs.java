package com.phantomwing.theleadage.ui;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadedGlassDyes;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.platform.CommonConfig;
import com.phantomwing.theleadage.platform.CreativeTabPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/**
 * The mod's items in vanilla's creative tabs too, each beside its vanilla counterpart, as The Silver Age's and The Tin
 * Age's are: lead's blocks after copper's, its ore, ingredients and gear beside iron's and copper's, the metals it
 * stands between, and leaded glass after stained glass. The mod's own tab keeps all of them together, and an item it
 * hides - the knife without Farmer's Delight, the spear and nautilus armour without the mods backporting them, the gear
 * with {@code enable_lead_gear} off - is left out here too.
 */
public final class VanillaTabs {
    private static final List<String> BUILDING = List.of("lead_block", "chiseled_lead", "lead_grate", "cut_lead",
            "cut_lead_stairs", "cut_lead_slab", "lead_bricks", "lead_brick_stairs", "lead_brick_slab", "lead_brick_wall",
            "lead_pillar", "lead_bars", "lead_chain", "lead_door", "leaded_glass_door", "lead_trapdoor",
            "leaded_glass_trapdoor", "lead_pressure_plate", "lead_bulb");
    private static final List<String> PANES = List.of("leaded_glass_pane", "leaded_glass_pane_split",
            "leaded_glass_pane_plus", "leaded_glass_pane_grid", "leaded_glass_pane_diagonal", "leaded_glass_pane_cross",
            "leaded_glass_pane_diamond", "leaded_glass_pane_lattice", "leaded_glass_pane_bars",
            "leaded_glass_pane_diagonal_bars");

    private static final ResourceKey<CreativeModeTab> FARMERS_DELIGHT_TAB = modTab("farmersdelight", "farmersdelight");
    private static final ResourceKey<CreativeModeTab> CREATE_TAB = modTab("create", "base");

    private VanillaTabs() {
    }

    public static void register() {
        // 1.20.1 has no copper grates, doors or bulbs, so lead follows its last copper building block.
        add(CreativeModeTabs.BUILDING_BLOCKS, Items.WAXED_OXIDIZED_CUT_COPPER_SLAB, BUILDING);

        List<String> glass = new ArrayList<>(List.of("leaded_glass"));
        for (DyeColor color : LeadedGlassDyes.colors()) {
            glass.add(color.getName() + "_leaded_glass");
        }
        add(CreativeModeTabs.COLORED_BLOCKS, Items.PINK_STAINED_GLASS, glass);
        add(CreativeModeTabs.COLORED_BLOCKS, Items.PINK_STAINED_GLASS_PANE, PANES);

        add(CreativeModeTabs.NATURAL_BLOCKS, Items.DEEPSLATE_COPPER_ORE, List.of("lead_ore", "deepslate_lead_ore"));
        add(CreativeModeTabs.NATURAL_BLOCKS, Items.RAW_COPPER_BLOCK, List.of("raw_lead_block"));

        add(CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.SOUL_TORCH, List.of("lead_torch"));
        add(CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.SOUL_LANTERN, List.of("lead_lantern"));
        add(CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.CHAIN, List.of("lead_chain"));
        // After the lamp, 1.20.1 having no copper bulb.
        add(CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.REDSTONE_LAMP, List.of("lead_bulb"));
        add(CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.DAMAGED_ANVIL,
                List.of("lead_weight", "chipped_lead_weight", "damaged_lead_weight"));

        add(CreativeModeTabs.REDSTONE_BLOCKS, Items.REDSTONE_LAMP, List.of("lead_bulb"));
        add(CreativeModeTabs.REDSTONE_BLOCKS, Items.HEAVY_WEIGHTED_PRESSURE_PLATE, List.of("lead_pressure_plate"));
        add(CreativeModeTabs.REDSTONE_BLOCKS, Items.IRON_DOOR, List.of("lead_door"));
        add(CreativeModeTabs.REDSTONE_BLOCKS, Items.IRON_TRAPDOOR, List.of("lead_trapdoor"));

        add(CreativeModeTabs.TOOLS_AND_UTILITIES, Items.IRON_HOE,
                List.of("lead_shovel", "lead_pickaxe", "lead_axe", "lead_hoe"));
        // Farmer's Delight lists its knives in its own tab, not vanilla's.
        add(FARMERS_DELIGHT_TAB, List.of(farmersDelight("iron_knife")), List.of("lead_knife"));

        add(CreativeModeTabs.COMBAT, Items.IRON_SWORD, List.of("lead_sword"));
        // 1.21.11's spear after the Iron Spear and nautilus armour after the Iron Nautilus Armor, where a mod
        // backporting them lists them, which mods.toml orders before this; else beside the sword and horse
        // armour.
        add(CreativeModeTabs.COMBAT, List.of(vanilla("iron_spear"), lead("lead_sword")), List.of("lead_spear"));
        add(CreativeModeTabs.COMBAT, Items.IRON_AXE, List.of("lead_axe"));
        add(CreativeModeTabs.COMBAT, Items.IRON_BOOTS,
                List.of("lead_helmet", "lead_chestplate", "lead_leggings", "lead_boots"));
        add(CreativeModeTabs.COMBAT, Items.IRON_HORSE_ARMOR, List.of("lead_horse_armor"));
        add(CreativeModeTabs.COMBAT, List.of(vanilla("iron_nautilus_armor"), lead("lead_horse_armor")),
                List.of("lead_nautilus_armor"));

        add(CreativeModeTabs.INGREDIENTS, Items.RAW_COPPER, List.of("raw_lead"));
        add(CreativeModeTabs.INGREDIENTS, Items.IRON_NUGGET, List.of("lead_nugget"));
        add(CreativeModeTabs.INGREDIENTS, Items.COPPER_INGOT, List.of("lead_ingot"));

        // Create lists its sheets in its own tab, not vanilla's.
        add(CREATE_TAB, List.of(create("iron_sheet")), List.of("lead_sheet"));
    }

    private static void add(ResourceKey<CreativeModeTab> tab, ItemLike after, List<String> ids) {
        add(tab, List.of(() -> after.asItem()), ids);
    }

    /** {@code ids} after the first of {@code anchors} the tab lists; an item no mod registers is air, and skipped. */
    private static void add(ResourceKey<CreativeModeTab> tab, List<ItemLike> anchors, List<String> ids) {
        CreativeTabPlatform.modify(tab, output -> {
            if (!CommonConfig.vanillaCreativeTabs()) {
                return;
            }
            List<ItemStack> stacks = new ArrayList<>();
            for (String id : ids) {
                Item item = BuiltInRegistries.ITEM.get(TheLeadAge.resourceLocation(id));
                if (ModCreativeModeTab.isShown(item)) {
                    ModItems.CREATIVE_TAB_ITEMS.stream().filter(entry -> entry.get() == item).findFirst()
                            .ifPresent(entry -> stacks.addAll(ModCreativeModeTab.stacksOf(entry)));
                }
            }
            if (stacks.isEmpty()) {
                return;
            }
            for (ItemLike anchor : anchors) {
                if (anchor.asItem() == Items.AIR) {
                    continue;
                }
                try {
                    insertAfter(output, anchor, stacks);
                    return;
                } catch (IllegalArgumentException notListed) {
                    // The tab doesn't list this anchor, so the next one is tried; a failed insert has inserted nothing.
                }
            }
        });
    }

    private static ItemLike vanilla(String path) {
        return () -> BuiltInRegistries.ITEM.get(new ResourceLocation(path));
    }

    private static ItemLike lead(String path) {
        return () -> BuiltInRegistries.ITEM.get(TheLeadAge.resourceLocation(path));
    }

    private static ItemLike farmersDelight(String path) {
        return modItem("farmersdelight", path);
    }

    private static ItemLike create(String path) {
        return modItem("create", path);
    }

    private static ItemLike modItem(String namespace, String path) {
        return () -> BuiltInRegistries.ITEM.get(new ResourceLocation(namespace, path));
    }

    private static ResourceKey<CreativeModeTab> modTab(String namespace, String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(namespace, path));
    }

    // Each after the one before: inserted after the same anchor, they would come out in reverse.
    private static void insertAfter(CreativeTabPlatform.Inserter output, ItemLike anchor, List<ItemStack> stacks) {
        ItemStack previous = new ItemStack(anchor);
        for (ItemStack stack : stacks) {
            output.insertAfter(previous, stack);
            previous = stack;
        }
    }
}

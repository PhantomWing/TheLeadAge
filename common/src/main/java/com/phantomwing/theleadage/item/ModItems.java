package com.phantomwing.theleadage.item;

import com.google.common.collect.Sets;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.armor.ModArmorMaterials;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.item.custom.LeadWeightItem;
import com.phantomwing.theleadage.item.custom.LeadArmorItem;
import com.phantomwing.theleadage.item.custom.LeadedGlassDoorItem;
import com.phantomwing.theleadage.item.custom.LeadedGlassPanelItem;
import com.phantomwing.theleadage.item.custom.LeadedGlassTrapdoorItem;
import com.phantomwing.theleadage.item.custom.LeadHorseArmorItem;
import com.phantomwing.theleadage.tool.ModTiers;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashSet;
import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(TheLeadAge.MOD_ID, Registries.ITEM);
    public static final LinkedHashSet<RegistrySupplier<Item>> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();

    // Declaration order = creative-tab order (mirrors The Silver Age:
    // materials -> tools -> armor -> ores -> block of lead -> decorative).

    // Materials
    public static final RegistrySupplier<Item> RAW_LEAD = register("raw_lead");
    public static final RegistrySupplier<Item> LEAD_INGOT = register("lead_ingot");
    public static final RegistrySupplier<Item> LEAD_NUGGET = register("lead_nugget");

    // Lead tools (glass-cannon stone tier — see ModTiers.LEAD).
    public static final RegistrySupplier<Item> LEAD_SHOVEL = registerShovel("lead_shovel", ModTiers.LEAD);
    public static final RegistrySupplier<Item> LEAD_PICKAXE = registerPickaxe("lead_pickaxe", ModTiers.LEAD);
    public static final RegistrySupplier<Item> LEAD_AXE = registerAxe("lead_axe", ModTiers.LEAD);
    public static final RegistrySupplier<Item> LEAD_HOE = registerHoe("lead_hoe", ModTiers.LEAD);
    public static final RegistrySupplier<Item> LEAD_SWORD = registerSword("lead_sword", ModTiers.LEAD);

    // Lead armor (very high protection, extremely low durability; weighs the wearer
    // down via the Heaviness mechanic — see LeadArmorItem).
    public static final RegistrySupplier<Item> LEAD_HELMET = registerArmor("lead_helmet", ModArmorMaterials.LEAD_ARMOR_MATERIAL, ArmorItem.Type.HELMET, 6);
    public static final RegistrySupplier<Item> LEAD_CHESTPLATE = registerArmor("lead_chestplate", ModArmorMaterials.LEAD_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, 6);
    public static final RegistrySupplier<Item> LEAD_LEGGINGS = registerArmor("lead_leggings", ModArmorMaterials.LEAD_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, 6);
    public static final RegistrySupplier<Item> LEAD_BOOTS = registerArmor("lead_boots", ModArmorMaterials.LEAD_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, 6);
    public static final RegistrySupplier<Item> LEAD_HORSE_ARMOR = register("lead_horse_armor",
            (props) -> new LeadHorseArmorItem(ModArmorMaterials.LEAD_ARMOR_MATERIAL, AnimalArmorItem.BodyType.EQUESTRIAN, false, props),
            baseItem().stacksTo(1));

    // Ores + raw storage block
    public static final RegistrySupplier<Item> LEAD_ORE = registerBlock("lead_ore", ModBlocks.LEAD_ORE);
    public static final RegistrySupplier<Item> DEEPSLATE_LEAD_ORE = registerBlock("deepslate_lead_ore", ModBlocks.DEEPSLATE_LEAD_ORE);
    public static final RegistrySupplier<Item> RAW_LEAD_BLOCK = registerBlock("raw_lead_block", ModBlocks.RAW_LEAD_BLOCK);

    // Block of Lead + decorative variants
    public static final RegistrySupplier<Item> LEAD_BLOCK = registerBlock("lead_block", ModBlocks.LEAD_BLOCK);
    public static final RegistrySupplier<Item> CUT_LEAD = registerBlock("cut_lead", ModBlocks.CUT_LEAD);
    public static final RegistrySupplier<Item> LEAD_BRICKS = registerBlock("lead_bricks", ModBlocks.LEAD_BRICKS);
    public static final RegistrySupplier<Item> LEAD_BRICK_SLAB = registerBlock("lead_brick_slab", ModBlocks.LEAD_BRICK_SLAB);
    public static final RegistrySupplier<Item> LEAD_BRICK_STAIRS = registerBlock("lead_brick_stairs", ModBlocks.LEAD_BRICK_STAIRS);
    public static final RegistrySupplier<Item> LEAD_BRICK_WALL = registerBlock("lead_brick_wall", ModBlocks.LEAD_BRICK_WALL);
    public static final RegistrySupplier<Item> CUT_LEAD_SLAB = registerBlock("cut_lead_slab", ModBlocks.CUT_LEAD_SLAB);
    public static final RegistrySupplier<Item> CUT_LEAD_STAIRS = registerBlock("cut_lead_stairs", ModBlocks.CUT_LEAD_STAIRS);
    public static final RegistrySupplier<Item> CHISELED_LEAD = registerBlock("chiseled_lead", ModBlocks.CHISELED_LEAD);
    public static final RegistrySupplier<Item> LEAD_PILLAR = registerBlock("lead_pillar", ModBlocks.LEAD_PILLAR);
    public static final RegistrySupplier<Item> LEAD_GRATE = registerBlock("lead_grate", ModBlocks.LEAD_GRATE);
    public static final RegistrySupplier<Item> LEAD_TRAPDOOR = registerBlock("lead_trapdoor", ModBlocks.LEAD_TRAPDOOR);
    public static final RegistrySupplier<Item> LEAD_DOOR = registerBlock("lead_door", ModBlocks.LEAD_DOOR);
    public static final RegistrySupplier<Item> LEAD_CHAIN = registerBlock("lead_chain", ModBlocks.LEAD_CHAIN);
    public static final RegistrySupplier<Item> LEAD_BARS = registerBlock("lead_bars", ModBlocks.LEAD_BARS);
    // One item places the standing or wall torch depending on the clicked face (like vanilla).
    public static final RegistrySupplier<Item> LEAD_TORCH = register("lead_torch",
            (props) -> new StandingAndWallBlockItem(ModBlocks.LEAD_TORCH.get(), ModBlocks.LEAD_WALL_TORCH.get(),
                    props, Direction.DOWN), baseItem());
    public static final RegistrySupplier<Item> LEAD_LANTERN = registerBlock("lead_lantern", ModBlocks.LEAD_LANTERN);

    // Leaded glass: glass reinforced with lead nuggets (drops itself when mined with a pickaxe).
    public static final RegistrySupplier<Item> LEADED_GLASS = registerBlock("leaded_glass", ModBlocks.LEADED_GLASS);
    // 16 dyed leaded glass blocks (creative-tab order: all glass blocks together).
    static {
        for (DyeColor color : DyeColor.values()) {
            registerBlock(color.getName() + "_leaded_glass", ModBlocks.STAINED_LEADED_GLASS.get(color));
        }
    }
    // Lead Weight — custom item: places normally (decorative / hangs), or air-drops as a falling
    // weapon when aimed at open space. Stackable; the three tiers (lead_weight / chipped / damaged)
    // are distinct stackable items, each placing its own block. Heavy, so they stack to only 16.
    public static final RegistrySupplier<Item> LEAD_WEIGHT = register("lead_weight",
            (props) -> new LeadWeightItem(ModBlocks.LEAD_WEIGHT.get(), props), baseItem().stacksTo(16));
    public static final RegistrySupplier<Item> CHIPPED_LEAD_WEIGHT = register("chipped_lead_weight",
            (props) -> new LeadWeightItem(ModBlocks.CHIPPED_LEAD_WEIGHT.get(), props), baseItem().stacksTo(16));
    public static final RegistrySupplier<Item> DAMAGED_LEAD_WEIGHT = register("damaged_lead_weight",
            (props) -> new LeadWeightItem(ModBlocks.DAMAGED_LEAD_WEIGHT.get(), props), baseItem().stacksTo(16));

    // Leaded glass panes — one item per came type (colours carried in the leaded_glass_config
    // component). Creative presets are added by ModCreativeModeTab.
    public static final RegistrySupplier<Item> LEADED_GLASS_PANEL = register("leaded_glass_pane",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANEL.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_SPLIT = register("leaded_glass_pane_split",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_SPLIT.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_PLUS = register("leaded_glass_pane_plus",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_PLUS.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_GRID = register("leaded_glass_pane_grid",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_GRID.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_DIAGONAL = register("leaded_glass_pane_diagonal",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_DIAGONAL.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_CROSS = register("leaded_glass_pane_cross",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_CROSS.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_DIAMOND = register("leaded_glass_pane_diamond",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_DIAMOND.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_LATTICE = register("leaded_glass_pane_lattice",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_LATTICE.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_BARS = register("leaded_glass_pane_bars",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_BARS.get(), props), baseItem());
    public static final RegistrySupplier<Item> LEADED_GLASS_PANE_DIAGONAL_BARS = register("leaded_glass_pane_diagonal_bars",
            (props) -> new LeadedGlassPanelItem(ModBlocks.LEADED_GLASS_PANE_DIAGONAL_BARS.get(), props), baseItem());

    // Leaded glass door — its top half shows the glass design carried in the component.
    public static final RegistrySupplier<Item> LEADED_GLASS_DOOR = register("leaded_glass_door",
            (props) -> new LeadedGlassDoorItem(ModBlocks.LEADED_GLASS_DOOR.get(), props), baseItem());

    // Leaded glass trapdoor — its flap shows the glass design carried in the component.
    public static final RegistrySupplier<Item> LEADED_GLASS_TRAPDOOR = register("leaded_glass_trapdoor",
            (props) -> new LeadedGlassTrapdoorItem(ModBlocks.LEADED_GLASS_TRAPDOOR.get(), props), baseItem());

    /** The pane item for a given came frame (orientations of a came type share one item). */
    public static Item paneItemFor(LeadedGlassFrame frame) {
        return switch (frame) {
            case PLAIN -> LEADED_GLASS_PANEL.get();
            case SPLIT_H, SPLIT_V -> LEADED_GLASS_PANE_SPLIT.get();
            case PLUS -> LEADED_GLASS_PANE_PLUS.get();
            case GRID -> LEADED_GLASS_PANE_GRID.get();
            case DIAGONAL_A, DIAGONAL_B -> LEADED_GLASS_PANE_DIAGONAL.get();
            case CROSS -> LEADED_GLASS_PANE_CROSS.get();
            case DIAMOND -> LEADED_GLASS_PANE_DIAMOND.get();
            case LATTICE -> LEADED_GLASS_PANE_LATTICE.get();
            case BARS_H, BARS_V -> LEADED_GLASS_PANE_BARS.get();
            case DIAGONAL_BARS_A, DIAGONAL_BARS_B -> LEADED_GLASS_PANE_DIAGONAL_BARS.get();
        };
    }

    /** True if the stack is any leaded glass pane item. */
    public static boolean isPaneItem(ItemStack stack) {
        return stack.is(LEADED_GLASS_PANEL.get()) || stack.is(LEADED_GLASS_PANE_SPLIT.get())
                || stack.is(LEADED_GLASS_PANE_PLUS.get()) || stack.is(LEADED_GLASS_PANE_GRID.get())
                || stack.is(LEADED_GLASS_PANE_DIAGONAL.get()) || stack.is(LEADED_GLASS_PANE_CROSS.get())
                || stack.is(LEADED_GLASS_PANE_DIAMOND.get()) || stack.is(LEADED_GLASS_PANE_LATTICE.get())
                || stack.is(LEADED_GLASS_PANE_BARS.get()) || stack.is(LEADED_GLASS_PANE_DIAGONAL_BARS.get());
    }

    public static Item.Properties baseItem() {
        return new Item.Properties();
    }

    // Lead swings very slowly (attack speed ~0.6/s) — the heavy, high-damage half of
    // the glass cannon. -3.4 = 4.0 base attack speed - 3.4.
    private static final float LEAD_ATTACK_SPEED = -3.4f;

    private static RegistrySupplier<Item> registerSword(String name, Tier tier) {
        return register(name, (props) -> new SwordItem(tier, props), baseItem().attributes(SwordItem.createAttributes(tier, 3, LEAD_ATTACK_SPEED)));
    }

    private static RegistrySupplier<Item> registerPickaxe(String name, Tier tier) {
        return register(name, (props) -> new PickaxeItem(tier, props), baseItem().attributes(PickaxeItem.createAttributes(tier, 1.0f, LEAD_ATTACK_SPEED)));
    }

    private static RegistrySupplier<Item> registerAxe(String name, Tier tier) {
        return register(name, (props) -> new AxeItem(tier, props), baseItem().attributes(AxeItem.createAttributes(tier, 4.5f, LEAD_ATTACK_SPEED)));
    }

    private static RegistrySupplier<Item> registerShovel(String name, Tier tier) {
        return register(name, (props) -> new ShovelItem(tier, props), baseItem().attributes(ShovelItem.createAttributes(tier, 1.5f, LEAD_ATTACK_SPEED)));
    }

    private static RegistrySupplier<Item> registerHoe(String name, Tier tier) {
        return register(name, (props) -> new HoeItem(tier, props), baseItem().attributes(HoeItem.createAttributes(tier, -2.5f, LEAD_ATTACK_SPEED)));
    }

    private static RegistrySupplier<Item> registerArmor(String name, Holder<ArmorMaterial> material, ArmorItem.Type type, int durabilityFactor) {
        return register(name, (props) -> new LeadArmorItem(material, type, props), baseItem().durability(type.getDurability(durabilityFactor)));
    }

    private static RegistrySupplier<Item> register(String name) {
        return register(name, Item::new, baseItem());
    }

    private static <T extends Block> RegistrySupplier<Item> registerBlock(String name, RegistrySupplier<T> block) {
        return register(name, (props) -> new BlockItem(block.get(), props), baseItem());
    }

    private static RegistrySupplier<Item> register(String name, Function<Item.Properties, Item> function, Item.Properties props) {
        RegistrySupplier<Item> item = ITEMS.register(name, () -> function.apply(props));
        CREATIVE_TAB_ITEMS.add(item);
        return item;
    }

    public static void register() {
        ITEMS.register();
    }
}

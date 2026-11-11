package com.phantomwing.theleadage.ui;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.world.item.Item;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MOD_TAB =
            CREATIVE_MODE_TABS.register(TheLeadAge.MOD_ID + "_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(ModItems.LEAD_INGOT.get()))
                    .title(Component.translatable("item_group." + TheLeadAge.MOD_ID))
                    // A category tab with nothing to show isn't drawn, which is how the option hides it.
                    .displayItems((parameters, output) -> {
                        if (!CommonConfig.creativeTab()) {
                            return;
                        }
                        ModItems.CREATIVE_TAB_ITEMS.forEach(item -> {
                            if (isShown(item.get())) {
                                output.acceptAll(stacksOf(item));
                            }
                        });
                    })
                    .build());

    /**
     * The stacks the tabs show for {@code item}. Pane items carry a design component: plain shows clear and every dye,
     * the other came types a single all-clear preset each orientation.
     */
    public static List<ItemStack> stacksOf(RegistrySupplier<Item> item) {
        List<ItemStack> stacks = new ArrayList<>();
        if (item == ModItems.LEADED_GLASS_PANEL) {
            stacks.add(pane(item, LeadedGlassFrame.PLAIN, List.of(LeadedGlassConfig.CLEAR)));
            for (DyeColor color : DyeColor.values()) {
                stacks.add(pane(item, LeadedGlassFrame.PLAIN, List.of(color.getId())));
            }
        } else if (item == ModItems.LEADED_GLASS_PANE_SPLIT) {
            stacks.add(pane(item, LeadedGlassFrame.SPLIT_H,
                    List.of(LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
            stacks.add(pane(item, LeadedGlassFrame.SPLIT_V,
                    List.of(LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
        } else if (item == ModItems.LEADED_GLASS_PANE_PLUS) {
            stacks.add(pane(item, LeadedGlassFrame.PLUS, List.of(
                    LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR,
                    LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
        } else if (item == ModItems.LEADED_GLASS_PANE_GRID) {
            stacks.add(pane(item, LeadedGlassFrame.GRID,
                    java.util.Collections.nCopies(9, LeadedGlassConfig.CLEAR)));
        } else if (item == ModItems.LEADED_GLASS_PANE_DIAGONAL) {
            stacks.add(pane(item, LeadedGlassFrame.DIAGONAL_A,
                    List.of(LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
            stacks.add(pane(item, LeadedGlassFrame.DIAGONAL_B,
                    List.of(LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
        } else if (item == ModItems.LEADED_GLASS_PANE_CROSS) {
            stacks.add(pane(item, LeadedGlassFrame.CROSS, List.of(
                    LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR,
                    LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
        } else if (item == ModItems.LEADED_GLASS_PANE_DIAMOND) {
            stacks.add(pane(item, LeadedGlassFrame.DIAMOND,
                    java.util.Collections.nCopies(5, LeadedGlassConfig.CLEAR)));
        } else if (item == ModItems.LEADED_GLASS_PANE_DIAGONAL_BARS) {
            stacks.add(pane(item, LeadedGlassFrame.DIAGONAL_BARS_A,
                    java.util.Collections.nCopies(4, LeadedGlassConfig.CLEAR)));
            stacks.add(pane(item, LeadedGlassFrame.DIAGONAL_BARS_B,
                    java.util.Collections.nCopies(4, LeadedGlassConfig.CLEAR)));
        } else if (item == ModItems.LEADED_GLASS_PANE_BARS) {
            stacks.add(pane(item, LeadedGlassFrame.BARS_H,
                    java.util.Collections.nCopies(3, LeadedGlassConfig.CLEAR)));
            stacks.add(pane(item, LeadedGlassFrame.BARS_V,
                    java.util.Collections.nCopies(3, LeadedGlassConfig.CLEAR)));
        } else if (item == ModItems.LEADED_GLASS_PANE_LATTICE) {
            stacks.add(pane(item, LeadedGlassFrame.LATTICE,
                    java.util.Collections.nCopies(12, LeadedGlassConfig.CLEAR)));
        } else {
            stacks.add(new ItemStack(item.get()));
        }
        return stacks;
    }

    /**
     * Whether the creative tabs list {@code item}: one of the mod's tab items, but not lead gear while
     * {@code enable_lead_gear} is off, as it can't be got then.
     */
    public static boolean isShown(Item item) {
        if (ModItems.CREATIVE_TAB_ITEMS.stream().noneMatch(entry -> entry.get() == item)) {
            return false;
        }
        return !ModItems.isLeadGear(item) || CommonConfig.leadGear();
    }

    private static ItemStack pane(RegistrySupplier<net.minecraft.world.item.Item> item, LeadedGlassFrame frame, List<Integer> colors) {
        ItemStack stack = new ItemStack(item.get());
        stack.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), new LeadedGlassConfig(frame, colors));
        return stack;
    }

    public static void register() {
        CREATIVE_MODE_TABS.register();
    }
}

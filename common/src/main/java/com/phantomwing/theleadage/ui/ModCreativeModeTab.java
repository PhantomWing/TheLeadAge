package com.phantomwing.theleadage.ui;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MOD_TAB =
            CREATIVE_MODE_TABS.register(TheLeadAge.MOD_ID + "_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(ModItems.LEAD_INGOT.get()))
                    .title(Component.translatable("item_group." + TheLeadAge.MOD_ID))
                    .displayItems((parameters, output) ->
                            ModItems.CREATIVE_TAB_ITEMS.forEach((item) -> {
                                // Pane items carry a design component. Plain shows clear + every dye;
                                // other came types just show a single all-clear preset.
                                if (item == ModItems.LEADED_GLASS_PANEL) {
                                    output.accept(pane(item, LeadedGlassFrame.PLAIN, List.of(LeadedGlassConfig.CLEAR)));
                                    for (DyeColor color : DyeColor.values()) {
                                        output.accept(pane(item, LeadedGlassFrame.PLAIN, List.of(color.getId())));
                                    }
                                } else if (item == ModItems.LEADED_GLASS_PANE_SPLIT) {
                                    output.accept(pane(item, LeadedGlassFrame.SPLIT_H,
                                            List.of(LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
                                    output.accept(pane(item, LeadedGlassFrame.SPLIT_V,
                                            List.of(LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
                                } else if (item == ModItems.LEADED_GLASS_PANE_PLUS) {
                                    output.accept(pane(item, LeadedGlassFrame.PLUS, List.of(
                                            LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR,
                                            LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
                                } else if (item == ModItems.LEADED_GLASS_PANE_GRID) {
                                    output.accept(pane(item, LeadedGlassFrame.GRID,
                                            java.util.Collections.nCopies(9, LeadedGlassConfig.CLEAR)));
                                } else if (item == ModItems.LEADED_GLASS_PANE_DIAGONAL) {
                                    output.accept(pane(item, LeadedGlassFrame.DIAGONAL_A,
                                            List.of(LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
                                    output.accept(pane(item, LeadedGlassFrame.DIAGONAL_B,
                                            List.of(LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
                                } else if (item == ModItems.LEADED_GLASS_PANE_CROSS) {
                                    output.accept(pane(item, LeadedGlassFrame.CROSS, List.of(
                                            LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR,
                                            LeadedGlassConfig.CLEAR, LeadedGlassConfig.CLEAR)));
                                } else if (item == ModItems.LEADED_GLASS_PANE_DIAMOND) {
                                    output.accept(pane(item, LeadedGlassFrame.DIAMOND,
                                            java.util.Collections.nCopies(5, LeadedGlassConfig.CLEAR)));
                                } else if (item == ModItems.LEADED_GLASS_PANE_BARS) {
                                    output.accept(pane(item, LeadedGlassFrame.BARS_H,
                                            java.util.Collections.nCopies(3, LeadedGlassConfig.CLEAR)));
                                    output.accept(pane(item, LeadedGlassFrame.BARS_V,
                                            java.util.Collections.nCopies(3, LeadedGlassConfig.CLEAR)));
                                } else if (item == ModItems.LEADED_GLASS_PANE_LATTICE) {
                                    output.accept(pane(item, LeadedGlassFrame.LATTICE,
                                            java.util.Collections.nCopies(12, LeadedGlassConfig.CLEAR)));
                                } else {
                                    output.accept(item.get());
                                }
                            }))
                    .build());

    private static ItemStack pane(RegistrySupplier<net.minecraft.world.item.Item> item, LeadedGlassFrame frame, List<Integer> colors) {
        ItemStack stack = new ItemStack(item.get());
        stack.set(ModDataComponents.LEADED_GLASS_CONFIG.get(), new LeadedGlassConfig(frame, colors));
        return stack;
    }

    public static void register() {
        CREATIVE_MODE_TABS.register();
    }
}

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
                                // The leaded glass pane is one item carrying a design component;
                                // show a clear preset plus one per dye colour instead of the bare item.
                                if (item == ModItems.LEADED_GLASS_PANEL) {
                                    output.accept(plainPane(LeadedGlassConfig.CLEAR));
                                    for (DyeColor color : DyeColor.values()) {
                                        output.accept(plainPane(color.getId()));
                                    }
                                } else {
                                    output.accept(item.get());
                                }
                            }))
                    .build());

    private static ItemStack plainPane(int colorId) {
        ItemStack stack = new ItemStack(ModItems.LEADED_GLASS_PANEL.get());
        stack.set(ModDataComponents.LEADED_GLASS_CONFIG.get(),
                new LeadedGlassConfig(LeadedGlassFrame.PLAIN, List.of(colorId)));
        return stack;
    }

    public static void register() {
        CREATIVE_MODE_TABS.register();
    }
}

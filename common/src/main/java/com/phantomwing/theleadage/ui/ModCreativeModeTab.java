package com.phantomwing.theleadage.ui;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MOD_TAB =
            CREATIVE_MODE_TABS.register(TheLeadAge.MOD_ID + "_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(ModItems.LEAD_INGOT.get()))
                    .title(Component.translatable("item_group." + TheLeadAge.MOD_ID))
                    .displayItems((parameters, output) ->
                            ModItems.CREATIVE_TAB_ITEMS.forEach((item) -> output.accept(item.get())))
                    .build());

    public static void register() {
        CREATIVE_MODE_TABS.register();
    }
}

package com.phantomwing.theleadage.compat;

import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.platform.CommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The info pages JEI and EMI show for lead's blocks that no recipe explains: who presses a Lead Pressure Plate, and the
 * creepers and pillagers keeping away from lead light. A page about something the config turns off is left out, as
 * The Silver Age's are.
 *
 * @param name   the page's name, its text {@code theleadage.info.<name>}
 * @param stacks the stacks it is shown for
 */
public record LeadInfo(String name, List<ItemStack> stacks) {
    public Component text() {
        return Component.translatable("theleadage.info." + name);
    }

    public static List<LeadInfo> all() {
        List<LeadInfo> pages = new ArrayList<>();
        pages.add(new LeadInfo("lead_pressure_plate", List.of(new ItemStack(ModItems.LEAD_PRESSURE_PLATE.get()))));
        if (CommonConfig.creepersAvoidLeadFumes() && CommonConfig.pillagersAvoidLeadFumes()) {
            pages.add(new LeadInfo("lead_light", List.of(new ItemStack(ModItems.LEAD_TORCH.get()),
                    new ItemStack(ModItems.LEAD_LANTERN.get()))));
        }
        return pages;
    }
}

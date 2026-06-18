package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.utils.ItemUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Generates The Lead Age's advancement tab — a small "obtain" tree under a root
 * (mirrors The Silver Age's setup). Titles/descriptions are translation keys
 * {@code theleadage.advancement.<id>[.description]}, resolved from the lang files.
 */
public class ModAdvancementProvider implements AdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(HolderLookup.@NotNull Provider provider, @NotNull Consumer<AdvancementHolder> consumer, @NotNull ExistingFileHelper existingFileHelper) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModItems.RAW_LEAD.get(),
                        title("root"), description("root"),
                        ResourceLocation.parse("theleadage:textures/block/cut_lead.png"),
                        AdvancementType.TASK, false, false, false)
                .addCriterion("root", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}))
                .save(consumer, id("root"));

        AdvancementHolder ingot = obtain(consumer, root, ModItems.LEAD_INGOT.get());
        obtain(consumer, ingot, ModItems.LEAD_CHESTPLATE.get());
        obtain(consumer, ingot, ModItems.LEAD_HORSE_ARMOR.get());
    }

    private static AdvancementHolder obtain(Consumer<AdvancementHolder> consumer, AdvancementHolder parent, ItemLike item) {
        String name = ItemUtils.getName(item);
        return Advancement.Builder.advancement().parent(parent)
                .display(item, title("obtain_" + name), description("obtain_" + name),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(item.asItem()))
                .save(consumer, id("obtain_" + name));
    }

    private static MutableComponent title(String key) {
        return Component.translatable(TheLeadAge.MOD_ID + ".advancement." + key);
    }

    private static MutableComponent description(String key) {
        return Component.translatable(TheLeadAge.MOD_ID + ".advancement." + key + ".description");
    }

    private static String id(String name) {
        return TheLeadAge.MOD_ID + ":main/" + name;
    }
}

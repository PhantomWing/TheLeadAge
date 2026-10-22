package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.advancement.ModTriggers;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.entity.ModEntities;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.tags.ModTags;
import com.phantomwing.theleadage.utils.ItemUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.data.advancements.AdvancementSubProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Generates The Lead Age's advancement tab — a small "obtain" tree under a root
 * (mirrors The Silver Age's setup). Titles/descriptions are translation keys
 * {@code theleadage.advancement.<id>[.description]}, resolved from the lang files.
 */
public class ModAdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NotNull Provider provider, @NotNull Consumer<AdvancementHolder> consumer) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModItems.RAW_LEAD.get(),
                        title("root"), description("root"),
                        ResourceLocation.parse("theleadage:textures/block/cut_lead.png"),
                        AdvancementType.TASK, false, false, false)
                .addCriterion("root", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}))
                .save(consumer, id("root"));

        AdvancementHolder ingot = obtain(consumer, root, ModItems.LEAD_INGOT.get());
        crushEnemy(provider, consumer, ingot);
        leadedLights(provider, consumer, ingot);
        poisonedWater(consumer, root);
    }

    /** Swim in water raw lead poisons, and breathe it. */
    private static void poisonedWater(Consumer<AdvancementHolder> consumer, AdvancementHolder parent) {
        Advancement.Builder.advancement().parent(parent)
                .display(ModBlocks.RAW_LEAD_BLOCK.get(), title("poisoned_water"), description("poisoned_water"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("poisoned_water", ModTriggers.SWAM_IN_POISONED_WATER.get()
                        .createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty())))
                .save(consumer, id("poisoned_water"));
    }

    /** Obtain any full leaded glass block, clear or stained in any colour, a dye mod's too: the tag holds them all. */
    private static void leadedLights(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer,
                                     AdvancementHolder parent) {
        Advancement.Builder.advancement().parent(parent)
                .display(ModBlocks.LEADED_GLASS.get(), title("leaded_lights"), description("leaded_lights"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_leaded_glass", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(provider.lookupOrThrow(Registries.ITEM),
                                ModTags.Items.LEADED_GLASS_BLOCKS)))
                .save(consumer, id("leaded_lights"));
    }

    /** A challenge: kill an entity with a falling Lead Weight (its damage source's direct entity). */
    private static void crushEnemy(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer, AdvancementHolder parent) {
        DamageSourcePredicate source = DamageSourcePredicate.Builder.damageType()
                .direct(EntityPredicate.Builder.entity().entityType(
                        EntityTypePredicate.of(provider.lookupOrThrow(Registries.ENTITY_TYPE), ModEntities.LEAD_WEIGHT.get())))
                .build();
        Advancement.Builder.advancement().parent(parent)
                .display(ModBlocks.LEAD_WEIGHT.get(), title("crush_enemy"), description("crush_enemy"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("crush_enemy",
                        KilledTrigger.TriggerInstance.playerKilledEntity(Optional.empty(), Optional.of(source)))
                .save(consumer, id("crush_enemy"));
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

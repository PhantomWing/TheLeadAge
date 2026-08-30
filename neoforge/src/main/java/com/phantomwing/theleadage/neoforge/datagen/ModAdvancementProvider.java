package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.ModBlocks;
import com.phantomwing.theleadage.entity.ModEntities;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.utils.ItemUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.data.advancements.AdvancementSubProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
                        ResourceLocation.parse("theleadage:block/cut_lead"),
                        AdvancementType.TASK, false, false, false)
                .addCriterion("root", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}))
                .save(consumer, id("root"));

        AdvancementHolder ingot = obtain(consumer, root, ModItems.LEAD_INGOT.get());
        crushEnemy(provider, consumer, ingot);
        leadedLights(consumer, ingot);
    }

    /** Obtain any full leaded glass block (clear or stained). Any one of them satisfies it (OR requirements). */
    private static void leadedLights(Consumer<AdvancementHolder> consumer, AdvancementHolder parent) {
        Advancement.Builder builder = Advancement.Builder.advancement().parent(parent)
                .display(ModBlocks.LEADED_GLASS.get(), title("leaded_lights"), description("leaded_lights"),
                        null, AdvancementType.TASK, true, true, false);
        List<ItemLike> glass = new ArrayList<>();
        glass.add(ModBlocks.LEADED_GLASS.get());
        for (DyeColor color : DyeColor.values()) {
            glass.add(ModBlocks.STAINED_LEADED_GLASS.get(color).get());
        }
        List<String> criteria = new ArrayList<>();
        for (ItemLike g : glass) {
            String crit = ItemUtils.getName(g);
            builder.addCriterion(crit, InventoryChangeTrigger.TriggerInstance.hasItems(g));
            criteria.add(crit);
        }
        builder.requirements(AdvancementRequirements.anyOf(criteria)).save(consumer, id("leaded_lights"));
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

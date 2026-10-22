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
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.KilledTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

/**
 * Generates The Lead Age's advancement tab — a small "obtain" tree under a root
 * (mirrors The Silver Age's setup). Titles/descriptions are translation keys
 * {@code theleadage.advancement.<id>[.description]}, resolved from the lang files.
 */
public class ModAdvancementProvider extends AdvancementSubProvider {
    public ModAdvancementProvider(BootstrapContext<Advancement> output) {
        super(output);
    }

    @Override
    public void generate() {
        // 26.3 dropped the background argument from display(); it lives on DisplayInfo as a
        // ClientAsset id, NOT a texture path, so it expands to "textures/<id>.png" itself.
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStackTemplate(ModItems.RAW_LEAD.get()),
                        title("root"), description("root"),
                        Optional.of(new ClientAsset.ResourceTexture(
                                Identifier.parse("theleadage:block/cut_lead"))),
                        AdvancementType.TASK, false, false, false))
                .addCriterion("root", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}))
                .save(this.output, id("root"));

        AdvancementHolder ingot = obtain(root, ModItems.LEAD_INGOT.get());
        crushEnemy(ingot);
        leadedLights(ingot);
        poisonedWater(root);
    }

    /** Swim in water raw lead poisons, and breathe it. */
    private void poisonedWater(AdvancementHolder parent) {
        Advancement.Builder.advancement().parent(parent)
                .display(ModBlocks.RAW_LEAD_BLOCK.get().asItem(), title("poisoned_water"), description("poisoned_water"),
                        AdvancementType.TASK, true, true, false)
                .addCriterion("poisoned_water", ModTriggers.SWAM_IN_POISONED_WATER.get()
                        .createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty())))
                .save(this.output, id("poisoned_water"));
    }

    /** Obtain any full leaded glass block, clear or stained in any colour, a dye mod's too: the tag holds them all. */
    private void leadedLights(AdvancementHolder parent) {
        Advancement.Builder.advancement().parent(parent)
                .display(ModBlocks.LEADED_GLASS.get().asItem(), title("leaded_lights"), description("leaded_lights"),
                        AdvancementType.TASK, true, true, false)
                .addCriterion("has_leaded_glass", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(this.output.lookup(Registries.ITEM),
                                ModTags.Items.LEADED_GLASS_BLOCKS)))
                .save(this.output, id("leaded_lights"));
    }

    /** A challenge: kill an entity with a falling Lead Weight (its damage source's direct entity). */
    private void crushEnemy(AdvancementHolder parent) {
        DamageSourcePredicate source = DamageSourcePredicate.Builder.damageType()
                .direct(EntityPredicate.Builder.entity().entityType(
                        EntityTypePredicate.of(this.output.lookup(Registries.ENTITY_TYPE), ModEntities.LEAD_WEIGHT.get())))
                .build();
        Advancement.Builder.advancement().parent(parent)
                .display(ModBlocks.LEAD_WEIGHT.get().asItem(), title("crush_enemy"), description("crush_enemy"),
                        AdvancementType.TASK, true, true, false)
                .addCriterion("crush_enemy",
                        KilledTrigger.TriggerInstance.playerKilledEntity(Optional.empty(), Optional.of(source)))
                .save(this.output, id("crush_enemy"));
    }

    private AdvancementHolder obtain(AdvancementHolder parent, ItemLike item) {
        String name = ItemUtils.getName(item);
        return Advancement.Builder.advancement().parent(parent)
                .display(item.asItem(), title("obtain_" + name), description("obtain_" + name),
                        AdvancementType.TASK, true, true, false)
                .addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(item.asItem()))
                .save(this.output, id("obtain_" + name));
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

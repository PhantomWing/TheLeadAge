package com.phantomwing.theleadage.armor;

import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.platform.CommonConfig;
import com.phantomwing.theleadage.platform.MonsterArmorPlatform;
import com.phantomwing.theleadage.tags.ModTags;
import com.phantomwing.theleadage.utils.ItemUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Replaces naturally-spawned mobs' iron armor and iron tools with lead ones (low chance).
 *
 * <p>The transmute LOGIC is loader-agnostic and lives here. The actual event
 * subscription is loader-specific (NeoForge keeps its exact
 * {@code EntityJoinLevelEvent} + {@code loadedFromDisk()} gating; Fabric uses a
 * {@code PersistentEntitySectionManager} mixin that carries the same
 * loaded-from-storage flag) and is bridged through {@link MonsterArmorPlatform}.</p>
 *
 * <p>Whatever gave the mob its iron - its own spawn, a raid, a trial spawner's equipment
 * table - has done so before it joins the level, so all of them are covered here.</p>
 */
public final class MonsterArmorHandler {
    private static final float REPLACE_CHANCE = 0.10f; // 10% chance per piece / full set / tool
    /**
     * The tag a mob gets once its gear has had its chance, which it keeps through a portal: a mob crossing one joins the
     * other level as a new entity. A map maker summoning a mob with it keeps its iron as given.
     */
    public static final String ROLLED_TAG = "theleadage.rolled_lead_gear";

    private MonsterArmorHandler() {
    }

    /** Wires the loader-specific entity-join hook. Called from {@code TheLeadAgeCommon.init()}. */
    public static void register() {
        MonsterArmorPlatform.registerMobSpawnHandler();
    }

    /**
     * Loader-agnostic equip logic. The caller is responsible for the spawn gating
     * (client-side / loaded-from-disk) so NeoForge can preserve its precise
     * {@code EntityJoinLevelEvent.loadedFromDisk()} semantics.
     *
     * @param entity         the entity that just joined the level
     * @param level          the level it joined
     * @param loadedFromDisk whether the entity was loaded from disk (NeoForge: from
     *                       the event; Fabric: from the section-manager mixin)
     */
    public static void tryEquipLeadGear(Entity entity, Level level, boolean loadedFromDisk) {
        // Only apply when the mob is spawning naturally in the world, not when loaded from disk or on the client side.
        if (level.isClientSide() || loadedFromDisk || !(entity instanceof Mob mob) || !CommonConfig.mobLeadEquipment()) {
            return;
        }
        // 26.1 removed EntityType.is(TagKey); tag membership goes through the built-in holder.
        boolean armor = entity.getType().builtInRegistryHolder().is(ModTags.EntityTypes.CAN_WEAR_LEAD_ARMOR);
        boolean tools = entity.getType().builtInRegistryHolder().is(ModTags.EntityTypes.CAN_HOLD_LEAD_TOOLS);
        if (!armor && !tools || !mob.addTag(ROLLED_TAG)) {
            return; // not one that wears lead, or its gear had its chance before a portal took it to this level
        }
        if (armor) {
            replaceArmor(mob);
        }
        if (tools) {
            replaceTool(mob);
        }
    }

    private static void replaceArmor(Mob mob) {
        RandomSource random = mob.getRandom();

        ItemStack helmet = mob.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = mob.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = mob.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = mob.getItemBySlot(EquipmentSlot.FEET);

        boolean hasIronHelmet = helmet.is(Items.IRON_HELMET);
        boolean hasIronChestplate = chestplate.is(Items.IRON_CHESTPLATE);
        boolean hasIronLeggings = leggings.is(Items.IRON_LEGGINGS);
        boolean hasIronBoots = boots.is(Items.IRON_BOOTS);
        boolean hasFullSet = hasIronHelmet && hasIronChestplate && hasIronLeggings && hasIronBoots;

        // If wearing a full set of Iron armor, try to replace the entire set with Lead armor.
        if (hasFullSet) {
            if (random.nextFloat() < REPLACE_CHANCE) {
                mob.setItemSlot(EquipmentSlot.HEAD, ItemUtils.tryTransmuteStack(helmet, ModItems.LEAD_HELMET.get()));
                mob.setItemSlot(EquipmentSlot.CHEST, ItemUtils.tryTransmuteStack(chestplate, ModItems.LEAD_CHESTPLATE.get()));
                mob.setItemSlot(EquipmentSlot.LEGS, ItemUtils.tryTransmuteStack(leggings, ModItems.LEAD_LEGGINGS.get()));
                mob.setItemSlot(EquipmentSlot.FEET, ItemUtils.tryTransmuteStack(boots, ModItems.LEAD_BOOTS.get()));
            }
        } else {
            // If mob is wearing any separate pieces of Iron armor, try to individually replace with a piece of Lead armor.
            if (hasIronHelmet && random.nextFloat() < REPLACE_CHANCE) {
                mob.setItemSlot(EquipmentSlot.HEAD, ItemUtils.tryTransmuteStack(helmet, ModItems.LEAD_HELMET.get()));
            }

            if (hasIronChestplate && random.nextFloat() < REPLACE_CHANCE) {
                mob.setItemSlot(EquipmentSlot.CHEST, ItemUtils.tryTransmuteStack(chestplate, ModItems.LEAD_CHESTPLATE.get()));
            }

            if (hasIronLeggings && random.nextFloat() < REPLACE_CHANCE) {
                mob.setItemSlot(EquipmentSlot.LEGS, ItemUtils.tryTransmuteStack(leggings, ModItems.LEAD_LEGGINGS.get()));
            }

            if (hasIronBoots && random.nextFloat() < REPLACE_CHANCE) {
                mob.setItemSlot(EquipmentSlot.FEET, ItemUtils.tryTransmuteStack(boots, ModItems.LEAD_BOOTS.get()));
            }
        }
    }

    // A zombie's iron sword, shovel or spear, a vindicator's iron axe. Enchantments, as a raid's axe has, carry over.
    private static void replaceTool(Mob mob) {
        ItemStack held = mob.getItemBySlot(EquipmentSlot.MAINHAND);
        Item lead = leadToolFor(held);
        if (lead != null && mob.getRandom().nextFloat() < REPLACE_CHANCE) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ItemUtils.tryTransmuteStack(held, lead));
        }
    }

    @Nullable
    private static Item leadToolFor(ItemStack held) {
        if (held.is(Items.IRON_SWORD)) {
            return ModItems.LEAD_SWORD.get();
        }
        if (held.is(Items.IRON_AXE)) {
            return ModItems.LEAD_AXE.get();
        }
        if (held.is(Items.IRON_SHOVEL)) {
            return ModItems.LEAD_SHOVEL.get();
        }
        if (held.is(Items.IRON_SPEAR)) {
            return ModItems.LEAD_SPEAR.get();
        }
        return null;
    }
}

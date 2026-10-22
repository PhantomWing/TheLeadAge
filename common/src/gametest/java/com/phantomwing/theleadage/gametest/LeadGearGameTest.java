package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.armor.ModTrimMaterials;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.item.custom.LeadArmorItem;
import com.phantomwing.theleadage.utils.ItemUtils;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

import static com.phantomwing.theleadage.gametest.LeadOreGameTest.FIND_TICKS;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.SPAWN_TICKS;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.clearSickness;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.whenFumesFind;
import static com.phantomwing.theleadage.gametest.LeadOreGameTest.whenSpawnLoaded;
import static com.phantomwing.theleadage.gametest.TestCompat.fail;
import static com.phantomwing.theleadage.gametest.TestCompat.moveTo;
import static com.phantomwing.theleadage.gametest.TestCompat.reloaded;
import static com.phantomwing.theleadage.gametest.TestCompat.trim;
import static com.phantomwing.theleadage.gametest.TestCompat.trimPalette;

/**
 * Game tests for lead gear: smelting it back into nuggets, the fumes a piece gives off as it breaks, the weight of its
 * armour on a mob, and the chests it turns up in. Listed in {@link GameTests}.
 */
public final class LeadGearGameTest {
    /** How many times a chest's loot is rolled: the swaps are rare, one in ten of a roll with the iron item. */
    private static final int ROLLS = 500;

    private LeadGearGameTest() {
    }

    private static List<Item> gear() {
        return List.of(ModItems.LEAD_PICKAXE, ModItems.LEAD_SHOVEL, ModItems.LEAD_AXE, ModItems.LEAD_HOE, ModItems.LEAD_SWORD,
                        ModItems.LEAD_HELMET, ModItems.LEAD_CHESTPLATE, ModItems.LEAD_LEGGINGS, ModItems.LEAD_BOOTS,
                        ModItems.LEAD_HORSE_ARMOR).stream()
                .map(RegistrySupplier::get).toList();
    }

    /** Every piece of lead gear smelts and blasts into a Lead Nugget. */
    public static void leadGearSmeltsIntoNuggets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (RecipeType<? extends AbstractCookingRecipe> type : List.of(RecipeType.SMELTING, RecipeType.BLASTING)) {
            for (Item item : gear()) {
                SingleRecipeInput input = new SingleRecipeInput(new ItemStack(item));
                ItemStack result = level.getServer().getRecipeManager().getRecipeFor(type, input, level)
                        .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                        .orElse(ItemStack.EMPTY);
                if (!result.is(ModItems.LEAD_NUGGET.get())) {
                    fail(helper, BuiltInRegistries.ITEM.getKey(item) + " in " + type + " gives " + result + ", not a Lead Nugget");
                    return;
                }
            }
        }
        helper.succeed();
    }

    /** A piece of lead armor that breaks on its wearer gives off fumes that dose them. */
    public static void brokenLeadArmorReleasesFumes(GameTestHelper helper) {
        whenSpawnLoaded(helper, SPAWN_TICKS, () -> {
            BlockPos stand = new BlockPos(1, 2, 1);
            for (int y = 2; y <= 4; y++) {
                helper.setBlock(new BlockPos(1, y, 1), Blocks.AIR);
            }
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            moveTo(player, Vec3.atBottomCenterOf(helper.absolutePos(stand)));
            whenFumesFind(helper, player, stand, FIND_TICKS, () -> {
                clearSickness(player);
                ItemStack chestplate = new ItemStack(ModItems.LEAD_CHESTPLATE.get());
                chestplate.setDamageValue(chestplate.getMaxDamage() - 1);
                player.setItemSlot(EquipmentSlot.CHEST, chestplate);
                player.getItemBySlot(EquipmentSlot.CHEST).hurtAndBreak(1, player, EquipmentSlot.CHEST);
                if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                    fail(helper, "The lead chestplate didn't break - the test's setup failed, not the mod");
                } else if (player.hasEffect(MobEffects.HUNGER)) {
                    helper.succeed();
                } else {
                    fail(helper, "A lead chestplate broke on its wearer without dosing them");
                }
            });
        });
    }

    /**
     * Lead armour slows a mob as it slows a player: one that put it on, and one loaded from a save wearing it, whose
     * slowness, never saved, comes back on its first tick. Taking it off takes the slowness away.
     */
    public static void leadArmorWeighsOnMobs(GameTestHelper helper) {
        ResourceLocation slowness = TheLeadAge.resourceLocation("lead_heaviness_slowness");
        ServerLevel level = helper.getLevel();
        Zombie wearing = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(0, 2, 0));
        wearing.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.LEAD_CHESTPLATE.get()));
        Zombie loaded = reloaded(wearing);
        loaded.setUUID(UUID.randomUUID());
        level.addFreshEntity(loaded);
        helper.runAfterDelay(3, () -> {
            if (!wearing.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(slowness)) {
                fail(helper, "A zombie that put on a lead chestplate isn't slowed by it");
                return;
            }
            if (!loaded.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(slowness)) {
                fail(helper, "A zombie loaded wearing a lead chestplate isn't slowed by it");
                return;
            }
            wearing.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            helper.runAfterDelay(3, () -> {
                if (wearing.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(slowness)) {
                    fail(helper, "A zombie that took off its lead chestplate is still slowed");
                } else {
                    helper.succeed();
                }
            });
        });
    }

    /** Lead gear turns up in a village weaponsmith's chest in place of iron gear. */
    public static void leadGearTurnsUpInChests(GameTestHelper helper) {
        int found = rollFor(helper, "chests/village/village_weaponsmith", gear());
        if (found > 0) {
            helper.succeed();
        } else {
            fail(helper, "No lead gear in " + ROLLS + " village weaponsmith chests");
        }
    }

    /** A trial chamber vault's reward can be a Lead Ingot in place of an iron one. */
    public static void trialVaultsGiveLeadIngots(GameTestHelper helper) {
        int found = rollFor(helper, "chests/trial_chambers/reward", List.of(ModItems.LEAD_INGOT.get()));
        if (found > 0) {
            helper.succeed();
        } else {
            fail(helper, "No Lead Ingot in " + ROLLS + " trial chamber vault rewards");
        }
    }

    /** How many of {@value #ROLLS} rolls of a vanilla loot table hold one of {@code wanted}. */
    private static int rollFor(GameTestHelper helper, String table, List<Item> wanted) {
        ServerLevel level = helper.getLevel();
        LootTable loot = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace(table)));
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        int found = 0;
        for (int i = 0; i < ROLLS; i++) {
            if (loot.getRandomItems(params).stream().anyMatch(stack -> wanted.contains(stack.getItem()))) {
                found++;
            }
        }
        return found;
    }

    /**
     * Iron gear swapped for lead keeps how worn it was as a share of its durability, its name and its trim: loot wears
     * iron by a share of its 250, which as damage would break a lead tool on first use.
     */
    public static void swappedGearKeepsItsWear(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.setDamageValue(sword.getMaxDamage() / 2);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("Old Faithful"));
        ItemStack lead = ItemUtils.tryTransmuteStack(sword, ModItems.LEAD_SWORD.get());
        if (lead.getDamageValue() != Math.round(lead.getMaxDamage() / 2.0F) || !lead.has(DataComponents.CUSTOM_NAME)) {
            fail(helper, "A half-worn named iron sword became a lead one worn " + lead.getDamageValue() + " of "
                    + lead.getMaxDamage() + (lead.has(DataComponents.CUSTOM_NAME) ? "" : ", its name lost"));
            return;
        }
        ItemStack nearlyBroken = new ItemStack(Items.IRON_AXE);
        nearlyBroken.setDamageValue(nearlyBroken.getMaxDamage() - 1);
        ItemStack leadAxe = ItemUtils.tryTransmuteStack(nearlyBroken, ModItems.LEAD_AXE.get());
        if (leadAxe.getDamageValue() != leadAxe.getMaxDamage() - 1) {
            fail(helper, "An iron axe with one use left became a lead one worn " + leadAxe.getDamageValue() + " of " + leadAxe.getMaxDamage());
            return;
        }
        ServerLevel level = helper.getLevel();
        ArmorTrim trim = trim(level, TrimMaterials.COPPER, TrimPatterns.FLOW);
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        helmet.set(DataComponents.TRIM, trim);
        if (!trim.equals(ItemUtils.tryTransmuteStack(helmet, ModItems.LEAD_HELMET.get()).get(DataComponents.TRIM))) {
            fail(helper, "A trimmed iron helmet became an untrimmed lead one");
            return;
        }
        helper.succeed();
    }

    /** The lead trim on worn lead armour takes its darker palette, which only the registry's holder of the material finds. */
    public static void leadTrimDarkensOnLeadArmor(GameTestHelper helper) {
        String palette = trimPalette(helper.getLevel(), ModTrimMaterials.LEAD, ModItems.LEAD_CHESTPLATE.get());
        if ("lead_darker".equals(palette)) {
            helper.succeed();
        } else {
            fail(helper, "A lead trim on lead armour draws with the palette " + palette + ", not lead_darker");
        }
    }

    /** Lead armour sets its slowness once, not anew every tick: each set sends the wearer's speed to everyone near. */
    public static void leadArmorSetsItsSlownessOnce(GameTestHelper helper) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(0, 2, 0));
        zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.LEAD_CHESTPLATE.get()));
        ResourceLocation slowness = TheLeadAge.resourceLocation("lead_heaviness_slowness");
        LeadArmorItem.refreshHeaviness(zombie);
        AttributeModifier first = zombie.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(slowness);
        LeadArmorItem.refreshHeaviness(zombie);
        if (first != null && first == zombie.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(slowness)) {
            helper.succeed();
        } else {
            fail(helper, "Lead armour's slowness was set again though nothing changed");
        }
    }

    /** The armorer buys lead ingots in a world with the Villager Trade Rebalance experiment, as in any other. */
    public static void armorersBuyLeadInRebalancedWorlds(GameTestHelper helper) {
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(0, 2, 0));
        RandomSource random = RandomSource.create(1L);
        VillagerTrades.ItemListing[] apprentice = VillagerTrades.EXPERIMENTAL_TRADES.get(VillagerProfession.ARMORER).get(2);
        for (VillagerTrades.ItemListing listing : apprentice) {
            MerchantOffer offer = listing.getOffer(villager, random);
            if (offer != null && offer.getCostA().is(ModItems.LEAD_INGOT.get())) {
                helper.succeed();
                return;
            }
        }
        fail(helper, "No apprentice armorer buys lead ingots with the trade rebalance on");
    }
}

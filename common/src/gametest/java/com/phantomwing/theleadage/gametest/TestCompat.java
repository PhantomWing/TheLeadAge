package com.phantomwing.theleadage.gametest;

import net.minecraft.world.item.Items;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.packs.PackResources;
import com.phantomwing.theleadage.recipe.StainedLeadedGlassRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

/**
 * The calls a test makes whose shape differs between Minecraft versions, each behind a signature every
 * line shares. This is the one test file that differs from line to line, so the bodies don't have to:
 * when a port turns up another such call, it goes here rather than into a body. A call each modloader
 * answers its own way goes in {@link TestLoader} instead.
 */
public final class TestCompat {
    /** {@code MobEffects.CONFUSION} before 1.21.5. */
    public static final Holder<MobEffect> NAUSEA = MobEffects.CONFUSION;

    /** {@code EntityType.ITEM} until 26.2 moved it to {@code EntityTypes}. */
    public static final EntityType<ItemEntity> ITEM_ENTITY = EntityType.ITEM;

    /**
     * Vanilla's entity types the bodies spawn, {@code EntityType}'s constants until 26.2 moved them to
     * {@code EntityTypes}. Most of their classes moved package in 1.21.11, so a body holds what it spawns in a var.
     */
    public static final EntityType<Axolotl> AXOLOTL = EntityType.AXOLOTL;
    public static final EntityType<Bee> BEE = EntityType.BEE;
    public static final EntityType<Chicken> CHICKEN = EntityType.CHICKEN;
    public static final EntityType<Cod> COD = EntityType.COD;
    public static final EntityType<Creeper> CREEPER = EntityType.CREEPER;
    public static final EntityType<Drowned> DROWNED = EntityType.DROWNED;
    public static final EntityType<Pig> PIG = EntityType.PIG;
    public static final EntityType<Squid> SQUID = EntityType.SQUID;
    public static final EntityType<Villager> VILLAGER = EntityType.VILLAGER;
    public static final EntityType<Zombie> ZOMBIE = EntityType.ZOMBIE;

    /** {@code PushReaction.BLOCK} until 26.3 renamed it {@code IMMOVEABLE}: what a piston can't push or pull. */
    public static final PushReaction IMMOVABLE = PushReaction.BLOCK;

    /** {@code #minecraft:spears}, which lets a spear take Lunge. Vanilla has neither the tag nor spears before 1.21.11. */
    public static final TagKey<Item> SPEARS = TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("spears"));

    /** {@code Items.RED_DYE} until 26.2 gathered the dyes into a colour collection. */
    public static final Item RED_DYE = Items.RED_DYE;

    /** {@code Items.RED_STAINED_GLASS} until 26.2 gathered the stained glass into a colour collection. */
    public static final Item RED_STAINED_GLASS = Items.RED_STAINED_GLASS;

    private TestCompat() {
    }

    /** The id {@code path} in the minecraft namespace. ResourceLocation was renamed Identifier in 1.21.11. */
    public static ResourceLocation vanillaId(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    /** Fails the test. {@code GameTestHelper.fail} takes only a Component from 1.21.5 to 1.21.8. */
    public static void fail(GameTestHelper helper, String message) {
        helper.fail(message);
    }

    /** Fails the test unless {@code condition} holds. {@code GameTestHelper.assertTrue} takes only a Component from 1.21.5 to 1.21.10. */
    public static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, message);
    }

    /** The block entity at {@code pos}, failing the test when there is none of that type. */
    public static <T extends BlockEntity> T blockEntity(GameTestHelper helper, BlockPos pos, Class<T> type) {
        BlockEntity found = helper.getBlockEntity(pos);
        if (!type.isInstance(found)) {
            helper.fail("expected a " + type.getSimpleName() + " at " + pos + ", found " + found);
        }
        return type.cast(found);
    }

    /**
     * A copy of {@code entity} made from its save data, as loading the world makes it, not yet in the level. Entities save
     * through {@code ValueOutput} from 1.21.6, and {@code EntityType.create} takes a spawn reason from 1.21.3.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T reloaded(T entity) {
        CompoundTag saved = entity.saveWithoutId(new CompoundTag());
        T copy = (T) entity.getType().create(entity.level(), EntitySpawnReason.LOAD);
        copy.load(saved);
        return copy;
    }

    /** Puts an entity at {@code at}, looking south and level. {@code moveTo} became {@code snapTo} in 1.21.5. */
    public static void moveTo(Entity entity, Vec3 at) {
        entity.moveTo(at.x, at.y, at.z, 0.0f, 0.0f);
    }

    /** What {@code recipe} makes from {@code input}, crafted or cooked. It no longer takes the registries from 26.1. */
    public static <I extends RecipeInput> ItemStack assemble(Recipe<I> recipe, I input, ServerLevel level) {
        return recipe.assemble(input, level.registryAccess());
    }

    /** The block of copper in {@code state}, waxed or not. 26.2 gathered them into a collection on Blocks. */
    public static Block copperBlock(WeatherState state, boolean isWaxed) {
        return switch (state) {
            case UNAFFECTED -> isWaxed ? Blocks.WAXED_COPPER_BLOCK : Blocks.COPPER_BLOCK;
            case EXPOSED -> isWaxed ? Blocks.WAXED_EXPOSED_COPPER : Blocks.EXPOSED_COPPER;
            case WEATHERED -> isWaxed ? Blocks.WAXED_WEATHERED_COPPER : Blocks.WEATHERED_COPPER;
            case OXIDIZED -> isWaxed ? Blocks.WAXED_OXIDIZED_COPPER : Blocks.OXIDIZED_COPPER;
        };
    }

    /** An item's own attribute modifiers, baked into its components from 1.21.2; the item worked them out before. */
    public static ItemAttributeModifiers defaultModifiers(Item item) {
        return item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);
    }

    /**
     * How much of its durability an item loses for each mob it hits. From 1.21.5 that is its weapon component; before,
     * its class: a sword loses 1 a hit, a digger 2, anything else nothing.
     */
    public static int itemDamagePerAttack(Item item) {
        return item instanceof SwordItem ? 1 : item instanceof DiggerItem ? 2 : 0;
    }

    /**
     * Whether the entities within {@code radius} chunks of the world spawn have loaded, which a mock
     * player, placed near the spawn, needs first. 1.21.9 moved the spawn into the level's respawn
     * data, and 26.1 renamed ChunkPos's factory and packing.
     */
    public static boolean entitiesLoadedAroundSpawn(ServerLevel level, int radius) {
        return ChunkPos.rangeClosed(new ChunkPos(level.getSharedSpawnPos()), radius)
                .allMatch(chunk -> level.areEntitiesLoaded(chunk.toLong()));
    }

    /** The recipe that crafts dyed leaded glass in a dye mod's colour. Its constructor lost the category in 26.1. */
    public static StainedLeadedGlassRecipe stainedLeadedGlassRecipe() {
        return new StainedLeadedGlassRecipe(CraftingBookCategory.BUILDING);
    }

    /** The level's noise settings of that name. Registry lookups were renamed in 1.21.2. */
    public static NoiseGeneratorSettings noiseSettings(ServerLevel level, ResourceKey<NoiseGeneratorSettings> key) {
        return level.registryAccess().lookupOrThrow(Registries.NOISE_SETTINGS).getValueOrThrow(key);
    }

    /** A level's noises seeded from {@code seed}, as the level makes its own. 26.3 takes the noises first. */
    public static RandomState randomState(ServerLevel level, NoiseGeneratorSettings settings, long seed) {
        return RandomState.create(settings, level.registryAccess().lookupOrThrow(Registries.NOISE), seed);
    }

    /** One ore vein's blocks read at single points, as its rule places them in a chunk; null where it places none. */
    @FunctionalInterface
    public interface VeinSampler {
        @Nullable
        BlockState at(int x, int y, int z);
    }

    /**
     * The lead veins of the level {@code random} was made for, read at single points: the mod's NoiseChunk filler, bound
     * to those noises, outside a chunk so with no caches to fill. Null where the veins aren't bound. From 26.3 they are
     * vanilla's ore vein rule over the mod's density functions.
     */
    @Nullable
    public static VeinSampler leadVeins(ServerLevel level, RandomState random) {
        LeadVeins.bind(random, level.registryAccess());
        NoiseChunk.BlockStateFiller veins = LeadVeins.filler(random, noiseSettings(level, NoiseGeneratorSettings.OVERWORLD),
                function -> function);
        return veins == null ? null : (x, y, z) -> veins.calculate(new DensityFunction.SinglePointContext(x, y, z));
    }

    /**
     * Vanilla's vein noise at a block: copper's veins lie where it is over 0.4, iron's where it is under -0.4. The noise
     * router's vein toggle before 26.3, the density function {@code minecraft:overworld/ore_vein/toggle} from 26.3.
     */
    public static float vanillaVeinToggle(ServerLevel level, RandomState random, int x, int y, int z) {
        return (float) random.router().veinToggle().compute(new DensityFunction.SinglePointContext(x, y, z));
    }

    /**
     * What is wrong with where a chunk's terrain is given the lead veins, or null where they come after vanilla's ore
     * veins. Before 26.3 the veins are the last of a NoiseChunk's fillers, after the aquifer's and vanilla's veins, which
     * only a chunk being filled can run, so their order alone is read here: the lead vein at {@code vein} is what 26.3
     * asks its material system for.
     */
    @Nullable
    public static String leadVeinsOutOfPlace(ServerLevel level, RandomState random, NoiseGeneratorSettings settings,
            BlockPos vein, BlockState expected) {
        List<NoiseChunk.BlockStateFiller> rules = materialRules(noiseChunk(level, random, settings));
        if (rules.size() < 3 || !rules.get(rules.size() - 1).getClass().getSimpleName().equals("LeadVeinifier")) {
            return "expected the lead veins last of a chunk's rules, after vanilla's veins, found " + rules;
        }
        return null;
    }

    /** The terrain filler for the chunk at the origin, as the level's generator makes one for each chunk it fills. */
    private static NoiseChunk noiseChunk(ServerLevel level, RandomState random, NoiseGeneratorSettings settings) {
        ChunkPos pos = new ChunkPos(0, 0);
        ProtoChunk chunk = new ProtoChunk(pos, UpgradeData.EMPTY, level, level.registryAccess().lookupOrThrow(Registries.BIOME), null);
        return NoiseChunk.forChunk(chunk, random, Beardifier.forStructuresInChunk(level.structureManager(), pos), settings,
                (x, y, z) -> new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState()), Blender.empty());
    }

    /**
     * The rules a chunk's terrain filler tries in turn for each solid block, the first block one gives being placed.
     * They are a {@code MaterialRuleList} before 26.3, of an array from 1.21.2.
     */
    private static List<NoiseChunk.BlockStateFiller> materialRules(NoiseChunk chunk) {
        try {
            Field rule = NoiseChunk.class.getDeclaredField("blockStateRule");
            rule.setAccessible(true);
            return List.of(((MaterialRuleList) rule.get(chunk)).materialRuleList());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("can't read a NoiseChunk's rules", e);
        }
    }

    /**
     * The trim of vanilla's material and pattern of those names. Registry lookups were renamed in 1.21.2, and trims
     * moved package.
     */
    public static ArmorTrim trim(ServerLevel level, String material, String pattern) {
        return new ArmorTrim(level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL)
                        .getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL, vanillaId(material))),
                level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN)
                        .getOrThrow(ResourceKey.create(Registries.TRIM_PATTERN, vanillaId(pattern))));
    }

    /**
     * The palette a trim of {@code material} draws with on worn {@code armor}. Before 1.21.2 a trim material named it by
     * the armour's material, its registry holder; from 1.21.2 by the armour's equipment model.
     */
    public static String trimPalette(ServerLevel level, ResourceKey<TrimMaterial> material, Item armor) {
        return level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getValueOrThrow(material)
                .overrideArmorMaterials().get(armor.components().get(DataComponents.EQUIPPABLE).model().orElseThrow());
    }

    /** Whether the recipe {@code id} is loaded. Recipes are found by a ResourceKey from 1.21.2. */
    public static boolean hasRecipe(RecipeManager recipes, ResourceLocation id) {
        return recipes.byKey(ResourceKey.create(Registries.RECIPE, id)).isPresent();
    }

    /** Every recipe of {@code type} that takes {@code input}. RecipeManager#getRecipesFor went in 1.21.2. */
    @SuppressWarnings("unchecked")
    public static <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> recipesFor(RecipeManager recipes,
            RecipeType<T> type, I input, Level level) {
        return recipes.getRecipes().stream()
                .filter(holder -> holder.value().getType() == type)
                .map(holder -> (RecipeHolder<T>) holder)
                .filter(holder -> holder.value().matches(input, level))
                .toList();
    }

    /** What using {@code item} in the air does. Item#use returns the InteractionResult itself from 1.21.2. */
    public static InteractionResult use(Item item, Level level, Player player, InteractionHand hand) {
        return item.use(level, player, hand);
    }

    /**
     * The lead gear of kinds vanilla added after 1.21.1, which the gear tests hold to what the rest of the set does: the
     * spear and the nautilus armor, from 1.21.11. None before.
     */
    public static List<Item> newerGear() {
        return List.of();
    }

    /** The Lead Spear, from 1.21.11, which brought spears. None before. */
    public static Optional<Item> leadSpear() {
        return Optional.empty();
    }

    /** The Lead Nautilus Armor, from 1.21.11, which brought the nautilus. None before. */
    public static Optional<Item> leadNautilusArmor() {
        return Optional.empty();
    }

    /** Whether {@code stack} charges as a spear does, with a kinetic weapon component from 1.21.11. Nothing does before. */
    public static boolean chargesAsASpear(ItemStack stack) {
        return false;
    }

    /** Whether {@code stack} goes on a nautilus's body. There is no nautilus before 1.21.11. */
    public static boolean fitsTheNautilus(ItemStack stack) {
        return false;
    }

    /**
     * Whether an apprentice armorer buys {@code item} in a world with the Villager Trade Rebalance experiment on. Up to
     * 1.21.11 the experiment's trades are code, VillagerTrades.EXPERIMENTAL_TRADES, whose listings make the offers. From
     * 26.1 trades are data, and the experiment is a datapack the world loads after every mod's.
     */
    public static boolean rebalancedApprenticeArmorerBuys(ServerLevel level, Villager villager, Item item) {
        return buys(VillagerTrades.EXPERIMENTAL_TRADES.get(VillagerProfession.ARMORER).get(2), villager, item);
    }

    /**
     * Whether the smith of that name - armorer, toolsmith or weaponsmith - buys lead ingots at level 2. Up to 1.21.11 its
     * trades are listings in VillagerTrades.TRADES, which the mod adds to through its modloader; from 26.1 a trade set
     * draws them from a tag.
     */
    public static boolean apprenticeSmithBuysLead(ServerLevel level, Villager villager, String smith) {
        VillagerProfession profession = switch (smith) {
            case "armorer" -> VillagerProfession.ARMORER;
            case "toolsmith" -> VillagerProfession.TOOLSMITH;
            case "weaponsmith" -> VillagerProfession.WEAPONSMITH;
            default -> throw new IllegalArgumentException("not a smith: " + smith);
        };
        return buys(VillagerTrades.TRADES.get(profession).get(2), villager, ModItems.LEAD_INGOT.get());
    }

    /** Whether any of {@code listings} offers {@code villager} to buy {@code item}. */
    private static boolean buys(VillagerTrades.ItemListing[] listings, Villager villager, Item item) {
        RandomSource random = RandomSource.create(1L);
        for (VillagerTrades.ItemListing listing : listings) {
            MerchantOffer offer = listing.getOffer(villager, random);
            if (offer != null && offer.getCostA().is(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the overworld's caves and canyons cut through {@code state}. Before 26.3 a carver replaced only the blocks
     * in {@code #overworld_carver_replaceables}; from 26.3 it cuts everything but {@code #uncarvable}.
     */
    public static boolean carvedByCaves(BlockState state) {
        return state.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES);
    }

    /**
     * What a break of leaded glass shows and sounds as under {@code breaker}: the glass itself, or the glass it shattered
     * into. From 26.3, whose Level#destroyBlock first hands the glass's destroy hook no breaker. Empty before, where only
     * a player's break comes there.
     */
    public static Optional<BlockState> shownAs(@Nullable Player breaker, BlockState state, BlockState shattered) {
        return Optional.empty();
    }

    /** The direction nearest a vector. {@code Direction.getNearest} took floats until 1.21.2 renamed it. */
    public static Direction nearestDirection(float x, float y, float z) {
        return Direction.getApproximateNearest(x, y, z);
    }

    /** Whether {@code pack} has the file {@code id}. 26.3's Pack#open gives a stream of the pack's parts. */
    public static boolean serves(Pack pack, PackType type, ResourceLocation id) {
        try (PackResources resources = pack.open()) {
            return resources.getResource(type, id) != null;
        }
    }
}

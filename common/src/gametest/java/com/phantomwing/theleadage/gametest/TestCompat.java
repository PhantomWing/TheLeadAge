package com.phantomwing.theleadage.gametest;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.util.RandomSource;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import org.jetbrains.annotations.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.recipe.StainedLeadedGlassRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.trading.VillagerTrade;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

/**
 * The calls a test makes whose shape differs between Minecraft versions, each behind a signature every
 * line shares. This is the one test file that differs from line to line, so the bodies don't have to:
 * when a port turns up another such call, it goes here rather than into a body. A call each modloader
 * answers its own way goes in {@link TestLoader} instead.
 */
public final class TestCompat {
    /** {@code MobEffects.CONFUSION} before 1.21.5. */
    public static final Holder<MobEffect> NAUSEA = MobEffects.NAUSEA;

    /** {@code EntityType.ITEM} until 26.2 moved it to {@code EntityTypes}. */
    public static final EntityType<ItemEntity> ITEM_ENTITY = EntityTypes.ITEM;

    /** {@code EntityType.NAUTILUS} until 26.2 moved it to {@code EntityTypes}. New in 1.21.11. */
    public static final EntityType<Nautilus> NAUTILUS = EntityTypes.NAUTILUS;

    /**
     * Vanilla's entity types the bodies spawn, {@code EntityType}'s constants until 26.2 moved them to
     * {@code EntityTypes}.
     */
    public static final EntityType<Axolotl> AXOLOTL = EntityTypes.AXOLOTL;
    public static final EntityType<Bee> BEE = EntityTypes.BEE;
    public static final EntityType<Chicken> CHICKEN = EntityTypes.CHICKEN;
    public static final EntityType<Cod> COD = EntityTypes.COD;
    public static final EntityType<Creeper> CREEPER = EntityTypes.CREEPER;
    public static final EntityType<Drowned> DROWNED = EntityTypes.DROWNED;
    public static final EntityType<Pig> PIG = EntityTypes.PIG;
    public static final EntityType<Squid> SQUID = EntityTypes.SQUID;
    public static final EntityType<Villager> VILLAGER = EntityTypes.VILLAGER;
    public static final EntityType<Zombie> ZOMBIE = EntityTypes.ZOMBIE;

    /** {@code PushReaction.BLOCK} until 26.3 renamed it {@code IMMOVEABLE}: what a piston can't push or pull. */
    public static final PushReaction IMMOVABLE = PushReaction.IMMOVEABLE;

    /** {@code Items.RED_DYE} until 26.2 gathered the dyes into a colour collection. */
    public static final Item RED_DYE = Items.DYE.pick(DyeColor.RED);

    /** {@code Items.RED_STAINED_GLASS} until 26.2 gathered the stained glass into a colour collection. */
    public static final Item RED_STAINED_GLASS = Items.STAINED_GLASS.pick(DyeColor.RED);

    private TestCompat() {
    }

    /** Fails the test. {@code GameTestHelper.fail} takes only a Component from 1.21.5 to 1.21.8. */
    public static void fail(GameTestHelper helper, String message) {
        helper.fail(Component.literal(message));
    }

    /** Fails the test unless {@code condition} holds. {@code GameTestHelper.assertTrue} takes only a Component from 1.21.5 to 1.21.10. */
    public static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    /** The block entity at {@code pos}, failing the test when there is none of that type. */
    public static <T extends BlockEntity> T blockEntity(GameTestHelper helper, BlockPos pos, Class<T> type) {
        return helper.getBlockEntity(pos, type);
    }

    /**
     * A copy of {@code entity} made from its save data, as loading the world makes it, not yet in the level. Entities save
     * through {@code ValueOutput} from 1.21.6, and {@code EntityType.create} takes a spawn reason from 1.21.3. What
     * couldn't be saved or read back fails the test rather than leaving a field at its default.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T reloaded(T entity) {
        ProblemReporter.Collector problems = new ProblemReporter.Collector();
        TagValueOutput saved = TagValueOutput.createWithContext(problems, entity.registryAccess());
        entity.saveWithoutId(saved);
        T copy = (T) entity.getType().create(entity.level(), EntitySpawnReason.LOAD);
        copy.load(TagValueInput.create(problems, entity.registryAccess(), saved.buildResult()));
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Saving and loading " + entity + " reported: " + problems.getReport());
        }
        return copy;
    }

    /** Puts an entity at {@code at}, looking south and level. {@code moveTo} became {@code snapTo} in 1.21.5. */
    public static void moveTo(Entity entity, Vec3 at) {
        entity.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
    }

    /** What {@code recipe} makes from {@code input}, crafted or cooked. It no longer takes the registries from 26.1. */
    public static <I extends RecipeInput> ItemStack assemble(Recipe<I> recipe, I input, ServerLevel level) {
        return recipe.assemble(input);
    }

    /** Whether {@code equippable} fits a mob of {@code type}. It takes the type's holder from 26.1. */
    public static boolean canBeEquippedBy(Equippable equippable, EntityType<?> type) {
        return equippable.canBeEquippedBy(type.builtInRegistryHolder());
    }

    /** The block of copper in {@code state}, waxed or not. 26.2 gathered them into a collection on Blocks. */
    public static Block copperBlock(WeatherState state, boolean isWaxed) {
        return (isWaxed ? Blocks.COPPER_BLOCK.waxed() : Blocks.COPPER_BLOCK.weathering()).pick(state);
    }

    /** An item's own attribute modifiers, baked into its components from 1.21.2; the item worked them out before. */
    public static ItemAttributeModifiers defaultModifiers(Item item) {
        return item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);
    }

    /**
     * Whether a mock player can be made yet. On 1.21.6 to 1.21.8 placing one waits for the entities
     * around the world spawn, so the tests wait for them first. Here placing a player waits for nothing
     * (a login waits in PrepareSpawnTask, which a mock player skips), and nothing keeps the spawn area
     * loaded, so waiting for it can only time out.
     */
    public static boolean entitiesLoadedAroundSpawn(ServerLevel level, int radius) {
        return true;
    }

    /** The recipe that crafts dyed leaded glass in a dye mod's colour. Its constructor lost the category in 26.1. */
    public static StainedLeadedGlassRecipe stainedLeadedGlassRecipe() {
        return new StainedLeadedGlassRecipe();
    }

    /** The level's noise settings of that name. Registry lookups were renamed in 1.21.2. */
    public static NoiseGeneratorSettings noiseSettings(ServerLevel level, ResourceKey<NoiseGeneratorSettings> key) {
        return level.registryAccess().lookupOrThrow(Registries.NOISE_SETTINGS).getValueOrThrow(key);
    }

    /** A level's noises seeded from {@code seed}, as the level makes its own. 26.3 takes the noises first. */
    public static RandomState randomState(ServerLevel level, NoiseGeneratorSettings settings, long seed) {
        return RandomState.create(level.registryAccess().lookupOrThrow(Registries.NOISE), seed, settings);
    }

    /** One ore vein's blocks read at single points, as its rule places them in a chunk; null where it places none. */
    @FunctionalInterface
    public interface VeinSampler {
        @Nullable
        BlockState at(int x, int y, int z);
    }

    /**
     * The lead veins of the level {@code random} was made for, read at single points: vanilla's ore vein rule over their
     * density functions, as 26.3 places every vein, outside a chunk so with no caches to fill. Null where the level's
     * registries have no lead vein rule. Before 26.3 the veins are a NoiseChunk filler of the mod's own.
     */
    @Nullable
    public static VeinSampler leadVeins(ServerLevel level, RandomState random) {
        return oreVein(level, random, LeadVeins.RULE);
    }

    /** The ore vein whose rule is {@code key}, vanilla's or the mod's, read as {@link #leadVeins} reads the lead veins. */
    @Nullable
    public static VeinSampler oreVein(ServerLevel level, RandomState random, ResourceKey<MaterialRule> key) {
        MaterialRule rule = level.registryAccess().lookupOrThrow(Registries.MATERIAL_RULE).getOptional(key).orElse(null);
        if (!(rule instanceof OreVeinRule veins)) {
            return null;
        }
        PositionalRandomFactory ore = random.getOrCreateRandomFactory(Identifier.withDefaultNamespace("ore"));
        return (x, y, z) -> {
            float density = random.sampleBlockValueUncached(veins.density(), x, y, z);
            if (density <= 0.0F) {
                return null;
            }
            RandomSource roll = ore.at(x, y, z);
            if (roll.nextFloat() > density) {
                return null;
            }
            if (roll.nextFloat() < random.sampleBlockValueUncached(veins.richness(), x, y, z)
                    && random.sampleBlockValueUncached(veins.fillerGap(), x, y, z) < 0.0F) {
                return roll.nextFloat() < veins.rawOreChance() ? veins.rawOreBlock() : veins.oreBlock();
            }
            return veins.fillerBlock();
        };
    }

    /**
     * Vanilla's vein noise at a block: copper's veins lie where it is over 0.4, iron's where it is under -0.4. The noise
     * router's vein toggle before 26.3, the density function {@code minecraft:overworld/ore_vein/toggle} from 26.3.
     */
    public static float vanillaVeinToggle(ServerLevel level, RandomState random, int x, int y, int z) {
        DensityFunction toggle = level.registryAccess().lookupOrThrow(Registries.DENSITY_FUNCTION).getValueOrThrow(
                ResourceKey.create(Registries.DENSITY_FUNCTION, Identifier.withDefaultNamespace("overworld/ore_vein/toggle")));
        return random.sampleBlockValueUncached(toggle, x, y, z);
    }

    /**
     * The block the material system builds {@code rule} into at {@code pos}, as a carver asks it for the top of what it
     * carves; empty where no rule of it places one. From 26.3 a chunk's material rule is where its ore veins are, which
     * the mod's {@code MaterialSystemMixin} adds the lead veins to. The chunk it reads the surface's slope from is an empty
     * one being generated, as a carver's is: a loaded chunk has no worldgen heightmaps, which it logs as an error.
     */
    public static Optional<BlockState> topMaterial(ServerLevel level, RandomState random, MaterialRule rule, BlockPos pos) {
        Holder<Biome> plains = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        ProtoChunk chunk = new ProtoChunk(ChunkPos.containing(pos), UpgradeData.EMPTY, level,
                PalettedContainerFactory.create(level.registryAccess()), null);
        return random.surfaceSystem().topMaterial(rule, random, WorldGenerationContext.of(level), at -> plains,
                chunk, random.samplersWithContext(SamplerContext.EMPTY_UNCACHED), pos, false);
    }

    /** The trim of that material and pattern. Registry lookups were renamed in 1.21.2, and trims moved package. */
    public static ArmorTrim trim(ServerLevel level, ResourceKey<TrimMaterial> material, ResourceKey<TrimPattern> pattern) {
        return new ArmorTrim(level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(material),
                level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(pattern));
    }

    /**
     * The palette a trim of {@code material} draws with on worn {@code armor}. Before 1.21.2 a trim material named it by
     * the armour's material, its registry holder; from 1.21.2 by the armour's equipment model, which 1.21.4 made an
     * equipment asset. 1.21.5 moved the palettes into the material's asset group, which answers its base palette where
     * the armour has none of its own. 26.3 left the material one palette and moved the armour's own into the equipment
     * asset's {@code trim_overrides}, a client asset, read here from the mod's resources as the client reads it: the first
     * override for this material, else the material's palette, each named as before, without {@code trim/}.
     */
    public static String trimPalette(ServerLevel level, ResourceKey<TrimMaterial> material, Item armor) {
        Identifier asset = armor.components().get(DataComponents.EQUIPPABLE).assetId().orElseThrow().identifier();
        String file = "/assets/" + asset.getNamespace() + "/equipment/" + asset.getPath() + ".json";
        Identifier palette = level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getValueOrThrow(material).paletteId();
        try (InputStream in = ModItems.class.getResourceAsStream(file)) {
            if (in == null) {
                throw new IllegalStateException("no equipment asset " + file);
            }
            JsonElement overrides = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .getAsJsonObject().get("trim_overrides");
            if (overrides != null) {
                for (JsonElement override : overrides.getAsJsonArray()) {
                    String when = override.getAsJsonObject().getAsJsonObject("when").get("material").getAsString();
                    if (Identifier.parse(when).equals(material.identifier())) {
                        palette = Identifier.parse(override.getAsJsonObject().get("palette").getAsString());
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("can't read " + file, e);
        }
        return palette.getPath().replaceFirst("^trim/", "");
    }

    /** Whether the recipe {@code id} is loaded. Recipes are found by a ResourceKey from 1.21.2. */
    public static boolean hasRecipe(RecipeManager recipes, Identifier id) {
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
        return List.of(ModItems.LEAD_SPEAR.get(), ModItems.LEAD_NAUTILUS_ARMOR.get());
    }

    /**
     * Whether an apprentice armorer buys {@code item} in a world with the Villager Trade Rebalance experiment on. Up to
     * 1.21.11 the experiment's trades are code, VillagerTrades.EXPERIMENTAL_TRADES, whose listings make the offers. From
     * 26.1 trades are data, and the experiment is a datapack the world loads after every mod's, which replaces the
     * minecraft:armorer/level_2 tag: a trade survives it only listed in a tag of another namespace that the armorer's level
     * 2 trade set draws from, which is what this reads.
     */
    public static boolean rebalancedApprenticeArmorerBuys(ServerLevel level, Villager villager, Item item) {
        HolderSet<VillagerTrade> trades = trades(level.registryAccess().lookupOrThrow(Registries.TRADE_SET)
                .getValueOrThrow(TradeSets.ARMORER_LEVEL_2));
        Optional<TagKey<VillagerTrade>> tag = trades.unwrapKey();
        if (tag.isEmpty() || tag.get().location().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return false;
        }
        Identifier file = tag.get().location().withPath(path -> "tags/villager_trade/" + path + ".json");
        String wanted = BuiltInRegistries.ITEM.getKey(item).toString();
        var registry = level.registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE);
        var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        for (Resource resource : level.getServer().getResourceManager().getResourceStack(file)) {
            try (Reader reader = resource.openAsReader()) {
                for (JsonElement value : JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("values")) {
                    String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
                    if (id.startsWith("#")) {
                        continue;
                    }
                    Optional<VillagerTrade> trade = registry.getOptional(Identifier.parse(id));
                    if (trade.isPresent() && wanted.equals(VillagerTrade.CODEC.encodeStart(ops, trade.get()).getOrThrow()
                            .getAsJsonObject().getAsJsonObject("wants").get("id").getAsString())) {
                        return true;
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("can't read " + file, e);
            }
        }
        return false;
    }

    /** The trades a trade set draws from. It was {@code getTrades} until 26.3 made the trade set a record. */
    public static HolderSet<VillagerTrade> trades(TradeSet set) {
        return set.trades();
    }

    /**
     * Whether the overworld's caves and canyons cut through {@code state}. Before 26.3 a carver replaced only the blocks
     * in {@code #overworld_carver_replaceables}; from 26.3 it cuts everything but {@code #uncarvable}.
     */
    public static boolean carvedByCaves(BlockState state) {
        return !state.is(BlockTags.UNCARVABLE);
    }

    /** The direction nearest a vector. {@code Direction.getNearest} took floats until 1.21.2 renamed it. */
    public static Direction nearestDirection(float x, float y, float z) {
        return Direction.getApproximateNearest(x, y, z);
    }

    /** Whether {@code pack} has the file {@code id}. 26.3's Pack#open gives a stream of the pack's parts. */
    public static boolean serves(Pack pack, PackType type, Identifier id) {
        return pack.open().anyMatch(resources -> {
            try (resources) {
                return resources.getResource(type, id) != null;
            }
        });
    }
}

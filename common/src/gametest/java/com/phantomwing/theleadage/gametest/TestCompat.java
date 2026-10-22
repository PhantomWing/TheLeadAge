package com.phantomwing.theleadage.gametest;

import net.minecraft.world.item.Items;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;
import java.lang.reflect.Field;
import java.util.List;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
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
    public static final EntityType<ItemEntity> ITEM_ENTITY = EntityType.ITEM;

    /** {@code Items.RED_DYE} until 26.2 gathered the dyes into a colour collection. */
    public static final Item RED_DYE = Items.RED_DYE;

    /** {@code Items.RED_STAINED_GLASS} until 26.2 gathered the stained glass into a colour collection. */
    public static final Item RED_STAINED_GLASS = Items.RED_STAINED_GLASS;

    private TestCompat() {
    }

    /** Fails the test. {@code GameTestHelper.fail} takes only a Component from 1.21.5 to 1.21.8. */
    public static void fail(GameTestHelper helper, String message) {
        helper.fail(Component.literal(message));
    }

    /** Fails the test unless {@code condition} holds. It takes only a Component from 1.21.5 to 1.21.10. */
    public static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    /** The block entity at {@code pos}, failing the test when there is none of that type. */
    public static <T extends BlockEntity> T blockEntity(GameTestHelper helper, BlockPos pos, Class<T> type) {
        return helper.getBlockEntity(pos, type);
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
        entity.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
    }

    /** What {@code recipe} crafts from {@code input}. It no longer takes the registries from 26.1. */
    public static ItemStack assemble(CraftingRecipe recipe, CraftingInput input, ServerLevel level) {
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

    /** A level's noises seeded from {@code seed}, as the level makes its own. */
    public static RandomState randomState(ServerLevel level, NoiseGeneratorSettings settings, long seed) {
        return RandomState.create(settings, level.registryAccess().lookupOrThrow(Registries.NOISE), seed);
    }

    /** The terrain filler for the chunk at the origin, as the level's generator makes one for each chunk it fills. */
    public static NoiseChunk noiseChunk(ServerLevel level, RandomState random, NoiseGeneratorSettings settings) {
        ChunkPos pos = new ChunkPos(0, 0);
        ProtoChunk chunk = new ProtoChunk(pos, UpgradeData.EMPTY, level, level.registryAccess().lookupOrThrow(Registries.BIOME), null);
        return NoiseChunk.forChunk(chunk, random, Beardifier.forStructuresInChunk(level.structureManager(), pos), settings,
                (x, y, z) -> new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState()), Blender.empty());
    }

    /**
     * The rules a chunk's terrain filler tries in turn for each solid block, the first block one gives being placed.
     * They are a {@code MaterialRuleList} before 26.3, of an array from 1.21.2.
     */
    public static List<NoiseChunk.BlockStateFiller> materialRules(NoiseChunk chunk) {
        try {
            Field rule = NoiseChunk.class.getDeclaredField("blockStateRule");
            rule.setAccessible(true);
            return List.of(((MaterialRuleList) rule.get(chunk)).materialRuleList());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("can't read a NoiseChunk's rules", e);
        }
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
     * the armour has none of its own.
     */
    public static String trimPalette(ServerLevel level, ResourceKey<TrimMaterial> material, Item armor) {
        return level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getValueOrThrow(material)
                .assets().assetId(armor.components().get(DataComponents.EQUIPPABLE).assetId().orElseThrow()).suffix();
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

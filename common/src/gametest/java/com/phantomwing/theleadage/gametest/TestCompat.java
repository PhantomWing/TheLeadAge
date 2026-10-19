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
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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

    /** {@code Items.RED_DYE} until 26.2 gathered the dyes into a colour collection. */
    public static final Item RED_DYE = Items.RED_DYE;

    /** {@code Items.RED_STAINED_GLASS} until 26.2 gathered the stained glass into a colour collection. */
    public static final Item RED_STAINED_GLASS = Items.RED_STAINED_GLASS;

    private TestCompat() {
    }

    /** Fails the test. {@code GameTestHelper.fail} takes only a Component from 1.21.5 to 1.21.8. */
    public static void fail(GameTestHelper helper, String message) {
        helper.fail(message);
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
        T copy = (T) entity.getType().create(entity.level());
        copy.load(saved);
        return copy;
    }

    /** Puts an entity at {@code at}, looking south and level. {@code moveTo} became {@code snapTo} in 1.21.5. */
    public static void moveTo(Entity entity, Vec3 at) {
        entity.moveTo(at.x, at.y, at.z, 0.0f, 0.0f);
    }

    /** What {@code recipe} crafts from {@code input}. It no longer takes the registries from 26.1. */
    public static ItemStack assemble(CraftingRecipe recipe, CraftingInput input, ServerLevel level) {
        return recipe.assemble(input, level.registryAccess());
    }

    /** An item's own attribute modifiers. On 1.21.1 the item works them out; 1.21.2 bakes them into its components. */
    public static ItemAttributeModifiers defaultModifiers(Item item) {
        return item.getDefaultAttributeModifiers();
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
        return level.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getOrThrow(key);
    }

    /** A level's noises seeded from {@code seed}, as the level makes its own. */
    public static RandomState randomState(ServerLevel level, NoiseGeneratorSettings settings, long seed) {
        return RandomState.create(settings, level.registryAccess().lookupOrThrow(Registries.NOISE), seed);
    }

    /** The terrain filler for the chunk at the origin, as the level's generator makes one for each chunk it fills. */
    public static NoiseChunk noiseChunk(ServerLevel level, RandomState random, NoiseGeneratorSettings settings) {
        ChunkPos pos = new ChunkPos(0, 0);
        ProtoChunk chunk = new ProtoChunk(pos, UpgradeData.EMPTY, level, level.registryAccess().registryOrThrow(Registries.BIOME), null);
        return NoiseChunk.forChunk(chunk, random, Beardifier.forStructuresInChunk(level.structureManager(), pos), settings,
                (x, y, z) -> new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState()), Blender.empty());
    }

    /**
     * The rules a chunk's terrain filler tries in turn for each solid block, the first block one gives being placed.
     * They are a {@code MaterialRuleList} before 26.3.
     */
    public static List<NoiseChunk.BlockStateFiller> materialRules(NoiseChunk chunk) {
        try {
            Field rule = NoiseChunk.class.getDeclaredField("blockStateRule");
            rule.setAccessible(true);
            return ((MaterialRuleList) rule.get(chunk)).materialRuleList();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("can't read a NoiseChunk's rules", e);
        }
    }

    /** Whether {@code pack} has the file {@code id}. 26.3's Pack#open gives a stream of the pack's parts. */
    public static boolean serves(Pack pack, PackType type, ResourceLocation id) {
        try (PackResources resources = pack.open()) {
            return resources.getResource(type, id) != null;
        }
    }
}

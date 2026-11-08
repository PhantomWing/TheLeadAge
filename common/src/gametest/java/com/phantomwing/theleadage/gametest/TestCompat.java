package com.phantomwing.theleadage.gametest;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.DataResult;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.block.custom.LeadedGlassDyes;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.ModDataComponents;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.network.LeadSettingsPayload;
import com.phantomwing.theleadage.recipe.StainedLeadedGlassRecipe;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The calls a test makes whose shape differs between Minecraft versions, each behind a signature every
 * line shares. This is the one test file that differs from line to line, so the bodies don't have to:
 * when a port turns up another such call, it goes here rather than into a body. A call each modloader
 * answers its own way goes in {@link TestLoader} instead.
 *
 * <p>On 1.20.1 the recipe inputs 1.20.5 and 1.21 brought ({@link CraftingInput}, {@link SingleRecipeInput},
 * {@link RecipeInput}) and 1.20.2's {@link RecipeHolder} are classes of this file's, which the bodies never name: they
 * hold what {@link #craftingInput}, {@link #singleInput} and {@link #recipeFor} give in a var.</p>
 */
public final class TestCompat {
    /** {@code MobEffects.CONFUSION} before 1.21.5. A plain MobEffect before 1.20.5 made effects registry holders. */
    public static final MobEffect NAUSEA = MobEffects.CONFUSION;

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
    public static final TagKey<Item> SPEARS = TagKey.create(Registries.ITEM, new ResourceLocation("spears"));

    /** {@code Items.RED_DYE} until 26.2 gathered the dyes into a colour collection. */
    public static final Item RED_DYE = Items.RED_DYE;

    /** {@code Items.RED_STAINED_GLASS} until 26.2 gathered the stained glass into a colour collection. */
    public static final Item RED_STAINED_GLASS = Items.RED_STAINED_GLASS;

    private TestCompat() {
    }

    /** The id {@code path} in the minecraft namespace. ResourceLocation was renamed Identifier in 1.21.11. */
    public static ResourceLocation vanillaId(String path) {
        return new ResourceLocation(path);
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
     * A player not in the level, in {@code mode}. {@code GameTestHelper.makeMockPlayer} takes the game mode from 1.20.5;
     * 1.20.1 has a creative one and a survival one, which stands in for adventure too.
     */
    public static Player mockPlayer(GameTestHelper helper, GameType mode) {
        return mode == GameType.CREATIVE ? helper.makeMockPlayer() : helper.makeMockSurvivalPlayer();
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

    /** A crafting grid {@code width} by {@code height} holding {@code items} row by row, as a recipe matches it. */
    public static CraftingInput craftingInput(int width, int height, List<ItemStack> items) {
        return new CraftingInput(width, height, items);
    }

    /** One stack, as a furnace or a stonecutter matches it. */
    public static SingleRecipeInput singleInput(ItemStack item) {
        return new SingleRecipeInput(item);
    }

    /**
     * What {@code recipe} makes from {@code input}, crafted or cooked. It no longer takes the registries from 26.1. A
     * 1.20.1 recipe takes a container rather than a recipe input.
     */
    @SuppressWarnings("unchecked")
    public static <C extends Container> ItemStack assemble(Recipe<C> recipe, RecipeInput input, ServerLevel level) {
        return recipe.assemble((C) input.container(), level.registryAccess());
    }

    /** Whether {@code recipe} takes {@code input}. A 1.20.1 recipe matches a container rather than a recipe input. */
    @SuppressWarnings("unchecked")
    public static <C extends Container> boolean matches(Recipe<C> recipe, RecipeInput input, Level level) {
        return recipe.matches((C) input.container(), level);
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

    /**
     * An item's own attribute modifiers. On 1.21.1 the item works them out; 1.21.2 bakes them into its components. On
     * 1.20.1 they are given per slot: an armour piece's for the slot it is worn in, anything else's for the main hand.
     */
    public static Multimap<Attribute, AttributeModifier> defaultModifiers(Item item) {
        EquipmentSlot slot = item instanceof ArmorItem armor ? armor.getEquipmentSlot() : EquipmentSlot.MAINHAND;
        return item.getDefaultAttributeModifiers(slot);
    }

    /**
     * The attribute modifiers an item has in the hand. A component of its own from 1.20.5, which 1.21.1's
     * {@code getDefaultAttributeModifiers} doesn't read for a tool; the main hand's on 1.20.1.
     */
    public static Multimap<Attribute, AttributeModifier> heldModifiers(Item item) {
        return item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
    }

    /**
     * The armour and knockback resistance modifiers a horse wearing {@code item} has. 1.20.1's horse armour carries no
     * attribute modifiers (1.20.5's body slot does): the horse gives itself the armour's protection, and the mod gives
     * a horse in lead armour its knockback resistance once vanilla's equipment check sees it put on, which a tick of the
     * horse runs.
     */
    public static Multimap<Attribute, AttributeModifier> horseArmorModifiers(GameTestHelper helper, Item item) {
        Horse horse = helper.spawnWithNoFreeWill(EntityType.HORSE, new BlockPos(0, 2, 0));
        horse.equipArmor(mockPlayer(helper, GameType.CREATIVE), new ItemStack(item));
        horse.tick();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers = ImmutableMultimap.builder();
        for (Attribute attribute : List.of(Attributes.ARMOR, Attributes.KNOCKBACK_RESISTANCE)) {
            AttributeInstance instance = horse.getAttribute(attribute);
            if (instance != null) {
                modifiers.putAll(attribute, instance.getModifiers());
            }
        }
        horse.discard();
        return modifiers.build();
    }

    /** Whether {@code modifiers} change {@code attribute}. They are a component of their own from 1.20.5. */
    public static boolean hasModifier(Multimap<Attribute, AttributeModifier> modifiers, Attribute attribute) {
        return modifiers != null && modifiers.containsKey(attribute);
    }

    /**
     * Whether {@code instance} has the mod's modifier {@code id}. 1.20.1 knows a modifier by a UUID, which the mod makes
     * from 1.21's id ({@code LeadArmorItem}).
     */
    public static boolean hasModifier(AttributeInstance instance, ResourceLocation id) {
        return getModifier(instance, id) != null;
    }

    /** The mod's modifier {@code id} on {@code instance}, or null. 1.20.1 knows a modifier by a UUID made from 1.21's id. */
    @Nullable
    public static AttributeModifier getModifier(AttributeInstance instance, ResourceLocation id) {
        return instance.getModifier(UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8)));
    }

    /** Whether {@code item} is a tool, which 1.20.5 gave a tool component of its own: a tiered item on 1.20.1. */
    public static boolean isTool(Item item) {
        return item instanceof TieredItem;
    }

    /**
     * How much of its durability an item loses for each mob it hits. From 1.21.5 that is its weapon component; before,
     * its class: a sword loses 1 a hit, a digger 2, anything else nothing.
     */
    public static int itemDamagePerAttack(Item item) {
        return item instanceof SwordItem ? 1 : item instanceof DiggerItem ? 2 : 0;
    }

    /**
     * Wears {@code stack}, worn in {@code slot} by {@code wearer}, by {@code amount}, breaking it as wear does. Before
     * 1.20.5 a break is told through the callback, which tells the wearer as vanilla's own wear does.
     */
    public static void hurtAndBreak(ItemStack stack, int amount, LivingEntity wearer, EquipmentSlot slot) {
        stack.hurtAndBreak(amount, wearer, entity -> entity.broadcastBreakEvent(slot));
    }

    /** {@code stack} named {@code name}, as an anvil names it. A name is a component from 1.20.5, a tag before. */
    public static void setCustomName(ItemStack stack, String name) {
        stack.setHoverName(Component.literal(name));
    }

    /** Whether {@code stack} carries a name it was given. A name is a component from 1.20.5, a tag before. */
    public static boolean hasCustomName(ItemStack stack) {
        return stack.hasCustomHoverName();
    }

    /** {@code stack} given {@code trim}. A trim is a component from 1.20.5, a tag before. */
    public static void setTrim(ServerLevel level, ItemStack stack, ArmorTrim trim) {
        ArmorTrim.setTrim(level.registryAccess(), stack, trim);
    }

    /** The trim {@code stack} carries, or null. A trim is a component from 1.20.5, a tag before. */
    @Nullable
    public static ArmorTrim trimOf(ServerLevel level, ItemStack stack) {
        return ArmorTrim.getTrim(level.registryAccess(), stack).orElse(null);
    }

    /** The leaded glass design {@code stack} carries, or null. A component from 1.20.5, kept in the stack's tag before. */
    @Nullable
    public static LeadedGlassConfig leadedGlassConfig(ItemStack stack) {
        return ModDataComponents.LEADED_GLASS_CONFIG.get(stack);
    }

    /** {@code stack} enchanted with {@code enchantment}. Enchantments are a registry of data from 1.21. */
    public static void enchant(ServerLevel level, ItemStack stack, Enchantment enchantment, int enchantmentLevel) {
        stack.enchant(enchantment, enchantmentLevel);
    }

    /** Whether two stacks are the same item with the same data. 1.20.5's components are a stack's tag before. */
    public static boolean isSameItemSameComponents(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }

    /** A dye's colour as {@code 0xFFRRGGBB}. {@code DyeColor.getTextureDiffuseColor} gives three floats before 1.20.5. */
    public static int textureDiffuseColor(DyeColor color) {
        return LeadedGlassDyes.rgbOf(color);
    }

    /** Whether a mob plans a path through {@code state}. It takes the level and place before 1.20.5. */
    public static boolean isPathfindable(BlockState state, PathComputationType type) {
        return state.isPathfindable(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, type);
    }

    /** What a codec read or wrote, or an exception saying why it couldn't. DataResult's getOrThrow takes no arguments from DFU 7. */
    public static <T> T orThrow(DataResult<T> result) {
        return result.getOrThrow(false, error -> {
        });
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

    /**
     * The recipe that crafts dyed leaded glass in a dye mod's colour. Its constructor lost the category in 26.1, and takes
     * its id before 1.20.2, whose RecipeHolder holds it.
     */
    public static StainedLeadedGlassRecipe stainedLeadedGlassRecipe() {
        return new StainedLeadedGlassRecipe(TheLeadAge.resourceLocation("stained_leaded_glass"), CraftingBookCategory.BUILDING);
    }

    /** The level's noise settings of that name. Registry lookups were renamed in 1.21.2. */
    public static NoiseGeneratorSettings noiseSettings(ServerLevel level, ResourceKey<NoiseGeneratorSettings> key) {
        return level.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getOrThrow(key);
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
        ProtoChunk chunk = new ProtoChunk(pos, UpgradeData.EMPTY, level, level.registryAccess().registryOrThrow(Registries.BIOME), null);
        return NoiseChunk.forChunk(chunk, random, Beardifier.forStructuresInChunk(level.structureManager(), pos), settings,
                (x, y, z) -> new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState()), Blender.empty());
    }

    /**
     * The rules a chunk's terrain filler tries in turn for each solid block, the first block one gives being placed.
     * They are a {@code MaterialRuleList} before 26.3.
     */
    private static List<NoiseChunk.BlockStateFiller> materialRules(NoiseChunk chunk) {
        try {
            Field rule = NoiseChunk.class.getDeclaredField("blockStateRule");
            rule.setAccessible(true);
            return ((MaterialRuleList) rule.get(chunk)).materialRuleList();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("can't read a NoiseChunk's rules", e);
        }
    }

    /**
     * The trim of vanilla's material and pattern of those names. Registry lookups were renamed in 1.21.2, and trims
     * moved package. A pattern newer than 1.20.1 (1.21's flow and bolt) is coast's here.
     */
    public static ArmorTrim trim(ServerLevel level, String material, String pattern) {
        if (pattern.equals("flow") || pattern.equals("bolt")) {
            pattern = "coast";
        }
        return new ArmorTrim(level.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL)
                        .getHolderOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL, vanillaId(material))),
                level.registryAccess().registryOrThrow(Registries.TRIM_PATTERN)
                        .getHolderOrThrow(ResourceKey.create(Registries.TRIM_PATTERN, vanillaId(pattern))));
    }

    /**
     * The palette a trim of {@code material} draws with on worn {@code armor}, read off the texture a trim of it draws
     * with: the palettes a 1.20.1 trim material names are only ever vanilla armour's, and lead armour's comes from
     * {@code ArmorTrimMixin}. Where nothing overrides it, it is the trim's own name.
     */
    public static Optional<String> trimPalette(ServerLevel level, ResourceKey<TrimMaterial> material, Item armor) {
        var pattern = level.registryAccess().registryOrThrow(Registries.TRIM_PATTERN).getHolderOrThrow(TrimPatterns.COAST);
        var trim = new ArmorTrim(level.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL).getHolderOrThrow(material), pattern);
        String path = trim.innerTexture(((ArmorItem) armor).getMaterial()).getPath();
        String prefix = "trims/models/armor/" + pattern.value().assetId().getPath() + "_leggings_";
        return Optional.of(path.startsWith(prefix) ? path.substring(prefix.length()) : path);
    }

    /** Whether the recipe {@code id} is loaded. Recipes are found by a ResourceKey from 1.21.2. */
    public static boolean hasRecipe(RecipeManager recipes, ResourceLocation id) {
        return recipes.byKey(id).isPresent();
    }

    /**
     * The first recipe of {@code type} that takes {@code input}, with its id. A 1.20.1 recipe matches a container and
     * carries its id itself.
     */
    @SuppressWarnings("unchecked")
    public static <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>> recipeFor(RecipeManager recipes,
            RecipeType<T> type, RecipeInput input, Level level) {
        return recipes.getRecipeFor(type, (C) input.container(), level).map(recipe -> new RecipeHolder<>(recipe.getId(), recipe));
    }

    /** Every recipe of {@code type} that takes {@code input}. RecipeManager#getRecipesFor went in 1.21.2. */
    @SuppressWarnings("unchecked")
    public static <C extends Container, T extends Recipe<C>> List<RecipeHolder<T>> recipesFor(RecipeManager recipes,
            RecipeType<T> type, RecipeInput input, Level level) {
        return recipes.getRecipesFor(type, (C) input.container(), level).stream()
                .map(recipe -> new RecipeHolder<>(recipe.getId(), recipe))
                .toList();
    }

    /** What using {@code item} in the air does. Item#use returns the InteractionResult itself from 1.21.2. */
    public static InteractionResult use(Item item, Level level, Player player, InteractionHand hand) {
        return item.use(level, player, hand).getResult();
    }

    /** A vanilla loot table by its path, e.g. {@code chests/village/village_weaponsmith}. Loot tables are a registry from 1.20.5. */
    public static LootTable lootTable(ServerLevel level, String path) {
        return level.getServer().getLootData().getLootTable(vanillaId(path));
    }

    /** The advancement {@code id}, or null. 1.20.2 gave advancements a holder with their id. */
    @Nullable
    public static Advancement advancement(MinecraftServer server, ResourceLocation id) {
        return server.getAdvancements().getAdvancement(id);
    }

    /**
     * A buffer the raw lead settings can be written to and read back from, as they go to a client. 1.20.5's payloads
     * write to a buffer that carries the registries; 1.20.1's to a plain one.
     */
    public static FriendlyByteBuf wire(ServerLevel level) {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    /** Writes {@code settings} to {@code wire}. A payload has a stream codec from 1.20.5. */
    public static void writeSettings(FriendlyByteBuf wire, LeadSettingsPayload settings) {
        settings.write(wire);
    }

    /** Reads the raw lead settings from {@code wire}. A payload has a stream codec from 1.20.5. */
    public static LeadSettingsPayload readSettings(FriendlyByteBuf wire) {
        return LeadSettingsPayload.read(wire);
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
     * 26.1 trades are data, and the experiment is a datapack the world loads after every mod's. Empty on 1.20.1, which
     * has no such experiment (1.20.2's).
     */
    public static Optional<Boolean> rebalancedApprenticeArmorerBuys(ServerLevel level, Villager villager, Item item) {
        return Optional.empty();
    }

    /** The loot table of a trial chamber vault's rewards. Empty before 1.21, which brought trial chambers. */
    public static Optional<String> trialVaultRewards() {
        return Optional.empty();
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
        return Direction.getNearest(x, y, z);
    }

    /** Whether {@code pack} has the file {@code id}. 26.3's Pack#open gives a stream of the pack's parts. */
    public static boolean serves(Pack pack, PackType type, ResourceLocation id) {
        try (PackResources resources = pack.open()) {
            return resources.getResource(type, id) != null;
        }
    }

    /** 1.21's {@code RecipeInput}: what a recipe is matched against, as the container 1.20.1's recipes take. */
    public interface RecipeInput {
        Container container();
    }

    /** 1.20.2's {@code RecipeHolder}: a recipe and its id, which a 1.20.1 recipe carries itself. */
    public record RecipeHolder<T>(ResourceLocation id, T value) {
    }

    /** 1.21's {@code SingleRecipeInput}: one stack, held as the container 1.20.1's recipes take. */
    public record SingleRecipeInput(ItemStack item) implements RecipeInput {
        @Override
        public SimpleContainer container() {
            return new SimpleContainer(item);
        }
    }

    /**
     * 1.21's {@code CraftingInput}: a crafting grid of its width and height, filled row by row. The mod's own
     * {@code recipe.CraftingInput} is what its recipes read a grid as.
     */
    public static final class CraftingInput implements RecipeInput {
        private final TransientCraftingContainer container;

        private CraftingInput(int width, int height, List<ItemStack> items) {
            this.container = new TransientCraftingContainer(new NoMenu(), width, height);
            for (int i = 0; i < items.size(); i++) {
                container.setItem(i, items.get(i));
            }
        }

        @Override
        public TransientCraftingContainer container() {
            return container;
        }

        /** A grid no menu shows: its slots tell no one when they change. */
        private static final class NoMenu extends AbstractContainerMenu {
            private NoMenu() {
                super((MenuType<?>) null, -1);
            }

            @Override
            public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(@NotNull Player player) {
                return false;
            }
        }
    }
}

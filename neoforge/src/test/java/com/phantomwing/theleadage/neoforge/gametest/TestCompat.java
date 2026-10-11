package com.phantomwing.theleadage.neoforge.gametest;

import net.minecraft.world.item.Items;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import com.phantomwing.theleadage.recipe.StainedLeadedGlassRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;

/**
 * The calls a test makes whose shape differs between Minecraft versions, each behind a signature every
 * line shares. This is the one test file that differs from line to line, so the bodies don't have to:
 * when a port turns up another such call, it goes here rather than into a body.
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

    /** The block entity at {@code pos}, failing the test when there is none of that type. */
    public static <T extends BlockEntity> T blockEntity(GameTestHelper helper, BlockPos pos, Class<T> type) {
        return helper.getBlockEntity(pos, type);
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
        return new StainedLeadedGlassRecipe(CraftingBookCategory.BUILDING);
    }

    /** Whether {@code pack} has the file {@code id}. 26.3's Pack#open gives a stream of the pack's parts. */
    public static boolean serves(Pack pack, PackType type, ResourceLocation id) {
        try (PackResources resources = pack.open()) {
            return resources.getResource(type, id) != null;
        }
    }
}

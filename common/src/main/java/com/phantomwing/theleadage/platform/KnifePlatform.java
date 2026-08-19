package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * {@code @ExpectPlatform} bridge that builds the Lead Knife.
 *
 * <p>Farmer's Delight is an <em>optional</em> dependency, but when it is present the knife should be a
 * real FD {@code KnifeItem} (Cutting Board support, the knife item abilities, the knife mining tag).
 * FD's {@code KnifeItem} is loader-specific and only on the classpath when FD is installed, so the
 * choice is made per loader:</p>
 * <ul>
 *   <li>FD loaded → the loader's {@code LeadKnifeItem}, extending
 *       {@code vectorwing.farmersdelight.common.item.KnifeItem}, with the knife properties from
 *       {@link #applyKnifeProperties}.</li>
 *   <li>FD absent → a plain {@link Item} carrying vanilla's sword properties
 *       ({@code Properties#sword}), so the item still exists (registry-consistent,
 *       multiplayer-safe), is still a usable weapon, and the mod loads standalone.</li>
 * </ul>
 *
 * <p>The item is always <em>registered</em>; only its concrete class and its weapon/tool components
 * are conditional. 1.21.5 moved those components out of the item class and onto the Properties, so
 * each branch applies its own: before 1.21.5 the same distinction came for free from extending FD's
 * {@code KnifeItem} versus {@code SwordItem}.</p>
 *
 * <p>Implemented per loader at {@code com.phantomwing.theleadage.platform.<loader>.KnifePlatformImpl}.</p>
 */
public final class KnifePlatform {
    /** Attack damage bonus, matching FD's own knives. */
    public static final float KNIFE_DAMAGE = 0.5f;
    /** Attack speed: 0.2 slower than FD's knives (-2.0f), the same penalty the rest of the lead tools carry. */
    public static final float KNIFE_SPEED = -2.2f;

    /** FD's knife mining tag. Referenced by id so neither loader needs an FD class to build the tool. */
    private static final TagKey<Block> MINEABLE_WITH_KNIFE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("farmersdelight", "mineable/knife"));

    private KnifePlatform() {
    }

    @ExpectPlatform
    public static Item createLeadKnife(Item.Properties properties, ToolMaterial material) {
        throw new AssertionError("@ExpectPlatform stub, replaced per loader at build time");
    }

    /**
     * Applies Farmer's Delight's own knife properties, so a Lead Knife behaves like every other FD
     * knife rather than like a sword: FD's attack attributes, its knife mining rules (the
     * {@code farmersdelight:mineable/knife} tag) and its 2-durability-per-attack weapon cost.
     * Mirrors how FD builds its knives in its {@code ModItems} (verified against FDR 1.21.5-3.2.5),
     * with this mod's slower swing.
     *
     * <p>Only called from the FD-loaded branch. Like vanilla's {@code Properties#sword}, this
     * resolves block tags through the bootstrap lookup, so it must run during registration.</p>
     */
    public static Item.Properties applyKnifeProperties(Item.Properties properties, ToolMaterial material) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return properties
                .durability(material.durability())
                .repairable(material.repairItems())
                .enchantable(material.enchantmentValue())
                .attributes(knifeAttributes(material))
                .component(DataComponents.TOOL, new Tool(List.of(
                        Tool.Rule.deniesDrops(blocks.getOrThrow(material.incorrectBlocksForDrops())),
                        Tool.Rule.minesAndDrops(blocks.getOrThrow(MINEABLE_WITH_KNIFE), material.speed())),
                        1.0f, 1, false))
                .component(DataComponents.WEAPON, new Weapon(2));
    }

    /** FD's {@code KnifeItem.createAttributes} formula, inlined so both loaders can share it. */
    private static ItemAttributeModifiers knifeAttributes(ToolMaterial material) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                                material.attackDamageBonus() + KNIFE_DAMAGE, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, KNIFE_SPEED,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }
}

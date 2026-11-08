package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * The attribute a living entity's gravity is read from. 1.20.1 has none of its own (1.20.5 added
 * {@code generic.gravity}): Forge adds {@code forge:entity_gravity}, and Fabric has nothing, so there lead armour's
 * weight is added in {@code LivingEntity#travel} itself.
 */
public final class GravityPlatform {
    private GravityPlatform() {
    }

    @ExpectPlatform
    @Nullable
    public static Attribute gravityAttribute() {
        throw new AssertionError();
    }
}

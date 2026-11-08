package com.phantomwing.theleadage.platform.fabric;

import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric implementation of {@link com.phantomwing.theleadage.platform.GravityPlatform}: none, so the weight is added in
 * {@code LivingEntityMixin}'s travel.
 */
public final class GravityPlatformImpl {
    private GravityPlatformImpl() {
    }

    @Nullable
    public static Attribute gravityAttribute() {
        return null;
    }
}

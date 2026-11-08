package com.phantomwing.theleadage.platform.forge;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.ForgeMod;

/** Forge implementation of {@link com.phantomwing.theleadage.platform.GravityPlatform}: Forge's own gravity attribute. */
public final class GravityPlatformImpl {
    private GravityPlatformImpl() {
    }

    public static Attribute gravityAttribute() {
        return ForgeMod.ENTITY_GRAVITY.get();
    }
}
